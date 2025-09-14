package com.repinfaust.mandrake.ui.viewmodels

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.repinfaust.mandrake.data.entity.BillingEntitlement
import com.repinfaust.mandrake.data.repo.BillingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BillingUiState(
    val isLoading: Boolean = false,
    val products: List<ProductDetails> = emptyList(),
    val entitlement: BillingEntitlement = BillingEntitlement(),
    val purchaseInProgress: Boolean = false,
    val error: String? = null
)

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val billingRepository = BillingRepository(application.applicationContext)
    private val TAG = "BillingViewModel"
    
    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()
    
    init {
        // Set up purchase update callback
        billingRepository.setPurchaseUpdateCallback { purchases ->
            handlePurchaseUpdate(purchases)
        }
        
        // Observe entitlement status
        viewModelScope.launch {
            billingRepository.getEntitlementStatus()
                .collect { entitlement ->
                    _uiState.update { it.copy(entitlement = entitlement) }
                }
        }
        
        // Load existing purchases on init
        queryExistingPurchases()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = billingRepository.getProductDetails()
                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            products = products,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to load products"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading products", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
    
    fun purchaseProduct(activity: Activity, productDetails: ProductDetails) {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseInProgress = true, error = null) }
            
            try {
                val result = billingRepository.launchPurchaseFlow(activity, productDetails)
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(
                            purchaseInProgress = false,
                            error = result.exceptionOrNull()?.message ?: "Purchase failed"
                        )
                    }
                }
                // Purchase completion will be handled via the callback
            } catch (e: Exception) {
                Log.e(TAG, "Error launching purchase", e)
                _uiState.update {
                    it.copy(
                        purchaseInProgress = false,
                        error = e.message ?: "Purchase error"
                    )
                }
            }
        }
    }
    
    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            queryExistingPurchases()
        }
    }
    
    private fun queryExistingPurchases() {
        viewModelScope.launch {
            try {
                val result = billingRepository.queryPurchases()
                if (result.isSuccess) {
                    val purchases = result.getOrNull() ?: emptyList()
                    handlePurchaseUpdate(purchases)
                } else {
                    Log.e(TAG, "Failed to query purchases: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying purchases", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun handlePurchaseUpdate(purchases: List<Purchase>) {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseInProgress = false) }
            
            // Process each purchase
            purchases.forEach { purchase ->
                processPurchase(purchase)
            }
        }
    }
    
    private suspend fun processPurchase(purchase: Purchase) {
        try {
            // Acknowledge purchase if not already acknowledged
            if (!purchase.isAcknowledged) {
                val ackResult = billingRepository.acknowledgePurchase(purchase.purchaseToken)
                if (ackResult.isFailure) {
                    Log.e(TAG, "Failed to acknowledge purchase: ${ackResult.exceptionOrNull()?.message}")
                } else {
                    Log.d(TAG, "Purchase acknowledged successfully")
                }
            }
            
            // Verify purchase with server
            purchase.products.forEach { productId ->
                val productType = when {
                    com.repinfaust.mandrake.data.entity.Products.SUBSCRIPTION_PRODUCTS.contains(productId) -> "subs"
                    com.repinfaust.mandrake.data.entity.Products.INAPP_PRODUCTS.contains(productId) -> "inapp"
                    else -> "unknown"
                }
                
                if (productType != "unknown") {
                    val verifyResult = billingRepository.verifyPurchaseWithServer(
                        packageName = purchase.packageName,
                        productId = productId,
                        purchaseToken = purchase.purchaseToken,
                        productType = productType
                    )
                    
                    if (verifyResult.isSuccess) {
                        Log.d(TAG, "Purchase verified successfully for $productId")
                    } else {
                        Log.e(TAG, "Failed to verify purchase for $productId: ${verifyResult.exceptionOrNull()?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing purchase", e)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        billingRepository.endConnection()
    }
}