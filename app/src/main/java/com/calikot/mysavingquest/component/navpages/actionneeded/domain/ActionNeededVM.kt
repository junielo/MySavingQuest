package com.calikot.mysavingquest.component.navpages.actionneeded.domain

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionNeededItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.AccUpdateItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsDeleteItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.BillsUpdateItem
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionDisplayItem
import com.calikot.mysavingquest.di.service.ActionNeededService
import com.calikot.mysavingquest.util.isoStringToTimestamp
import com.calikot.mysavingquest.util.longToFormattedDateString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ActionNeededVM @Inject constructor(
    private val actionNeededService: ActionNeededService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Expose a list of UI-friendly display items (mapped from domain models)
    private val _actionNeededList = MutableStateFlow<List<ActionDisplayItem>>(emptyList())
    val actionNeededList: StateFlow<List<ActionDisplayItem>> = _actionNeededList

    /**
     * Reactive count of actionable items derived from `_actionNeededList`.
     * Consumers should collect this StateFlow (e.g., collectAsState in Compose)
     * so the UI is recomposed when the count changes.
     */
    val actionableCount: StateFlow<Int> = _actionNeededList
        .map { list -> list.count { it.notifType != "BILL_C_NONE" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
    fun updateAccountItemAmount(id: Int, newAmount: Float) {
        _accountItems.value = _accountItems.value.map { if (it.id == id) it.copy(billAmount = newAmount) else it }
    }

    /**
     * Non-suspending wrapper that runs fetch in viewModelScope and reports progress
     * via _isLoading and onComplete callback. Mirrors the sample pattern.
     *
     * This function now maps domain models to ActionDisplayItem (subtitle formatting,
     * grouping for account items) so the UI can consume a ready-to-render list.
     */
    fun fetchPendingNotifications(onComplete: (Result<List<ActionNeededItem>>) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rpcResult = actionNeededService.fetchPendingNotifications()

                val finalResult = rpcResult.fold(onSuccess = { list ->
                    // Compute start/end of current month in device default zone (inclusive)
                    val zone = ZoneId.systemDefault()
                    val todayDate = java.time.LocalDate.now(zone)

                    // Parse notifTime string to Instant. Fallback handles epoch seconds or millis.
                    fun parseNotifInstant(s: String): Instant? {
                        try {
                            return Instant.parse(s)
                        } catch (_: DateTimeParseException) {
                            // fallthrough to fallback
                        }

                        val fallback = isoStringToTimestamp(s)
                        if (fallback == 0L) return null

                        // Heuristic: if value looks like millis (> 1e12) treat as millis, otherwise as seconds
                        val millis = if (fallback > 1_000_000_000_000L) fallback else fallback * 1000L
                        return Instant.ofEpochMilli(millis)
                    }

                    // Parse each item once and drop those with unparsable times
                    val parsedItems = list.mapNotNull { item ->
                        val inst = parseNotifInstant(item.notifTime)
                        if (inst == null) null else Triple(item, inst, inst.atZone(zone).toLocalDate())
                    }

                    // Partition account items and others
                    val (accountTriples, otherTriples) = parsedItems.partition { (item, _, _) ->
                        item.notifType.equals("ACCOUNT", ignoreCase = true)
                    }

                    // Save account items (original items)
                    _accountItems.value = accountTriples.map { it.first }

                    // Categorize non-account items: if notif date is <= today (not after), mark A_REQ/B_AUTO; else C_NONE
                    val categorizedOtherItems = otherTriples.map { (item, _, localDate) ->
                        val newType = if (!localDate.isAfter(todayDate)) {
                            if (item.billIsAuto) "BILL_B_AUTO" else "BILL_A_REQ"
                        } else {
                            "BILL_C_NONE"
                        }
                        item.copy(notifType = newType)
                    }

                    // TODO: Do not include if the date input is for the future
                    val resultList = if (accountTriples.isNotEmpty()) {
                        // compute total as Float to match model type
                        val totalBill = accountTriples.fold(0f) { acc, triple -> acc + triple.first.billAmount }
                        val representativeNotifTime = accountTriples.minByOrNull { it.second.toEpochMilli() }?.first?.notifTime ?: ""

                        val grouped = ActionNeededItem(
                            id = -1,
                            notifType = "ACCOUNT_GROUP",
                            notifName = "Account Balances",
                            billAmount = totalBill,
                            notifTime = representativeNotifTime,
                            billIsAuto = false
                        )

                        listOf(grouped) + categorizedOtherItems
                    } else categorizedOtherItems

                    Result.success(resultList)
                }, onFailure = { err -> Result.failure(err) })

                if (finalResult.isSuccess) {
                    // Map domain ActionNeededItem -> ActionDisplayItem (subtitle formatting)
                    _actionNeededList.value = (finalResult.getOrNull() ?: emptyList()).sortedBy { it.notifType }.map { d ->
                        val subtitle = run {
                            val ts = try { isoStringToTimestamp(d.notifTime) } catch (_: Exception) { 0L }
                            if (ts > 0L) {
                                // Format: "October 22, 2025 - 05:00 PM"
                                val datePart = longToFormattedDateString(ts)
                                val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                val timePart = try { timeFormatter.format(Date(ts)) } catch (_: Exception) { "" }
                                val amountPart = if (d.billAmount > 0) " - ₱${d.billAmount}" else ""
                                if (timePart.isNotBlank()) "$datePart - $timePart$amountPart" else "$datePart$amountPart"
                            } else {
                                // Fallback: show raw notifTime or amount if available
                                val amountPart = if (d.billAmount > 0) " - ₱${d.billAmount}" else ""
                                (d.notifTime.ifBlank { "" } + amountPart).trim()
                            }
                        }

                        ActionDisplayItem(
                            id = d.id,
                            title = d.notifName,
                            subtitle = subtitle,
                            isInputBalance = (d.notifType == "ACCOUNT_GROUP"),
                            billIsAuto = d.billIsAuto,
                            notifType = d.notifType
                        )
                    }
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
                        true
                    } catch (_: Exception) {
                        false
                    }
                    results[item.id] = success
                }
                fetchPendingNotifications()
            } finally {
                if (results.values.all { it }) {
                    actionNeededService.updateAutoBillsAndComputeSavings()
                }
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