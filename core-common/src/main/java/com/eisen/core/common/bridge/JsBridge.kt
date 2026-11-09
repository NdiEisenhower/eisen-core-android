package com.eisen.core.common.bridge

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import java.lang.ref.WeakReference

/**
 * Base class for all Javascript Bridges across Eisen Core modules.
 * Ensures consistent naming, lifecycle attachment, and UI-thread safe JS evaluations.
 */
abstract class JsBridge(val interfaceName: String) {

    protected var webViewRef: WeakReference<WebView>? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    open fun attach(webView: WebView) {
        this.webViewRef = WeakReference(webView)
    }

    open fun detach() {
        this.webViewRef?.clear()
        this.webViewRef = null
    }

    /**
     * Safely executes JavaScript code on the attached WebView on the Main Thread.
     */
    fun evaluateJavascript(script: String) {
        mainHandler.post {
            val webView = webViewRef?.get()
            if (webView != null && webView.isAttachedToWindow) {
                webView.evaluateJavascript(script, null)
            }
        }
    }
}

