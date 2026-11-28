package com.example.healthcal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodLog(
    val id: Long,
    @SerialName("nama_makanan") val foodName: String,
    val kategori: String,
    @SerialName("jumlah_porsi") val portion: Double? = null,
    val kalori: Double,
    val protein: Double? = null,
    val karbohidrat: Double? = null,
    val lemak: Double? = null,
    @SerialName("gambar_url") val imageUrl: String? = null,
    @SerialName("waktu_konsumsi") val consumedAt: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class NewFoodLog(
    @SerialName("nama_makanan") val foodName: String,
    val kategori: String,
    @SerialName("jumlah_porsi") val portion: Double?,
    val kalori: Double,
    val protein: Double? = null,
    val karbohidrat: Double? = null,
    val lemak: Double? = null,
    @SerialName("gambar_url") val imageUrl: String? = null,
    @SerialName("waktu_konsumsi") val consumedAt: String
)
