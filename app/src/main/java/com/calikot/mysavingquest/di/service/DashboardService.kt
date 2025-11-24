package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ACTUAL_SAVINGS_RECORD
import com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ActualSavingsRecordModel
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {
    suspend fun getActualSavingsRecord(): Result<ActualSavingsRecordModel> {
        return supabaseWrapper.getOwnSingleData(ACTUAL_SAVINGS_RECORD)
    }
}