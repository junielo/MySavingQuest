package com.calikot.mysavingquest.ui.shared.chart.properties

data class ParentGraphProps(
    val backgroundColor: Long = 0xFFE2FBEDL,
    val titleTextColor: Long = 0xFF000000,
    val titleTextSize: Float = 16f,
    val titleTopPadding: Float = 8f,
    val titleBottomPadding: Float = 8f,
    val chartTextColor: Long = 0xFF000000,
    val chartTextSize: Float = 14f,
    val borderWidth: Float = 2f,
    val borderColor: Long = 0xFF000000,
    val gridWidth: Float = 1f,
    val gridColor: Long = 0xFF888888,
    val graphLeftPadding: Float = 130f,
    val graphBottomPadding: Float = 80f,
    val graphRightPadding: Float = 60f,
    val trendLabelCount: Int = 5,
    val timelineLabelCount: Int = 5
)
