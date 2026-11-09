package com.eisen.core.notifications

import com.eisen.core.notifications.model.NotificationPayload

interface PushNotificationListener {
    fun onTokenReceived(token: String) {}
    fun onNotificationReceived(payload: NotificationPayload) {}
    fun onNotificationClicked(deepLink: String, data: Map<String, String>) {}
}

