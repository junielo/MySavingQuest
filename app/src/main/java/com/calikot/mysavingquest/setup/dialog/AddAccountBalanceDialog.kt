package com.calikot.mysavingquest.setup.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBalanceDialog(
    onDismiss: () -> Unit,
    onCreate: (type: String, name: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("Credit/Debit Balance") }
    var name by remember { mutableStateOf("") }
    val typeOptions = listOf("Credit", "Debit")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 300.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Balance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Type", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Credit/Debit Balance") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        singleLine = true,

                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedType = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Name", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onCreate(selectedType, name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create", color = Color.White)
                    }
                }
            }
        }
    }
}
