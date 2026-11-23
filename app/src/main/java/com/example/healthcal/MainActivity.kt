package com.example.healthcal

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            return
        }

        val params = fragment.split("&").associate { part ->
            val pieces = part.split("=", limit = 2)
            pieces[0] to (pieces.getOrNull(1) ?: "")
        }

        // Check for OAuth error
        val error = params["error"]
        if (error != null) {
            Log.e("MainActivity", "OAuth error: $error – ${params["error_description"]}")
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
        }
    }
}
