package com.eisen.core.notifications.channel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eisen.core.notifications.deeplink.PushDeepLinkHandler
import com.eisen.core.notifications.model.NotificationChannelConfig
import com.eisen.core.notifications.model.NotificationPayload

object NotificationChannelHelper {

    fun createDefaultChannels(context: Context) {
        createChannel(
            context,
            NotificationChannelConfig(
                id = NotificationChannelConfig.DEFAULT_CHANNEL_ID,
                name = "General Announcements",
                description = "General app notifications and news",
                importance = NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        createChannel(
            context,
            NotificationChannelConfig(
                id = NotificationChannelConfig.DEEP_LINK_CHANNEL_ID,
                name = "Events & Offers",
                description = "Special promotions, game events, and deep link rewards",
                importance = NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun createChannel(context: Context, config: NotificationChannelConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val channel = NotificationChannel(config.id, config.name, config.importance).apply {
                description = config.description
                enableVibration(config.enableVibration)
                enableLights(config.enableLights)
                if (config.soundUri != null) {
                    setSound(Uri.parse(config.soundUri), null)
                }
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        payload: NotificationPayload,
        targetActivityClass: Class<*>? = null
    ) {
        // Ensure channel exists
        createChannel(
            context,
            NotificationChannelConfig(id = payload.channelId, name = "App Notifications")
        )

        // Resolve target intent
        val launchIntent = if (targetActivityClass != null) {
            Intent(context, targetActivityClass)
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
        }

        launchIntent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            payload.deepLink?.let { link ->
                putExtra(PushDeepLinkHandler.EXTRA_DEEP_LINK, link)
            }
            payload.data.forEach { (k, v) ->
                putExtra(k, v)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Default small icon (fallback to app icon)
        val smallIcon = context.applicationInfo.icon.let {
            if (it != 0) it else android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(context, payload.channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(payload.notificationId, builder.build())
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS runtime permission on Android 13+
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

