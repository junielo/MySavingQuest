package com.calikot.mysavingquest.component.setup.notification.domain

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.setup.notification.domain.models.BillsNotificationItem
import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.di.service.AccountBalanceService
import com.calikot.mysavingquest.di.service.NotificationSettingsService
import com.calikot.mysavingquest.di.service.RecurringBillsService
import com.calikot.mysavingquest.di.service.UserHandlerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsVM @Inject constructor(
    private val notificationSettingsService: NotificationSettingsService,
    private val recurringBillsService: RecurringBillsService,
    private val accountBalanceService: AccountBalanceService,
    private val userHandlerService: UserHandlerService
) : ViewModel() {

    private val _notifSettings = MutableStateFlow<NotificationSettingsItem?>(null)
    val notifSettings: StateFlow<NotificationSettingsItem?> = _notifSettings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        getNotificationSettings()
    }

    fun getNotificationSettings(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = notificationSettingsService.getNotificationSettings()
            if(result.isSuccess){
                _notifSettings.value = result.getOrNull()
            }
        }
    }

    fun upsertNotificationSettings(notifSettings: NotificationSettingsItem) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            notificationSettingsService.upsertNotificationSettings(notifSettings)
            getNotificationSettingsSync() // Waits for completion
            _isLoading.value = false
        }
    }

    private suspend fun getNotificationSettingsSync() {
        val result = notificationSettingsService.getNotificationSettings()
        if(result.isSuccess){
            _notifSettings.value = result.getOrNull()
        }
    }

    fun updateNotificationSettingsStatus() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            userHandlerService.updateUserSetupStatus("notif_settings", true)
            _isLoading.value = false
        }
    }

    /**
     * ###############################################################################
     * Note: Setting up the initial notification for the recurring and account balance
     * ###############################################################################
     *
     * Process:
     * 1. Fetch the recurring and account balance data from the database.
     * 2. For each recurring transaction, schedule a notification based on its due date.
     * 3. For account balance, set up a daily notification to remind the user to check their balance.
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun initiateInitialNotificationSetup(onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            var setupSuccess = false
            // Fetch recurring bills and set notifications
            val recurringBillsResult = recurringBillsService.getAllRecurringBills()
            val accountBalancesResult = accountBalanceService.getAllAccountBalances()
            val notifSetting = notificationSettingsService.getNotificationSettings()
            if (recurringBillsResult.isSuccess && accountBalancesResult.isSuccess && notifSetting.isSuccess) {
                val recurringBills = recurringBillsResult.getOrNull() ?: emptyList()
                val accountBalances = accountBalancesResult.getOrNull() ?: emptyList()
                val notificationSettings = notifSetting.getOrNull()
                if (recurringBills.isNotEmpty() && accountBalances.isNotEmpty() && notificationSettings != null) {
                    val time = notificationSettings.notifTime
                    val interval = notificationSettings.accBalanceInterval
                    val billsNotificationItems = recurringBills.map { bill ->
                        // Parse bill.date (assumed format: yyyy-MM-dd)
                        val billLocalDate = try { LocalDate.parse(bill.date) } catch (_: Exception) { LocalDate.now() }
                        val billDateTime = LocalDateTime.of(billLocalDate.plusMonths(1),
                            (LocalTime.parse(time as String) ?: LocalTime.MIDNIGHT)
                        )
                        val billDateTimeStr = billDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        BillsNotificationItem(
                            billId = bill.id!!,
                            isRecorded = false,
                            billIsAuto = bill.isAuto,
                            amount = bill.amount,
                            billName = bill.name,
                            billDate = billDateTimeStr
                        )
                    }
                    val accBalanceNotificationItems = accountBalances.map { acc ->
                        val baseDate = LocalDate.now()
                        val nextDate = when (interval) {
                            "Daily" -> baseDate.plusDays(1)
                            "Weekly" -> baseDate.plusWeeks(1)
                            "Monthly" -> baseDate.plusMonths(1)
                            else -> baseDate
                        }
                        val dateTime = LocalDateTime.of(nextDate, LocalTime.parse(time as String))
                        val dateTimeStr = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        com.calikot.mysavingquest.component.setup.notification.domain.models.AccountNotificationItem(
                            accountId = acc.id!!,
                            accountName = acc.accName,
                            accountType = acc.accType,
                            amount = 0,
                            accInputDate = dateTimeStr
                        )
                    }

                    println("qwerty - Account Balance Notification Items: $accBalanceNotificationItems")
                    val bulkAccBalNotification = notificationSettingsService.bulkCreateAccBalanceNotification(accBalanceNotificationItems)
                    val bulkBillNotification = notificationSettingsService.bulkCreateBillNotifications(billsNotificationItems)

                    setupSuccess = bulkBillNotification.isSuccess && bulkAccBalNotification.isSuccess
                }
            }
            _isLoading.value = false
            onComplete(setupSuccess)
        }
    }

}