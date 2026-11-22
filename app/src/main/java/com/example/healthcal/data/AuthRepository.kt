package com.example.healthcal.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository(private val client: SupabaseClient = supabase) {

    val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus

    // Opens the browser for Google login. The result comes back through the deep link.
    suspend fun signInWithGoogle() {
        client.auth.signInWith(Google)
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    suspend fun fetchCurrentUser(): UserInfo? =
        try {
            client.auth.retrieveUserForCurrentSession(updateSession = false)
        } catch (e: Exception) {
            client.auth.currentUserOrNull()
        }
}
