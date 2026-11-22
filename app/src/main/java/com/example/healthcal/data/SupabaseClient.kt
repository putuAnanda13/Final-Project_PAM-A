package com.example.healthcal.data

import com.example.healthcal.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) {
        scheme = "healthcal"          // must match AndroidManifest + Supabase redirect URL
        host = "login-callback"
    }
    install(Postgrest)            // used later for the CRUD feature
}
