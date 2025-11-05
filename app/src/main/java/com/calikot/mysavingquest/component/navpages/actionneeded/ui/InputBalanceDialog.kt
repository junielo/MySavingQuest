package com.calikot.mysavingquest.component.navpages.actionneeded.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.ActionNeededVM
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Icon

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InputBalanceDialog(
    viewModel: ActionNeededVM,
    showDialog: Boolean,
    title: String,
    onCancel: () -> Unit,
    onSubmit: (Boolean) -> Unit
) {
    // kept minimal local UI state — per-account inputs are in `amounts` map

    // Collect account items from the ViewModel
    val accounts by viewModel.accountItems.collectAsState(initial = emptyList())

    // Collect loading state from ViewModel
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // Keep a local editable map of input strings keyed by account id so Compose recomposes
    // when a field changes. Initialize from accounts when dialog opens or accounts change.
    val amounts = remember { mutableStateMapOf<Int, String>() }

    // Sync local amounts with current accounts when accounts change
    LaunchedEffect(accounts) {
        accounts.forEach { acc ->
            // initialize with existing billAmount if not present
            if (!amounts.containsKey(acc.id)) {
                amounts[acc.id] = if (acc.billAmount > 0) acc.billAmount.toString() else ""
            }
        }

        // Remove keys for accounts that no longer exist
        val ids = accounts.map { it.id }.toSet()
        val toRemove = amounts.keys.filter { it !in ids }
        toRemove.forEach { amounts.remove(it) }
    }

    if (showDialog) {
        Box(modifier = Modifier.fillMaxSize()) {
            AlertDialog(
                onDismissRequest = onCancel,
                confirmButton = {
                    Button(
                        onClick = {
                            // When submit is tapped, sync account notifications via ViewModel.
                            // The ViewModel will flip its loading state; once done we call onSubmit.
                            viewModel.syncAccountNotifications { it ->
                                if (it.values.all { it }) {
                                    onSubmit(true)
                                } else {
                                    onSubmit(false)
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Submit", color = Color.White, fontSize = 18.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = Color.Black, fontSize = 18.sp)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onCancel() }
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                    ) {
                        // If there are no account items show the old three inputs so dialog remains useful.
                        if (accounts.isNotEmpty()) {
                            // Show editable rows for each account item
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(accounts, key = { it.id }) { acc ->
                                    Column(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                    ) {
                                        Text(text = acc.notifName, style = MaterialTheme.typography.titleMedium)
                                        OutlinedTextField(
                                            value = amounts[acc.id] ?: "",
                                            onValueChange = { new ->
                                                // Keep only digits and optional negative? We'll allow digits only for simplicity
                                                val filtered = new.filter { it.isDigit() }
                                                amounts[acc.id] = filtered
                                                val newVal = filtered.toIntOrNull() ?: 0
                                                // Update ViewModel's accountItems via explicit helper
                                                viewModel.updateAccountItemAmount(acc.id, newVal)
                                            },
                                            placeholder = { Text("Enter amount") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )

            // Loading overlay on top of the dialog when ViewModel reports loading
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
