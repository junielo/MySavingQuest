package com.calikot.mysavingquest.component.setup.notification.domain

import androidx.lifecycle.ViewModel
import com.calikot.mysavingquest.component.setup.notification.domain.models.BillsNotificationItem
import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.di.service.AccountBalanceService
import com.calikot.mysavingquest.di.service.NotificationSettingsService
import com.calikot.mysavingquest.di.service.RecurringBillsService
import com.calikot.mysavingquest.di.service.UserHandlerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        CoroutineScope(Dispatchers.IO).launch {
            val result = notificationSettingsService.getNotificationSettings()
            if(result.isSuccess){
                _notifSettings.value = result.getOrNull()
            }
        }
    }

    fun upsertNotificationSettings(notifSettings: NotificationSettingsItem){
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            notificationSettingsService.upsertNotificationSettings(notifSettings)
            getNotificationSettings()
            _isLoading.value = false
        }
    }

    fun updateNotificationSettingsStatus() {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
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

    fun initiateInitialNotificationSetup() {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            // Fetch recurring bills and set notifications
            val recurringBillsResult = recurringBillsService.getAllRecurringBills()
            val accountBalancesResult = accountBalanceService.getAllAccountBalances()
            if (recurringBillsResult.isSuccess && accountBalancesResult.isSuccess) {
                val recurringBills = recurringBillsResult.getOrNull() ?: emptyList()
                val accountBalances = accountBalancesResult.getOrNull() ?: emptyList()
                if (recurringBills.isNotEmpty() && accountBalances.isNotEmpty()) {
                    // Schedule notification for each bill
                    // NotificationScheduler.scheduleRecurringBillNotification(bill)

                    val billsNotificationItems = recurringBills.map { bill ->
                        BillsNotificationItem(
                            billId = bill.id!!,
                            isRecorded = false,
                            billIsAuto = bill.isAuto,
                            amount = bill.amount,
                            billName = bill.name,
                            billDate = bill.date
                        )
                    }

                    val accBalanceNotificationItems = accountBalances.map { acc ->
                        com.calikot.mysavingquest.component.setup.notification.domain.models.AccountNotificationItem(
                            accountId = acc.id!!,
                            accountName = acc.accName,
                            accountType = acc.accType
                        )
                    }

                    val bulkCreateBill =  notificationSettingsService.bulkCreateBillNotifications(billsNotificationItems)
                    val bulkCreateAccBalance = notificationSettingsService.bulkCreateAccBalanceNotification(accBalanceNotificationItems)

                    if(bulkCreateBill.isSuccess && bulkCreateAccBalance.isSuccess){
                        // TODO create a scheduled push notification for each item in the list
                    } else {
                        // Handle failure in creating notification settings
                    }
                }
            }

            _isLoading.value = false
        }
    }

}