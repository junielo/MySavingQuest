package com.calikot.mysavingquest.component.setup.recurringbills.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.util.convertDateMillisToISOString
import com.calikot.mysavingquest.util.isoStringToTimestamp
import com.calikot.mysavingquest.util.longToFormattedDateString
import com.calikot.mysavingquest.util.validateDataClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryBillDialog(
    bill: RecurringBillItem? = null,
    onDismiss: () -> Unit,
    onUpdate: (RecurringBillItem) -> Unit = {},
    onCreate: (RecurringBillItem) -> Unit = {}
) {
    val context = LocalContext.current
    val isEditMode = bill != null
    println("qwerty - date: ${bill?.date}")
    var date by remember { mutableLongStateOf(isoStringToTimestamp(bill?.date ?: "")) }
    var name by remember { mutableStateOf(bill?.name ?: "") }
    var amount by remember { mutableStateOf(bill?.amount?.toString() ?: "") }
    var autoPay by remember { mutableStateOf(bill?.isAuto ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (date > 0L) date else null)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        date = it
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEditMode) "Edit Recurring Bill" else "Recurring Bills",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Date", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                OutlinedTextField(
                    value = longToFormattedDateString(date),
                    onValueChange = {},
                    placeholder = { Text("Select date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Calendar")
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                Text("Name", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Expense name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Text("Amount", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { mAmount -> amount = mAmount.filter { it.isDigit() } },
                    placeholder = { Text("Expense amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Amount")
                    }
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto Pay", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = autoPay, onCheckedChange = { autoPay = it })
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
                            val billItem = if (isEditMode) {
                                RecurringBillItem(
                                    id = bill.id,
                                    name = name,
                                    date = convertDateMillisToISOString(date),
                                    amount = amount.toIntOrNull() ?: bill.amount,
                                    isAuto = autoPay
                                )
                            } else {
                                RecurringBillItem(
                                    name = name,
                                    date = convertDateMillisToISOString(date),
                                    amount = amount.toIntOrNull() ?: 0,
                                    isAuto = autoPay
                                )
                            }
                            if (validateDataClass(billItem, listOf("id"))) {
                                if (isEditMode) onUpdate(billItem) else onCreate(billItem)
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
