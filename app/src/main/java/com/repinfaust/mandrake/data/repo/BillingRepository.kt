package com.repinfaust.mandrake.data.repo

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.repinfaust.mandrake.data.entity.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class BillingRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    private val TAG = "BillingRepository"
    
    private var _purchaseUpdateCallback: ((List<Purchase>) -> Unit)? = null
    
    init {
        initializeBillingClient()
    }
    
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }
    
    private suspend fun ensureConnected(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val client = billingClient ?: run {
                initializeBillingClient()
                billingClient!!
            }
            
            if (client.isReady) {
                if (continuation.isActive) {
                    continuation.resume(true)
                }
                return@suspendCancellableCoroutine
            }
            
            var resumed = false
            
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    synchronized(this) {
                        if (!resumed && continuation.isActive) {
                            resumed = true
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Log.d(TAG, "Billing client connected successfully")
                                continuation.resume(true)
                            } else {
                                Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                                continuation.resume(false)
                            }
                        }
                    }
                }
                
                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing service disconnected")
                    synchronized(this) {
                        if (!resumed && continuation.isActive) {
                            resumed = true
                            continuation.resume(false)
                        }
                    }
                }
            })
            
            continuation.invokeOnCancellation {
                synchronized(this) {
                    resumed = true
                }
                try {
                    client.endConnection()
                } catch (e: Exception) {
                    Log.w(TAG, "Error ending connection on cancellation", e)
                }
            }
        }
    }
    
    suspend fun getProductDetails(): Result<List<ProductDetails>> {
        return try {
            if (!ensureConnected()) {
                return Result.failure(Exception("Billing client not connected"))
            }
            
            val subscriptionParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    Products.SUBSCRIPTION_PRODUCTS.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                )
                .build()
                
            val inappParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    Products.INAPP_PRODUCTS.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                )
                .build()
            
            val allProducts = mutableListOf<ProductDetails>()
            
            // Query subscription products
            val subsResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>?>> { continuation ->
                billingClient?.queryProductDetailsAsync(subscriptionParams) { result, productDetailsList ->
                    continuation.resume(Pair(result, productDetailsList))
                }
            }
            
            if (subsResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                allProducts.addAll(subsResult.second ?: emptyList())
            }
            
            // Query in-app products
            val inappResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>?>> { continuation ->
                billingClient?.queryProductDetailsAsync(inappParams) { result, productDetailsList ->
                    continuation.resume(Pair(result, productDetailsList))
                }
            }
            
            if (inappResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                allProducts.addAll(inappResult.second ?: emptyList())
            }
            
            Result.success(allProducts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product details", e)
            Result.failure(e)
        }
    }
    
    suspend fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String? = null
    ): Result<Unit> {
        return try {
            if (!ensureConnected()) {
                return Result.failure(Exception("Billing client not connected"))
            }
            
            val purchaseParams = BillingFlowParams.newBuilder()
            
            if (productDetails.productType == BillingClient.ProductType.SUBS) {
                val offerTokenToUse = offerToken ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerTokenToUse == null) {
                    return Result.failure(Exception("No subscription offer available"))
                }
                
                purchaseParams.setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerTokenToUse)
                            .build()
                    )
                )
            } else {
                purchaseParams.setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
            }
            
            val result = billingClient?.launchBillingFlow(activity, purchaseParams.build())
            
            if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to launch billing flow: ${result?.debugMessage}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching purchase flow", e)
            Result.failure(e)
        }
    }
    
    suspend fun queryPurchases(): Result<List<Purchase>> {
        return try {
            if (!ensureConnected()) {
                return Result.failure(Exception("Billing client not connected"))
            }
            
            val subscriptionResult = billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            
            val inappResult = billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            
            val allPurchases = mutableListOf<Purchase>()
            
            subscriptionResult?.let { result ->
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(result.purchasesList)
                }
            }
            
            inappResult?.let { result ->
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(result.purchasesList)
                }
            }
            
            Result.success(allPurchases)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying purchases", e)
            Result.failure(e)
        }
    }
    
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        return try {
            if (!ensureConnected()) {
                return Result.failure(Exception("Billing client not connected"))
            }
            
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
                
            val result = suspendCancellableCoroutine<BillingResult> { continuation ->
                billingClient?.acknowledgePurchase(ackParams) { result ->
                    continuation.resume(result)
                }
            }
            
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to acknowledge purchase: ${result.debugMessage}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acknowledging purchase", e)
            Result.failure(e)
        }
    }
    
    suspend fun verifyPurchaseWithServer(
        packageName: String,
        productId: String,
        purchaseToken: String,
        productType: String
    ): Result<PurchaseVerificationResponse> {
        return try {
            val uid = auth.currentUser?.uid
            val request = mapOf(
                "packageName" to packageName,
                "productId" to productId,
                "purchaseToken" to purchaseToken,
                "productType" to productType,
                "uid" to uid
            )
            
            val result = functions
                .getHttpsCallable("verifyPlayPurchase")
                .call(request)
                .await()
            
            val data = result.data as? Map<String, Any> ?: throw Exception("Invalid server response")
            
            Result.success(
                PurchaseVerificationResponse(
                    state = data["state"] as? String ?: "UNKNOWN",
                    expiryTimeMillis = (data["expiryTimeMillis"] as? Number)?.toLong(),
                    success = true
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying purchase with server", e)
            Result.success(
                PurchaseVerificationResponse(
                    state = "UNKNOWN",
                    success = false,
                    error = e.message
                )
            )
        }
    }
    
    fun getEntitlementStatus(): Flow<BillingEntitlement> {
        return callbackFlow {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                trySend(BillingEntitlement())
                close()
                return@callbackFlow
            }
            
            val listener = firestore
                .document("users/$uid/entitlement/status")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to entitlement", error)
                        trySend(BillingEntitlement())
                        return@addSnapshotListener
                    }
                    
                    val data = snapshot?.data
                    if (data != null) {
                        trySend(
                            BillingEntitlement(
                                active = data["active"] as? Boolean ?: false,
                                productId = data["productId"] as? String,
                                expiryTimeMillis = (data["expiryTimeMillis"] as? Number)?.toLong(),
                                lastSynced = (data["lastSynced"] as? Number)?.toLong(),
                                isLifetime = data["productId"] == Products.LIFETIME_UNLOCK
                            )
                        )
                    } else {
                        trySend(BillingEntitlement())
                    }
                }
            
            awaitClose { listener.remove() }
        }.distinctUntilChanged()
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.let { purchaseList ->
                Log.d(TAG, "Purchases updated: ${purchaseList.size} purchases")
                _purchaseUpdateCallback?.invoke(purchaseList)
            }
        } else {
            Log.e(TAG, "Purchase update failed: ${billingResult.debugMessage}")
            _purchaseUpdateCallback?.invoke(emptyList())
        }
    }
    
    fun setPurchaseUpdateCallback(callback: (List<Purchase>) -> Unit) {
        _purchaseUpdateCallback = callback
    }
    
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}