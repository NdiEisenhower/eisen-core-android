package com.eisen.core.billing

import android.app.Activity

interface IBillingManager {
    fun initialize(onConnected: (() -> Unit)? = null)
    fun queryProducts(productIds: List<String>, isSubscription: Boolean = false)
    fun launchPurchase(activity: Activity, productId: String)
    fun consumePurchase(purchaseToken: String, onComplete: (Boolean) -> Unit)
    fun queryPurchases()
    fun addListener(listener: BillingListener)
    fun removeListener(listener: BillingListener)
    fun destroy()
}

