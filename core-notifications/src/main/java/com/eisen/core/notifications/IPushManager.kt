package com.eisen.core.notifications

import android.app.Activity
import com.eisen.core.notifications.model.PushTokenResult

interface IPushManager {
    fun initialize(activity: Activity? = null)
    fun getPushToken(callback: (PushTokenResult) -> Unit)
    fun subscribeToTopic(topic: String, callback: ((Boolean) -> Unit)? = null)
    fun unsubscribeFromTopic(topic: String, callback: ((Boolean) -> Unit)? = null)
    fun addListener(listener: PushNotificationListener)
    fun removeListener(listener: PushNotificationListener)
    fun destroy()
}

