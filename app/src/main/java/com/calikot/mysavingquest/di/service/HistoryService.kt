package com.calikot.mysavingquest.di.service

import com.calikot.mysavingquest.di.global.SupabaseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryService @Inject constructor(
    private val supabaseWrapper: SupabaseWrapper
) {

}