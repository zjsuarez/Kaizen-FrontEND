package com.example.kaizenfrontend.feature.dashboard.data.remote.dto.request

data class RegisterMeasurementMultipartRequest(
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val progressPhotoBytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg"
)
