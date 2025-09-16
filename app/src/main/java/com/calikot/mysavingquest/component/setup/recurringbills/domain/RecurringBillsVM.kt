package com.calikot.mysavingquest.component.setup.recurringbills.domain

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.RecurringBillsService
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.di.service.UserHandlerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class RecurringBillsVM @Inject constructor(
    private val recurringBillsService: RecurringBillsService,
    private val userAuthState: UserHandlerService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _recurringBills = MutableStateFlow<List<RecurringBillItem>>(emptyList())
    val recurringBills: StateFlow<List<RecurringBillItem>> = _recurringBills

    fun showLoading() {
        _isLoading.value = true
    }

    fun dismissLoading() {
        _isLoading.value = false
    }

    init {
        getAllRecurringBills()
    }

    fun addRecurringBill(item: RecurringBillItem) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            recurringBillsService.insertRecurringBill(item)
            getAllRecurringBills()
            _isLoading.value = false
        }
    }

    fun getAllRecurringBills() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = recurringBillsService.getAllRecurringBills()
            if (result.isSuccess) {
                _recurringBills.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun removeRecurringBill(item: RecurringBillItem) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            recurringBillsService.deleteRecurringBill(item)
            getAllRecurringBills()
            _isLoading.value = false
        }
    }

    fun updateRecurringBillStatus() {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            userAuthState.updateUserSetupStatus("recurring_bills", true)
            _isLoading.value = false
        }
    }
}
