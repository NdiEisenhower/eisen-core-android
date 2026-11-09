package com.eisen.core.notifications.model

import android.app.NotificationManager

data class NotificationChannelConfig(
    val id: String = DEFAULT_CHANNEL_ID,
    val name: String = "General Notifications",
    val description: String = "Default notification channel for updates and announcements",
    val importance: Int = NotificationManager.IMPORTANCE_HIGH,
    val enableVibration: Boolean = true,
    val enableLights: Boolean = true,
    val soundUri: String? = null
) {
    companion object {
        const val DEFAULT_CHANNEL_ID = "general_notifications"
        const val DEEP_LINK_CHANNEL_ID = "deeplink_notifications"
    }
}

