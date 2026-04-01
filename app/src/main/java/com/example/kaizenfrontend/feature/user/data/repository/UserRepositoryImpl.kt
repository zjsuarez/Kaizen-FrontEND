package com.example.kaizenfrontend.feature.user.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.network.RetrofitClient
import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.domain.model.User
import com.example.kaizenfrontend.feature.user.domain.repository.UserRepository

class UserRepositoryImpl(
    private val sessionManager: SessionManager
) : UserRepository {

    private val api = RetrofitClient.userService

    private fun bearerToken(): String = "Bearer ${sessionManager.getToken()}"

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getCurrentUser(bearerToken())
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    User(
                        id = dto.id,
                        username = dto.username,
                        email = dto.email,
                        unitSystem = dto.unitSystem?.uppercase() ?: "METRIC",
                        effortMeasurement = dto.effortMeasurement?.uppercase() ?: "RPE",
                        restTimerDefault = dto.restTimerDefault ?: 90,
                        profilePic = dto.profilePic,
                        primaryGoal = dto.primaryGoal,
                        equipmentAvailable = dto.equipmentAvailable ?: emptyList()
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(request: UserUpdateRequest): Result<User> {
        return try {
            val response = api.updateUserProfile(bearerToken(), request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    User(
                        id = dto.id,
                        username = dto.username,
                        email = dto.email,
                        unitSystem = dto.unitSystem?.uppercase() ?: "METRIC",
                        effortMeasurement = dto.effortMeasurement?.uppercase() ?: "RPE",
                        restTimerDefault = dto.restTimerDefault ?: 90,
                        profilePic = dto.profilePic,
                        primaryGoal = dto.primaryGoal,
                        equipmentAvailable = dto.equipmentAvailable ?: emptyList()
                    )
                )
            } else {
                Result.failure(Exception("Failed to update user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
