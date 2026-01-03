package com.calikot.mysavingquest.ui.shared.chart.properties

import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

data class SingleLineProps(
    val lineWidth: Float = 3f,
    @ColorInt val lineColor: Int = Color.BLACK,
    @ColorInt val fillColor: Int = Color.TRANSPARENT,
    val showPoints: Boolean = true,
    val pointRadius: Float = 4f,
    @ColorInt val pointColor: Int = lineColor,
    val strokeCap: Paint.Cap = Paint.Cap.ROUND,
    val dashed: Boolean = false,
    val dashInterval: FloatArray = floatArrayOf(10f, 10f),
    @ColorInt val backgroundColor: Int = "#e2fbed".toColorInt()
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
        if (backgroundColor != other.backgroundColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lineWidth.hashCode()
        result = 31 * result + lineColor
        result = 31 * result + fillColor
        result = 31 * result + showPoints.hashCode()
        result = 31 * result + pointRadius.hashCode()
        result = 31 * result + pointColor
        result = 31 * result + dashed.hashCode()
        result = 31 * result + strokeCap.hashCode()
        result = 31 * result + dashInterval.contentHashCode()
        result = 31 * result + backgroundColor
        return result
    }
}
