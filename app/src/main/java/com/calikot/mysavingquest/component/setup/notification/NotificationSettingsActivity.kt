package com.calikot.mysavingquest.component.setup.notification

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.R
import com.calikot.mysavingquest.component.setup.accountbalance.ui.AccountBalanceActivity
import com.calikot.mysavingquest.component.setup.notification.domain.NotificationSettingsVM
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import androidx.compose.runtime.collectAsState
import com.calikot.mysavingquest.component.navpages.drawer.MainDrawerActivity
import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.component.setup.notification.domain.models.INTERVAL_OPTIONS
import com.calikot.mysavingquest.ui.shared.LoadingDialog
import com.calikot.mysavingquest.util.convert24HourTo12Hour
import com.calikot.mysavingquest.util.convertTimeMillisToISOString

@AndroidEntryPoint
class NotificationSettingsActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySavingQuestTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                NotificationSettingsTopBar()
                            },
                            navigationIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Use your icon resource
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .shadow(8.dp, ambientColor = Color.Black, spotColor = Color.Black)
                        )
                    }
                ) { innerPadding ->
                    NotificationSettingsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotificationSettingsBody(modifier)
        }
        FloatingActionButton(
            onClick = {
                val intent = Intent(context, AccountBalanceActivity::class.java)
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            },
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationSettingsTopBar() {
    val context = LocalContext.current
    val viewModel: NotificationSettingsVM = hiltViewModel()
    val notifSettings by viewModel.notifSettings.collectAsState()
    var isButtonEnabled by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notification Settings",
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                val notifTimeFilled = notifSettings?.notifTime?.isNotEmpty() == true
                val intervalFilled = notifSettings?.accBalanceInterval?.isNotEmpty() == true
                if (notifTimeFilled && intervalFilled) {
                    if (isButtonEnabled) {
                        isButtonEnabled = false
                        viewModel.updateNotificationSettingsStatus()
                        val intent = Intent(context, MainDrawerActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                        Toast.makeText(context, "You're all set up.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please fill all the forms", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isButtonEnabled,
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
    var showTimePicker by remember { mutableStateOf(false) }

//    var expanded by remember { mutableStateOf(false) }
    val viewModel: NotificationSettingsVM = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val notifSettings by viewModel.notifSettings.collectAsState()
//    val interval = notifSettings?.accBalanceInterval ?: "Select interval"
    val time  = notifSettings?.notifTime ?: ""

    LoadingDialog(show = isLoading)

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(32.dp))
        // Interval
//        Text("Interval", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
//        ExposedDropdownMenuBox(
//            expanded = expanded,
//            onExpandedChange = { expanded = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp)
//        ) {
//            OutlinedTextField(
//                value = interval,
//                onValueChange = {},
//                readOnly = true,
//                trailingIcon = {
//                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                },
//                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth()
//            )
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier.exposedDropdownSize()
//            ) {
//                INTERVAL_OPTIONS.forEach { option ->
//                    DropdownMenuItem(
//                        text = { Text(option) },
//                        onClick = {
//                            val newNotifSetting = NotificationSettingsItem(
//                                id = notifSettings?.id,
//                                accBalanceInterval = option
//                            )
//                            viewModel.upsertNotificationSettings(newNotifSetting)
//                            expanded = false
//                        }
//                    )
//                }
//            }
//        }
        Spacer(modifier = Modifier.height(24.dp))
        // Time
        Text("Time", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = convert24HourTo12Hour(time),
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
                    .clickable { showTimePicker = true },
            )
        }
        if (showTimePicker) {
            val context = LocalContext.current
            val cal = Calendar.getInstance()
            val initialHour = try { time.split(":")[0].toInt() } catch (_: Exception) { cal.get(Calendar.HOUR_OF_DAY) }
            val initialMinute = try { time.split(":")[1].toInt() } catch (_: Exception) { cal.get(Calendar.MINUTE) }
            DisposableEffect(showTimePicker) {
                if (showTimePicker) {
                    val dialog = TimePickerDialog(context, { _, hour: Int, minute: Int ->
                        val notifTimeMillis = cal.apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val newNotifSetting = NotificationSettingsItem(
                            id = notifSettings?.id,
                            accBalanceInterval = "Daily",
                            notifTime = convertTimeMillisToISOString(notifTimeMillis)
                        )
                        viewModel.upsertNotificationSettings(newNotifSetting)
                        showTimePicker = false
                    }, initialHour, initialMinute, true)
                    dialog.setOnDismissListener { showTimePicker = false }
                    dialog.show()
                }
                onDispose { }
            }
        }
    }
}
