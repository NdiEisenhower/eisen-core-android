package com.eisen.core.notifications.model

data class NotificationPayload(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val channelId: String = NotificationChannelConfig.DEFAULT_CHANNEL_ID,
    val data: Map<String, String> = emptyMap(),
    val notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
)

