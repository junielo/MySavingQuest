package com.calikot.mysavingquest.component.navpages.history.domain

import androidx.lifecycle.ViewModel
import com.calikot.mysavingquest.di.service.HistoryService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryVM @Inject constructor(
    private val historyService: HistoryService
) : ViewModel() {

}