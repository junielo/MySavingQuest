package com.calikot.mysavingquest.di.global

import com.calikot.mysavingquest.conn.Connections.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthState @Inject constructor() {

    fun getUserLoggedIn(): UserSession? {
        return supabase.auth.currentSessionOrNull()
    }
}