package com.eisen.core.notifications.deeplink

import android.content.Intent
import android.webkit.WebView

object PushDeepLinkHandler {

    const val EXTRA_DEEP_LINK = "eisen_extra_deep_link"

    private val DEEP_LINK_KEYS = listOf(
        EXTRA_DEEP_LINK,
        "deep_link",
        "deeplink",
        "url",
        "target_url",
        "targetUrl",
        "path",
        "route",
        "link"
    )

    /**
     * Extracts deep link URL or path from an Intent.
     */
    fun extractDeepLink(intent: Intent?): String? {
        if (intent == null) return null

        // 1. Check data URI
        intent.dataString?.let { return it }

        // 2. Check intent extras
        val extras = intent.extras ?: return null
        for (key in DEEP_LINK_KEYS) {
            val value = extras.getString(key)
            if (!value.isNullOrBlank()) {
                return value
            }
        }

        return null
    }

    /**
     * Extracts deep link URL or path from a push payload data map.
     */
    fun extractDeepLink(data: Map<String, String>?): String? {
        if (data == null) return null
        for (key in DEEP_LINK_KEYS) {
            val value = data[key]
            if (!value.isNullOrBlank()) {
                return value
            }
        }
        return null
    }

    /**
     * Checks an Intent for deep links and invokes callback if found.
     */
    fun handleIntent(intent: Intent?, onDeepLinkFound: (String) -> Unit): Boolean {
        val link = extractDeepLink(intent)
        return if (!link.isNullOrBlank()) {
            onDeepLinkFound(link)
            true
        } else {
            false
        }
    }

    /**
     * Navigates a WebView to the deep link or dispatches a JS event to the single-page app.
     */
    fun routeToWebView(
        webView: WebView?,
        deepLink: String,
        baseWebUrl: String? = null
    ) {
        if (webView == null || deepLink.isBlank()) return

        when {
            // Absolute URL -> Load directly
            deepLink.startsWith("http://") || deepLink.startsWith("https://") -> {
                webView.loadUrl(deepLink)
            }
            // Relative path with base URL -> Combine and load
            deepLink.startsWith("/") && !baseWebUrl.isNullOrBlank() -> {
                val fullUrl = baseWebUrl.trimEnd('/') + deepLink
                webView.loadUrl(fullUrl)
            }
            // In-app route or custom scheme -> Dispatch JS event to the frontend
            else -> {
                val safeLink = deepLink.replace("'", "\\'")
                val js = """
                    (function() {
                        if (typeof window.onNotificationDeepLink === 'function') {
                            window.onNotificationDeepLink('$safeLink');
                        } else if (typeof window.onNotificationClicked === 'function') {
                            window.onNotificationClicked('$safeLink', {});
                        } else {
                            window.dispatchEvent(new CustomEvent('eisen:deeplink', { detail: { path: '$safeLink' } }));
                        }
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
            }
        }
    }
}

