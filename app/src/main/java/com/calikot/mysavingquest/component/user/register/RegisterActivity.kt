package com.calikot.mysavingquest.component.user.register

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
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
import com.calikot.mysavingquest.ui.theme.AppBackground
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



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
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                            registerUser(context, fullName, email, password, confirmPassword)
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
    }
}

fun registerUser(context: Context, fullName: String, email: String, password: String, confirmPassword: String) {
    val errorMsg = validateRegistrationForm(fullName, email, password, confirmPassword)
    if (errorMsg == null) {
        Toast.makeText(context, "Registration form is valid!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
}

fun validateRegistrationForm(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String
): String? {
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.isNotEmpty() && password.length >= 6
    val isFullNameValid = fullName.isNotEmpty()
    val isConfirmPasswordValid = password == confirmPassword
    return when {
        !isFullNameValid -> "Full Name is required."
        !isEmailValid -> "Invalid email address."
        !isPasswordValid -> "Password must be at least 6 characters."
        !isConfirmPasswordValid -> "Passwords do not match."
        else -> null
    }
}
