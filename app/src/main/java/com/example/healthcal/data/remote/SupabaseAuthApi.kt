package com.example.healthcal.data.remote

import com.example.healthcal.data.model.AuthResponse
import com.example.healthcal.data.model.RefreshTokenRequest
import com.example.healthcal.data.model.ResetPasswordRequest
import com.example.healthcal.data.model.SignInRequest
import com.example.healthcal.data.model.SignUpRequest
import com.example.healthcal.data.model.SupabaseUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the Supabase GoTrue (Auth) REST API.
 *
 * Base URL: https://<project-ref>.supabase.co/auth/v1/
 */
interface SupabaseAuthApi {

    /** Register a new user with email + password. */
    @POST("signup")
    suspend fun signUp(
        @Body body: SignUpRequest,
        @Header("apikey") apiKey: String,
        @Query("redirect_to") redirectTo: String = "healthcal://login-callback"
    ): Response<AuthResponse>

    /** Sign in with email + password. */
    @POST("token?grant_type=password")
    suspend fun signIn(
        @Body body: SignInRequest,
        @Header("apikey") apiKey: String
    ): Response<AuthResponse>

    /** Refresh an expired access token. */
    @POST("token?grant_type=refresh_token")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequest,
        @Header("apikey") apiKey: String
    ): Response<AuthResponse>

    /** Send a password-reset email. */
    @POST("recover")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequest,
        @Header("apikey") apiKey: String
    ): Response<Unit>

    /** Get the currently authenticated user's profile. */
    @GET("user")
    suspend fun getUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String
    ): Response<SupabaseUser>

    /** Sign out (invalidate the session server-side). */
    @POST("logout")
    suspend fun logout(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String
    ): Response<Unit>
}
