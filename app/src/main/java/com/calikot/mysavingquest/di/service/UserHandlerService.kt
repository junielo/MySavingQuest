package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.conn.Connections.supabase
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHandlerService @Inject constructor() {
    suspend fun signUpNewUser(fullName: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    suspend fun signInUser(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun resendVerificationEmail(email: String) {
        supabase.auth.resendEmail(OtpType.Email.SIGNUP, email)
    }
}