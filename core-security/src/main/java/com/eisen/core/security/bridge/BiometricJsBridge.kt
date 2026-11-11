package com.eisen.core.security.bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.security.BiometricHelper
import com.eisen.core.security.SecureStorage

class BiometricJsBridge(
    private val activity: FragmentActivity,
    private val webView: WebView? = null
) : JsBridge("AndroidBiometrics") {

    private val secureStorage by lazy { SecureStorage(activity) }

    @JavascriptInterface
    fun isAvailable(): Boolean {
        return BiometricHelper.isBiometricAvailable(activity)
    }

    @JavascriptInterface
    fun authenticate(title: String? = null, subtitle: String? = null) {
        activity.runOnUiThread {
            BiometricHelper.authenticate(
                activity = activity,
                title = title ?: "Authenticate",
                subtitle = subtitle,
                onSuccess = {
                    val js = """
                        if (typeof window.onBiometricSuccess === 'function') {
                            window.onBiometricSuccess();
                        }
                    """.trimIndent()
                    webView?.evaluateJavascript(js, null)
                },
                onError = { errorCode, errString ->
                    val errSafe = errString.toString().replace("'", "\\'")
                    val js = """
                        if (typeof window.onBiometricFailed === 'function') {
                            window.onBiometricFailed($errorCode, '$errSafe');
                        }
                    """.trimIndent()
                    webView?.evaluateJavascript(js, null)
                },
                onFailed = {
                    val js = """
                        if (typeof window.onBiometricFailed === 'function') {
                            window.onBiometricFailed(-1, 'Authentication failed');
                        }
                    """.trimIndent()
                    webView?.evaluateJavascript(js, null)
                }
            )
        }
    }

    @JavascriptInterface
    fun setSecureItem(key: String, value: String) {
        secureStorage.putString(key, value)
    }

    @JavascriptInterface
    fun getSecureItem(key: String): String {
        return secureStorage.getString(key, "") ?: ""
    }

    @JavascriptInterface
    fun removeSecureItem(key: String) {
        secureStorage.remove(key)
    }
}

