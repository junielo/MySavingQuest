package com.calikot.mysavingquest.util

import com.calikot.mysavingquest.conn.Connections.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object SupabaseHandler {
    // Event emitter for session status changes
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents

    fun startSessionListener() {
        CoroutineScope(Dispatchers.IO).launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        println("Received new authenticated session.")
                        when (status.source) {
                            SessionSource.External -> emitEvent(SessionEvent.External)
                            is SessionSource.Refresh -> emitEvent(SessionEvent.Refresh)
                            is SessionSource.SignIn -> emitEvent(SessionEvent.SignIn)
                            is SessionSource.SignUp -> emitEvent(SessionEvent.SignUp)
                            SessionSource.Storage -> emitEvent(SessionEvent.Storage)
                            SessionSource.Unknown -> emitEvent(SessionEvent.Unknown)
                            is SessionSource.UserChanged -> emitEvent(SessionEvent.UserChanged)
                            is SessionSource.UserIdentitiesChanged -> emitEvent(SessionEvent.UserIdentitiesChanged)
                            SessionSource.AnonymousSignIn -> TODO() // No anonymous sign-in in this app
                        }
                    }
                    SessionStatus.Initializing -> {
                        println("Initializing")
                        emitEvent(SessionEvent.Initializing)
                    }
                    is SessionStatus.RefreshFailure -> {
                        println("Session expired and could not be refreshed")
                        emitEvent(SessionEvent.RefreshFailure)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (status.isSignOut) {
                            println("User signed out")
                            emitEvent(SessionEvent.SignedOut)
                        } else {
                            println("User not signed in")
                            emitEvent(SessionEvent.NotSignedIn)
                        }
                    }
                }
            }
        }
    }

    private suspend fun emitEvent(event: SessionEvent) {
        _sessionEvents.emit(event)
    }

    // Session event types for listeners
    sealed class SessionEvent {
        object SignIn : SessionEvent()
        object SignUp : SessionEvent()
        object Refresh : SessionEvent()
        object External : SessionEvent()
        object Storage : SessionEvent()
        object Unknown : SessionEvent()
        object UserChanged : SessionEvent()
        object UserIdentitiesChanged : SessionEvent()
        object Initializing : SessionEvent()
        object RefreshFailure : SessionEvent()
        object SignedOut : SessionEvent()
        object NotSignedIn : SessionEvent()
    }
}
