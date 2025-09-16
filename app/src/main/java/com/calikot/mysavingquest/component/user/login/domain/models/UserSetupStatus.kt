package com.calikot.mysavingquest.component.user.login.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSetupStatus(
    @SerialName("recurring_bills")
    val recurringBills: Boolean = false,

    @SerialName("account_balance")
    val accountBalance: Boolean = false,

    @SerialName("notif_settings")
    val notifSettings: Boolean = false,

    val errorMessage: String? = null
)
