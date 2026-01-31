package com.calikot.mysavingquest.component.navpages.dashboard.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.di.service.DashboardService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ActualSavingsRecordModel
import com.calikot.mysavingquest.ui.shared.chart.data.SingleLineData

@HiltViewModel
class DashboardVM @Inject constructor(
    private val dashboardService: DashboardService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Actual savings record model state
    private val _actualSavingsRecord = MutableStateFlow<ActualSavingsRecordModel?>(null)
    val actualSavingsRecord: StateFlow<ActualSavingsRecordModel?> = _actualSavingsRecord

    // Also expose SingleLineData list for charting (keys like "Jan 1")
    private val _monthlySavingsChart = MutableStateFlow<List<SingleLineData>>(emptyList())
    val monthlySavingsChart: StateFlow<List<SingleLineData>> = _monthlySavingsChart

    init {
        getActualSavingsRecord()
        getActualSavingsCurrentMonth() // fetch current month records on init
    }

    fun getActualSavingsRecord() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = dashboardService.getActualSavingsRecord()
            if (result.isSuccess) {
                _actualSavingsRecord.value = result.getOrNull()
            }
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch actual savings records for the current month and build a list with one entry per day.
     * key = "Jan 1", "Jan 2"... value = sum of netAmount for that day.
     * This implementation is shorter and keeps robust date parsing (regex first, then common formats).
     */
    fun getActualSavingsCurrentMonth() {
        // current month in MM-yyyy (service expects this format)
        val sdf = SimpleDateFormat("MM-yyyy", Locale.US)
        val monthYear = sdf.format(Calendar.getInstance().time)

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = dashboardService.getActualSavingsCurrentMonth(monthYear)

            // prepare calendar for month metadata
            val cal = Calendar.getInstance()
            try {
                val parsed = SimpleDateFormat("MM-yyyy", Locale.US).parse(monthYear)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) {
                // fallback to current date
                cal.time = Calendar.getInstance().time
            }

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthShort = SimpleDateFormat("MMM", Locale.US).format(cal.time)

            // Helper: extract day-of-month from various possible createdAt formats
            val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
            fun extractDayOfMonth(createdAt: String): Int? {
                // Try quick regex YYYY-MM-DD
                val m = dateRegex.find(createdAt)
                if (m != null) {
                    val parts = m.value.split("-")
                    val d = parts.getOrNull(2)?.toIntOrNull()
                    if (d != null) return d
                }

                // TODO: Fix this into a single date pattern
                // Try a few common timestamp formats
                val patterns = arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
                )
                for (p in patterns) {
                    try {
                        val fmt = SimpleDateFormat(p, Locale.US)
                        fmt.isLenient = false
                        val parsed = fmt.parse(createdAt)
                        if (parsed != null) {
                            val tmp = Calendar.getInstance()
                            tmp.time = parsed
                            return tmp.get(Calendar.DAY_OF_MONTH)
                        }
                    } catch (_: Exception) {
                        // try next
                    }
                }

                return null
            }

            // Sum netAmount per day (1..daysInMonth)
            val sums = IntArray(daysInMonth)
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                for (rec in list) {
                    val day = extractDayOfMonth(rec.createdAt)
                    if (day != null && day in 1..daysInMonth) {
                        sums[day - 1] = rec.netAmount
                    }
                }
            } else {
                val ex = result.exceptionOrNull()
                Log.d("DashboardVM", "getActualSavingsCurrentMonth failure: ${ex?.message}")
                // leave sums as zeros
            }

            // Build SingleLineData list (one entry per day)
            val outputChart = ArrayList<SingleLineData>(daysInMonth)
            for (i in 1..daysInMonth) {
                val key = "$monthShort $i"
                outputChart.add(SingleLineData(key = key, value = sums[i - 1]))
            }

            _monthlySavingsChart.value = outputChart

            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }


}