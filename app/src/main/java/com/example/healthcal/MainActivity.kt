package com.example.healthcal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.example.healthcal.data.AuthRepository
import com.example.healthcal.ui.AppRoot
import com.example.healthcal.ui.theme.HealthCalTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle deep-link if the Activity is launched from an OAuth callback
        handleDeepLink(intent)

        setContent {
            HealthCalTheme {
                MaterialTheme {
                    Surface { AppRoot() }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * Parse the OAuth callback deep-link.
     *
     * Supabase redirects to:
     *   healthcal://login-callback#access_token=…&refresh_token=…&…
     *
     * Tokens are in the **fragment** (after #), not in query parameters.
     */
    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != "healthcal" || uri.host != "login-callback") return

        val fragment = uri.fragment
        if (fragment.isNullOrBlank()) {
            Log.w("MainActivity", "OAuth callback had no fragment")
            Toast.makeText(this, "Login Google tidak mengembalikan sesi. Silakan coba lagi.", Toast.LENGTH_LONG).show()
            return
        }

        val callback = Uri.parse("https://oauth-callback.invalid/?$fragment")
        val params = callback.queryParameterNames.associateWith { callback.getQueryParameter(it).orEmpty() }

        // Check for OAuth error
        val error = params["error"]
        if (error != null) {
            val description = params["error_description"] ?: params["error_code"] ?: error
            Log.e("MainActivity", "OAuth error: $description")
            Toast.makeText(this, "Login Google gagal: $description", Toast.LENGTH_LONG).show()
            return
        }

        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]

        if (accessToken != null && refreshToken != null) {
            lifecycleScope.launch {
                repository.handleOAuthCallback(accessToken, refreshToken)
            }
        } else {
            Log.w("MainActivity", "OAuth callback missing tokens")
            Toast.makeText(this, "Login Google gagal: token tidak ditemukan.", Toast.LENGTH_LONG).show()
        }
    }
}
