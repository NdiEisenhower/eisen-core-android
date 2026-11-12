package com.eisen.core.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.net.URISyntaxException

class EngineWebViewClient(
    private val context: Context,
    private val allowedHosts: List<String> = emptyList(),
    private val localAssetDomain: String? = null,
    private val blockWebAds: Boolean = false,
    private val adBlockDomains: Set<String> = DEFAULT_AD_DOMAINS,
    private val customAdHidingCss: String? = null,
    private val hideWebTopBar: Boolean = false,
    private val webTopBarSelectors: List<String> = DEFAULT_TOP_BAR_SELECTORS,
    private val customCss: String? = null,
    private val onPageStarted: ((url: String?) -> Unit)? = null,
    private val onPageFinished: ((url: String?) -> Unit)? = null,
    private val onErrorReceived: ((errorCode: Int, description: String?) -> Unit)? = null
) : WebViewClient() {

    companion object {
        val DEFAULT_AD_DOMAINS: Set<String> = setOf(
            "googlesyndication.com",
            "adservice.google.com",
            "doubleclick.net",
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "adsystem.amazon.com",
            "adnxs.com"
        )

        const val DEFAULT_AD_HIDING_CSS: String =
            "ins.adsbygoogle, .adsbygoogle, [id^='google_ads_iframe'], div[id*='google_ads'], .google-auto-placed { display: none !important; height: 0 !important; width: 0 !important; visibility: hidden !important; }"

        val DEFAULT_TOP_BAR_SELECTORS: List<String> = listOf(
            "header",
            "nav",
            ".navbar",
            ".top-bar",
            ".site-header",
            ".main-header",
            ".app-header",
            "#header",
            "#topbar",
            "#navbar",
            ".header-wrapper"
        )
    }

    private val assetLoader: WebViewAssetLoader? = localAssetDomain?.let { domain ->
        WebViewAssetLoader.Builder()
            .setDomain(domain)
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val uri = request?.url ?: return null
        assetLoader?.shouldInterceptRequest(uri)?.let { return it }

        // Native Ad Interception when you don't control the website
        if (blockWebAds) {
            val host = uri.host?.lowercase()
            if (host != null && adBlockDomains.any { host.contains(it) }) {
                return WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    ByteArrayInputStream(ByteArray(0))
                )
            }
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false

        // If it matches local asset domain, do not override
        if (!localAssetDomain.isNullOrEmpty() && uri.host.equals(localAssetDomain, ignoreCase = true)) {
            return false
        }

        // Handle native protocols outside HTTP/HTTPS
        if (scheme != "http" && scheme != "https") {
            return handleExternalScheme(uri)
        }

        // Host filter if configured
        if (allowedHosts.isNotEmpty()) {
            val host = uri.host?.lowercase()
            val isAllowed = allowedHosts.any { allowed -> host?.endsWith(allowed.lowercase()) == true }
            if (!isAllowed) {
                // Open unknown domains in external browser
                return openExternalBrowser(uri)
            }
        }

        return false
    }

    private fun handleExternalScheme(uri: Uri): Boolean {
        val url = uri.toString()
        try {
            if (url.startsWith("intent:")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent != null) {
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.component = null
                    intent.selector = null
                    if (context.packageManager.resolveActivity(intent, 0) != null) {
                        context.startActivity(intent)
                        return true
                    }
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (!fallbackUrl.isNullOrEmpty()) {
                        return handleExternalScheme(Uri.parse(fallbackUrl))
                    }
                }
            } else {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun openExternalBrowser(uri: Uri): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted?.invoke(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        val cssRules = mutableListOf<String>()

        // 1. Collapse blocked ads if enabled
        if (blockWebAds) {
            cssRules.add(customAdHidingCss ?: DEFAULT_AD_HIDING_CSS)
        }

        // 2. Hide top bar / header / navigation menu if enabled
        if (hideWebTopBar) {
            val selectors = webTopBarSelectors.joinToString(", ")
            cssRules.add("$selectors { display: none !important; height: 0 !important; max-height: 0 !important; overflow: hidden !important; visibility: hidden !important; }")
        }

        // 3. Any additional custom CSS rule passed by user
        if (!customCss.isNullOrBlank()) {
            cssRules.add(customCss)
        }

        if (cssRules.isNotEmpty()) {
            val combinedCss = cssRules.joinToString(" ").replace("'", "\\'")
            val js = """
                (function() {
                    var el = document.getElementById('eisen-injected-style');
                    if (!el) {
                        el = document.createElement('style');
                        el.id = 'eisen-injected-style';
                        el.type = 'text/css';
                        document.head.appendChild(el);
                    }
                    el.innerHTML = '$combinedCss';
                })();
            """.trimIndent()
            view?.evaluateJavascript(js, null)
        }

        // 4. Inject global app status into window.__EISEN_APP_STATUS__ and dispatch CustomEvent
        try {
            val statusJson = AppStatusHelper.getFullAppStatusJson(context)
            val escapedStatus = statusJson.replace("\\", "\\\\").replace("'", "\\'")
            val statusJs = """
                (function() {
                    try {
                        var status = JSON.parse('$escapedStatus');
                        window.__EISEN_APP_STATUS__ = status;
                        if (typeof window.onAppStatusLoaded === 'function') {
                            window.onAppStatusLoaded(status);
                        }
                        window.dispatchEvent(new CustomEvent('eisen:appstatus', { detail: status }));
                    } catch (e) {}
                })();
            """.trimIndent()
            view?.evaluateJavascript(statusJs, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onPageFinished?.invoke(url)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onErrorReceived?.invoke(error?.errorCode ?: -1, error?.description?.toString())
            } else {
                onErrorReceived?.invoke(-1, "Error loading page")
            }
        }
    }
}
