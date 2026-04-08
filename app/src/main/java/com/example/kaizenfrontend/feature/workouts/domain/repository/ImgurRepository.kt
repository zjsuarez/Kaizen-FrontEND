package com.example.kaizenfrontend.feature.workouts.domain.repository

interface ImgurRepository {
    suspend fun uploadImage(bytes: ByteArray, fileName: String): Result<String>
}
