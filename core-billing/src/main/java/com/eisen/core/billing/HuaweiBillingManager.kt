package com.eisen.core.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList

class HuaweiBillingManager(context: Context) : IBillingManager {

    companion object {
        private const val TAG = "HuaweiBillingManager"
        const val REQ_CODE_BUY = 6666
    }

    private val applicationContext = context.applicationContext
    private val listeners = CopyOnWriteArrayList<BillingListener>()
    private val cachedProductMap = mutableMapOf<String, ProductInfo>()
    private var iapClient: IapClient = Iap.getIapClient(applicationContext)

    override fun initialize(onConnected: (() -> Unit)?) {
        val task = iapClient.isEnvReady
        task.addOnSuccessListener {
            Log.d(TAG, "Huawei IAP Environment is ready")
            onConnected?.invoke()
            listeners.forEach { it.onBillingServiceConnected() }
            queryPurchases()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Huawei IAP Environment check failed: ${exception.message}")
            if (exception is IapApiException) {
                val statusCode = exception.statusCode
                Log.e(TAG, "Status code: $statusCode")
            }
        }
    }

    override fun queryProducts(productIds: List<String>, isSubscription: Boolean) {
        val req = ProductInfoReq().apply {
            priceType = if (isSubscription) IapClient.PriceType.IN_APP_SUBSCRIPTION else IapClient.PriceType.IN_APP_CONSUMABLE
            this.productIds = ArrayList(productIds)
        }

        iapClient.obtainProductInfo(req)
            .addOnSuccessListener { result ->
                val list = result.productInfoList ?: emptyList()
                val coreList = mutableListOf<CoreProductItem>()
                for (info in list) {
                    cachedProductMap[info.productId] = info
                    coreList.add(
                        CoreProductItem(
                            productId = info.productId,
                            title = info.productName ?: "",
                            description = info.productDesc ?: "",
                            formattedPrice = info.price ?: "",
                            priceAmountMicros = info.microsPrice,
                            priceCurrencyCode = info.currency ?: "",
                            kind = if (isSubscription) ProductKind.SUBSCRIPTION else ProductKind.INAPP
                        )
                    )
                }
                listeners.forEach { it.onProductsRetrieved(coreList) }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to query Huawei products: ${e.message}")
            }
    }

    override fun launchPurchase(activity: Activity, productId: String) {
        val req = PurchaseIntentReq().apply {
            this.productId = productId
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
        }

        iapClient.createPurchaseIntent(req)
            .addOnSuccessListener { result ->
                val status = result.status
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, REQ_CODE_BUY)
                    } catch (e: Exception) {
                        listeners.forEach { it.onPurchaseFailure(-1, e.message ?: "Failed to start resolution") }
                    }
                }
            }
            .addOnFailureListener { e ->
                listeners.forEach { it.onPurchaseFailure(-1, e.message ?: "Purchase intent failed") }
            }
    }

    /**
     * Call this in Activity's onActivityResult to complete the Huawei purchase flow.
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQ_CODE_BUY || data == null) return false

        val purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(data)
        if (purchaseResultInfo.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
            val inAppData = purchaseResultInfo.inAppPurchaseData
            try {
                val json = JSONObject(inAppData)
                val purchase = CorePurchase(
                    orderId = json.optString("orderID"),
                    productId = json.optString("productId"),
                    purchaseToken = json.optString("purchaseToken"),
                    purchaseTime = json.optLong("purchaseTime"),
                    isAcknowledged = true
                )
                listeners.forEach { it.onPurchaseSuccessful(purchase) }
                return true
            } catch (e: Exception) {
                listeners.forEach { it.onPurchaseFailure(-1, "Failed to parse purchase data") }
            }
        } else {
            listeners.forEach { it.onPurchaseFailure(purchaseResultInfo.returnCode, purchaseResultInfo.errMsg ?: "Purchase cancelled") }
        }
        return false
    }

    override fun consumePurchase(purchaseToken: String, onComplete: (Boolean) -> Unit) {
        val req = ConsumeOwnedPurchaseReq().apply {
            this.purchaseToken = purchaseToken
        }
        iapClient.consumeOwnedPurchase(req)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    override fun queryPurchases() {
        val req = OwnedPurchasesReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
        }
        iapClient.obtainOwnedPurchases(req)
            .addOnSuccessListener { result ->
                val list = result.inAppPurchaseDataList ?: emptyList()
                for (item in list) {
                    try {
                        val json = JSONObject(item)
                        val purchase = CorePurchase(
                            orderId = json.optString("orderID"),
                            productId = json.optString("productId"),
                            purchaseToken = json.optString("purchaseToken"),
                            purchaseTime = json.optLong("purchaseTime"),
                            isAcknowledged = true
                        )
                        listeners.forEach { it.onPurchaseSuccessful(purchase) }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
    }
}
