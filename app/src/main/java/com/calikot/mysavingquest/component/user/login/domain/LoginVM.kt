package com.calikot.mysavingquest.component.user.login.domain

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.di.service.resendVerificationEmail
import com.calikot.mysavingquest.di.service.signInUser
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginVM : ViewModel() {
    fun signIn(
        context: Context,
        email: String,
        password: String,
    ) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email address.", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                signInUser(email, password)
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Sign in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resendVerificationEmail(context: android.content.Context, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resendVerificationEmail(email)
//                val response = supabase.from("user_profile").select()
//                val data = response.data
//                println("user_profile: $data")
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
