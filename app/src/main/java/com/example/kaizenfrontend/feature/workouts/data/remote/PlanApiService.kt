package com.example.kaizenfrontend.feature.workouts.data.remote

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.TrainingPlanRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.TrainingPlanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlanApiService {

    @POST("/api/plans")
    suspend fun createPlan(
        @Header("Authorization") token: String,
        @Body request: TrainingPlanRequest
    ): Response<TrainingPlanResponse>

    @GET("/api/plans")
    suspend fun getAllPlans(
        @Header("Authorization") token: String
    ): Response<List<TrainingPlanResponse>>

    @GET("/api/plans/{planId}")
    suspend fun getPlan(
        @Header("Authorization") token: String,
        @Path("planId") planId: String
    ): Response<TrainingPlanResponse>

    @PUT("/api/plans/{planId}")
    suspend fun editPlan(
        @Header("Authorization") token: String,
        @Path("planId") planId: String,
        @Body request: TrainingPlanRequest
    ): Response<TrainingPlanResponse>

    @DELETE("/api/plans/{planId}")
    suspend fun deletePlan(
        @Header("Authorization") token: String,
        @Path("planId") planId: String
    ): Response<Unit>
}
