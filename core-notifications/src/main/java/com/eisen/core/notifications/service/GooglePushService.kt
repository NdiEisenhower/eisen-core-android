package com.eisen.core.notifications.service

import com.eisen.core.notifications.GooglePushManager
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.deeplink.PushDeepLinkHandler
import com.eisen.core.notifications.model.NotificationChannelConfig
import com.eisen.core.notifications.model.NotificationPayload
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GooglePushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GooglePushManager.listeners.forEach {
            it.onTokenReceived(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: applicationInfo.loadLabel(packageManager).toString()
        val body = notification?.body ?: data["body"] ?: data["message"] ?: ""
        val deepLink = PushDeepLinkHandler.extractDeepLink(data)
        val channelId = notification?.channelId ?: data["channel_id"] ?: NotificationChannelConfig.DEFAULT_CHANNEL_ID

        val payload = NotificationPayload(
            title = title,
            body = body,
            deepLink = deepLink,
            channelId = channelId,
            data = data
        )

        // Post notification locally with deep link PendingIntent
        NotificationChannelHelper.showNotification(applicationContext, payload)

        // Notify active in-app listeners
        GooglePushManager.listeners.forEach {
            it.onNotificationReceived(payload)
        }
    }
}

