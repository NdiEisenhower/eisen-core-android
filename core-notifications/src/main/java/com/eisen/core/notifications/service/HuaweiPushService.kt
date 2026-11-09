package com.eisen.core.notifications.service

import com.eisen.core.notifications.HuaweiPushManager
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.deeplink.PushDeepLinkHandler
import com.eisen.core.notifications.model.NotificationChannelConfig
import com.eisen.core.notifications.model.NotificationPayload
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import org.json.JSONObject

class HuaweiPushService : HmsMessageService() {

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        if (!token.isNullOrEmpty()) {
            HuaweiPushManager.listeners.forEach {
                it.onTokenReceived(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage == null) return

        val dataMap = mutableMapOf<String, String>()
        val rawData = remoteMessage.data
        if (!rawData.isNullOrEmpty()) {
            try {
                val json = JSONObject(rawData)
                json.keys().forEach { key ->
                    dataMap[key] = json.optString(key, "")
                }
            } catch (e: Exception) {
                dataMap["raw_data"] = rawData
            }
        }

        val notification = remoteMessage.notification
        val title = notification?.title ?: dataMap["title"] ?: applicationInfo.loadLabel(packageManager).toString()
        val body = notification?.body ?: dataMap["body"] ?: dataMap["message"] ?: ""
        val deepLink = PushDeepLinkHandler.extractDeepLink(dataMap)
        val channelId = notification?.channelId ?: dataMap["channel_id"] ?: NotificationChannelConfig.DEFAULT_CHANNEL_ID

        val payload = NotificationPayload(
            title = title,
            body = body,
            deepLink = deepLink,
            channelId = channelId,
            data = dataMap
        )

        // Post notification locally with deep link PendingIntent
        NotificationChannelHelper.showNotification(applicationContext, payload)

        // Notify active in-app listeners
        HuaweiPushManager.listeners.forEach {
            it.onNotificationReceived(payload)
        }
    }
}

