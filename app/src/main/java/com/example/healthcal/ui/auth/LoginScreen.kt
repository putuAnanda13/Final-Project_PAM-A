package com.example.healthcal.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Minimal auth screen – functional, not final design.
 *
 * Modes:
 *  - LOGIN           → email + password + "Login"
 *  - REGISTER        → email + password + "Daftar"
 *  - FORGOT_PASSWORD → email + "Kirim Link Reset"
 *
 * Google OAuth always visible at the bottom.
 */
private enum class AuthMode { LOGIN, REGISTER, FORGOT_PASSWORD }

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (authMode) {
                AuthMode.LOGIN -> "Login"
                AuthMode.REGISTER -> "Buat Akun"
                AuthMode.FORGOT_PASSWORD -> "Reset Password"
            },
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        // ── Email ──
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = if (authMode == AuthMode.FORGOT_PASSWORD)
                    ImeAction.Done else ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // ── Password (hidden in forgot-password mode) ──
        if (authMode != AuthMode.FORGOT_PASSWORD) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Primary action ──
        Button(
            onClick = {
                viewModel.clearMessages()
                when (authMode) {
                    AuthMode.LOGIN -> viewModel.signInWithEmail(email.trim(), password)
                    AuthMode.REGISTER -> viewModel.signUpWithEmail(email.trim(), password)
                    AuthMode.FORGOT_PASSWORD -> viewModel.resetPassword(email.trim())
                }
            },
            enabled = !state.isLoading && email.isNotBlank() &&
                    (authMode == AuthMode.FORGOT_PASSWORD || password.isNotBlank()),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    when (authMode) {
                        AuthMode.LOGIN -> "Login"
                        AuthMode.REGISTER -> "Daftar"
                        AuthMode.FORGOT_PASSWORD -> "Kirim Link Reset"
                    }
                )
            }
        }

        // ── Forgot password link ──
        if (authMode == AuthMode.LOGIN) {
            TextButton(onClick = {
                viewModel.clearMessages()
                authMode = AuthMode.FORGOT_PASSWORD
            }) {
                Text("Lupa password?")
            }
        }

        // ── Toggle login ↔ register ──
        TextButton(onClick = {
            viewModel.clearMessages()
            authMode = when (authMode) {
                AuthMode.LOGIN -> AuthMode.REGISTER
                AuthMode.REGISTER -> AuthMode.LOGIN
                AuthMode.FORGOT_PASSWORD -> AuthMode.LOGIN
            }
        }) {
            Text(
                when (authMode) {
                    AuthMode.LOGIN -> "Belum punya akun? Daftar"
                    AuthMode.REGISTER -> "Sudah punya akun? Login"
                    AuthMode.FORGOT_PASSWORD -> "Kembali ke Login"
                }
            )
        }

        // ── Success message ──
        val successMsg = state.successMessage
        if (successMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = successMsg,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // ── Error message ──
        val errorMsg = state.errorMessage
        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ── Google OAuth (opens browser → deep-link callback) ──
        OutlinedButton(
            onClick = {
                val url = viewModel.getGoogleSignInUrl()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login dengan Google")
        }
    }
}
