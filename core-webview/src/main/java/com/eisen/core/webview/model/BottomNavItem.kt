package com.eisen.core.webview.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BottomNavItem(
    val id: String,
    val label: String,
    val iconName: String = "home",
    val targetUrl: String? = null,
    val badge: String? = null
) : Parcelable

