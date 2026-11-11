package com.eisen.core.permissions.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.eisen.core.permissions.PermissionHelper

@Composable
fun NotificationPermissionRequester(
    onResult: (Boolean) -> Unit,
    showRationale: Boolean = true,
    rationaleTitle: String = "Enable Notifications",
    rationaleMessage: String = "Stay updated on new game events, rewards, and important announcements."
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(context)) {
                if (showRationale) {
                    showDialog = true
                } else {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                onResult(true)
            }
        } else {
            onResult(true)
        }
    }

    if (showDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionRationaleDialog(
            title = rationaleTitle,
            message = rationaleMessage,
            onConfirm = {
                showDialog = false
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDismiss = {
                showDialog = false
                onResult(false)
            }
        )
    }
}

