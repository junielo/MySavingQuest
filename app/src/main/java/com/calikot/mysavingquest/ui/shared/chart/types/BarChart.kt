package com.calikot.mysavingquest.ui.shared.chart.types

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.calikot.mysavingquest.ui.shared.chart.data.BarChartData
import com.calikot.mysavingquest.ui.shared.chart.properties.BarChartProps
import com.calikot.mysavingquest.ui.shared.chart.properties.ParentGraphProps

fun DrawScope.barChartView(
    data: List<BarChartData> = emptyList(),
    mainChartProps: ParentGraphProps = ParentGraphProps(),
    barChartProps: BarChartProps = BarChartProps()
) {
    val values = data.map { it.value.toFloat() }
    if (values.isEmpty()) return

    val minVal = values.minOrNull() ?: 0f
    val maxVal = values.maxOrNull() ?: 1f
    val count = values.size

    // Compute drawable chart area using paddings from ParentGraphProps
    val leftX = mainChartProps.graphLeftPadding
    val rightX = size.width - mainChartProps.graphRightPadding
    val bottomY = size.height - mainChartProps.graphBottomPadding
    val chartWidth = (rightX - leftX).coerceAtLeast(0f)
    val chartHeight = (bottomY - 0f).coerceAtLeast(0f) // top assumed 0f here

    // compute bar sizes based on available chartWidth
    val barWidth = if (count > 0) chartWidth / (count * 1.5f) else 0f
    val gap = barWidth * 0.5f
    val totalWidth = count * barWidth + (count - 1) * gap
    val startX = leftX + (chartWidth - totalWidth) / 2f

    for ((i, v) in values.withIndex()) {
        val normalized = if (maxVal == minVal) 0.5f else (v - minVal) / (maxVal - minVal)
        val barHeight = normalized * chartHeight
        val left = startX + i * (barWidth + gap)
        val top = bottomY - barHeight
        drawRect(
            color = barChartProps.barColor,
            topLeft = Offset(left, top),
            size = Size(barWidth, barHeight)
        )
    }
}