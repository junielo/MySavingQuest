package com.calikot.mysavingquest.component.setup.accountbalance.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.AccountBalanceService
import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem
import com.calikot.mysavingquest.di.service.UserHandlerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

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

    fun addAccountBalance(item: AccountBalanceItem, onComplete: (Result<AccountBalanceItem>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountBalanceService.insertAccountBalance(item)
            if (result.isSuccess) getAllAccountBalancesSync()
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(result)
            }
        }
    }

    fun getAllAccountBalances() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllAccountBalancesSync()
        }
    }

    private suspend fun getAllAccountBalancesSync() {
        val result = accountBalanceService.getAllAccountBalances()
        if (result.isSuccess) {
            _accountBalances.value = result.getOrNull() ?: emptyList()
        }
    }

    fun removeAccountBalance(item: AccountBalanceItem, onComplete: (Result<Boolean>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountBalanceService.deleteAccountBalance(item)
            if (result.isSuccess) getAllAccountBalancesSync()
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(result)
            }
        }
    }

    fun updateAccountBalance(item: AccountBalanceItem, onComplete: (Result<AccountBalanceItem>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = accountBalanceService.updateAccountBalance(item)
            if (result.isSuccess) {
                getAllAccountBalancesSync()
            }
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(result)
            }
        }
    }

    fun updateAccountBalanceStatus(value: Boolean) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            userHandlerService.updateUserSetupStatus("account_balance", value)
            _isLoading.value = false
        }
    }

}