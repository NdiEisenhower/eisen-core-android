package com.eisen.core.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.eisen.core.common.bridge.JsBridgeRegistry

abstract class BaseWebViewActivity : AppCompatActivity() {

    protected lateinit var webView: WebView
    protected lateinit var progressBar: ProgressBar
    protected lateinit var errorContainer: View
    protected lateinit var customViewContainer: FrameLayout
    protected lateinit var btnRetry: Button

    protected val bridgeRegistry = JsBridgeRegistry()
    private var chromeClient: EngineWebChromeClient? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    abstract fun getWebViewConfig(): WebViewConfig

    open fun onCreateBridges(registry: JsBridgeRegistry) {
        // Register standard AndroidEngine bridge by default
        registry.register(EngineJsBridge(this))
    }

    open fun onPageLoaded(url: String?) {}

    open fun onPageError(errorCode: Int, description: String?) {
        errorContainer.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_webview)

        webView = findViewById(R.id.coreWebView)
        progressBar = findViewById(R.id.coreProgressBar)
        errorContainer = findViewById(R.id.coreErrorView)
        customViewContainer = findViewById(R.id.coreCustomViewContainer)
        btnRetry = findViewById(R.id.btnRetry)

        val config = getWebViewConfig()
        setupWebView(config)
        setupBackNavigation(config)

        btnRetry.setOnClickListener {
            errorContainer.visibility = View.GONE
            webView.reload()
        }

        onCreateBridges(bridgeRegistry)
        bridgeRegistry.attachAll(webView)

        loadTargetUrl(config.url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(config: WebViewConfig) {
        with(webView.settings) {
            javaScriptEnabled = config.enableJavaScript
            domStorageEnabled = config.enableDomStorage
            databaseEnabled = config.enableDatabase
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            config.userAgentSuffix?.let { suffix ->
                userAgentString = "$userAgentString $suffix"
            }
        }

        if (config.enableDpadFocus) {
            webView.isFocusable = true
            webView.isFocusableInTouchMode = true
            webView.requestFocus()
        }

        chromeClient = EngineWebChromeClient(
            progressBar = if (config.showProgressBar) progressBar else null,
            customViewContainer = customViewContainer,
            onFileChooser = { callback, params ->
                if (!config.enableFileUpload) return@EngineWebChromeClient false
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = callback
                try {
                    val intent = params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    fileUploadCallback = null
                    false
                }
            }
        )

        webView.webChromeClient = chromeClient
        webView.webViewClient = EngineWebViewClient(
            context = this,
            allowedHosts = config.allowedHosts,
            onPageFinished = { url ->
                errorContainer.visibility = View.GONE
                onPageLoaded(url)
            },
            onErrorReceived = { code, desc ->
                onPageError(code, desc)
            }
        )
    }

    private fun setupBackNavigation(config: WebViewConfig) {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (chromeClient?.isCustomViewShowing == true) {
                    chromeClient?.onHideCustomView()
                    return
                }
                if (config.allowBackNavigation && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    protected fun loadTargetUrl(url: String) {
        webView.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        bridgeRegistry.detachAll()
        webView.destroy()
        super.onDestroy()
    }
}

