package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RECURRING_BILLS
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringBillsService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {

    suspend fun insertRecurringBill(item: RecurringBillItem): Result<RecurringBillItem> {
        return supabaseWrapper.addOwnData(RECURRING_BILLS, item)
    }

    suspend fun getAllRecurringBills(): Result<List<RecurringBillItem>> {
        return supabaseWrapper.getOwnListData(RECURRING_BILLS)
    }

    suspend fun deleteRecurringBill(item: RecurringBillItem): Result<Boolean> {
        return supabaseWrapper.deleteOwnData(RECURRING_BILLS, item.id)
    }

    suspend fun updateRecurringBill(item: RecurringBillItem): Result<RecurringBillItem> {
        return supabaseWrapper.updateOwnData(RECURRING_BILLS, item.id, item)
    }
}