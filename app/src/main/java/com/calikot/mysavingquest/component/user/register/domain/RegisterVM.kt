package com.calikot.mysavingquest.component.user.register.domain

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.UserHandlerService
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterVM @Inject constructor(
    private val userHandlerService: UserHandlerService
) : ViewModel() {
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
            viewModelScope.launch {
                try {
                    userHandlerService.signUpNewUser(fullName, email, password)
                    onFinished() // Registration success
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error occurred.")
                    onFinished() // Close loading dialog on error
                }
            }
        } else {
            onError(errorMsg)
            onFinished()
        }
    }

    fun resendVerificationEmail(context: Context, email: String) {
        viewModelScope.launch {
            try {
                userHandlerService.resendVerificationEmail(email)
                Toast.makeText(context, "Verification email resent!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to resend verification email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateRegistrationForm(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (fullName.isBlank()) return "Full name is required."
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email address."
        if (password.length < 6) return "Password must be at least 6 characters."
        if (password != confirmPassword) return "Passwords do not match."
        return null
    }
}
