package com.calikot.mysavingquest.component.setup.accountbalance.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountBalanceItem(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("acc_name")
    val accName: String,

    @SerialName("acc_type")
    val accType: String // "Debit" or "Credit"
)