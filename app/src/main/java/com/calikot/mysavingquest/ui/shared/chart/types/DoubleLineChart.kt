package com.calikot.mysavingquest.ui.shared.chart.types

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.calikot.mysavingquest.ui.shared.chart.data.DoubleLineData
import com.calikot.mysavingquest.ui.shared.chart.properties.DoubleLineProps
import com.calikot.mysavingquest.ui.shared.chart.properties.ParentGraphProps

fun DrawScope.doubleLineChartView(
    data: List<DoubleLineData> = emptyList(),
    doubleLineProps: DoubleLineProps = DoubleLineProps(),
    mainChartProps: ParentGraphProps = ParentGraphProps()
) {
    if (data.isEmpty()) return

    val firstValues = data.map { it.firstValue.toFloat() }
    val secondValues = data.map { it.secondValue.toFloat() }

    // compute min/max and avoid division by zero
    var minVal = (firstValues + secondValues).minOrNull() ?: 0f
    var maxVal = (firstValues + secondValues).maxOrNull() ?: 0f
    if (minVal == maxVal) {
        minVal -= 1f
        maxVal += 1f
    }

    // compute drawable graph area respecting left, right and bottom paddings from props
    val leftX = mainChartProps.graphLeftPadding
    val rightX = size.width - mainChartProps.graphRightPadding
    val bottomY = size.height - mainChartProps.graphBottomPadding
    val graphWidth = (rightX - leftX).coerceAtLeast(0f)
    val graphHeight = bottomY.coerceAtLeast(0f)

    val pointCount = data.size
    val step = if (pointCount == 1) 0f else graphWidth / (pointCount - 1).toFloat()
    fun xForIndex(i: Int): Float = if (pointCount == 1) leftX + graphWidth / 2f else leftX + i * step

    fun yForValue(v: Float): Float {
        val norm = (v - minVal) / (maxVal - minVal)
        return bottomY - norm * graphHeight
    }

    // build paths for both lines
    val p1 = Path().apply {
        fillType = PathFillType.NonZero
        for ((i, v) in firstValues.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    val p2 = Path().apply {
        fillType = PathFillType.NonZero
        for ((i, v) in secondValues.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    // prepare stroke caps and path effects per line
    val cap1 = when (doubleLineProps.firstLineProps.strokeCap) {
        Paint.Cap.BUTT -> StrokeCap.Butt
        Paint.Cap.ROUND -> StrokeCap.Round
        Paint.Cap.SQUARE -> StrokeCap.Square
    }
    val cap2 = when (doubleLineProps.secondLineProps.strokeCap) {
        Paint.Cap.BUTT -> StrokeCap.Butt
        Paint.Cap.ROUND -> StrokeCap.Round
        Paint.Cap.SQUARE -> StrokeCap.Square
    }

    val effect1 = if (doubleLineProps.firstLineProps.dashed) {
        PathEffect.dashPathEffect(doubleLineProps.firstLineProps.dashInterval, 0f)
    } else null
    val effect2 = if (doubleLineProps.secondLineProps.dashed) {
        PathEffect.dashPathEffect(doubleLineProps.secondLineProps.dashInterval, 0f)
    } else null

    // draw lines
    drawPath(
        path = p1,
        color = doubleLineProps.firstLineProps.lineColor,
        style = Stroke(
            width = doubleLineProps.firstLineProps.lineWidth,
            cap = cap1,
            pathEffect = effect1
        )
    )

    drawPath(
        path = p2,
        color = doubleLineProps.secondLineProps.lineColor,
        style = Stroke(
            width = doubleLineProps.secondLineProps.lineWidth,
            cap = cap2,
            pathEffect = effect2
        )
    )

    // draw points if requested (use each line's point props)
    if (doubleLineProps.firstLineProps.showPoints) {
        for ((i, v) in firstValues.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            drawCircle(
                color = doubleLineProps.firstLineProps.pointColor,
                radius = doubleLineProps.firstLineProps.pointRadius,
                center = Offset(x, y)
            )
        }
    }

    if (doubleLineProps.secondLineProps.showPoints) {
        for ((i, v) in secondValues.withIndex()) {
            val x = xForIndex(i)
            val y = yForValue(v)
            drawCircle(
                color = doubleLineProps.secondLineProps.pointColor,
                radius = doubleLineProps.secondLineProps.pointRadius,
                center = Offset(x, y)
            )
        }
    }
}
