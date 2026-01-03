package com.calikot.mysavingquest.ui.shared.chart.types

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.calikot.mysavingquest.ui.shared.chart.data.DoubleLineData

@Composable
fun DoubleLineChartView(
    modifier: Modifier = Modifier,
    data: List<DoubleLineData> = emptyList(),
    backgroundColor: Color = Color(0xFF00C853),
    triangleColor: Color = Color.Red
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // background
            drawRect(color = backgroundColor, size = size)

            // If data provided, draw a simple visualization: two polylines
            if (data.isNotEmpty()) {
                val firstValues = data.map { it.firstValue.toFloat() }
                val secondValues = data.map { it.secondValue.toFloat() }

                // simple scaling
                val minVal = (firstValues + secondValues).minOrNull() ?: 0f
                val maxVal = (firstValues + secondValues).maxOrNull() ?: 1f
                val count = data.size
                val step = if (count == 1) 0f else size.width / (count - 1)
                fun xFor(i: Int) = if (count == 1) size.width / 2f else i * step
                fun yFor(v: Float) = size.height - (v - minVal) / (maxVal - minVal) * size.height

                val p1 = Path().apply {
                    moveTo(xFor(0), yFor(firstValues[0]))
                    for (i in 1 until firstValues.size) lineTo(xFor(i), yFor(firstValues[i]))
                }
                val p2 = Path().apply {
                    moveTo(xFor(0), yFor(secondValues[0]))
                    for (i in 1 until secondValues.size) lineTo(xFor(i), yFor(secondValues[i]))
                }

                drawPath(path = p1, color = triangleColor)
                drawPath(path = p2, color = triangleColor.copy(alpha = 0.6f))
                return@Canvas
            }

            // centered triangle (placeholder when no data)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val halfWidth = size.width * 0.2f
            val halfHeight = size.height * 0.2f

            val path = Path().apply {
                moveTo(centerX, centerY - halfHeight) // top
                lineTo(centerX - halfWidth, centerY + halfHeight) // bottom left
                lineTo(centerX + halfWidth, centerY + halfHeight) // bottom right
                close()
            }

            drawPath(path = path, color = triangleColor)
        }
    }
}
