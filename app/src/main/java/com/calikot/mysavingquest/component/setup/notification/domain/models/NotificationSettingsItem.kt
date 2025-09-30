package com.calikot.mysavingquest.component.setup.notification.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingsItem(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("notif_time_all")
    val notifTime: String? = null,

    @SerialName("acc_balance_interval")
    val accBalanceInterval: String? = null,
)

const val NOTIF_SETTINGS = "notif_settings"