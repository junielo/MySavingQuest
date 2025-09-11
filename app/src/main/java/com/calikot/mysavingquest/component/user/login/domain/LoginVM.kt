package com.calikot.mysavingquest.component.user.login.domain

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.user.login.domain.models.UserSetupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.UserHandlerService
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch

@HiltViewModel
class LoginVM @Inject constructor(
    private val userHandlerService: UserHandlerService,
    private val userAuthState: UserAuthState
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

    fun getUserSession(): UserSession? {
        return userAuthState.getUserLoggedIn()
    }

    suspend fun insertUserSetupStatus(): String {
        val result = userHandlerService.insertUserSetupStatus()
        return if (result.isSuccess) {
            "User setup status initialized."
        } else {
            "Failed to initialize user setup status: ${result.exceptionOrNull()?.message}"
        }
    }

    suspend fun getCurrentUserSetupStatus(): UserSetupStatus {
        val result = userHandlerService.getCurrentUserSetupStatus()
        return if (result.isSuccess) {
            result.getOrNull() ?: UserSetupStatus()
        } else {
            UserSetupStatus(errorMessage = result.exceptionOrNull()?.message)
        }
    }

}
