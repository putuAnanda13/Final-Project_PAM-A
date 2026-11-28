package com.example.healthcal.data.repository

import com.example.healthcal.BuildConfig
import com.example.healthcal.data.local.TokenManager
import com.example.healthcal.data.model.FoodLog
import com.example.healthcal.data.model.NewFoodLog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodRepository {
    private val http = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    fun getToday(): List<FoodLog> {
        val token = requireToken()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = formatTimestamp(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = formatTimestamp(calendar.time)
        val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/rest/v1/makanan".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*")
            .addQueryParameter("and", "(waktu_konsumsi.gte.$start,waktu_konsumsi.lt.$end)")
            .addQueryParameter("order", "created_at.desc")
            .build()
        http.newCall(request(url.toString(), token).get().build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            check(response.isSuccessful) { "Gagal memuat makanan (${response.code}): $body" }
            return json.decodeFromString(body)
        }
    }

    fun add(foodName: String, category: String, calories: Double, portion: String, carbs: Double?, protein: Double?, fat: Double?) {
        val token = requireToken()
        val item = NewFoodLog(
            foodName = foodName,
            kategori = category,
            portion = portion.replace(',', '.').toDoubleOrNull(),
            kalori = calories,
            protein = protein,
            karbohidrat = carbs,
            lemak = fat,
            consumedAt = formatTimestamp(Date())
        )
        val body = json.encodeToString(item).toRequestBody("application/json".toMediaType())
        val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/rest/v1/makanan"
        http.newCall(request(url, token).post(body).addHeader("Prefer", "return=minimal").build()).execute().use { response ->
            val message = response.body?.string().orEmpty()
            check(response.isSuccessful) { "Gagal menyimpan makanan (${response.code}): $message" }
        }
    }

    private fun requireToken() = TokenManager.getAccessToken() ?: error("Sesi berakhir. Silakan masuk kembali.")

    private fun formatTimestamp(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(date)

    private fun request(url: String, token: String) = Request.Builder().url(url)
        .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
}
