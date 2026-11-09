package com.eisen.core.analytics

import android.content.Context
import android.os.Build

object AnalyticsFactory {

    fun create(
        context: Context,
        store: AnalyticsStore = AnalyticsStore.AUTO
    ): IAnalyticsManager {
        return when (store) {
            AnalyticsStore.GOOGLE -> FirebaseAnalyticsManager(context)
            AnalyticsStore.HUAWEI -> HuaweiHiAnalyticsManager(context)
            AnalyticsStore.COMPOSITE -> CompositeAnalyticsManager(
                FirebaseAnalyticsManager(context),
                HuaweiHiAnalyticsManager(context)
            )
            AnalyticsStore.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiHiAnalyticsManager(context)
                } else {
                    FirebaseAnalyticsManager(context)
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

