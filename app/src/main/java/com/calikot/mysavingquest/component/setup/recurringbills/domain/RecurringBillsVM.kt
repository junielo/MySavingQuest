package com.calikot.mysavingquest.component.setup.recurringbills.domain

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.calikot.mysavingquest.di.service.RecurringBillsService
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
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

    fun addRecurringBill(item: RecurringBillItem): Result<RecurringBillItem> {
        _isLoading.value = true
        val deferred: Deferred<Result<RecurringBillItem>> = CoroutineScope(Dispatchers.IO).async {
            val response = recurringBillsService.insertRecurringBill(item)
            if (response.isSuccess) {
                getAllRecurringBills()
            }
            _isLoading.value = false
            response
        }
        return runBlocking { deferred.await() }
    }

    fun getAllRecurringBills() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = recurringBillsService.getAllRecurringBills()
            if (response.isSuccess) {
                _recurringBills.value = response.getOrNull() ?: emptyList()
            }
        }
    }

    fun removeRecurringBill(item: RecurringBillItem): Result<Boolean> {
        _isLoading.value = true
        val deferred: Deferred<Result<Boolean>> = CoroutineScope(Dispatchers.IO).async {
            val response = recurringBillsService.deleteRecurringBill(item)
            if (response.isSuccess) {
                getAllRecurringBills()
            }
            _isLoading.value = false
            response
        }
        return runBlocking { deferred.await() }
    }

    fun updateRecurringBill(item: RecurringBillItem): Result<RecurringBillItem?> {
        _isLoading.value = true
        val deferred: Deferred<Result<RecurringBillItem?>> = CoroutineScope(Dispatchers.IO).async {
            val response = recurringBillsService.updateRecurringBill(item)
            if (response.isSuccess) {
                getAllRecurringBills()
            }
            _isLoading.value = false
            response
        }
        return runBlocking { deferred.await() }
    }

    fun updateRecurringBillStatus(value: Boolean) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            userHandlerService.updateUserSetupStatus("recurring_bills", value)
            _isLoading.value = false
        }
    }
}
