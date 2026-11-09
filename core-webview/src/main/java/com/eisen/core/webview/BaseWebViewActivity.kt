package com.eisen.core.webview

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.eisen.core.common.bridge.JsBridgeRegistry
import com.eisen.core.webview.ui.CoreWebView

/**
 * Pure Jetpack Compose Base WebView Activity.
 * No XML layouts required.
 */
abstract class BaseWebViewActivity : ComponentActivity() {

    protected val bridgeRegistry = JsBridgeRegistry()
    protected var webView: WebView? = null

    abstract fun getWebViewConfig(): WebViewConfig

    open fun onCreateBridges(registry: JsBridgeRegistry) {
        // Subclasses can register BillingBridge, AuthBridge, AdBridge, etc.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onCreateBridges(bridgeRegistry)

        setContent {
            MaterialTheme {
                CoreWebView(
                    config = getWebViewConfig(),
                    bridgeRegistry = bridgeRegistry,
                    onWebViewCreated = { webView = it }
                )
            }
        }
    }

    override fun onDestroy() {
        bridgeRegistry.detachAll()
        super.onDestroy()
    }
}
