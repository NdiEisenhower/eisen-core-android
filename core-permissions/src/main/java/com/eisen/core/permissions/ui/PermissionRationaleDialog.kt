package com.eisen.core.permissions.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PermissionRationaleDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Allow",
    dismissButtonText: String = "Not Now",
    isPermanentlyDenied: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(if (isPermanentlyDenied) "Open Settings" else confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        },
        modifier = modifier
    )
}

