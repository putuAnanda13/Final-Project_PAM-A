package com.example.healthcal.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class AuthMode { LOGIN, REGISTER, FORGOT_PASSWORD }

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    val title = when (authMode) {
        AuthMode.LOGIN -> "Selamat datang kembali"
        AuthMode.REGISTER -> "Mulai hidup lebih sehat"
        AuthMode.FORGOT_PASSWORD -> "Atur ulang password"
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFFAF9FC))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(76.dp).background(Color(0xFFF3E8FF), CircleShape), Alignment.Center) {
                Text("✦", color = Color(0xFF7C3AED), style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(Modifier.height(22.dp))
            Text("NutriTrack", color = Color(0xFF7C3AED), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color(0xFF1F1B2D), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                when (authMode) {
                    AuthMode.LOGIN -> "Pantau nutrisi dan capai tujuan sehatmu setiap hari."
                    AuthMode.REGISTER -> "Buat akun untuk memulai perjalanan sehatmu."
                    AuthMode.FORGOT_PASSWORD -> "Masukkan email akunmu untuk menerima tautan reset."
                },
                color = Color(0xFF8A8496), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(30.dp))
            if (authMode == AuthMode.REGISTER) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama lengkap") }, placeholder = { Text("Nama kamu") }, singleLine = true, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(), colors = authFieldColors())
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") }, placeholder = { Text("nama@email.com") },
                singleLine = true, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = if (authMode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next),
                colors = authFieldColors()
            )
            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Password") }, placeholder = { Text("Minimal 6 karakter") },
                    singleLine = true, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    colors = authFieldColors()
                )
                if (authMode == AuthMode.REGISTER) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Konfirmasi password") }, singleLine = true, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), colors = authFieldColors())
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C3AED)))
                        Text("Saya menyetujui Syarat Layanan dan Kebijakan Privasi", color = Color(0xFF4A4455), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (authMode == AuthMode.LOGIN) {
                TextButton(onClick = { viewModel.clearMessages(); authMode = AuthMode.FORGOT_PASSWORD }, modifier = Modifier.align(Alignment.End)) { Text("Lupa password?", color = Color(0xFF7C3AED)) }
            } else Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.clearMessages()
                    when (authMode) {
                        AuthMode.LOGIN -> viewModel.signInWithEmail(email.trim(), password)
                        AuthMode.REGISTER -> viewModel.signUpWithEmail(email.trim(), password, fullName)
                        AuthMode.FORGOT_PASSWORD -> viewModel.resetPassword(email.trim())
                    }
                },
                enabled = !state.isLoading && email.isNotBlank() && when (authMode) {
                    AuthMode.LOGIN -> password.isNotBlank()
                    AuthMode.REGISTER -> fullName.isNotBlank() && password.length >= 6 && password == confirmPassword && acceptedTerms
                    AuthMode.FORGOT_PASSWORD -> true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                else Text(when (authMode) { AuthMode.LOGIN -> "Masuk"; AuthMode.REGISTER -> "Buat akun"; AuthMode.FORGOT_PASSWORD -> "Kirim tautan reset" }, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = Color(0xFFEDE9FE)); Text("  atau  ", color = Color(0xFF8A8496), style = MaterialTheme.typography.bodySmall); HorizontalDivider(Modifier.weight(1f), color = Color(0xFFEDE9FE))
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getGoogleSignInUrl()))) },
                enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CircleShape
            ) { Text("G  Lanjutkan dengan Google", color = Color(0xFF1F1B2D)) }
            TextButton(onClick = {
                viewModel.clearMessages()
                authMode = when (authMode) { AuthMode.LOGIN -> AuthMode.REGISTER; AuthMode.REGISTER, AuthMode.FORGOT_PASSWORD -> AuthMode.LOGIN }
            }) {
                Text(when (authMode) { AuthMode.LOGIN -> "Belum punya akun? Daftar"; AuthMode.REGISTER -> "Sudah punya akun? Masuk"; AuthMode.FORGOT_PASSWORD -> "Kembali ke login" }, color = Color(0xFF7C3AED))
            }
            state.successMessage?.let { Text(it, color = Color(0xFF16834A), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium) }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF7C3AED), unfocusedBorderColor = Color(0xFFEDE9FE),
    focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF6F2FA)
)
