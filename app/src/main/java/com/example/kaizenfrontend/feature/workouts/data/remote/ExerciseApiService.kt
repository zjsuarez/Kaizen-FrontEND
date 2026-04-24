package com.example.kaizenfrontend.feature.workouts.data.remote

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ExerciseApiService {

    @GET("api/exercises")
    suspend fun getExercises(
        @Header("Authorization") token: String,
        @Query("createdByUserId") createdByUserId: String? = null
    ): Response<List<ExerciseResponse>>

    @POST("api/exercises")
    suspend fun createExercise(
        @Header("Authorization") token: String,
        @Body request: ExerciseRequest
    ): Response<ExerciseResponse>
}
