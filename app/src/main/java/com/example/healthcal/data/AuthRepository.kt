package com.example.healthcal.data

import com.example.healthcal.BuildConfig
import com.example.healthcal.data.local.SessionManager
import com.example.healthcal.data.local.TokenManager
import com.example.healthcal.data.model.AuthError
import com.example.healthcal.data.model.AuthResponse
import com.example.healthcal.data.model.RefreshTokenRequest
import com.example.healthcal.data.model.ResetPasswordRequest
import com.example.healthcal.data.model.SignInRequest
import com.example.healthcal.data.model.SignUpRequest
import com.example.healthcal.data.model.SupabaseUser
import com.example.healthcal.data.remote.RetrofitClient
import com.example.healthcal.data.remote.SupabaseAuthApi
import kotlinx.serialization.json.Json

/** Sealed result wrapper for every Retrofit call. */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

/**
 * Single source of truth for all authentication operations.
 *
 * Every call goes through Retrofit → Supabase Auth REST API.
 * Tokens are persisted via [TokenManager]; observable state lives
 * in [SessionManager].
 */
class AuthRepository(
    private val api: SupabaseAuthApi = RetrofitClient.authApi,
    private val apiKey: String = BuildConfig.SUPABASE_ANON_KEY
) {
    private val json = Json { ignoreUnknownKeys = true }

    // ── Cold-start session restoration ──────────────────────

    /**
     * Called once when the app starts.
     * Checks stored tokens, validates them (or refreshes), and
     * updates [SessionManager] accordingly.
     */
    suspend fun initializeSession() {
        val accessToken = TokenManager.getAccessToken()
        if (accessToken == null) {
            SessionManager.setNotAuthenticated()
            return
        }

        // Try to validate the stored access token
        when (val result = getCurrentUser(accessToken)) {
            is AuthResult.Success -> {
                SessionManager.setAuthenticated(result.data.email)
            }
            is AuthResult.Error -> {
                // Token expired? Try refreshing.
                val refreshed = tryRefreshSession()
                if (!refreshed) {
                    TokenManager.clearTokens()
                    SessionManager.setNotAuthenticated()
                }
            }
        }
    }

    // ── Email / Password ────────────────────────────────────

    /**
     * Register with email + password.
     * If email confirmation is enabled on the project, the response
     * contains a user but **no access token** until the email is verified.
     */
    suspend fun signUpWithEmail(email: String, password: String, fullName: String = ""): AuthResult<AuthResponse> {
        return try {
            val metadata = if (fullName.isBlank()) emptyMap() else mapOf("full_name" to fullName.trim())
            val response = api.signUp(
                body = SignUpRequest(email, password, metadata),
                apiKey = apiKey,
                redirectTo = "healthcal://login-callback"
            )
            if (response.isSuccessful) {
                val body = response.body() ?: AuthResponse()

                // If we got tokens back (auto-confirm), persist them
                if (body.accessToken != null && body.refreshToken != null) {
                    TokenManager.saveTokens(body.accessToken, body.refreshToken)
                    SessionManager.setAuthenticated(body.user?.email)
                }

                AuthResult.Success(body)
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    /** Sign in with email + password. Persists tokens on success. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = api.signIn(SignInRequest(email, password), apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return AuthResult.Error("Empty response")

                if (body.accessToken != null && body.refreshToken != null) {
                    TokenManager.saveTokens(body.accessToken, body.refreshToken)
                    SessionManager.setAuthenticated(body.user?.email)
                }

                AuthResult.Success(body)
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    /** Request a password-reset email. */
    suspend fun resetPassword(email: String): AuthResult<Unit> {
        return try {
            val response = api.resetPassword(ResetPasswordRequest(email), apiKey)
            if (response.isSuccessful) {
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    // ── Google OAuth (browser-based) ────────────────────────

    /**
     * Returns the full OAuth authorize URL.
     * The UI opens this in a browser; Supabase redirects back to the
     * deep-link with tokens in the URL fragment.
     */
    fun getGoogleSignInUrl(): String {
        val redirect = java.net.URLEncoder.encode("healthcal://login-callback", Charsets.UTF_8.name())
        return "${BuildConfig.SUPABASE_URL.trimEnd('/')}/auth/v1/authorize" +
                "?provider=google" +
                "&redirect_to=$redirect"
    }

    /**
     * Called from mainActivity after the OAuth deep-link callback.
     * Saves tokens and fetches user info.
     */
    suspend fun handleOAuthCallback(accessToken: String, refreshToken: String) {
        TokenManager.saveTokens(accessToken, refreshToken)

        when (val result = getCurrentUser(accessToken)) {
            is AuthResult.Success -> {
                SessionManager.setAuthenticated(result.data.email)
            }
            is AuthResult.Error -> SessionManager.setAuthenticated() // authenticated but email unknown
        }
    }

    // ── Token refresh ───────────────────────────────────────

    /** Refresh the session. Returns true if the refresh succeeded. */
    suspend fun tryRefreshSession(): Boolean {
        val refreshToken = TokenManager.getRefreshToken() ?: return false
        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken), apiKey)
            if (response.isSuccessful) {
                val body = response.body() ?: return false
                if (body.accessToken != null && body.refreshToken != null) {
                    TokenManager.saveTokens(body.accessToken, body.refreshToken)
                    SessionManager.setAuthenticated(body.user?.email)
                    true
                } else false
            } else false
        } catch (_: Exception) {
            false
        }
    }

    // ── User info ───────────────────────────────────────────

    /** Fetch the current user's profile from Supabase. */
    suspend fun getCurrentUser(accessToken: String): AuthResult<SupabaseUser> {
        return try {
            val response = api.getUser(apiKey, "Bearer $accessToken")
            if (response.isSuccessful) {
                val user = response.body() ?: return AuthResult.Error("Empty user response")
                AuthResult.Success(user)
            } else {
                AuthResult.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    // ── Sign out ────────────────────────────────────────────

    /** Sign out: revoke server-side session, clear local tokens. */
    suspend fun signOut() {
        try {
            val accessToken = TokenManager.getAccessToken()
            if (accessToken != null) {
                // Best-effort server-side logout
                api.logout(apiKey, "Bearer $accessToken")
            }
        } catch (_: Exception) {
            // Ignore – we clear local state regardless
        }
        TokenManager.clearTokens()
        SessionManager.setNotAuthenticated()
    }

    // ── Helpers ─────────────────────────────────────────────

    private fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown error"
        return try {
            json.decodeFromString<AuthError>(errorBody).displayMessage()
        } catch (_: Exception) {
            errorBody
        }
    }
}
