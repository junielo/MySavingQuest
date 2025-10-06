package com.calikot.mysavingquest.component.setup.recurringbills.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.RecurringBillsService
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.di.service.UserHandlerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@HiltViewModel
class RecurringBillsVM @Inject constructor(
    private val recurringBillsService: RecurringBillsService,
    private val userHandlerService: UserHandlerService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _recurringBills = MutableStateFlow<List<RecurringBillItem>>(emptyList())
    val recurringBills: StateFlow<List<RecurringBillItem>> = _recurringBills

    init {
        getAllRecurringBills()
    }

    fun addRecurringBill(item: RecurringBillItem, onComplete: (Result<RecurringBillItem>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = recurringBillsService.insertRecurringBill(item)
            if (response.isSuccess) getAllRecurringBills()
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(response)
            }
        }
    }

    fun getAllRecurringBills() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllRecurringBillsSync()
        }
    }

    private suspend fun getAllRecurringBillsSync() {
        val response = recurringBillsService.getAllRecurringBills()
        if (response.isSuccess) {
            _recurringBills.value = response.getOrNull() ?: emptyList()
        }
    }

    fun removeRecurringBill(item: RecurringBillItem, onComplete: (Result<Boolean>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = recurringBillsService.deleteRecurringBill(item)
            if (response.isSuccess) getAllRecurringBillsSync()
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(response)
            }
        }
    }

    fun updateRecurringBill(item: RecurringBillItem, onComplete: (Result<RecurringBillItem>) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = recurringBillsService.updateRecurringBill(item)
            if (response.isSuccess) getAllRecurringBillsSync()
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                onComplete(response)
            }
        }
    }

    fun updateRecurringBillStatus(value: Boolean) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            userHandlerService.updateUserSetupStatus("recurring_bills", value)
            _isLoading.value = false
        }
    }
}
