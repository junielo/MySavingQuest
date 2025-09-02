package com.calikot.mysavingquest.component.navpages.actionneeded.ui

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

@Composable
fun InputBalanceDialog(
    showDialog: Boolean,
    title: String,
    onCancel: () -> Unit,
    onSubmit: (bdoDebit: String, bdoCredit: String, gCredit: String) -> Unit
) {
    var bdoDebit by remember { mutableStateOf("") }
    var bdoCredit by remember { mutableStateOf("") }
    var gCredit by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onCancel,
            confirmButton = {
                Button(onClick = { onSubmit(bdoDebit, bdoCredit, gCredit) }) {
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
                    Text("BDO Debit", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = bdoDebit,
                        onValueChange = { bdoDebit = it },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BDO Credit", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = bdoCredit,
                        onValueChange = { bdoCredit = it },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GCredit", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = gCredit,
                        onValueChange = { gCredit = it },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
