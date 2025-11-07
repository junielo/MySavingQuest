package com.calikot.mysavingquest.component.navpages.actionneeded.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.ActionNeededVM
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionDisplayItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsDeleteItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsUpdateItem
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
    val actionNeededList = domainItems.sortedBy { it.notifType }.map { d ->
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

        ActionDisplayItem(
            id = d.id,
            title = d.notifName,
            subtitle = subtitle,
            isInputBalance = (d.notifType == "ACCOUNT_GROUP"),
            billIsAuto = d.billIsAuto,
            notifType = d.notifType
        )
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        // Use a Box so we can overlay a centered loading indicator correctly
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(actionNeededList) { item ->
                    ActionNeededListItem(
                        viewModel,
                        item = item
                    )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActionNeededListItem(
    viewModel: ActionNeededVM,
    item: ActionDisplayItem
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCheckDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }

    // Hide action buttons and dim text when notifType is BILL_C_NONE
    val isHidden = item.notifType == "BILL_C_NONE"

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
                fontSize = 18.sp,
                color = if (isHidden) Color(0xFF757575) else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = if (isHidden) Color(0xFF9E9E9E) else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (!item.isInputBalance && !isHidden) {
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
        }
        if (!isHidden) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = Color(0xFF43A047), shape = CircleShape)
                    .clickable {
                        if (item.isInputBalance) {
                            showInputDialog = true
                        } else if (item.billIsAuto) {
                            // for automated bills show the info/confirmation dialog
                            showInfoDialog = true
                        } else {
                            showCheckDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isInputBalance) Icons.Filled.Settings else if (item.billIsAuto) Icons.Filled.Info else Icons.Filled.Check,
                    contentDescription = if (item.isInputBalance) "Settings" else if (item.billIsAuto) "Info" else "Check",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    ConfirmationDialog(
        showDialog = showDeleteDialog,
        title = "Delete Confirmation",
        message = "Are you sure you want to delete this item?",
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            val billDelete = BillsDeleteItem(
                id = item.id,
                isRecorded = true,
                billAmount = 0
            )
            viewModel.deleteBillsNotification(billDelete) { success ->
                showCheckDialog = false
                if (success) {
                    Toast.makeText(
                        context,
                        "Bill marked as deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Something went wrong please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )
    ConfirmationDialog(
        showDialog = showCheckDialog,
        title = "Bill Confirmation",
        message = "Are you sure you want to confirm this bill?",
        onDismiss = { showCheckDialog = false },
        onConfirm = {
            val billUpdate = BillsUpdateItem(
                id = item.id,
                isRecorded = true
            )
            viewModel.updateBillsNotification(billUpdate) { success ->
                showCheckDialog = false
                if (success) {
                    Toast.makeText(
                        context,
                        "Bill is marked as recorded successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Something went wrong please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )
    ConfirmationDialog(
        showDialog = showInfoDialog,
        title = "Information",
        message = "This bill is set to automatic payment. Input your account balances to confirm this payment.",
        onConfirm = { showInfoDialog = false },
        onDismiss = { showInfoDialog = false }
    )
    InputBalanceDialog(
        viewModel = viewModel,
        showDialog = showInputDialog,
        title = item.title,
        onCancel = { showInputDialog = false },
        onSubmit = { it ->
            if (it) {
                showInputDialog = false
                Toast.makeText(
                    context,
                    "Account balances updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Something went wrong please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    )
}
