package com.calikot.mysavingquest.component.setup.accountbalance.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryAccountBalanceDialog(
    account: AccountBalanceItem? = null,
    onDismiss: () -> Unit,
    onUpdate: (AccountBalanceItem) -> Unit = {},
    onCreate: (type: String, name: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val isEditMode = account != null
    var accName by remember { mutableStateOf(account?.accName ?: "") }
    var accType by remember { mutableStateOf(account?.accType ?: "Credit") }
    val typeOptions = listOf("Credit", "Debit")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {}, // Custom buttons below
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEditMode) "Edit Account" else "Add Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Account Name", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                OutlinedTextField(
                    value = accName,
                    onValueChange = { accName = it },
                    placeholder = { Text("Account name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Text("Account Type", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = accType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Select type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        enabled = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    accType = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (accName.isNotBlank()) {
                                if (isEditMode) {
                                    val updatedAccount = AccountBalanceItem(
                                        id = account.id,
                                        accName = accName,
                                        accType = accType
                                    )
                                    onUpdate(updatedAccount)
                                } else {
                                    onCreate(accType, accName)
                                }
                            } else {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C), contentColor = Color.White)
                    ) {
                        Text(if (isEditMode) "Update" else "Create")
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
