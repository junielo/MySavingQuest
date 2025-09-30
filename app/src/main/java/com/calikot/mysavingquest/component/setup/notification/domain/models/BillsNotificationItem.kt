package com.calikot.mysavingquest.component.setup.notification.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillsNotificationItem(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("bill_id")
    val billId: Int,

    @SerialName("is_recorded")
    val isRecorded: Boolean,

    @SerialName("bill_name")
    val billName: String,

    @SerialName("bill_is_auto")
    val billIsAuto: Boolean,

    @SerialName("bill_amount")
    val amount: Int,

    @SerialName("bill_date")
    val billDate: String,
)

const val BILLS_NOTIFICATION_LIST: String = "bills_notification_list"
