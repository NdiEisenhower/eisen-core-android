package com.eisen.core.review

import android.content.Context
import android.os.Build

enum class StoreFlavor {
    GOOGLE,
    HUAWEI,
    AUTO
}

object ReviewManagerFactory {

    fun create(context: Context, flavor: StoreFlavor = StoreFlavor.AUTO, huaweiAppId: String? = null): IReviewManager {
        return when (flavor) {
            StoreFlavor.GOOGLE -> GoogleReviewManager()
            StoreFlavor.HUAWEI -> HuaweiReviewManager(huaweiAppId)
            StoreFlavor.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiReviewManager(huaweiAppId)
                } else {
                    GoogleReviewManager()
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

