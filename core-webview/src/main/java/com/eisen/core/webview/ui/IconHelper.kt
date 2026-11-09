package com.eisen.core.webview.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getIconByName(name: String): ImageVector {
        return when (name.lowercase()) {
            "home" -> Icons.Default.Home
            "store", "shop", "market", "cart" -> Icons.Default.ShoppingCart
            "profile", "user", "account", "person" -> Icons.Default.Person
            "settings", "gear" -> Icons.Default.Settings
            "notifications", "bell" -> Icons.Default.Notifications
            "star", "favorite", "heart" -> Icons.Default.Favorite
            "play", "game", "games" -> Icons.Default.PlayArrow
            "search" -> Icons.Default.Search
            "info" -> Icons.Default.Info
            "share" -> Icons.Default.Share
            "email", "mail" -> Icons.Default.Email
            "help" -> Icons.Default.Help
            else -> Icons.Default.Home
        }
    }
}
