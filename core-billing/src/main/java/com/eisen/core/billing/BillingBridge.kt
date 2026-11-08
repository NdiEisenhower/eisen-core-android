package com.eisen.core.billing

import android.app.Activity
import android.webkit.JavascriptInterface
import com.eisen.core.common.bridge.JsBridge
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

class BillingBridge(
    activity: Activity,
    private val billingManager: IBillingManager
) : JsBridge("AndroidBilling"), BillingListener {

    private val activityRef = WeakReference(activity)

    init {
        billingManager.addListener(this)
    }

    @JavascriptInterface
    fun launchPurchase(productId: String) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            billingManager.launchPurchase(activity, productId)
        }
    }

    @JavascriptInterface
    fun queryProducts(productIdsJson: String) {
        try {
            val jsonArray = JSONArray(productIdsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            billingManager.queryProducts(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun consumePurchase(purchaseToken: String) {
        billingManager.consumePurchase(purchaseToken) { success ->
            evaluateJavascript("window.onPurchaseConsumed && window.onPurchaseConsumed('$purchaseToken', $success)")
        }
    }

    override fun onProductsRetrieved(products: List<CoreProductItem>) {
        val jsonArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject().apply {
                put("productId", p.productId)
                put("title", p.title)
                put("description", p.description)
                put("formattedPrice", p.formattedPrice)
                put("priceAmountMicros", p.priceAmountMicros)
                put("priceCurrencyCode", p.priceCurrencyCode)
            }
            jsonArray.put(obj)
        }
        evaluateJavascript("window.onProductsAvailable && window.onProductsAvailable($jsonArray)")
    }

    override fun onPurchaseSuccessful(purchase: CorePurchase) {
        val obj = JSONObject().apply {
            put("orderId", purchase.orderId)
            put("productId", purchase.productId)
            put("purchaseToken", purchase.purchaseToken)
            put("purchaseTime", purchase.purchaseTime)
            put("isAcknowledged", purchase.isAcknowledged)
        }
        evaluateJavascript("window.onPurchaseSuccess && window.onPurchaseSuccess($obj)")
    }

    override fun onPurchaseFailure(errorCode: Int, message: String) {
        val escapedMessage = JSONObject.quote(message)
        evaluateJavascript("window.onPurchaseFailed && window.onPurchaseFailed($errorCode, $escapedMessage)")
    }

    override fun detach() {
        billingManager.removeListener(this)
        super.detach()
    }
}
