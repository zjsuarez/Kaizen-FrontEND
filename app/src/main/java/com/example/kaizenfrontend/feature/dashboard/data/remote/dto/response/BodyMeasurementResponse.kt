package com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class BodyMeasurementResponse(
    val id: String,
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val progressPhotoUrl: String? = null,
    @SerializedName("recordedAt") val recordedAt: String? = null,
    @SerializedName("date") val date: String? = null
)
