package com.calikot.mysavingquest.component.navpages.actionneeded.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.calikot.mysavingquest.util.isoStringToTimestamp
import com.calikot.mysavingquest.util.longToFormattedDateString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.ActionNeededVM
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionNeededItem2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActionNeededActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActionNeededScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActionNeededScreen() {

    val viewModel = hiltViewModel<ActionNeededVM>()

    // Collect list and loading state from ViewModel
    val domainItems by viewModel.actionNeededList.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // Map domain model to UI model with friendly subtitle formatting
    val items = domainItems.map { d ->
        val subtitle = run {
            val ts = try { isoStringToTimestamp(d.notifTime) } catch (_: Exception) { 0L }
            if (ts > 0L) {
                // Format: "October 22, 2025 - 05:00 PM"
                val datePart = longToFormattedDateString(ts)
                val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timePart = try { timeFormatter.format(Date(ts)) } catch (_: Exception) { "" }
                val amountPart = if (d.billAmount > 0) " - ₱${d.billAmount}" else ""
                if (timePart.isNotBlank()) "$datePart - $timePart$amountPart" else "$datePart$amountPart"
            } else {
                // Fallback: show raw notifTime or amount if available
                val amountPart = if (d.billAmount > 0) " - ₱${d.billAmount}" else ""
                (d.notifTime.ifBlank { "" } + amountPart).trim()
            }
        }

        ActionNeededItem2(
            title = d.notifName,
            subtitle = subtitle,
            isInputBalance = (d.notifType == "account_balance")
        )
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        // Use a Box so we can overlay a centered loading indicator correctly
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items) { item ->
                    ActionNeededListItem(item = item)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionNeededListItem(item: ActionNeededItem2) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCheckDialog by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = Color(0xFFD32F2F), shape = CircleShape)
                .clickable { showDeleteDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = Color(0xFF43A047), shape = CircleShape)
                .clickable {
                    if (item.isInputBalance) {
                        showInputDialog = true
                    } else {
                        showCheckDialog = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isInputBalance) Icons.Filled.Settings else Icons.Filled.Check,
                contentDescription = if (item.isInputBalance) "Settings" else "Check",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    ConfirmationDialog(
        showDialog = showDeleteDialog,
        title = "Delete Confirmation",
        message = "Are you sure you want to delete this item?",
        onConfirm = { showDeleteDialog = false /* handle delete here */ },
        onDismiss = { showDeleteDialog = false }
    )
    ConfirmationDialog(
        showDialog = showCheckDialog,
        title = "Check Confirmation",
        message = "Are you sure you want to check this item?",
        onConfirm = { showCheckDialog = false /* handle check here */ },
        onDismiss = { showCheckDialog = false }
    )
    InputBalanceDialog(
        showDialog = showInputDialog,
        title = item.title,
        onCancel = { showInputDialog = false },
        onSubmit = { bdoDebit, bdoCredit, gCredit ->
            showInputDialog = false
            // handle input values here
        }
    )
}
