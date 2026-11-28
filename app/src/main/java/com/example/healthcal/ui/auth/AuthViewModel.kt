package com.example.healthcal.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcal.data.AuthRepository
import com.example.healthcal.data.AuthResult
import com.example.healthcal.data.local.AuthState
import com.example.healthcal.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI-only state (loading spinner, messages) ───────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    /** Observable auth state used by [AppRoot] for navigation. */
    val authState: StateFlow<AuthState> = SessionManager.authState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        // 1. Restore session from stored tokens on cold start
        viewModelScope.launch {
            repository.initializeSession()
        }

        // 2. Keep userEmail in sync with auth state changes
        viewModelScope.launch {
            SessionManager.authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    _userEmail.value = state.email
                } else {
                    _userEmail.value = null
                }
            }
        }
    }

    // ── Google OAuth ────────────────────────────────────────

    /**
     * Returns the URL that the UI should open in a browser.
     * The OAuth result arrives via deep-link in [MainActivity].
     */
    fun getGoogleSignInUrl(): String = repository.getGoogleSignInUrl()

    // ── Email / Password ────────────────────────────────────

    fun signUpWithEmail(email: String, password: String, fullName: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = repository.signUpWithEmail(email, password, fullName)) {
                is AuthResult.Success -> {
                    if (result.data.accessToken == null) {
                        _uiState.update {
                            it.copy(
                                successMessage = "Registrasi berhasil! Silakan cek email Anda untuk verifikasi."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(successMessage = "Registrasi berhasil!")
                        }
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = repository.signInWithEmail(email, password)) {
                is AuthResult.Success -> { /* SessionManager updated by repository */ }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = repository.resetPassword(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            successMessage = "Link reset password telah dikirim ke email Anda."
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // ── Sign out ────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Sign out failed") }
            }
        }
    }
}
