package com.example.healthcal.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Placeholder. Replace later with the CRUD feature (LazyColumn + its own ViewModel).
@Composable
fun HomeScreen(email: String?, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logged in", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(email ?: "Unknown user", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignOut) { Text("Sign out") }
    }
}

/*@Preview(showBackground = true)
@Composable
fun SimpleComposablePreview() {
    HomeScreen(
        email = "putua2270@gmail.com",
        onSignOut = {}
    )
} */