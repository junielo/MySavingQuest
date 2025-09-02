package com.calikot.mysavingquest.component.setup.recurringbills.domain.models

data class BillItem(
    val name: String,
    val date: String,
    val amount: Number,
    val isAuto: Boolean
)