package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.notification.domain.models.NOTIF_SETTINGS
import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {

    suspend fun upsertNotificationSettings(notifSettings: NotificationSettingsItem): Result<NotificationSettingsItem> {
        return supabaseWrapper.upsertOwnData(NOTIF_SETTINGS, notifSettings)
    }

    suspend fun getNotificationSettings(): Result<NotificationSettingsItem> {
        return supabaseWrapper.getOwnSingleData(NOTIF_SETTINGS)
    }

}