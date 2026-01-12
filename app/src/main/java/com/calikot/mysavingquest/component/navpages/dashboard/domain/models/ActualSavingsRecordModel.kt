package com.calikot.mysavingquest.component.navpages.dashboard.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActualSavingsRecordModel(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("debit_amount")
    val debitAmount: Int,

    @SerialName("credit_amount")
    val creditAmount: Int,

    @SerialName("account_amount")
    val accountAmount: Int,

    @SerialName("net_amount")
    val netAmount: Int,

    @SerialName("created_at")
    val createdAt: String
)

const val ACTUAL_SAVINGS_RECORD = "actual_savings_record"