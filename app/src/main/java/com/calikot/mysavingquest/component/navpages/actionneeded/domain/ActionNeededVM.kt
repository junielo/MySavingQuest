package com.calikot.mysavingquest.component.navpages.actionneeded.domain

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionNeededItem
import com.calikot.mysavingquest.di.service.ActionNeededService
import com.calikot.mysavingquest.util.isoStringToTimestamp
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ActionNeededVM @Inject constructor(
    private val actionNeededService: ActionNeededService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _actionNeededList = MutableStateFlow<List<ActionNeededItem>>(emptyList())
    val actionNeededList: StateFlow<List<ActionNeededItem>> = _actionNeededList

    init {
        fetchPendingNotifications { /* no-op */ }
    }

    /**
     * Non-suspending wrapper that runs fetch in viewModelScope and reports progress
     * via _isLoading and onComplete callback. Mirrors the sample pattern.
     */

    fun fetchPendingNotifications(onComplete: (Result<List<ActionNeededItem>>) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rpcResult = actionNeededService.fetchPendingNotifications()

                val finalResult = rpcResult.fold(onSuccess = { list ->
                    // Compute local start/end of today
                    val nowCal = Calendar.getInstance()
                    val startOfDayCal = Calendar.getInstance().apply {
                        timeInMillis = nowCal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDay = startOfDayCal.timeInMillis
                    val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

                    // Helper to parse notifTime string to epoch millis. Try ISO Instant first, then fallback.
                    fun parseNotifTimeMillis(s: String): Long? {
                        return try {
                            Instant.parse(s).toEpochMilli()
                        } catch (_: DateTimeParseException) {
                            val fallback = isoStringToTimestamp(s)
                            // isoStringToTimestamp returns 0 on failure; treat that as parse failure
                            if (fallback == 0L) null else fallback
                        }
                    }

                    // Keep items whose parsed notifTime is <= end of today (includes past dates).
                    // Exclude items with unparsable notifTime (parse returns null).
                    val filtered = list.filter { item ->
                        val ts = parseNotifTimeMillis(item.notifTime)
                        ts != null && ts <= endOfDay
                    }

                    Result.success(filtered)
                }, onFailure = { err ->
                    Result.failure(err)
                })

                if (finalResult.isSuccess) {
                    _actionNeededList.value = finalResult.getOrNull() ?: emptyList()
                }

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(finalResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(Result.failure(e))
                }
            }
        }
    }
}