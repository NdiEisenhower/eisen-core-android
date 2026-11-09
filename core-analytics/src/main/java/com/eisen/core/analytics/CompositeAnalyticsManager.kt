package com.eisen.core.analytics

import android.os.Bundle

class CompositeAnalyticsManager(
    private val managers: List<IAnalyticsManager>
) : IAnalyticsManager {

    constructor(vararg managers: IAnalyticsManager) : this(managers.toList())

    override fun logEvent(name: String, params: Map<String, Any>?) {
        managers.forEach { it.logEvent(name, params) }
    }

    override fun logEvent(name: String, bundle: Bundle?) {
        managers.forEach { it.logEvent(name, bundle) }
    }

    override fun logScreen(screenName: String, screenClass: String?) {
        managers.forEach { it.logScreen(screenName, screenClass) }
    }

    override fun setUserId(userId: String?) {
        managers.forEach { it.setUserId(userId) }
    }

    override fun setUserProperty(name: String, value: String) {
        managers.forEach { it.setUserProperty(name, value) }
    }

    override fun resetAnalyticsData() {
        managers.forEach { it.resetAnalyticsData() }
    }
}

