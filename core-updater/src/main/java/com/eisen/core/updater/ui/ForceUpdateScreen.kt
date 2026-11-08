package com.eisen.core.updater.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eisen.core.updater.UpdateInfo
import com.eisen.core.updater.UpdateType

@Composable
fun ForceUpdateScreen(
    appName: String = "the application",
    updateInfo: UpdateInfo = UpdateInfo(type = UpdateType.HARD),
    onUpdateClick: () -> Unit,
    onDismissClick: () -> Unit = {}
) {
    val isHardUpdate = updateInfo.type == UpdateType.HARD

    Dialog(
        onDismissRequest = {
            if (!isHardUpdate) {
                onDismissClick()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isHardUpdate,
            dismissOnClickOutside = !isHardUpdate
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isHardUpdate) "🚀" else "✨",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (isHardUpdate) "Update Required" else "Update Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isHardUpdate) {
                        "A mandatory update is required to continue using $appName. Please update to the latest version."
                    } else {
                        "A new version of $appName is available with performance improvements and new features!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Update Now",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (!isHardUpdate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDismissClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Later",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
