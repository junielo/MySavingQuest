package com.calikot.mysavingquest.component.setup.notification.domain

import androidx.lifecycle.ViewModel
import com.calikot.mysavingquest.component.setup.notification.domain.models.NotificationSettingsItem
import com.calikot.mysavingquest.di.service.NotificationSettingsService
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

}