package com.eisen.core.share.bridge

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.eisen.core.common.bridge.JsBridge
import com.eisen.core.share.ShareHelper

class ShareJsBridge(
    private val activity: Activity,
    private val webView: WebView? = null
) : JsBridge("AndroidShare") {

    @JavascriptInterface
    fun shareText(text: String, title: String? = null) {
        activity.runOnUiThread {
            ShareHelper.shareText(activity, text, title ?: "Share")
        }
    }

    @JavascriptInterface
    fun shareLink(url: String, text: String? = null, title: String? = null) {
        activity.runOnUiThread {
            ShareHelper.shareLink(activity, url, text, title ?: "Share Link")
        }
    }

    @JavascriptInterface
    fun shareScore(gameName: String, score: Long, storeUrl: String? = null) {
        activity.runOnUiThread {
            ShareHelper.shareScore(activity, gameName, score, storeUrl)
        }
    }

    @JavascriptInterface
    fun captureAndShare(title: String? = null, message: String? = null) {
        activity.runOnUiThread {
            ShareHelper.captureAndShare(activity, webView, title ?: "Share", message)
        }
    }
}

