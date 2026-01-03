package com.calikot.mysavingquest.ui.shared.chart

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.ui.shared.chart.data.ChartData
import com.calikot.mysavingquest.ui.shared.chart.data.SingleLineData
import com.calikot.mysavingquest.ui.shared.chart.properties.ParentGraphProps
import com.calikot.mysavingquest.ui.shared.chart.types.singleLineChartView
import com.calikot.mysavingquest.ui.shared.chart.util.getMinMaxFromList
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.nativeCanvas

// Enum to pick which chart type to render
enum class ChartType {
    BAR,
    SINGLE_LINE,
    DOUBLE_LINE
}

/**
 * Delegator composable: chooses which chart view to render. Does not perform any drawing itself.
 * Now accepts a generic `T` constrained to `ChartData` so callers can pass any of the supported data lists.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun <T : ChartData> MainChart(
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.BAR,
    data: List<T> = emptyList(),
    props: ParentGraphProps = ParentGraphProps(),
    title: String = "Sample Chart Title",
) {
    // Use a Column so we can render an optional title above the Canvas, and apply the background to the parent
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(props.backgroundColor)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        BasicText(
            modifier = Modifier.padding(
                top = props.titleTopPadding.dp,
                bottom = props.titleBottomPadding.dp
            ),
            text = title,
            style = TextStyle(color = Color(props.titleTextColor), fontSize = props.titleTextSize.sp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {

                when (chartType) {
                    ChartType.BAR -> {
//                val bars = data.filterIsInstance<BarChartData>()
//                BarChartView(modifier = Modifier.fillMaxSize(), backgroundColor = backgroundColor, rectColor = primaryColor, data = bars)
                    }

                    ChartType.SINGLE_LINE -> {
                        val singles = data.filterIsInstance<SingleLineData>()

                        drawGraphDetails(
                            props,
                            getMinMaxFromList(singles.map { it.value }) ?: Pair(0f, 0f),
                            singles.map { it.key }
                        )

                        singleLineChartView(data = singles, mainChartProps = props)
                    }

                    ChartType.DOUBLE_LINE -> {
//                val doubles = data.filterIsInstance<DoubleLineData>()
//                DoubleLineChartView(modifier = Modifier.fillMaxSize(), data = doubles, backgroundColor = backgroundColor, triangleColor = primaryColor)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawGraphDetails(
    mainChartProp: ParentGraphProps,
    maxMin: Pair<Float, Float>,
    labelList: List<String>
) {
    drawLineBorder(mainChartProp)
    drawTrendLabels(mainChartProp, maxMin)
    drawTimelineLabels(mainChartProp, labelList)
}

private fun DrawScope.drawLineBorder(mainChartProp: ParentGraphProps) {
    // compute padded positions
    val leftX = mainChartProp.graphLeftPadding
    val bottomY = size.height - mainChartProp.graphBottomPadding

    // draw vertical line at left padding (from top to bottom minus bottom padding)
    drawLine(
        color = Color(mainChartProp.borderColor),
        start = androidx.compose.ui.geometry.Offset(leftX, 0f),
        end = androidx.compose.ui.geometry.Offset(leftX, bottomY),
        strokeWidth = mainChartProp.borderWidth
    )

    // draw horizontal line at bottom padding (from left padding to right edge minus right padding)
    drawLine(
        color = Color(mainChartProp.borderColor),
        start = androidx.compose.ui.geometry.Offset(leftX, bottomY),
        end = androidx.compose.ui.geometry.Offset(size.width - mainChartProp.graphRightPadding, bottomY),
        strokeWidth = mainChartProp.borderWidth
    )
}

private fun DrawScope.drawTrendLabels(
    mainChartProp: ParentGraphProps,
    maxMin: Pair<Float, Float>
) {
    // number of labels/grid lines
    val labelsCount = mainChartProp.trendLabelCount

    // chart bounds
    val leftX = mainChartProp.graphLeftPadding
    val bottomY = size.height - mainChartProp.graphBottomPadding

    // derive actual min/max and ensure non-zero range
    var actualMax = maxMin.first
    var actualMin = maxMin.second
    if (!actualMax.isFinite() || !actualMin.isFinite()) {
        actualMax = 0f
        actualMin = 0f
    }
    if (abs(actualMax - actualMin) < 1e-6f) {
        val pad = if (actualMax == 0f) 1f else (abs(actualMax) * 0.1f)
        actualMax += pad
        actualMin -= pad
    }

    // paint for text using native android canvas
    val paint = Paint().apply {
        isAntiAlias = true
        color = (mainChartProp.chartTextColor and 0xFFFFFFFF).toInt()
        // convert chartTextSize (assumed sp-ish) to px using DrawScope.density
        textSize = mainChartProp.chartTextSize * density
        textAlign = Paint.Align.RIGHT
    }

    for (i in 0 until labelsCount) {
        val fraction = i.toFloat() / (labelsCount - 1).coerceAtLeast(1)
        // y position: 0 -> top (max), 1 -> bottom (min)
        val y = fraction * bottomY

        // draw grid line from leftX to right edge
        drawLine(
            color = Color(mainChartProp.gridColor),
            start = androidx.compose.ui.geometry.Offset(leftX, y),
            end = androidx.compose.ui.geometry.Offset(size.width - mainChartProp.graphRightPadding, y),
            strokeWidth = mainChartProp.gridWidth
        )

        // compute label value (top label is max)
        val value = actualMax - fraction * (actualMax - actualMin)

        // format label: if nearly integer show no decimals, else show two decimals
        val labelText = if (abs(value % 1f) < 0.01f) {
            String.format(Locale.getDefault(), "%,.0f", value)
        } else {
            String.format(Locale.getDefault(), "%,.2f", value)
        }

        // measure text height to vertically center label on the grid line
        val textHeight = paint.descent() - paint.ascent()
        val baseline = y + textHeight / 2 - paint.descent()

        // draw label using native canvas, positioned just to the left of the leftX padding
        drawContext.canvas.nativeCanvas.drawText(labelText, leftX - 8f, baseline, paint)
    }

}

private fun DrawScope.drawTimelineLabels(
    mainChartProp: ParentGraphProps,
    labelList: List<String>
) {
    // limit grids to timelineLabelCount (default 5)
    val gridsCount = mainChartProp.timelineLabelCount.coerceAtLeast(1)

    // chart bounds
    val leftX = mainChartProp.graphLeftPadding
    val rightX = size.width - mainChartProp.graphRightPadding
    val bottomY = size.height - mainChartProp.graphBottomPadding

    // paint for text using native android canvas
    val paint = Paint().apply {
        isAntiAlias = true
        color = (mainChartProp.chartTextColor and 0xFFFFFFFF).toInt()
        textSize = mainChartProp.chartTextSize * density
        textAlign = Paint.Align.CENTER
    }

    // draw vertical grid lines and labels
    for (i in 0 until gridsCount) {
        val fraction = if (gridsCount == 1) 0f else i.toFloat() / (gridsCount - 1)
        val x = leftX + fraction * (rightX - leftX)

        // draw vertical grid from top to bottomY
        drawLine(
            color = Color(mainChartProp.gridColor),
            start = androidx.compose.ui.geometry.Offset(x, 0f),
            end = androidx.compose.ui.geometry.Offset(x, bottomY),
            strokeWidth = mainChartProp.gridWidth
        )

        // choose label if available: pick from labelList evenly spread
        if (labelList.isNotEmpty()) {
            val labelIndex = if (labelList.size == 1) 0 else {
                val idxFloat = fraction * (labelList.size - 1)
                idxFloat.roundToInt().coerceIn(0, labelList.size - 1)
            }
            val labelText = labelList.getOrNull(labelIndex) ?: ""

            // measure text height to place it below the chart area
            val textHeight = paint.descent() - paint.ascent()
            // baseline placed below bottomY with a small gap
            val baseline = bottomY + textHeight - paint.descent() + 20f

            // draw centered label at x
            drawContext.canvas.nativeCanvas.drawText(labelText, x, baseline, paint)
        }
    }

}