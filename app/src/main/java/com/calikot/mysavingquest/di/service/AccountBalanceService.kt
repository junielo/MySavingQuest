package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.ACCOUNT_BALANCE
import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountBalanceService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {

    suspend fun insertAccountBalance(item: AccountBalanceItem): Result<AccountBalanceItem> {
        return supabaseWrapper.addOwnData(ACCOUNT_BALANCE, item)
    }

    suspend fun getAllAccountBalances(): Result<List<AccountBalanceItem>> {
        return supabaseWrapper.getOwnListData(ACCOUNT_BALANCE)
    }

    suspend fun deleteAccountBalance(item: AccountBalanceItem): Result<Boolean> {
        return supabaseWrapper.deleteOwnData(ACCOUNT_BALANCE, item.id)
    }
    
}