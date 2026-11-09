package com.eisen.core.notifications

import android.app.Activity
import android.content.Context
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.model.PushTokenResult
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.CopyOnWriteArrayList

class GooglePushManager(
    private val context: Context
) : IPushManager {

    companion object {
        internal val listeners = CopyOnWriteArrayList<PushNotificationListener>()
    }

    override fun initialize(activity: Activity?) {
        NotificationChannelHelper.createDefaultChannels(context)
    }

    override fun getPushToken(callback: (PushTokenResult) -> Unit) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        callback(PushTokenResult(token = token, provider = "GOOGLE", isSuccess = true))
                        token?.let { t ->
                            listeners.forEach { it.onTokenReceived(t) }
                        }
                    } else {
                        callback(
                            PushTokenResult(
                                token = null,
                                provider = "GOOGLE",
                                isSuccess = false,
                                errorMessage = task.exception?.localizedMessage
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            callback(
                PushTokenResult(
                    token = null,
                    provider = "GOOGLE",
                    isSuccess = false,
                    errorMessage = e.localizedMessage
                )
            )
        }
    }

    override fun subscribeToTopic(topic: String, callback: ((Boolean) -> Unit)?) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                callback?.invoke(task.isSuccessful)
            }
    }

    override fun unsubscribeFromTopic(topic: String, callback: ((Boolean) -> Unit)?) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                callback?.invoke(task.isSuccessful)
            }
    }

    override fun addListener(listener: PushNotificationListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: PushNotificationListener) {
        listeners.remove(listener)
    }

    override fun destroy() {
        listeners.clear()
    }
}

