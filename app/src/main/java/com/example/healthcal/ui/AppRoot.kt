package com.example.healthcal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthcal.data.local.AuthState
import com.example.healthcal.ui.auth.AuthViewModel
import com.example.healthcal.ui.auth.LoginScreen
import com.example.healthcal.ui.home.HomeScreen

@Composable
fun AppRoot(authViewModel: AuthViewModel = viewModel()) {
    val state by authViewModel.authState.collectAsStateWithLifecycle()
    val email by authViewModel.userEmail.collectAsStateWithLifecycle()

    when (state) {
        is AuthState.Authenticated -> HomeScreen(
            email = email ?: (state as AuthState.Authenticated).email,
            onSignOut = authViewModel::signOut
        )
        is AuthState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is AuthState.NotAuthenticated -> LoginScreen(authViewModel)
    }
}
