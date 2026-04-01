package com.example.kaizenfrontend.feature.auth.data.remote

import com.example.kaizenfrontend.feature.auth.data.remote.dto.LoginRequest
import com.example.kaizenfrontend.feature.auth.data.remote.dto.LoginResponse
import com.example.kaizenfrontend.feature.auth.data.remote.dto.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}
