package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.user.login.domain.models.UserSetupStatus
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHandlerService @Inject constructor(
    private val userAuthState: UserAuthState
) {
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

    suspend fun insertUserSetupStatus(): Result<Any?> {
        return try {
            val result = supabase.from("user_setup_status").insert(
                mapOf(
                    "recurring_bills" to false,
                    "account_balance" to false,
                    "notif_settings" to false,
                )
            )
            Result.success(result.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserSetupStatus(): Result<UserSetupStatus?> {
        return try {
            val userId = userAuthState.getUserLoggedIn()?.user?.id
            val result = supabase.from("user_setup_status")
                .select(columns = Columns.list("recurring_bills", "account_balance", "account_balance")) {
                filter {
                    userId?.let { eq("user_id", it) }
                }
            }.decodeSingle<UserSetupStatus>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}