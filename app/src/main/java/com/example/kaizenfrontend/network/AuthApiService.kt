package com.example.kaizenfrontend.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @PUT("api/users/me")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): Response<UserResponse>
}