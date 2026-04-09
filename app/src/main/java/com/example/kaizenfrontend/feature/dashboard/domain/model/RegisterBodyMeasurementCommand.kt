package com.example.kaizenfrontend.feature.dashboard.domain.model

data class RegisterBodyMeasurementCommand(
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val progressPhotoBytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg"
)
