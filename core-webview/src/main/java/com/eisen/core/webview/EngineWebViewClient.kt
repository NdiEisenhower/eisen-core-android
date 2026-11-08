package com.eisen.core.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URISyntaxException

class EngineWebViewClient(
    private val context: Context,
    private val allowedHosts: List<String> = emptyList(),
    private val onPageStarted: ((url: String?) -> Unit)? = null,
    private val onPageFinished: ((url: String?) -> Unit)? = null,
    private val onErrorReceived: ((errorCode: Int, description: String?) -> Unit)? = null
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false

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
