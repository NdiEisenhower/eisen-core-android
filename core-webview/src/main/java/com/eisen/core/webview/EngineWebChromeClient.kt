package com.eisen.core.webview

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar

class EngineWebChromeClient(
    private val progressBar: ProgressBar?,
    private val customViewContainer: ViewGroup?,
    private val onFileChooser: ((ValueCallback<Array<Uri>>?, FileChooserParams?) -> Boolean)? = null
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar?.let {
            if (newProgress in 1..99) {
                it.visibility = View.VISIBLE
                it.progress = newProgress
            } else {
                it.visibility = View.GONE
            }
        }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return onFileChooser?.invoke(filePathCallback, fileChooserParams) ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, true, false)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback
        customViewContainer?.addView(view)
        customViewContainer?.visibility = View.VISIBLE
    }

    override fun onHideCustomView() {
        if (customView == null) return
        customViewContainer?.removeView(customView)
        customViewContainer?.visibility = View.GONE
        customViewCallback?.onCustomViewHidden()
        customView = null
        customViewCallback = null
    }

    val isCustomViewShowing: Boolean get() = customView != null
}

