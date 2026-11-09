package com.eisen.core.webview

import android.content.Context
import android.content.Intent
import android.os.Build

class QuickWebViewActivity : BaseWebViewActivity() {

    companion object {
        const val EXTRA_CONFIG = "extra_core_webview_config"

        /**
         * Launch a ready-to-go WebView app with just a URL.
         */
        fun launch(context: Context, url: String) {
            launch(context, WebViewConfig.simple(url))
        }

        /**
         * Launch with full customized configuration.
         */
        fun launch(context: Context, config: WebViewConfig) {
            val intent = Intent(context, QuickWebViewActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, config)
            }
            context.startActivity(intent)
        }
    }

    override fun getWebViewConfig(): WebViewConfig {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CONFIG, WebViewConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONFIG)
        } ?: WebViewConfig.simple("about:blank")
    }
}

