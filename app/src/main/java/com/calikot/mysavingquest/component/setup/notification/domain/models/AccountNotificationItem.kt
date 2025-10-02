package com.calikot.mysavingquest.component.setup.notification.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountNotificationItem(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("acc_id")
    val accountId: Int,

    @SerialName("acc_name")
    val accountName: String,

    @SerialName("acc_type")
    val accountType: String,

    @SerialName("acc_amount")
    val amount: Int,

    @SerialName("acc_input_date")
    val accInputDate: String
)

const val ACC_NOTIF_LIST: String = "acc_notification_list"