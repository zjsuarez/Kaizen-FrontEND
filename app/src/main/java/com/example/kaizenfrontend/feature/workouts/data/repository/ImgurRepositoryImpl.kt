package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.feature.workouts.data.remote.imgur.ImgurApiService
import com.example.kaizenfrontend.feature.workouts.domain.repository.ImgurRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImgurRepositoryImpl(
    private val apiService: ImgurApiService,
    private val clientId: String
) : ImgurRepository {

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (clientId.isBlank()) {
                return@withContext Result.failure(
                    Exception("IMGUR_CLIENT_ID is missing. Add IMGUR_CLIENT_ID to gradle.properties or local.properties, then rebuild the app.")
                )
            }

            try {
                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = fileName,
                    body = requestBody
                )

                val response = apiService.uploadImage(
                    authorization = "Client-ID $clientId",
                    image = imagePart
                )

                if (!response.isSuccessful) {
                    if (response.code() == 429) {
                        return@withContext Result.failure(
                            Exception("Imgur upload rate limited (429). Please try again later.")
                        )
                    }

                    return@withContext Result.failure(
                        Exception("Imgur upload failed: ${response.code()} ${response.message()}")
                    )
                }

                val link = response.body()?.data?.link
                if (link.isNullOrBlank()) {
                    Result.failure(Exception("Imgur upload succeeded but link is empty"))
                } else {
                    Result.success(link)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
