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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

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

    // Keep a local editable map of TextFieldValue keyed by account id so Compose recomposes
    // when a field changes. Initialize from accounts when dialog opens or accounts change.
    val amounts = remember { mutableStateMapOf<Int, TextFieldValue>() }

    // Helper: format a raw numeric string (digits and optional one dot) with commas for integer part
    fun formatNumber(raw: String): String {
        if (raw.isEmpty()) return ""
        // Ensure only digits and at most one dot
        val filtered = raw.filter { it.isDigit() || it == '.' }
        val dotIndex = filtered.indexOf('.')
        val intPart = if (dotIndex >= 0) filtered.substring(0, dotIndex) else filtered
        val fracPart = if (dotIndex >= 0) filtered.substring(dotIndex + 1) else null

        // Format integer part with commas
        val intDigits = intPart.filter { it.isDigit() }
        val intToFormat = intDigits.ifEmpty { "0" }
        val sb = StringBuilder()
        var count = 0
        for (i in intToFormat.length - 1 downTo 0) {
            sb.append(intToFormat[i])
            count++
            if (count % 3 == 0 && i != 0) sb.append(',')
        }
        val formattedInt = sb.reverse().toString()

        return if (dotIndex >= 0) {
            // Preserve fractional digits as typed (no automatic rounding/truncation)
            if (filtered.endsWith('.')) {
                // User typed a dot at the end
                "$formattedInt."
            } else {
                "$formattedInt.${fracPart ?: ""}"
            }
        } else {
            formattedInt
        }
    }

    // Helper: strip formatting and keep only digits and at most first dot
    fun extractRaw(text: String): String {
        val sb = StringBuilder()
        var dotSeen = false
        for (ch in text) {
            if (ch.isDigit()) sb.append(ch)
            else if (ch == '.' && !dotSeen) {
                sb.append('.')
                dotSeen = true
            }
        }
        return sb.toString()
    }

    // Sync local amounts with current accounts when accounts change
    LaunchedEffect(accounts) {
        accounts.forEach { acc ->
            // initialize with existing billAmount if not present
            if (!amounts.containsKey(acc.id)) {
                // We initialize using the integer billAmount; format it for display
                val initRaw = if (acc.billAmount > 0) acc.billAmount.toString() else ""
                val formatted = formatNumber(initRaw)
                amounts[acc.id] = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
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
                                            value = amounts[acc.id] ?: TextFieldValue("") ,
                                            onValueChange = { newValue ->
                                                // newValue is a TextFieldValue — we need to extract raw numeric, format it,
                                                // compute new cursor position and store formatted TextFieldValue while
                                                // updating the ViewModel (we keep calling the Int-based API by truncating decimals).

                                                val incoming = newValue.text

                                                // Compute how many non-comma characters (digits and dot) were to the left of the cursor
                                                val cursorPos = newValue.selection.start.coerceIn(0, incoming.length)
                                                val nonCommaBeforeCursor = incoming.substring(0, cursorPos).count { it != ',' }

                                                // Extract raw numeric (digits and at most one dot)
                                                val raw = extractRaw(incoming)

                                                // Format raw for display
                                                val formatted = formatNumber(raw)

                                                // Compute new cursor position in formatted text so caret stays near same digit position
                                                var newCursor = 0
                                                var nonCommaSeen = 0
                                                for (i in formatted.indices) {
                                                    if (formatted[i] != ',') nonCommaSeen++
                                                    if (nonCommaSeen >= nonCommaBeforeCursor) {
                                                        // place cursor after this non-comma character (digit or dot)
                                                        newCursor = i + 1
                                                        break
                                                    }
                                                }
                                                if (nonCommaBeforeCursor == 0) {
                                                    // place at start
                                                    newCursor = 0
                                                }
                                                if (newCursor > formatted.length) newCursor = formatted.length

                                                // If the original incoming cursor was after the dot and fractional exists, attempt to place accordingly
                                                // (above logic already counts fractional digits as digits so it works across the dot)

                                                amounts[acc.id] = TextFieldValue(text = formatted, selection = TextRange(newCursor))

                                                // Update ViewModel — we call Float API. We'll convert raw to Double and then to Float()
                                                val numericValue = raw.toDoubleOrNull() ?: 0.0
                                                viewModel.updateAccountItemAmount(acc.id, numericValue.toFloat())
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
