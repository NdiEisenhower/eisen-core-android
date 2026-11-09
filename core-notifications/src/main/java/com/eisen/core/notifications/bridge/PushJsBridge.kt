package com.eisen.core.notifications.bridge

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.notifications.IPushManager
import com.eisen.core.notifications.PushNotificationListener
import com.eisen.core.notifications.channel.NotificationChannelHelper
import com.eisen.core.notifications.model.NotificationChannelConfig
import com.eisen.core.notifications.model.NotificationPayload
import org.json.JSONObject

class PushJsBridge(
    private val activity: Activity,
    private val pushManager: IPushManager,
    private val webView: WebView? = null
) : JsBridge("AndroidPush"), PushNotificationListener {

    init {
        pushManager.addListener(this)
    }

    @JavascriptInterface
    fun getPushToken() {
        activity.runOnUiThread {
            pushManager.getPushToken { result ->
                val token = result.token ?: ""
                val provider = result.provider
                val isSuccess = result.isSuccess
                val error = result.errorMessage ?: ""
                dispatchJs(
                    """
                    if (typeof window.onPushTokenReceived === 'function') {
                        window.onPushTokenReceived('$token', '$provider', $isSuccess, '$error');
                    }
                    """.trimIndent()
                )
            }
        }
    }

    @JavascriptInterface
    fun subscribeToTopic(topic: String) {
        activity.runOnUiThread {
            pushManager.subscribeToTopic(topic) { success ->
                dispatchJs(
                    """
                    if (typeof window.onTopicSubscribed === 'function') {
                        window.onTopicSubscribed('$topic', $success);
                    }
                    """.trimIndent()
                )
            }
        }
    }

    @JavascriptInterface
    fun unsubscribeFromTopic(topic: String) {
        activity.runOnUiThread {
            pushManager.unsubscribeFromTopic(topic) { success ->
                dispatchJs(
                    """
                    if (typeof window.onTopicUnsubscribed === 'function') {
                        window.onTopicUnsubscribed('$topic', $success);
                    }
                    """.trimIndent()
                )
            }
        }
    }

    @JavascriptInterface
    fun createChannel(id: String, name: String, description: String = "") {
        activity.runOnUiThread {
            NotificationChannelHelper.createChannel(
                activity,
                NotificationChannelConfig(
                    id = id,
                    name = name,
                    description = description
                )
            )
        }
    }

    override fun onTokenReceived(token: String) {
        activity.runOnUiThread {
            dispatchJs(
                """
                if (typeof window.onPushTokenReceived === 'function') {
                    window.onPushTokenReceived('$token', 'ACTIVE', true, '');
                }
                """.trimIndent()
            )
        }
    }

    override fun onNotificationReceived(payload: NotificationPayload) {
        activity.runOnUiThread {
            val json = JSONObject().apply {
                put("title", payload.title)
                put("body", payload.body)
                put("deepLink", payload.deepLink ?: "")
                put("channelId", payload.channelId)
                val dataObj = JSONObject()
                payload.data.forEach { (k, v) -> dataObj.put(k, v) }
                put("data", dataObj)
            }
            dispatchJs(
                """
                if (typeof window.onNotificationReceived === 'function') {
                    window.onNotificationReceived(${json.toString()});
                }
                """.trimIndent()
            )
        }
    }

    override fun onNotificationClicked(deepLink: String, data: Map<String, String>) {
        activity.runOnUiThread {
            val dataJson = JSONObject().apply {
                data.forEach { (k, v) -> put(k, v) }
            }.toString()
            dispatchJs(
                """
                if (typeof window.onNotificationClicked === 'function') {
                    window.onNotificationClicked('$deepLink', $dataJson);
                }
                """.trimIndent()
            )
        }
    }

    private fun dispatchJs(script: String) {
        webView?.evaluateJavascript(script, null)
    }

    fun destroy() {
        pushManager.removeListener(this)
    }
}

