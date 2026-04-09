package com.example.kaizenfrontend.feature.dashboard.domain.usecase

import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.request.RegisterMeasurementMultipartRequest
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.MeasurementUpsertResponse
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.domain.model.RegisterBodyMeasurementCommand
import javax.inject.Inject

class RegisterBodyMeasurementUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(
        command: RegisterBodyMeasurementCommand
    ): Result<MeasurementUpsertResponse> {
        val weightKg = command.weightKg
        val bodyFatPercentage = command.bodyFatPercentage

        if (weightKg != null && weightKg <= 0.0) {
            return Result.failure(IllegalArgumentException("weightKg must be > 0"))
        }

        if (bodyFatPercentage != null && bodyFatPercentage !in 0.0..100.0) {
            return Result.failure(IllegalArgumentException("bodyFatPercentage must be between 0 and 100"))
        }

        if (command.progressPhotoBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("progressPhoto is required"))
        }

        val request = RegisterMeasurementMultipartRequest(
            weightKg = weightKg,
            bodyFatPercentage = bodyFatPercentage,
            progressPhotoBytes = command.progressPhotoBytes,
            fileName = command.fileName,
            mimeType = command.mimeType
        )

        return repository.registerBodyMeasurement(request)
    }
}
