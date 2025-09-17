package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem
import com.calikot.mysavingquest.conn.Connections.supabase
import com.calikot.mysavingquest.di.global.UserAuthState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountBalanceService @Inject constructor(
    private val userAuthState: UserAuthState
) {

    suspend fun insertAccountBalance(item: AccountBalanceItem) {
        supabase.from("account_balance").insert(item)
    }

    suspend fun getAllAccountBalances(): Result<List<AccountBalanceItem>?> {
        return try {
            val userId = userAuthState.getUserLoggedIn()?.user?.id
            val result = supabase.from("account_balance")
                .select(columns = Columns.ALL) {
                    filter {
                        userId?.let { eq("user_id", it) }
                    }
                }
                .decodeList<AccountBalanceItem>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccountBalance(item: AccountBalanceItem) {
        val id = item.id
        if (id != null) {
            supabase.from("account_balance").delete {
                filter { eq("id", id) }
            }
        }
    }
    
}