package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ACTUAL_SAVINGS_RECORD
import com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ActualSavingsRecordModel
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {
    suspend fun getActualSavingsRecord(): Result<ActualSavingsRecordModel> {
        return supabaseWrapper.getOwnSingleData(ACTUAL_SAVINGS_RECORD)
    }

    /**
     * Fetch actual savings records for a specific month-year.
     *
     * @param monthYear string representing month+year in MM-yyyy format (e.g., "01-2026")
     *
     * Returns a Result with the list or a failure if parsing fails or no data found.
     */
    suspend fun getActualSavingsCurrentMonth(monthYear: String): Result<List<ActualSavingsRecordModel>> {
        // Only accept MM-yyyy format explicitly (e.g., 01-2026)
        val pattern = "MM-yyyy"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.isLenient = false

        val parsedDate = try {
            sdf.parse(monthYear)
        } catch (e: ParseException) {
            return Result.failure(IllegalArgumentException("monthYear must be in MM-yyyy format (e.g., 01-2026)", e))
        }

        val cal = Calendar.getInstance()
        cal.time = parsedDate

        val sdfOut = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // first day of the requested month
        val firstCal = cal.clone() as Calendar
        firstCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDate = sdfOut.format(firstCal.time)

        // last day of the requested month
        val lastCal = cal.clone() as Calendar
        val lastDay = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        lastCal.set(Calendar.DAY_OF_MONTH, lastDay)
        val lastDate = sdfOut.format(lastCal.time)

        return supabaseWrapper.getRowsFromRange(
            ACTUAL_SAVINGS_RECORD,
            "created_at",
            firstDate,
            lastDate
        )
    }
}