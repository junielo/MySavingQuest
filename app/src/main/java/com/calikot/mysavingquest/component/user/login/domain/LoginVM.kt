package com.calikot.mysavingquest.component.user.login.domain

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
class LoginVM @Inject constructor(
    private val userHandlerService: UserHandlerService
) : ViewModel() {
    fun signIn(
        context: Context,
        email: String,
        password: String,
    ) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email address.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            try {
                userHandlerService.signInUser(email, password)
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Sign in failed.", Toast.LENGTH_SHORT).show()
            }
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
}
