package com.eisen.core.notifications

import android.content.Context
import android.os.Build

object PushFactory {

    fun create(
        context: Context,
        store: PushStore = PushStore.AUTO,
        huaweiAppId: String? = null
    ): IPushManager {
        return when (store) {
            PushStore.GOOGLE -> GooglePushManager(context)
            PushStore.HUAWEI -> HuaweiPushManager(context, huaweiAppId)
            PushStore.AUTO -> {
                if (isHuaweiDevice(context)) {
                    HuaweiPushManager(context, huaweiAppId)
                } else {
                    GooglePushManager(context)
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

