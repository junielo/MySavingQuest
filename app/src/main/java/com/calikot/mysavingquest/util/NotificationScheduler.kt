package com.calikot.mysavingquest.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.edit

object NotificationScheduler {
    data class ScheduledNotification(
        val notificationId: Int,
        val triggerTimeMillis: Long,
        val title: String,
        val message: String
    )

    private const val PREFS_NAME = "scheduled_notifications_prefs"
    private const val KEY_SCHEDULED_NOTIFICATIONS = "scheduled_notifications"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun saveScheduledNotification(context: Context, notification: ScheduledNotification) {
        val prefs = getPrefs(context)
        val notifications = getScheduledNotifications(context).toMutableList()
        notifications.removeAll { it.notificationId == notification.notificationId }
        notifications.add(notification)
        prefs.edit { putString(KEY_SCHEDULED_NOTIFICATIONS, serializeNotifications(notifications)) }
    }

    private fun removeScheduledNotification(context: Context, notificationId: Int) {
        val prefs = getPrefs(context)
        val notifications = getScheduledNotifications(context).toMutableList()
        notifications.removeAll { it.notificationId == notificationId }
        prefs.edit { putString(KEY_SCHEDULED_NOTIFICATIONS, serializeNotifications(notifications)) }
    }

    private fun serializeNotifications(list: List<ScheduledNotification>): String =
        list.joinToString("|||") { "${it.notificationId}::${it.triggerTimeMillis}::${it.title}::${it.message}" }

    private fun deserializeNotifications(str: String?): List<ScheduledNotification> {
        if (str.isNullOrEmpty()) return emptyList()
        return str.split("|||").mapNotNull {
            val parts = it.split("::")
            if (parts.size == 4) {
                ScheduledNotification(
                    parts[0].toIntOrNull() ?: return@mapNotNull null,
                    parts[1].toLongOrNull() ?: return@mapNotNull null,
                    parts[2],
                    parts[3]
                )
            } else null
        }
    }

    fun getScheduledNotifications(context: Context): List<ScheduledNotification> {
        val prefs = getPrefs(context)
        return deserializeNotifications(prefs.getString(KEY_SCHEDULED_NOTIFICATIONS, null))
    }

    fun updateScheduledNotification(
        context: Context,
        notificationId: Int,
        newTriggerTimeMillis: Long,
        newTitle: String,
        newMessage: String
    ): Boolean {
        val existing = getScheduledNotifications(context).find { it.notificationId == notificationId } ?: return false
        // Cancel old alarm
        val intent = Intent(context, ScheduledNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        removeScheduledNotification(context, notificationId)
        // Schedule new alarm
        scheduleNotification(context, newTriggerTimeMillis, notificationId, newTitle, newMessage)
        return true
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotification(
        context: Context,
        triggerTimeMillis: Long,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, ScheduledNotificationReceiver::class.java).apply {
            putExtra("notificationId", notificationId)
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Optionally, notify the user or fallback to inexact alarms
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            saveScheduledNotification(
                context,
                ScheduledNotification(notificationId, triggerTimeMillis, title, message)
            )
        } catch (_: SecurityException) {
            // Optionally, handle the exception (e.g., log or notify the user)
        }
    }
}
