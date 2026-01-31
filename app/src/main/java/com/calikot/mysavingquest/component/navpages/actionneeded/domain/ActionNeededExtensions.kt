package com.calikot.mysavingquest.component.navpages.actionneeded.domain

import kotlinx.coroutines.flow.StateFlow
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionDisplayItem

/**
 * Convenience extension on StateFlow<List<ActionDisplayItem>> to count actionable items.
 * Returns the number of items whose notifType is not equal to "BILL_C_NONE".
 *
 * Usage:
 * val actionable = viewModel.actionNeededList.countActionable()
 */
fun StateFlow<List<ActionDisplayItem>>.countActionable(): Int = this.value.count { it.notifType != "BILL_C_NONE" }

