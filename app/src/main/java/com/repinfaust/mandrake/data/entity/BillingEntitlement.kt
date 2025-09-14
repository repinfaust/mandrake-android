package com.repinfaust.mandrake.data.entity

data class BillingEntitlement(
    val active: Boolean = false,
    val productId: String? = null,
    val expiryTimeMillis: Long? = null,
    val lastSynced: Long? = null,
    val isLifetime: Boolean = false
)

data class PurchaseVerificationRequest(
    val packageName: String,
    val productId: String,
    val purchaseToken: String,
    val productType: String, // "subs" or "inapp"
    val uid: String? = null
)

data class PurchaseVerificationResponse(
    val state: String,
    val expiryTimeMillis: Long? = null,
    val success: Boolean = true,
    val error: String? = null
)

enum class EntitlementState {
    ACTIVE,
    PAUSED,
    IN_GRACE,
    ON_HOLD,
    CANCELED,
    EXPIRED,
    PENDING,
    UNKNOWN
}

data class ProductConfig(
    val productId: String,
    val isSubscription: Boolean,
    val displayName: String,
    val description: String,
    val features: List<String>
)

// Product IDs - should match Google Play Console
object Products {
    const val SUPPORTER_MONTHLY = "supporter_monthly"
    const val SUPPORTER_YEARLY = "supporter_yearly"
    const val LIFETIME_UNLOCK = "lifetime_unlock"
    
    val SUBSCRIPTION_PRODUCTS = listOf(SUPPORTER_MONTHLY, SUPPORTER_YEARLY)
    val INAPP_PRODUCTS = listOf(LIFETIME_UNLOCK)
    
    val ALL_PRODUCTS = mapOf(
        SUPPORTER_MONTHLY to ProductConfig(
            productId = SUPPORTER_MONTHLY,
            isSubscription = true,
            displayName = "Monthly Access",
            description = "Monthly access to core tracking features",
            features = listOf(
                "Access to Home screen tracking",
                "Access to Journey history",
                "Access to Rewards system",
                "Settings and Support always free"
            )
        ),
        SUPPORTER_YEARLY to ProductConfig(
            productId = SUPPORTER_YEARLY,
            isSubscription = true,
            displayName = "Yearly Access", 
            description = "Yearly access to core tracking features (save vs monthly)",
            features = listOf(
                "Access to Home screen tracking",
                "Access to Journey history",
                "Access to Rewards system",
                "Settings and Support always free",
                "Save vs monthly pricing"
            )
        ),
        LIFETIME_UNLOCK to ProductConfig(
            productId = LIFETIME_UNLOCK,
            isSubscription = false,
            displayName = "Lifetime Access",
            description = "One-time purchase for permanent access to all features",
            features = listOf(
                "Permanent access to Home screen tracking",
                "Permanent access to Journey history", 
                "Permanent access to Rewards system",
                "Settings and Support always free",
                "No recurring payments"
            )
        )
    )
}