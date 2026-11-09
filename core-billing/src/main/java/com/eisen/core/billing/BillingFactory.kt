package com.eisen.core.billing

import android.content.Context
import android.os.Build

enum class BillingStore {
    GOOGLE,
    HUAWEI,
    AUTO
}

object BillingFactory {

    fun create(context: Context, store: BillingStore = BillingStore.AUTO): IBillingManager {
        return when (store) {
            BillingStore.GOOGLE -> GoogleBillingManager(context)
            BillingStore.HUAWEI -> HuaweiBillingManager(context)
            BillingStore.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiBillingManager(context)
                } else {
                    GoogleBillingManager(context)
                }
            }
        }
    }

    private fun isHuaweiDevice(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val brand = Build.BRAND?.lowercase() ?: ""
        return manufacturer.contains("huawei") || manufacturer.contains("honor") ||
                brand.contains("huawei") || brand.contains("honor")
    }
}
