package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsService @Inject constructor(
    private val userAuthState: UserAuthState
) {

    suspend fun upsertNotificationSettings(notifSettings: NotificationSettingsItem) {
        supabase.from("notif_settings").upsert(notifSettings)
    }

    suspend fun getNotificationSettings(): Result<NotificationSettingsItem?> {
        return try {
            val userId = userAuthState.getUserLoggedIn()?.user?.id
            val result = supabase.from("notif_settings")
                .select(columns = Columns.ALL) {
                    filter {
                        userId?.let { eq("user_id", it) }
                    }
                }
                .decodeSingle<NotificationSettingsItem>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}