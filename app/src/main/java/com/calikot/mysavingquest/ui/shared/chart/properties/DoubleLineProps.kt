package com.calikot.mysavingquest.ui.shared.chart.properties

import androidx.compose.ui.graphics.Color

data class DoubleLineProps(
    val firstLineProps: SingleLineProps = SingleLineProps(),
    val secondLineProps: SingleLineProps = SingleLineProps(
        lineColor = Color(0xFF357AD0),
        pointColor = Color(0xFF357AD0)
    )
)
