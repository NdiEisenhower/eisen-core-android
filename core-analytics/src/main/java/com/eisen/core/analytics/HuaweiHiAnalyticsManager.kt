package com.eisen.core.analytics

import android.content.Context
import android.os.Bundle
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance

class HuaweiHiAnalyticsManager(
    context: Context
) : IAnalyticsManager {

    private val instance: HiAnalyticsInstance = HiAnalytics.getInstance(context)

    override fun logEvent(name: String, params: Map<String, Any>?) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        logEvent(name, bundle)
    }

    override fun logEvent(name: String, bundle: Bundle?) {
        instance.onEvent(name, bundle)
    }

    override fun logScreen(screenName: String, screenClass: String?) {
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
            if (screenClass != null) {
                putString("screen_class", screenClass)
            }
        }
        instance.onEvent("screen_view", bundle)
    }

    override fun setUserId(userId: String?) {
        instance.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String) {
        instance.setUserProfile(name, value)
    }

    override fun resetAnalyticsData() {
        instance.clearCachedData()
    }
}

