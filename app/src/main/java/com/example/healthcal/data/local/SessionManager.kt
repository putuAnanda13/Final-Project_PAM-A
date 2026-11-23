package com.example.healthcal.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observable authentication state, shared across the entire app.
 *
 * [AuthState.Loading]          → checking stored tokens on cold start
 * [AuthState.Authenticated]    → user is signed in
 * [AuthState.NotAuthenticated] → no valid session
 */
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val email: String? = null) : AuthState()
    object NotAuthenticated : AuthState()
}

object SessionManager {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun setAuthenticated(email: String? = null) {
        _authState.value = AuthState.Authenticated(email)
    }

    fun setNotAuthenticated() {
        _authState.value = AuthState.NotAuthenticated
    }

    fun setLoading() {
        _authState.value = AuthState.Loading
    }
}
