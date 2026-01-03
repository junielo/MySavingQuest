package com.calikot.mysavingquest.ui.shared.chart.util

fun getMinMaxFromList(values: List<Number>): Pair<Float, Float>? {
    val finite = values.map { it.toFloat() }.filter { it.isFinite() }
    if (finite.isEmpty()) return null
    val min = finite.minOrNull() ?: return null
    val max = finite.maxOrNull() ?: return null
    return Pair(min, max)
}

fun getMinMaxFromTwoLists(a: List<Number>, b: List<Number>): Pair<Float, Float>? {
    // Delegate to single-list helper by concatenating the lists; this is fine for typical chart sizes.
    return getMinMaxFromList(a + b)
}
