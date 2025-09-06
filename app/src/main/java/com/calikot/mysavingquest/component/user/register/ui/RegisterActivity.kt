package com.calikot.mysavingquest.component.user.register.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.calikot.mysavingquest.R
import com.calikot.mysavingquest.component.user.register.domain.RegisterVM
import com.calikot.mysavingquest.ui.theme.AppBackground
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import com.calikot.mysavingquest.util.SupabaseHandler


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySavingQuestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val registerVM = remember { RegisterVM() }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

//    SessionSignUpListener { showWelcomeDialog = true }
    WelcomeDialog(showDialog = showWelcomeDialog, onContinue = {
        showWelcomeDialog = false
        (context as? ComponentActivity)?.finish()
    }, onResend = {
        // Handle resend verification email
        registerVM.resendVerificationEmail(context, email)
    })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Top-right Sign up button
        Button(
            onClick = {
                (context as? ComponentActivity)?.finish()
            },
            modifier = modifier
                .align(Alignment.TopEnd),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Log In", color = Color.White)
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
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", style = MaterialTheme.typography.bodySmall) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", style = MaterialTheme.typography.bodySmall) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(45.dp))

                    // Sign In button
                    Button(
                        onClick = {
                            showLoadingDialog = true
                            registerVM.registerUser(
                                context,
                                fullName,
                                email,
                                password,
                                confirmPassword,
                                onFinished = {
                                    showLoadingDialog = false
                                    showWelcomeDialog = true
                                },
                                onError = { errorMsg ->
                                    showLoadingDialog = false
                                    errorDialogMessage = errorMsg
                                }
                            )
                        },
                        modifier = modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Register", color = Color.White)
                    }

                    // Forgot password
                    Text(
                        text = "Forgot password?",
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { /* TODO */ }
                    )
                }
            }
        }

        LoadingDialog(showDialog = showLoadingDialog)
        ErrorDialog(errorDialogMessage) { errorDialogMessage = null }
    }
}

//@Composable
//fun SessionSignUpListener(onSignUp: () -> Unit) {
//    LaunchedEffect(Unit) {
//        SupabaseHandler.sessionEvents.collect { event ->
//            if (event is SupabaseHandler.SessionEvent.SignUp) {
//                onSignUp()
//            }
//        }
//    }
//}

@Composable
fun WelcomeDialog(showDialog: Boolean, onContinue: () -> Unit, onResend: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {}, // Unclosable by tapping outside
            title = { Text("Check Your Email") },
            text = {
                Text("Your account has been created. Please check your email for a verification link to activate your account. If you did not receive the email, you can resend the verification.")
            },
            confirmButton = {
                Button(onClick = onContinue) {
                    Text("Continue")
                }
            },
            dismissButton = {
                Button(onClick = onResend) {
                    Text("Resend Verification Email")
                }
            }
        )
    }
}

@Composable
fun LoadingDialog(showDialog: Boolean) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Registering...") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Please wait while we create your account.")
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
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Registration Error") },
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
