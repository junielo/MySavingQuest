package com.calikot.mysavingquest.component.setup.recurringbills.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecurringBillItem(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("bill_name")
    val name: String,

    @SerialName("bill_date")
    val date: String,

    @SerialName("bill_amount")
    val amount: Int,

    @SerialName("bill_is_auto")
    val isAuto: Boolean
)

const val RECURRING_BILLS: String = "recurring_bills"