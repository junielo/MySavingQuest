package com.calikot.mysavingquest.ui.shared.chart.properties

import androidx.compose.ui.graphics.Color

data class ParentGraphProps(
    val backgroundColor: Color = Color(0xFFE2FBEDL),
    val titleTextColor: Color = Color(0xFF000000),
    val titleTextSize: Float = 16f,
    val titleTopPadding: Float = 8f,
    val titleBottomPadding: Float = 8f,
    val chartTextColor: Color = Color(0xFF000000),
    val chartTextSize: Float = 12f,
    val borderWidth: Float = 2f,
    val borderColor: Color = Color(0xFF000000),
    val gridWidth: Float = 1f,
    val gridColor: Color = Color(0xFF888888),
    val graphLeftPadding: Float = 130f,
    val graphBottomPadding: Float = 80f,
    val graphRightPadding: Float = 60f,
    val graphVerticalDrawOffset: Float = 0.1f,
    val trendLabelCount: Int = 5,
    val timelineLabelCount: Int = 5
)
