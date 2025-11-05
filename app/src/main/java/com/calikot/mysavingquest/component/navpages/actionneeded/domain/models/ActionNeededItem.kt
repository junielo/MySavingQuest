package com.calikot.mysavingquest.component.navpages.actionneeded.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ActionDisplayItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isInputBalance: Boolean = false,
    val billIsAuto: Boolean
)

@Serializable
data class ActionNeededItem(
    @SerialName("id")
    val id: Int,

    @SerialName("notif_type")
    val notifType: String,

    @SerialName("notif_name")
    val notifName: String,

    @SerialName("bill_amount")
    val billAmount: Int,

    @SerialName("notif_time")
    val notifTime: String,

    @SerialName("bill_is_auto")
    val billIsAuto: Boolean
)

@Serializable
data class AccUpdateItem(
    @SerialName("id")
    val id: Int,

    @SerialName("acc_amount")
    val accAmount: Int
)

@Serializable
data class BillsUpdateItem(
    @SerialName("id")
    val id: Int,

    @SerialName("is_recorded")
    val isRecorded: Boolean
)

@Serializable
data class BillsDeleteItem(
    @SerialName("id")
    val id: Int,

    @SerialName("is_recorded")
    val isRecorded: Boolean,

    @SerialName("bill_amount")
    val billAmount: Int
)