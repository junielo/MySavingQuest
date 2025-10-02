package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.notification.domain.models.ACC_NOTIF_LIST
import com.calikot.mysavingquest.component.setup.notification.domain.models.AccountNotificationItem
import com.calikot.mysavingquest.component.setup.notification.domain.models.BILLS_NOTIFICATION_LIST
import com.calikot.mysavingquest.component.setup.notification.domain.models.BillsNotificationItem
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

    suspend fun bulkCreateBillNotifications(billsNotificationList: List<BillsNotificationItem>): Result<Boolean> {
        return supabaseWrapper.addBulkOwnData(BILLS_NOTIFICATION_LIST, billsNotificationList)
    }

    suspend fun bulkCreateAccBalanceNotification(accBalanceNotificationList: List<AccountNotificationItem>): Result<Boolean> {
        return supabaseWrapper.addBulkOwnData(ACC_NOTIF_LIST, accBalanceNotificationList)
    }

}