package com.calikot.mysavingquest.component.navpages.actionneeded.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ActionNeededItem2(
    val title: String,
    val subtitle: String,
    val isInputBalance: Boolean = false
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
)