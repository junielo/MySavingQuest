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
     * Fetch actual savings records for the current month. The month/year string passed to the service
     * is in MM-yyyy format (e.g., "01-2026").
     * Builds a list with one entry per day of the month using `SingleLineData` where
     * key = "Jan 1", "Jan 2"... and value = sum of netAmount for that day.
     * Logs the monthYear and the resulting list for debugging.
     */
    fun getActualSavingsCurrentMonth() {
        // compute current month-year in MM-yyyy
        val sdf = SimpleDateFormat("MM-yyyy", Locale.US)
        val monthYear = sdf.format(Calendar.getInstance().time)

        Log.d("DashboardVM", "Fetching actual savings for monthYear=$monthYear")

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = dashboardService.getActualSavingsCurrentMonth(monthYear)

            // Prepare calendar info for current month
            val cal = Calendar.getInstance()
            val parsedSdf = SimpleDateFormat("MM-yyyy", Locale.US)
            parsedSdf.isLenient = false
            val parsedDate = try {
                parsedSdf.parse(monthYear)
            } catch (e: Exception) {
                Log.d("DashboardVM", "Failed to parse monthYear: $monthYear", e)
                null
            }
            if (parsedDate != null) {
                cal.time = parsedDate
            } else {
                // fallback to current date (shouldn't normally happen because monthYear is generated locally)
                cal.time = Calendar.getInstance().time
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthShort = SimpleDateFormat("MMM", Locale.US).format(cal.time)

            // sums per day (1-based indexed stored at [day-1])
            val sums = IntArray(daysInMonth)

            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()

                // extract day from createdAt using regex YYYY-MM-DD
                val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")

                for (rec in list) {
                    val createdAt = rec.createdAt
                    val m = dateRegex.find(createdAt)
                    if (m != null) {
                        val datePart = m.value // yyyy-MM-dd
                        val parts = datePart.split("-")
                        if (parts.size == 3) {
                            val dayInt = parts[2].toIntOrNull()
                            if (dayInt != null && dayInt in 1..daysInMonth) {
                                sums[dayInt - 1] = sums[dayInt - 1] + rec.netAmount
                            }
                        }
                    } else {
                        // fallback: try to parse with common datetime patterns
                        try {
                            val alt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                            val d = alt.parse(createdAt)
                            if (d != null) {
                                val tmpCal = Calendar.getInstance()
                                tmpCal.time = d
                                val dayInt = tmpCal.get(Calendar.DAY_OF_MONTH)
                                if (dayInt in 1..daysInMonth) {
                                    sums[dayInt - 1] = sums[dayInt - 1] + rec.netAmount
                                }
                            }
                        } catch (_: Exception) {
                            // ignore unparseable createdAt
                        }
                    }
                }

                // Build SingleLineData list
                val outputChart = ArrayList<SingleLineData>(daysInMonth)
                for (i in 1..daysInMonth) {
                    val key = "$monthShort $i"
                    val valueNum: Number = sums[i - 1]
                    outputChart.add(SingleLineData(key = key, value = valueNum))
                }

                _monthlySavingsChart.value = outputChart

            } else {
                val ex = result.exceptionOrNull()
                Log.d("DashboardVM", "getActualSavingsCurrentMonth failure: ${ex?.message}")

                // still build empty list with zeros so UI has consistent size
                val outputChart = ArrayList<SingleLineData>(daysInMonth)
                for (i in 1..daysInMonth) {
                    val key = "$monthShort $i"
                    outputChart.add(SingleLineData(key = key, value = 0))
                }
                _monthlySavingsChart.value = outputChart
            }

            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }


}