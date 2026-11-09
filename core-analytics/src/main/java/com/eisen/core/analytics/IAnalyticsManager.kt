package com.eisen.core.analytics

import android.os.Bundle

interface IAnalyticsManager {
    fun logEvent(name: String, params: Map<String, Any>? = null)
    fun logEvent(name: String, bundle: Bundle?)
    fun logScreen(screenName: String, screenClass: String? = null)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
    fun resetAnalyticsData()
}

