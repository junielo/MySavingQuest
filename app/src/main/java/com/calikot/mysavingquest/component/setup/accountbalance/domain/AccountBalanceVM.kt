package com.calikot.mysavingquest.component.setup.accountbalance.domain

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.AccountBalanceService
import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem
import com.calikot.mysavingquest.di.service.UserHandlerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

@HiltViewModel
class AccountBalanceVM @Inject constructor(
    private val accountBalanceService: AccountBalanceService,
    private val userHandlerService: UserHandlerService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _accountBalances = MutableStateFlow<List<AccountBalanceItem>>(emptyList())
    val accountBalances: StateFlow<List<AccountBalanceItem>> = _accountBalances

    init {
        getAllAccountBalances()
    }

    fun addAccountBalance(item: AccountBalanceItem): Result<AccountBalanceItem> {
        _isLoading.value = true
        val deferred: Deferred<Result<AccountBalanceItem>> = CoroutineScope(Dispatchers.IO).async {
            val result = accountBalanceService.insertAccountBalance(item)
            getAllAccountBalances()
            _isLoading.value = false
            result
        }
        return runBlocking { deferred.await() }
    }

    fun getAllAccountBalances() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = accountBalanceService.getAllAccountBalances()
            if (result.isSuccess) {
                _accountBalances.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun removeAccountBalance(item: AccountBalanceItem): Result<Boolean> {
        _isLoading.value = true
        val deferred: Deferred<Result<Boolean>> = CoroutineScope(Dispatchers.IO).async {
            val result = accountBalanceService.deleteAccountBalance(item)
            if (result.isSuccess) {
                getAllAccountBalances()
            }
            _isLoading.value = false
            result
        }
        return runBlocking { deferred.await() }
    }

    fun updateAccountBalanceStatus(value: Boolean) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            userHandlerService.updateUserSetupStatus("account_balance", value)
            _isLoading.value = false
        }
    }

}