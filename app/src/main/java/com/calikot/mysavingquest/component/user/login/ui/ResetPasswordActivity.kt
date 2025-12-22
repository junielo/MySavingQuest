package com.calikot.mysavingquest.component.user.login.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import dagger.hilt.android.AndroidEntryPoint
import com.calikot.mysavingquest.component.user.login.domain.LoginVM

@AndroidEntryPoint
class ResetPasswordActivity : ComponentActivity() {
    private val loginVM: LoginVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySavingQuestTheme(dynamicColor = false) {
                Scaffold { innerPadding ->
                    ResetPasswordScreen(modifier = Modifier.padding(innerPadding), loginVM = loginVM, deepLinkData = intent?.data)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(modifier: Modifier = Modifier, loginVM: LoginVM, deepLinkData: Uri?) {
    val activity = LocalContext.current as? ComponentActivity

    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    val errorDialogMessage by loginVM.errorMessage.collectAsState()
    val infoDialogMessage by loginVM.infoMessage.collectAsState()
    val isLoading by loginVM.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create a new password",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(bottom = 12.dp)
        )

        Text(
            text = "Please choose a strong password. Your password should be at least 8 characters long and include a mix of letters and numbers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("New password") },
            placeholder = { Text(text = "Enter new password") },
            singleLine = true,
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                // purely UI: no action; keep keyboard behavior natural
            }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Text(if (passwordVisible.value) "Hide" else "Show")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Button(
            onClick = {
                // Delegate all verification and reset logic to the ViewModel.
                loginVM.resetPassword(password.value, deepLinkData)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Confirm", color = Color.White)
        }
    }

    // Reuse dialogs from LoginActivity (same package)
    LoadingDialog(showDialog = isLoading)
    ErrorDialog(errorDialogMessage) { loginVM.clearError() }
    InfoDialog(infoDialogMessage) {
        // On dismiss, clear info and navigate to LoginActivity
        loginVM.clearInfo()
        activity?.let {
            val intent = Intent(it, LoginActivity::class.java)
            it.startActivity(intent)
            it.finish()
        }
    }
}
