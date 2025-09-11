package com.calikot.mysavingquest.component.user.login.domain.models

data class UserSetupStatus(
    val recurring_bills: Boolean = false,
    val account_balance: Boolean = false,
    val notif_settings: Boolean = false,
    val errorMessage: String? = null
)
