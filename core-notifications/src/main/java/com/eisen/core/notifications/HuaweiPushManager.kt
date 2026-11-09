package com.eisen.core.notifications

import android.app.Activity
import android.content.Context
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.model.PushTokenResult
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.push.HmsMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class HuaweiPushManager(
    private val context: Context,
    private val huaweiAppId: String? = null
) : IPushManager {

    companion object {
        internal val listeners = CopyOnWriteArrayList<PushNotificationListener>()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun initialize(activity: Activity?) {
        NotificationChannelHelper.createDefaultChannels(context)
    }

    override fun getPushToken(callback: (PushTokenResult) -> Unit) {
        scope.launch {
            try {
                val appId = huaweiAppId ?: AGConnectServicesConfig.fromContext(context).getString("client/app_id")
                val token = HmsInstanceId.getInstance(context).getToken(appId, "HCM")

                withContext(Dispatchers.Main) {
                    if (!token.isNullOrEmpty()) {
                        callback(PushTokenResult(token = token, provider = "HUAWEI", isSuccess = true))
                        listeners.forEach { it.onTokenReceived(token) }
                    } else {
                        callback(
                            PushTokenResult(
                                token = null,
                                provider = "HUAWEI",
                                isSuccess = false,
                                errorMessage = "HMS returned empty token"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(
                        PushTokenResult(
                            token = null,
                            provider = "HUAWEI",
                            isSuccess = false,
                            errorMessage = e.localizedMessage
                        )
                    )
                }
            }
        }
    }

    override fun subscribeToTopic(topic: String, callback: ((Boolean) -> Unit)?) {
        try {
            HmsMessaging.getInstance(context).subscribe(topic)
                .addOnCompleteListener { task ->
                    callback?.invoke(task.isSuccessful)
                }
        } catch (e: Exception) {
            callback?.invoke(false)
        }
    }

    override fun unsubscribeFromTopic(topic: String, callback: ((Boolean) -> Unit)?) {
        try {
            HmsMessaging.getInstance(context).unsubscribe(topic)
                .addOnCompleteListener { task ->
                    callback?.invoke(task.isSuccessful)
                }
        } catch (e: Exception) {
            callback?.invoke(false)
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

