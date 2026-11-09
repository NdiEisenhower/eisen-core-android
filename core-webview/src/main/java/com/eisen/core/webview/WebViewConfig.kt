package com.eisen.core.webview

import android.os.Parcelable
import com.eisen.core.webview.model.BottomNavItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class WebViewConfig(
    val url: String,
    val enableJavaScript: Boolean = true,
    val enableDomStorage: Boolean = true,
    val enableDatabase: Boolean = true,
    val enableFileUpload: Boolean = true,
    val enableGeolocation: Boolean = false,
    val enableDpadFocus: Boolean = true,
    val allowBackNavigation: Boolean = true,
    val showProgressBar: Boolean = true,
    val userAgentSuffix: String? = null,
    val allowedHosts: List<String> = emptyList(),
    val bottomNavItems: List<BottomNavItem> = emptyList(),
    val initialSelectedNavId: String? = null
) : Parcelable {
    companion object {
        fun simple(url: String): WebViewConfig = WebViewConfig(url = url)
    }
}
