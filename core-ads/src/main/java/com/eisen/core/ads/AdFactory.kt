package com.eisen.core.ads

import android.content.Context
import android.os.Build

enum class AdStore {
    GOOGLE,
    HUAWEI,
    AUTO
}

object AdFactory {

    fun create(context: Context, store: AdStore = AdStore.AUTO): IAdManager {
        return when (store) {
            AdStore.GOOGLE -> AdMobManager()
            AdStore.HUAWEI -> HuaweiAdsManager()
            AdStore.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiAdsManager()
                } else {
                    AdMobManager()
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

