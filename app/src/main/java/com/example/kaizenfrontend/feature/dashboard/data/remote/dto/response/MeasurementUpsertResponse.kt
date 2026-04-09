package com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class MeasurementUpsertResponse(
    val id: String,
    val weightKg: Double,
    val bodyFatPercentage: Double,
    val progressPhotoUrl: String,
    @SerializedName("recordedAt") val recordedAt: String
)
