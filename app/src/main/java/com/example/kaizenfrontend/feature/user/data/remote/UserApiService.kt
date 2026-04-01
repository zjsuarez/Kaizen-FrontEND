package com.example.kaizenfrontend.feature.user.data.remote

import com.example.kaizenfrontend.feature.user.data.remote.dto.UserResponse
import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @PUT("api/users/me")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): Response<UserResponse>
}
