package com.calikot.mysavingquest.ui.shared.chart.data

// Rename model to be explicit that it's chart data and implement ChartData
data class DoubleLineData(
    val key: String,
    val firstValue: Number,
    val secondValue: Number
)
