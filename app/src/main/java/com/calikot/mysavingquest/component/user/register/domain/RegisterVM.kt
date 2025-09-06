package com.calikot.mysavingquest.component.user.register.domain

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.calikot.mysavingquest.di.service.resendVerificationEmail
import com.calikot.mysavingquest.di.service.signUpNewUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterVM : ViewModel() {
    fun registerUser(
        context: Context,
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onFinished: () -> Unit,
        onError: (String) -> Unit
    ) {
        val errorMsg = validateRegistrationForm(fullName, email, password, confirmPassword)
        if (errorMsg == null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    signUpNewUser(fullName, email, password)
                    onFinished() // Registration success
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error occurred.")
                    onFinished() // Close loading dialog on error
                }
            }
        } else {
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            onFinished() // Also close loading dialog if validation fails
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

    fun resendVerificationEmail(context: android.content.Context, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resendVerificationEmail(email)
                // Show a toast on success
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Verification email resent!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to resend verification email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
