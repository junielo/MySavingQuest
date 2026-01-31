package com.calikot.mysavingquest.component.navpages.history.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccInputBalanceModel(
    @SerialName("id")
    val id: Int,

    @SerialName("acc_name")
    val accName: String,

    @SerialName("acc_type")
    val accType: String,

    @SerialName("acc_amount")
    val accAmount: Int,

    @SerialName("acc_input_date")
    val accInputDate: String,

    @SerialName("created_at")
    val createAt: String,

    @SerialName("updated_at")
    val updateAt: String
)

const val ACC_NOTIFICATION_LIST = "acc_notification_list"