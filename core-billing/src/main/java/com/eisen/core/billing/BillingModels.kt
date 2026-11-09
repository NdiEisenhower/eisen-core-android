package com.eisen.core.billing

enum class ProductKind {
    INAPP,
    SUBSCRIPTION
}

data class CoreProductItem(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val kind: ProductKind = ProductKind.INAPP
)

data class CorePurchase(
    val orderId: String?,
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val isAcknowledged: Boolean
)

interface BillingListener {
    fun onBillingServiceConnected() {}
    fun onProductsRetrieved(products: List<CoreProductItem>) {}
    fun onPurchaseSuccessful(purchase: CorePurchase) {}
    fun onPurchaseFailure(errorCode: Int, message: String) {}
}

