package com.example.healthcal.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.healthcal.HealthCalApp

/**
 * Manages access & refresh token persistence using SharedPreferences.
 *
 * In production you should swap this for EncryptedSharedPreferences
 * (androidx.security:security-crypto) so tokens are encrypted at rest.
 */
object TokenManager {

    private const val PREF_NAME = "healthcal_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private val prefs: SharedPreferences by lazy {
        HealthCalApp.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    fun hasTokens(): Boolean = getAccessToken() != null
}
