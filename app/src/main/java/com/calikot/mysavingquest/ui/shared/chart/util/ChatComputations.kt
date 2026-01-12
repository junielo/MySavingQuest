package com.calikot.mysavingquest.ui.shared.chart.util

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

fun getMaxMinFromList(values: List<Number>, verticalDrawOffset: Float): Pair<Float, Float>? {
    val finite = values.map { it.toFloat() }.filter { it.isFinite() }
    if (finite.isEmpty()) return null

    // compute max normally
    val max = finite.maxOrNull() ?: return null

    // prefer the smallest positive value for the minimum (do not use zero if positives exist)
    val positive = finite.filter { it > 0f }
    val min = if (positive.isNotEmpty()) {
        positive.minOrNull() ?: return null
    } else {
        // fall back to the overall minimum (could be zero or negative)
        finite.minOrNull() ?: return null
    }
    val diff = max - min
    return Pair(
        roundOffMax(max, diff, verticalDrawOffset),
        roundOffMin(min, diff, verticalDrawOffset)
    )
}

fun getMaxMinFromTwoLists(a: List<Number>, b: List<Number>, verticalDrawOffset: Float): Pair<Float, Float>? {
    // Delegate to single-list helper by concatenating the lists; this is fine for typical chart sizes.
    return getMaxMinFromList(a + b, verticalDrawOffset)
}

/**
 * Round the given value upward to a coarser step depending on magnitude.
 * Rules:
 * - If |value| >= 1000 -> round up to the next 100 (e.g. 24,543 -> 24,600)
 * - Else if |value| >= 100 -> round up to the next 10 (e.g. 989 -> 990)
 * - Else -> return the value unchanged (do not round tens/units)
 */
fun roundOffMax(value: Float, diff: Float, verticalDrawOffset: Float): Float {
    if (!value.isFinite()) return value
    val offsetVal: Float = value + (verticalDrawOffset * diff)
    val sign = if (offsetVal < 0f) -1f else 1f
    val a = abs(offsetVal)
    val step = when {
        a >= 1000f -> 100f
        a >= 100f -> 10f
        else -> return offsetVal
    }
    return sign * (ceil(a / step) * step)
}

/**
 * Round the given value downward to a coarser step depending on magnitude.
 * For positive values this rounds toward smaller numbers (floor).
 * For negative values this rounds toward more negative numbers (e.g., -2543 -> -2600).
 */
fun roundOffMin(value: Float, diff: Float, verticalDrawOffset: Float): Float {
    if (!value.isFinite()) return value
    val offsetVal: Float = value - (verticalDrawOffset * diff)
    val a = abs(offsetVal)
    val step = when {
        a >= 1000f -> 100f
        a >= 100f -> 10f
        else -> return offsetVal
    }

    return if (offsetVal >= 0f) {
        // positive: round down (toward smaller positive)
        (floor(a / step) * step)
    } else {
        // negative: round down (toward more negative)
        -(ceil(a / step) * step)
    }
}
