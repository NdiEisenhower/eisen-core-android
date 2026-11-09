package com.eisen.core.analytics.bridge

import android.webkit.JavascriptInterface
import com.eisen.core.analytics.IAnalyticsManager
import com.eisen.core.common.bridge.JsBridge
import org.json.JSONObject

class AnalyticsJsBridge(
    private val analyticsManager: IAnalyticsManager
) : JsBridge("AndroidAnalytics") {

    @JavascriptInterface
    fun logEvent(name: String, paramsJson: String? = null) {
        val map = mutableMapOf<String, Any>()
        if (!paramsJson.isNullOrBlank()) {
            try {
                val json = JSONObject(paramsJson)
                json.keys().forEach { key ->
                    map[key] = json.get(key)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        analyticsManager.logEvent(name, map)
    }

    @JavascriptInterface
    fun logScreen(screenName: String, screenClass: String? = null) {
        analyticsManager.logScreen(screenName, screenClass)
    }

    @JavascriptInterface
    fun setUserId(userId: String) {
        analyticsManager.setUserId(userId.ifEmpty { null })
    }

    @JavascriptInterface
    fun setUserProperty(name: String, value: String) {
        analyticsManager.setUserProperty(name, value)
    }

    @JavascriptInterface
    fun reset() {
        analyticsManager.resetAnalyticsData()
    }
}

