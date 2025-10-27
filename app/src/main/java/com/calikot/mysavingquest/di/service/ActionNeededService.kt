package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.component.navpages.actionneeded.domain.models.ActionNeededItem
import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionNeededService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {
    /**
     * Fetch pending notifications for the currently logged-in user using the
     * `get_pending_notifications_for_user` RPC. Returns a Result wrapping a list
     * of [ActionNeededItem].
     */
    suspend fun fetchPendingNotifications(): Result<List<ActionNeededItem>> {
        return supabaseWrapper.fetchListFunctionWithUserId<ActionNeededItem>("get_pending_notifications_for_user")
    }
}