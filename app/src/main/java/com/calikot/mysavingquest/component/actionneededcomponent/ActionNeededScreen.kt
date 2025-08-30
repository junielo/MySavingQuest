package com.calikot.mysavingquest.component.actionneededcomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import com.calikot.mysavingquest.models.ActionNeededItem

@Composable
fun ActionNeededScreen(navController: NavController) {
    val items = listOf(
        ActionNeededItem(
            title = "House Rent",
            subtitle = "January 5, 2025 - ₱10,000",
            isInputBalance = false
        ),
        ActionNeededItem(
            title = "Account Balances",
            subtitle = "January 5, 2025 - 05:00 PM",
            isInputBalance = true
        )
    )
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items.size) { index ->
                ActionNeededListItem(item = items[index])
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
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
fun ActionNeededListItem(item: ActionNeededItem) {
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
