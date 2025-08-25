package com.calikot.mysavingquest.models

data class BillItem(
    val name: String,
    val date: String,
    val amount: Number,
    val isAuto: Boolean
)