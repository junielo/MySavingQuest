package com.calikot.mysavingquest.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.reflect.full.memberProperties

fun longToFormattedDateString(value: Long): String {
    val date = Date(value)
    val format = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return format.format(date)
}

inline fun <reified T : Any> validateDataClass(
    instance: T,
    excludeProperties: List<String> = emptyList()
): Boolean {
    for (prop in T::class.memberProperties) {
        if (excludeProperties.contains(prop.name)) continue
        val value = prop.get(instance)
        if (value == null) return false
        when (value) {
            is String -> if (value.isEmpty()) return false
            is Number -> if (value.toDouble() <= 0) return false
            is Collection<*> -> if (value.isEmpty()) return false
        }
    }
    return true
}

fun convertDateMillisToISOString(millis: Long): String {
    val date = Date(millis)
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}

fun convertTimeMillisToISOString(millis: Long): String {
    val date = Date(millis)
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}

fun convert24HourTo12Hour(timeString: String): String {
    // Expects input in "HH:mm:ss" format
    return try {
        val sdf24 = SimpleDateFormat("HH:mm:ss", Locale.US)
        val date = sdf24.parse(timeString)
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
        sdf12.format(date!!)
    } catch (_: Exception) {
        "Select time"
    }
}

fun formatWithCommas(value: Int): String {
    return "%,d".format(value)
}

fun formatRecurringDay(dateString: String): String {
    // Expecting format: "yyyy-MM-dd'T'HH:mm:ss"
    val day = try {
        dateString.substring(8, 10).toInt()
    } catch (_: Exception) {
        return "Invalid date"
    }
    val ordinal = getOrdinal(day)
    return "Every $ordinal day of the month"
}

fun getOrdinal(day: Int): String {
    if (day in 11..13) return "${day}th"
    return when (day % 10) {
        1 -> "${day}st"
        2 -> "${day}nd"
        3 -> "${day}rd"
        else -> "${day}th"
    }
}

fun isoStringToTimestamp(isoString: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString)
        date?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}

// Helper to convert Kotlin values into kotlinx.serialization.json.JsonElement
fun toJsonElement(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is JsonElement -> value
    is Boolean -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value.toString())
    is String -> JsonPrimitive(value)
    is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to toJsonElement(v) })
    is List<*> -> JsonArray(value.map { toJsonElement(it) })
    else -> JsonPrimitive(value.toString())
}

fun formatCompact(value: Float): String {
    val d = value.toDouble()
    val absVal = abs(d)
    val sign = if (d < 0) "-" else ""

    // For small values show full integer with commas
    if (absVal < 100_000) {
        val rounded = round(absVal).toLong()
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(rounded)
        return sign + formatted
    }

    val units = listOf(
        1_000_000_000_000.0 to "T",
        1_000_000_000.0 to "B",
        1_000_000.0 to "M",
        1_000.0 to "K"
    )

    for ((div, suffix) in units) {
        if (absVal >= div) {
            val scaled = absVal / div
            val digitsBefore = floor(log10(scaled)).toInt() + 1
            val desiredSig = if (digitsBefore == 2) 3 else 4
            val decimals = max(0, desiredSig - digitsBefore)

            val factor = 10.0.pow(decimals)
            val truncated = (scaled * factor).toLong().toDouble() / factor

            val plain = BigDecimal.valueOf(truncated).stripTrailingZeros().toPlainString()
            return sign + plain + suffix
        }
    }

    // Fallback (shouldn't be reached)
    return sign + NumberFormat.getIntegerInstance(Locale.US).format(round(absVal).toLong())
}