package com.example.healthcal.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcal.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.status.SessionStatus

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val sessionStatus = repository.sessionStatus

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()
    init {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    val directEmail = repository.getUserEmail(status.session.user)
                    if (directEmail != null) {
                        _userEmail.value = directEmail
                    } else {
                        val fetchedUser = repository.fetchCurrentUser()
                        _userEmail.value = repository.getUserEmail(fetchedUser)
                    }
                } else {
                    _userEmail.value = null
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.signInWithGoogle()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Login failed") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

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