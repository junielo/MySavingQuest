package com.calikot.mysavingquest.ui.shared.chart.types

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.calikot.mysavingquest.ui.shared.chart.data.BarChartData

@Composable
fun BarChartView(
    modifier: Modifier = Modifier,
    data: List<BarChartData> = emptyList(),
    backgroundColor: Color = Color(0xFF00C853),
    rectColor: Color = Color.Red
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // background
            drawRect(color = backgroundColor, size = size)

            if (data.isNotEmpty()) {
                // draw simple bars based on data values
                val values = data.map { it.value.toFloat() }
                val minVal = values.minOrNull() ?: 0f
                val maxVal = values.maxOrNull() ?: 1f
                val count = values.size
                val barWidth = size.width / (count * 1.5f)
                val gap = barWidth * 0.5f
                val totalWidth = count * barWidth + (count - 1) * gap
                val startX = (size.width - totalWidth) / 2f

                for ((i, v) in values.withIndex()) {
                    val normalized = if (maxVal == minVal) 0.5f else (v - minVal) / (maxVal - minVal)
                    val barHeight = normalized * size.height
                    val left = startX + i * (barWidth + gap)
                    val top = size.height - barHeight
                    drawRect(
                        color = rectColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight)
                    )
                }

                return@Canvas
            }

            // centered rectangle (bar-like) placeholder
//            val rectWidth = size.width * 0.6f
//            val rectHeight = size.height * 0.4f
//            val left = (size.width - rectWidth) / 2f
//            val top = (size.height - rectHeight) / 2f
//
//            drawRect(
//                color = rectColor,
//                topLeft = Offset(left, top),
//                size = Size(rectWidth, rectHeight)
//            )
        }
    }
}