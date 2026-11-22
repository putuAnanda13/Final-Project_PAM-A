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
import com.example.healthcal.ui.auth.AuthViewModel
import com.example.healthcal.ui.auth.LoginScreen
import com.example.healthcal.ui.home.HomeScreen
import io.github.jan.supabase.auth.status.SessionStatus


@Composable
fun AppRoot(authViewModel: AuthViewModel = viewModel()) {
    val status by authViewModel.sessionStatus.collectAsStateWithLifecycle()
    val email by authViewModel.userEmail.collectAsStateWithLifecycle()

    when (status) {
        is SessionStatus.Authenticated -> HomeScreen(
            email = email,
            onSignOut = authViewModel::signOut
        )
        is SessionStatus.Initializing -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> LoginScreen(authViewModel)
    }
}