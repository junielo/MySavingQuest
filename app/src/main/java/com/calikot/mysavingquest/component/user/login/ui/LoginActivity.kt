package com.calikot.mysavingquest.component.user.login.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.calikot.mysavingquest.component.navpages.drawer.MainDrawerActivity
import com.calikot.mysavingquest.R
import com.calikot.mysavingquest.component.setup.recurringbills.ui.RecurringBillsActivity
import com.calikot.mysavingquest.component.user.register.ui.RegisterActivity
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import com.calikot.mysavingquest.util.SupabaseHandler.startSessionListener
import com.calikot.mysavingquest.component.user.login.domain.LoginVM
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.util.SupabaseHandler
import io.github.jan.supabase.auth.handleDeeplinks
import androidx.lifecycle.lifecycleScope
import com.calikot.mysavingquest.component.setup.accountbalance.ui.AccountBalanceActivity
import com.calikot.mysavingquest.component.setup.notification.NotificationSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val loginVM: LoginVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSessionListener()
        supabase.handleDeeplinks(
            intent,
        ) { session ->
            lifecycleScope.launch {
                val setupStatusMsg = loginVM.insertUserSetupStatus()
                Toast.makeText(this@LoginActivity, setupStatusMsg, Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, RecurringBillsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        enableEdgeToEdge()
        setContent {
            MySavingQuestTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding), loginVM = loginVM)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, loginVM: LoginVM) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showLoadingDialog = remember { mutableStateOf(false) }
    val errorDialogMessage = remember { mutableStateOf<String?>(null) }
    val resendToggle = remember { mutableStateOf(false) }

    CheckAndNavigateIfLoggedIn(context, loginVM)

    SessionSignInListener {
        val intent = Intent(context, MainDrawerActivity::class.java)
        context.startActivity(intent)
        if (context is ComponentActivity) {
            context.finish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top-right Sign up button
        Button(
            onClick = {
                val intent = Intent(context, RegisterActivity::class.java)
                context.startActivity(intent)
            },
            modifier = modifier
                .align(Alignment.TopEnd),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Sign up", color = Color.White)
        }

        // Main content
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.my_app_logo), // Replace with your logo
                contentDescription = "Logo",
                modifier = modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            // Card with form
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    if (!resendToggle.value) {
                        OutlinedTextField(
                            value = password.value,
                            onValueChange = { password.value = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        // Sign In button
                        Button(
                            onClick = {
                                handleSignIn(
                                    context,
                                    loginVM,
                                    email,
                                    password,
                                    showLoadingDialog
                                )
                            },
                            modifier = modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Sign In", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { handleResendVerification(context, loginVM, email, showLoadingDialog) },
                            modifier = modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Send Verification", color = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resend Verification Link", modifier = Modifier.weight(1f))
                        Switch(
                            checked = resendToggle.value,
                            onCheckedChange = { resendToggle.value = it }
                        )
                    }

                    // Forgot password
//                    Text(
//                        text = "Forgot password?",
//                        color = Color.Black,
//                        textDecoration = TextDecoration.Underline,
//                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally)
//                            .clickable { /* TODO */ }
//                    )
                }
            }
        }
        LoadingDialog(showDialog = showLoadingDialog.value)
        ErrorDialog(errorDialogMessage.value) { errorDialogMessage.value = null }
    }
}

@Composable
fun CheckAndNavigateIfLoggedIn(context: Context, loginVM: LoginVM) {
    LaunchedEffect(Unit) {
        val session = loginVM.getUserSession()
        if (session != null) {
            val name = session.user?.identities?.get(0)?.identityData?.get("full_name")?.toString()
            val displayName = if (!name.isNullOrBlank()) name else session.user?.email
            Toast.makeText(context, "Welcome, $displayName!", Toast.LENGTH_LONG).show()
            val setupStatus = loginVM.getCurrentUserSetupStatus()
            when {
                !setupStatus.recurring_bills -> {
                    val intent = Intent(context, RecurringBillsActivity::class.java)
                    context.startActivity(intent)
                    if (context is ComponentActivity) context.finish()
                }
                !setupStatus.account_balance -> {
                    val intent = Intent(context, AccountBalanceActivity::class.java)
                    context.startActivity(intent)
                    if (context is ComponentActivity) context.finish()
                }
                !setupStatus.notif_settings -> {
                    val intent = Intent(context, NotificationSettingsActivity::class.java)
                    context.startActivity(intent)
                    if (context is ComponentActivity) context.finish()
                }
                else -> {
                    if (setupStatus.errorMessage == null) {
                        Toast.makeText(context, "Welcome, $displayName!", Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(context, MainDrawerActivity::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) context.finish()
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingDialog(showDialog: Boolean) {
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { Text("Signing In...") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Please wait while we log you in.")
                }
            },
            confirmButton = {},
            dismissButton = null
        )
    }
}

@Composable
fun ErrorDialog(errorMessage: String?, onDismiss: () -> Unit) {
    if (errorMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Login Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun SessionSignInListener(onSignIn: () -> Unit) {
    LaunchedEffect(Unit) {
        SupabaseHandler.sessionEvents.collect { event ->
            if (event is SupabaseHandler.SessionEvent.SignIn) {
                onSignIn()
            }
        }
    }
}

fun handleSignIn(
    context: Context,
    loginVM: LoginVM,
    email: MutableState<String>,
    password: MutableState<String>,
    showLoadingDialog: MutableState<Boolean>,
) {
    showLoadingDialog.value = true
    loginVM.signIn(context, email.value, password.value)
}

fun handleResendVerification(
    context: Context,
    loginVM: LoginVM,
    email: MutableState<String>,
    showLoadingDialog: MutableState<Boolean>
) {
    showLoadingDialog.value = true
    loginVM.resendVerificationEmail(context, email.value)
    showLoadingDialog.value = false
}
