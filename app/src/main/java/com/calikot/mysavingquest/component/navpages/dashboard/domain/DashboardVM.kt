package com.calikot.mysavingquest.component.navpages.dashboard.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.di.service.AccountBalanceService
import com.calikot.mysavingquest.di.service.DashboardService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DashboardVM @Inject constructor(
    private val dashboardService: DashboardService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Actual savings record model state
    private val _actualSavingsRecord = MutableStateFlow<com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ActualSavingsRecordModel?>(null)
    val actualSavingsRecord: StateFlow<com.calikot.mysavingquest.component.navpages.dashboard.domain.models.ActualSavingsRecordModel?> = _actualSavingsRecord

    init {
        getActualSavingsRecord()
    }

    fun getActualSavingsRecord() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = dashboardService.getActualSavingsRecord()
            println("qwerty - actual savings record fetch result: $result")
            if (result.isSuccess) {
                _actualSavingsRecord.value = result.getOrNull()
            }
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

}