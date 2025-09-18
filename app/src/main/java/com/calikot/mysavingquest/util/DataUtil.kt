package com.calikot.mysavingquest.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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