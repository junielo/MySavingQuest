package com.calikot.mysavingquest.setup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.calikot.mysavingquest.ui.theme.AppBackground

class NotificationSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NotificationSettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotificationSettingsTopBar()
            NotificationSettingsBody(modifier)
        }
        FloatingActionButton(
            onClick = { (context as? ComponentActivity)?.finish() },
            shape = CircleShape,
            containerColor = Color(0xFF2C2C2C),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun NotificationSettingsTopBar() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notification Icon",
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Notification Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = { (context as? ComponentActivity)?.finish() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsBody(
    modifier: Modifier = Modifier,
) {
    var interval by remember { mutableStateOf("Select interval") }
    var time by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    val intervalOptions = listOf("Daily", "Weekly", "Monthly")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(32.dp))
        // Interval
        Text("Interval", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = interval,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    intervalOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                interval = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Time
        Text("Time", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = time.ifEmpty { "Select time" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showTimePicker = true }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Time")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { showTimePicker = true },
                placeholder = { Text("Select time", color = Color(0xFFBDBDBD)) }
            )
        }
        // Time picker dialog (simple placeholder)
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Pick Time") },
                text = { Text("Time picker not implemented.") },
                confirmButton = {
                    TextButton(onClick = {
                        time = "08:00 AM"
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                }
            )
        }
    }
}
