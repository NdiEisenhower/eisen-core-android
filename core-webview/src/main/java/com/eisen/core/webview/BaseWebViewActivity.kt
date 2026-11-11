package com.eisen.core.webview

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.eisen.core.common.bridge.JsBridgeRegistry
import com.eisen.core.webview.ui.CoreWebView

/**
 * Pure Jetpack Compose Base WebView Activity with automatic deep link handling,
 * audio focus management, and background pause/resume handling.
 * No XML layouts required.
 */
abstract class BaseWebViewActivity : ComponentActivity() {

    protected val bridgeRegistry = JsBridgeRegistry()
    protected var webView: WebView? = null
    protected val audioFocusHelper by lazy { AudioFocusHelper(this) { webView } }

    abstract fun getWebViewConfig(): WebViewConfig

    open fun onCreateBridges(registry: JsBridgeRegistry) {
        // Subclasses can register BillingBridge, AuthBridge, AdBridge, PushJsBridge, AnalyticsJsBridge, GameJsBridge, etc.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        onCreateBridges(bridgeRegistry)

        setContent {
            MaterialTheme {
                CoreWebView(
                    config = getWebViewConfig(),
                    bridgeRegistry = bridgeRegistry,
                    onWebViewCreated = {
                        webView = it
                        audioFocusHelper.requestAudioFocus()
                        handleDeepLinkIntent(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()
        audioFocusHelper.requestAudioFocus()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        webView?.pauseTimers()
        audioFocusHelper.abandonAudioFocus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    /**
     * Inspects incoming intent for deep link extras from push notifications or external URIs.
     */
    protected open fun handleDeepLinkIntent(intent: Intent?) {
        val deepLink = intent?.getStringExtra("eisen_extra_deep_link")
            ?: intent?.dataString
            ?: intent?.getStringExtra("deep_link")
            ?: intent?.getStringExtra("path")
            ?: intent?.getStringExtra("url")

        if (!deepLink.isNullOrBlank()) {
            onDeepLinkReceived(deepLink)
        }
    }

    /**
     * Default deep link routing to WebView or frontend JavaScript event.
     */
    protected open fun onDeepLinkReceived(deepLink: String) {
        val wv = webView ?: return
        when {
            deepLink.startsWith("http://") || deepLink.startsWith("https://") -> {
                wv.loadUrl(deepLink)
            }
            deepLink.startsWith("/") -> {
                val base = getWebViewConfig().url.trimEnd('/')
                wv.loadUrl("$base$deepLink")
            }
            else -> {
                val safeLink = deepLink.replace("'", "\\'")
                wv.evaluateJavascript(
                    """
                    (function() {
                        if (typeof window.onNotificationDeepLink === 'function') {
                            window.onNotificationDeepLink('$safeLink');
                        } else if (typeof window.onNotificationClicked === 'function') {
                            window.onNotificationClicked('$safeLink', {});
                        } else {
                            window.dispatchEvent(new CustomEvent('eisen:deeplink', { detail: { path: '$safeLink' } }));
                        }
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }
    }

    override fun onDestroy() {
        audioFocusHelper.abandonAudioFocus()
        bridgeRegistry.detachAll()
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}
