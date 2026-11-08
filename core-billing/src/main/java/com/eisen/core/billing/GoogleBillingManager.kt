package com.eisen.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import java.util.concurrent.CopyOnWriteArrayList

class GoogleBillingManager(context: Context) : IBillingManager, PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "GoogleBillingManager"
    }

    private val applicationContext = context.applicationContext
    private val listeners = CopyOnWriteArrayList<BillingListener>()
    private val cachedProductDetails = mutableMapOf<String, ProductDetails>()

    private var billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var onConnectedCallback: (() -> Unit)? = null

    override fun initialize(onConnected: (() -> Unit)?) {
        this.onConnectedCallback = onConnected
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        } else {
            onConnected?.invoke()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing client successfully connected.")
            onConnectedCallback?.invoke()
            listeners.forEach { it.onBillingServiceConnected() }
            queryPurchases()
        } else {
            Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected. Will reconnect on next request.")
    }

    override fun queryProducts(productIds: List<String>, isSubscription: Boolean) {
        if (!billingClient.isReady) {
            initialize { queryProducts(productIds, isSubscription) }
            return
        }

        val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val coreProducts = mutableListOf<CoreProductItem>()
                for (details in queryProductDetailsList.productDetailsList) {
                    cachedProductDetails[details.productId] = details
                    val oneTimeOffer = details.oneTimePurchaseOfferDetails
                    coreProducts.add(
                        CoreProductItem(
                            productId = details.productId,
                            title = details.title,
                            description = details.description,
                            formattedPrice = oneTimeOffer?.formattedPrice ?: "",
                            priceAmountMicros = oneTimeOffer?.priceAmountMicros ?: 0L,
                            priceCurrencyCode = oneTimeOffer?.priceCurrencyCode ?: "",
                            kind = if (isSubscription) ProductKind.SUBSCRIPTION else ProductKind.INAPP
                        )
                    )
                }
                listeners.forEach { it.onProductsRetrieved(coreProducts) }
            } else {
                Log.e(TAG, "Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    override fun launchPurchase(activity: Activity, productId: String) {
        val details = cachedProductDetails[productId]
        if (details == null) {
            queryProducts(listOf(productId)) {
                val fetchedDetails = cachedProductDetails[productId]
                if (fetchedDetails != null) {
                    startBillingFlow(activity, fetchedDetails)
                } else {
                    listeners.forEach { it.onPurchaseFailure(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Item details not found") }
                }
            }
            return
        }
        startBillingFlow(activity, details)
    }

    private fun startBillingFlow(activity: Activity, details: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (p in purchases) {
                if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    val corePurchase = CorePurchase(
                        orderId = p.orderId,
                        productId = p.products.firstOrNull() ?: "",
                        purchaseToken = p.purchaseToken,
                        purchaseTime = p.purchaseTime,
                        isAcknowledged = p.isAcknowledged
                    )
                    listeners.forEach { it.onPurchaseSuccessful(corePurchase) }
                }
            }
        } else {
            listeners.forEach {
                it.onPurchaseFailure(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }

    override fun consumePurchase(purchaseToken: String, onComplete: (Boolean) -> Unit) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.consumeAsync(params) { billingResult, _ ->
            onComplete(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    override fun queryPurchases() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { p ->
                    if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val corePurchase = CorePurchase(
                            orderId = p.orderId,
                            productId = p.products.firstOrNull() ?: "",
                            purchaseToken = p.purchaseToken,
                            purchaseTime = p.purchaseTime,
                            isAcknowledged = p.isAcknowledged
                        )
                        listeners.forEach { it.onPurchaseSuccessful(corePurchase) }
                    }
                }
            }
        }
    }

    override fun addListener(listener: BillingListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: BillingListener) {
        listeners.remove(listener)
    }

    override fun destroy() {
        listeners.clear()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
