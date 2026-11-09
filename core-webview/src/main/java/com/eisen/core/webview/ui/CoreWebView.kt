package com.eisen.core.webview.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.eisen.core.common.bridge.JsBridgeRegistry
import com.eisen.core.webview.EngineJsBridge
import com.eisen.core.webview.EngineWebChromeClient
import com.eisen.core.webview.EngineWebViewClient
import com.eisen.core.webview.WebViewConfig

/**
 * Pure Jetpack Compose WebView Component.
 * Supports progress bar, file uploads, full-screen video, D-Pad focus, and offline retry.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CoreWebView(
    config: WebViewConfig,
    modifier: Modifier = Modifier,
    bridgeRegistry: JsBridgeRegistry = remember { JsBridgeRegistry() },
    onWebViewCreated: ((WebView) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fullscreen HTML5 video container
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // File Upload Result Launcher
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    // Compose Back Navigation
    BackHandler(enabled = true) {
        if (customVideoView != null) {
            customViewCallback?.onCustomViewHidden()
            customVideoView = null
            customViewCallback = null
        } else if (config.allowBackNavigation && webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            activity?.finish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Native WebView wrapped into Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    with(settings) {
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
                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            isLoading = newProgress in 1..99
                        }

                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            if (!config.enableFileUpload) return false
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = filePathCallback

                            val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            return try {
                                fileChooserLauncher.launch(intent)
                                true
                            } catch (e: Exception) {
                                fileUploadCallback = null
                                false
                            }
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            customVideoView = view
                            customViewCallback = callback
                        }

                        override fun onHideCustomView() {
                            customVideoView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }
                    }

                    webViewClient = EngineWebViewClient(
                        context = ctx,
                        allowedHosts = config.allowedHosts,
                        onPageFinished = {
                            hasError = false
                            isLoading = false
                        },
                        onErrorReceived = { _, desc ->
                            hasError = true
                            errorMessage = desc
                            isLoading = false
                        }
                    )

                    if (activity != null) {
                        bridgeRegistry.register(EngineJsBridge(activity))
                    }
                    bridgeRegistry.attachAll(this)

                    webViewInstance = this
                    onWebViewCreated?.invoke(this)
                    loadUrl(config.url)
                }
            },
            update = {
                webViewInstance = it
            }
        )

        // Compose Material3 Linear Progress Bar
        if (config.showProgressBar && isLoading && !hasError) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // Fullscreen Video View Overlay
        customVideoView?.let { videoView ->
            AndroidView(
                factory = { videoView },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Compose Offline / Error Screen
        if (hasError) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📡",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Connection Failed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Could not connect to the server. Please check your network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            hasError = false
                            webViewInstance?.reload()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bridgeRegistry.detachAll()
            webViewInstance?.destroy()
        }
    }
}
