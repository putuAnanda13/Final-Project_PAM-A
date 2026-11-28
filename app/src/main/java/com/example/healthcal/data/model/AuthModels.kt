package com.example.healthcal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ══════════════════════════════════════════════════════════════
//  Request bodies
// ══════════════════════════════════════════════════════════════

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    @SerialName("data") val userMetadata: Map<String, String> = emptyMap()
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String
)

// ══════════════════════════════════════════════════════════════
//  Response bodies
// ══════════════════════════════════════════════════════════════

@Serializable
data class AuthResponse(
    @SerialName("access_token")  val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type")    val tokenType: String? = null,
    @SerialName("expires_in")    val expiresIn: Long? = null,
    val user: SupabaseUser? = null
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    @SerialName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerialName("created_at")         val createdAt: String? = null,
    @SerialName("updated_at")         val updatedAt: String? = null,
    @SerialName("user_metadata")      val userMetadata: JsonObject = JsonObject(emptyMap())
)

// ══════════════════════════════════════════════════════════════
//  Error body (Supabase GoTrue returns errors inconsistently)
// ══════════════════════════════════════════════════════════════

@Serializable
data class AuthError(
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    val msg: String? = null,
    val message: String? = null,
    val code: Int? = null
) {
    fun displayMessage(): String =
        errorDescription ?: message ?: msg ?: error ?: "Unknown error"
}
