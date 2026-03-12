package com.example.kaizenfrontend.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ResponseBody>
}