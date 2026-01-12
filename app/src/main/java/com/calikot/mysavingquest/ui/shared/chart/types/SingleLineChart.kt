package com.calikot.mysavingquest.ui.shared.chart.types

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.calikot.mysavingquest.ui.shared.chart.data.SingleLineData
import com.calikot.mysavingquest.ui.shared.chart.properties.ParentGraphProps
import com.calikot.mysavingquest.ui.shared.chart.properties.SingleLineProps
import kotlin.math.max

fun DrawScope.singleLineChartView(
    data: List<SingleLineData>,
    singleLineProp: SingleLineProps = SingleLineProps(),
    mainChartProps: ParentGraphProps = ParentGraphProps(),
    maxMin: Pair<Float, Float>
) {

    // extract numeric values
    val values = data.map { it.value.toFloat() }

    var minVal = maxMin.second
    var maxVal = maxMin.first
    if (minVal == maxVal) {
        // avoid division by zero
        minVal -= 1f
        maxVal += 1f
    }

    // compute drawable graph area respecting left, right and bottom paddings from props
    val leftX = mainChartProps.graphLeftPadding
    val rightX = size.width - mainChartProps.graphRightPadding
    val bottomY = size.height - mainChartProps.graphBottomPadding
    val graphWidth = (rightX - leftX).coerceAtLeast(0f)
    val graphHeight = bottomY.coerceAtLeast(0f)

    val pointCount = values.size
    val step = if (pointCount == 1) 0f else graphWidth / (pointCount - 1).toFloat()
    fun xForIndex(i: Int): Float = if (pointCount == 1) leftX + graphWidth / 2f else leftX + i * step

    fun yForValue(v: Float): Float {
        val norm = (max(v, minVal) - minVal) / (maxVal - minVal)
        // map normalized value into [0 .. graphHeight], then invert because canvas y grows downward
        return bottomY - norm * graphHeight
    }

    // Build path
    val linePath = Path().apply {
        fillType = PathFillType.NonZero
        for ((i, v) in values.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            if (i == 0) moveTo(x, y)
            else
                if (v != 0f) lineTo(x, y)
        }
    }

    // Fill under the line if requested
    if (singleLineProp.fillColor != Color.Transparent) {
        val fillPath = Path().apply {
            fillType = PathFillType.NonZero
            for ((i, v) in values.withIndex()) {
                val x = xForIndex(i)
                val y = yForValue(v)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            val lastX = xForIndex(values.lastIndex)
            // use bottomY (respecting bottom padding) when closing the fill
            lineTo(lastX, bottomY)
            val firstX = xForIndex(0)
            lineTo(firstX, bottomY)
            close()
        }
        drawPath(
            path = fillPath,
            color = singleLineProp.fillColor
        )
    }

    // map stroke cap
    val composeCap = when (singleLineProp.strokeCap) {
        Paint.Cap.BUTT -> StrokeCap.Butt
        Paint.Cap.ROUND -> StrokeCap.Round
        Paint.Cap.SQUARE -> StrokeCap.Square
    }

    val pathEffect = if (singleLineProp.dashed) {
        PathEffect.dashPathEffect(singleLineProp.dashInterval, 0f)
    } else null

    // draw polyline
    drawPath(
        path = linePath,
        color = singleLineProp.lineColor,
        style = Stroke(
            width = singleLineProp.lineWidth,
            cap = composeCap,
            pathEffect = pathEffect
        )
    )

    // draw points
    if (singleLineProp.showPoints) {
        for ((i, v) in values.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            drawCircle(
                color = singleLineProp.pointColor,
                radius = singleLineProp.pointRadius,
                center = Offset(x, y)
            )
        }
    }
}