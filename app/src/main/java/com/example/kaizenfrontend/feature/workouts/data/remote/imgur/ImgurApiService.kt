package com.example.kaizenfrontend.feature.workouts.data.remote.imgur

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ImgurUploadResponseDto(
    val data: ImgurImageDataDto?,
    val success: Boolean,
    val status: Int
)

data class ImgurImageDataDto(
    val id: String?,
    val link: String?
)

interface ImgurApiService {
    @Multipart
    @POST("3/image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Response<ImgurUploadResponseDto>
}
