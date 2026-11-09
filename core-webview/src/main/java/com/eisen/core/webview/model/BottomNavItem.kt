package com.eisen.core.webview.model

import java.io.Serializable

data class BottomNavItem(
    val id: String,
    val label: String,
    val iconName: String = "home",
    val targetUrl: String? = null,
    val badge: String? = null
) : Serializable
