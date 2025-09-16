package com.calikot.mysavingquest.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class DialogType {
    SUCCESS, ERROR, WARNING, INFORMATION
}

@Composable
fun AlertDialogMessage(
    type: DialogType,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val (icon, iconColor) = when (type) {
        DialogType.SUCCESS -> Icons.Filled.CheckCircle to Color(0xFF4CAF50)
        DialogType.ERROR -> Icons.Filled.Clear to Color(0xFFF44336)
        DialogType.WARNING -> Icons.Filled.Warning to Color(0xFFFFC107)
        DialogType.INFORMATION -> Icons.Filled.Info to Color(0xFF2196F3)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        icon = {
            Icon(icon, contentDescription = null, tint = iconColor)
        },
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    )
}

@Composable
fun LoadingDialog(show: Boolean) {
    if (show) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000)), // semi-transparent background
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
    }
}
