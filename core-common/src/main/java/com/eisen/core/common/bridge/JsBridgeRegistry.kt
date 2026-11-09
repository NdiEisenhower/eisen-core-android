package com.eisen.core.common.bridge

import android.webkit.WebView

/**
 * Manages registration of multiple JsBridges onto a WebView instance.
 */
class JsBridgeRegistry {

    private val bridges = mutableMapOf<String, JsBridge>()

    fun register(bridge: JsBridge): JsBridgeRegistry {
        bridges[bridge.interfaceName] = bridge
        return this
    }

    fun unregister(interfaceName: String): JsBridgeRegistry {
        bridges.remove(interfaceName)?.detach()
        return this
    }

    fun attachAll(webView: WebView) {
        bridges.values.forEach { bridge ->
            bridge.attach(webView)
            webView.addJavascriptInterface(bridge, bridge.interfaceName)
        }
    }

    fun detachAll() {
        bridges.values.forEach { it.detach() }
        bridges.clear()
    }

    fun getBridge(interfaceName: String): JsBridge? = bridges[interfaceName]
}

