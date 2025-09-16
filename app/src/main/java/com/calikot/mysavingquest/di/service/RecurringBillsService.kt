package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringBillsService @Inject constructor(
    private val userAuthState: UserAuthState
) {

    suspend fun insertRecurringBill(item: RecurringBillItem) {
        supabase.from("recurring_bills").insert(item)
    }

    suspend fun getAllRecurringBills(): Result<List<RecurringBillItem>?> {
        return try {
            val userId = userAuthState.getUserLoggedIn()?.user?.id
            val result = supabase.from("recurring_bills")
                .select(columns = Columns.ALL) {
                    filter {
                        userId?.let { eq("user_id", it) }
                    }
                }
                .decodeList<RecurringBillItem>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecurringBill(item: RecurringBillItem) {
        val id = item.id
        if (id != null) {
            supabase.from("recurring_bills").delete {
                filter { eq("id", id) }
            }
        }
    }
}