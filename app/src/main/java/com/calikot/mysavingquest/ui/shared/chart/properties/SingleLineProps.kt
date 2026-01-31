package com.calikot.mysavingquest.ui.shared.chart.properties

import android.graphics.Paint
import androidx.compose.ui.graphics.Color

data class SingleLineProps(
    val lineWidth: Float = 3f,
    val lineColor: Color = Color(0xFF00A86B),
    val fillColor: Color = Color.Black.copy(alpha = 0f),
    val showPoints: Boolean = true,
    val pointRadius: Float = 4f,
    val pointColor: Color = lineColor,
    val strokeCap: Paint.Cap = Paint.Cap.ROUND,
    val dashed: Boolean = false,
    val dashInterval: FloatArray = floatArrayOf(10f, 10f),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleLineProps

        if (lineWidth != other.lineWidth) return false
        if (lineColor != other.lineColor) return false
        if (fillColor != other.fillColor) return false
        if (showPoints != other.showPoints) return false
        if (pointRadius != other.pointRadius) return false
        if (pointColor != other.pointColor) return false
        if (dashed != other.dashed) return false
        if (strokeCap != other.strokeCap) return false
        if (!dashInterval.contentEquals(other.dashInterval)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lineWidth.hashCode()
        result = 31 * result + lineColor.hashCode()
        result = 31 * result + fillColor.hashCode()
        result = 31 * result + showPoints.hashCode()
        result = 31 * result + pointRadius.hashCode()
        result = 31 * result + pointColor.hashCode()
        result = 31 * result + dashed.hashCode()
        result = 31 * result + strokeCap.hashCode()
        result = 31 * result + dashInterval.contentHashCode()
        return result
    }
}
