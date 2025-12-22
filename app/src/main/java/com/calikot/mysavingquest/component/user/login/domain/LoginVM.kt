package com.calikot.mysavingquest.component.user.login.domain

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.user.login.domain.models.UserSetupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.UserHandlerService
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@HiltViewModel
class LoginVM @Inject constructor(
    private val userHandlerService: UserHandlerService,
    private val userAuthState: UserAuthState
) : ViewModel() {
    // Loading state moved to ViewModel
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message exposed to UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Info message for non-error confirmations (e.g., reset email sent)
    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    // Resend toggle state (recommended by ViewModel when login failure indicates unverified email)
    private val _resendToggle = MutableStateFlow(false)
    val resendToggle: StateFlow<Boolean> = _resendToggle.asStateFlow()

    // Reset-password toggle state (UI switch to request password reset email)
    private val _resetToggle = MutableStateFlow(false)
    val resetToggle: StateFlow<Boolean> = _resetToggle.asStateFlow()

    fun signIn(
        email: String,
        password: String,
    ) {
        // Keep client-side validation, but surface errors via state instead of Toast
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email address."
            return
        }
        if (password.isBlank()) {
            _errorMessage.value = "Please enter your password."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                userHandlerService.signInUser(email, password)
                // success: Supabase session listener elsewhere will handle navigation
                _isLoading.value = false
                _errorMessage.value = null
                _resendToggle.value = false
            } catch (e: Exception) {
                val msg = e.message ?: "Sign in failed."
                // Normalize and provide friendly messages for common failure modes
                val lower = msg.lowercase()
                when {
                    lower.contains("invalid") && lower.contains("password") -> {
                        _errorMessage.value = "Incorrect password. Please try again."
                    }
                    lower.contains("user") && (lower.contains("not found") || lower.contains("doesn't exist") || lower.contains("no user")) -> {
                        _errorMessage.value = "No account found with that email. Please register first."
                    }
                    lower.contains("verify") || lower.contains("verified") || lower.contains("confirmation") || lower.contains("confirm") -> {
                        _errorMessage.value = "Email not verified. Please verify your email or resend verification."
                        _resendToggle.value = true
                    }
                    lower.contains("network") || lower.contains("timeout") || lower.contains("unable to resolve") -> {
                        _errorMessage.value = "Network error. Check your connection and try again."
                    }
                    else -> {
                        _errorMessage.value = msg
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userHandlerService.resendVerificationEmail(email)
                _errorMessage.value = "Verification email resent! Check your inbox (and spam)."
                _resendToggle.value = false
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to resend verification email."
                val lower = msg.lowercase()
                if (lower.contains("too many") || lower.contains("rate")) {
                    _errorMessage.value = "Too many requests. Please wait a moment before trying again."
                } else {
                    _errorMessage.value = msg
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendResetPasswordEmail(email: String) {
        // Validate email format before calling service
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email address."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userHandlerService.sendResetPasswordEmail(email)
                // Put success into infoMessage so UI shows a confirmation dialog
                _infoMessage.value = "Password reset email sent! Check your inbox."
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to send password reset email."
                val lower = msg.lowercase()
                if (lower.contains("too many") || lower.contains("rate")) {
                    _errorMessage.value = "Too many requests. Please wait a moment before trying again."
                } else {
                    _errorMessage.value = msg
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Call service.resetPassword to update the user's password (used after clicking reset link)
    fun resetPassword(newPassword: String, deepLinkData: Uri? = null) {
        // basic client-side validation
        if (newPassword.length < 8) {
            _errorMessage.value = "Password must be at least 8 characters long."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSession = userAuthState.getUserLoggedIn()
                if (currentSession == null) {
                    // Robustly obtain fragment raw text (handles fragment, encodedFragment, or raw URI string)
                    val rawFragment = deepLinkData?.fragment
                        ?: deepLinkData?.encodedFragment
                        ?: deepLinkData?.toString()?.substringAfter('#', "")

                    val fragmentParams = rawFragment
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { "?$it".toUri() } // prefix with ? so getQueryParameter works

                    // Try both common keys
                    val accessToken = fragmentParams?.getQueryParameter("access_token")
                    val refreshToken = fragmentParams?.getQueryParameter("refresh_token")
                    val expiresIn = fragmentParams?.getQueryParameter("expires_in")
                    val tokenType = fragmentParams?.getQueryParameter("token_type")

                    println("DEBUG: deepLinkData=$deepLinkData")
                    println("DEBUG: accessToken=$accessToken, refresh_token=$refreshToken, expires_in=$expiresIn, token_type=$tokenType")

                    userHandlerService.setAuthentication(
                        accessToken = accessToken ?: "",
                        refreshToken = refreshToken ?: "",
                        expiresIn = expiresIn?.toLongOrNull() ?: 0L,
                        tokenType = tokenType ?: ""
                    )

                    // Must have a recovery token (either access_token or token_hash) and type == "recovery"
//                    if (type != "recovery" || (accessToken.isNullOrEmpty() && tokenHash.isNullOrEmpty())) {
//                        _errorMessage.value = "No valid reset token found. Please request a new reset email."
//                        _isLoading.value = false
//                        return@launch
//                    }
//
//                    // Prefer access_token (full session token) if present, otherwise token_hash flow
//                    val verifyResult = when {
//                        !accessToken.isNullOrEmpty() -> userHandlerService.verifyRecoveryToken(accessToken)
//                        !tokenHash.isNullOrEmpty() -> userHandlerService.verifyRecoveryToken(tokenHash)
//                        else -> Result.failure(Exception("No token to verify"))
//                    }
//
//                    if (verifyResult.isFailure) {
//                        _errorMessage.value = "Failed to verify token: ${verifyResult.exceptionOrNull()?.message}"
//                        _isLoading.value = false
//                        return@launch
//                    }
                }

                // Session should now exist; perform password reset via service
                val resetResult = userHandlerService.resetPassword(newPassword)
                if (resetResult.isFailure) {
                    _errorMessage.value = resetResult.exceptionOrNull()?.message ?: "Failed to reset password."
                } else {
                    _infoMessage.value = "Password updated successfully. Please sign in."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper for UI to clear errors
    fun clearError() {
        _errorMessage.value = null
    }

    // Helper to clear info messages
    fun clearInfo() {
        _infoMessage.value = null
    }

    fun setResendToggle(value: Boolean) {
        _resendToggle.value = value
    }

    fun setResetToggle(value: Boolean) {
        _resetToggle.value = value
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
