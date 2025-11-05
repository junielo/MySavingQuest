package com.calikot.mysavingquest.component.navpages.actionneeded.domain

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionNeededItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.AccUpdateItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsDeleteItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsUpdateItem
import com.calikot.mysavingquest.di.service.ActionNeededService
import com.calikot.mysavingquest.util.isoStringToTimestamp
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ActionNeededVM @Inject constructor(
    private val actionNeededService: ActionNeededService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _actionNeededList = MutableStateFlow<List<ActionNeededItem>>(emptyList())
    val actionNeededList: StateFlow<List<ActionNeededItem>> = _actionNeededList

    // Holds the original account-type items so callers can access the full list
    private val _accountItems = MutableStateFlow<List<ActionNeededItem>>(emptyList())
    val accountItems: StateFlow<List<ActionNeededItem>> = _accountItems

    init {
        fetchPendingNotifications()
    }

    /**
     * Public helper to update the billAmount for an account item by id.
     * This is safe to call from the UI thread and updates the backing MutableStateFlow.
     */
    fun updateAccountItemAmount(id: Int, newAmount: Int) {
        _accountItems.value = _accountItems.value.map { if (it.id == id) it.copy(billAmount = newAmount) else it }
    }

    /**
     * Non-suspending wrapper that runs fetch in viewModelScope and reports progress
     * via _isLoading and onComplete callback. Mirrors the sample pattern.
     */

    fun fetchPendingNotifications(onComplete: (Result<List<ActionNeededItem>>) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rpcResult = actionNeededService.fetchPendingNotifications()

                val finalResult = rpcResult.fold(onSuccess = { list ->
                    // Compute local start/end of today
                    val nowCal = Calendar.getInstance()
                    val startOfDayCal = Calendar.getInstance().apply {
                        timeInMillis = nowCal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDay = startOfDayCal.timeInMillis
                    val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

                    // Helper to parse notifTime string to epoch millis. Try ISO Instant first, then fallback.
                    fun parseNotifTimeMillis(s: String): Long? {
                        return try {
                            Instant.parse(s).toEpochMilli()
                        } catch (_: DateTimeParseException) {
                            val fallback = isoStringToTimestamp(s)
                            // isoStringToTimestamp returns 0 on failure; treat that as parse failure
                            if (fallback == 0L) null else fallback
                        }
                    }

                    // Keep items whose parsed notifTime is <= end of today (includes past dates).
                    // Exclude items with unparsable notifTime (parse returns null).
                    val filtered = list.filter { item ->
                        val ts = parseNotifTimeMillis(item.notifTime)
                        ts != null && ts <= endOfDay
                    }

                    // Partition into account-type items and the rest. Treat common variants (ACCOUNT, account_balance)
                    val (accountItemsList, otherItems) = filtered.partition { item ->
                        val nt = item.notifType
                        nt.equals("ACCOUNT", ignoreCase = true)
                    }

                    // Save original account items for later use (so UI/actions can access them)
                    _accountItems.value = accountItemsList

                    // If there are account items, create a grouped synthetic row with notif_name = "Account Balances"
                    val resultList = if (accountItemsList.isNotEmpty()) {
                        val totalBill = accountItemsList.sumOf { it.billAmount }
                        // pick a representative notifTime (earliest) or empty string
                        val representativeNotifTime = accountItemsList.minByOrNull { it.notifTime.let { s -> parseNotifTimeMillis(s) ?: Long.MAX_VALUE } }?.notifTime ?: ""

                        val grouped = ActionNeededItem(
                            id = -1,
                            notifType = "ACCOUNT_GROUP",
                            notifName = "Account Balances",
                            billAmount = totalBill,
                            notifTime = representativeNotifTime,
                            billIsAuto = false
                        )

                        // Place the grouped account row first, then the rest (adjust ordering if you prefer)
                        listOf(grouped) + otherItems
                    } else {
                        otherItems
                    }

                    Result.success(resultList)
                }, onFailure = { err ->
                    Result.failure(err)
                })

                if (finalResult.isSuccess) {
                    _actionNeededList.value = finalResult.getOrNull() ?: emptyList()
                }

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(finalResult)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(Result.failure(e))
                }
            }
        }
    }

    /**
     * Loops through the stored account items, creates an AccUpdateItem for each, calls
     * ActionNeededService.updateAccNotification, and returns a map of id -> success.
     * Runs on IO and reports progress with _isLoading; onComplete is invoked on the main thread.
     */
    // TODO: Handle if not all updates succeed
    fun syncAccountNotifications(onComplete: (Map<Int, Boolean>) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableMapOf<Int, Boolean>()
            try {
                val items = _accountItems.value
                for (item in items) {
                    val update = AccUpdateItem(
                        id = item.id,
                        accAmount = item.billAmount
                    )
                    val success = try {
                        actionNeededService.updateAccNotification(update)
                    } catch (_: Exception) {
                        false
                    }
                    results[item.id] = success
                }
                fetchPendingNotifications()
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(results)
                }
            }
        }
    }

    /**
     * Update a single bill notification by id. Runs on IO in viewModelScope, toggles _isLoading,
     * calls the service and invokes onComplete on the main thread with the success boolean.
     * On success we remove the updated bill from local lists so UI reflects the change.
     */
    fun updateBillsNotification(item: BillsUpdateItem, onComplete: (Boolean) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            var success = false
            try {
                success = try {
                    actionNeededService.updateBillsNotification(item)
                } catch (_: Exception) {
                    false
                }

                if (success) {
                    fetchPendingNotifications()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(success)
                }
            }
        }
    }


    fun deleteBillsNotification(item: BillsDeleteItem, onComplete: (Boolean) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            var success = false
            try {
                success = try {
                    actionNeededService.deleteBillsNotification(item)
                } catch (_: Exception) {
                    false
                }

                if (success) {
                    fetchPendingNotifications()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete(success)
                }
            }
        }
    }
}