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

    private fun toBearer(tokenOrBearer: String?): String? {
        val normalized = tokenOrBearer
            ?.trim()
            ?.removePrefix("Bearer ")
            ?.removePrefix("bearer ")
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return "Bearer $normalized"
    }

    private fun bearerToken(): String? = toBearer(sessionManager.getToken())

    override suspend fun getCurrentUser(): Result<User> =
        fetchUserWithBearer(bearerToken())

    override suspend fun getCurrentUserWithToken(bearerToken: String): Result<User> =
        fetchUserWithBearer(bearerToken)

    private suspend fun fetchUserWithBearer(tokenOrBearer: String?): Result<User> {
        val bearer = toBearer(tokenOrBearer)
            ?: return Result.failure(Exception("Missing authentication token"))
        return try {
            val response = api.getCurrentUser(bearer)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                android.util.Log.d("UserRepo", "getCurrentUser DTO=$dto")
                Result.success(dto.toDomain())
            } else {
                android.util.Log.w("UserRepo", "getCurrentUser HTTP ${response.code()}: ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to fetch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(request: UserUpdateRequest): Result<User> {
        val bearer = bearerToken() ?: return Result.failure(Exception("Missing authentication token"))
        return try {
            val response = api.updateUserProfile(bearer, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to update user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── DTO → Domain mapper ────────────────────────────────────────────────────
    private fun com.example.kaizenfrontend.feature.user.data.remote.dto.UserResponse.toDomain() = User(
        id = id,
        username = username,
        email = email,
        unitSystem = unitSystem?.uppercase() ?: "METRIC",
        effortMeasurement = effortMeasurement?.uppercase() ?: "RPE",
        restTimerDefault = restTimerDefault ?: 90,
        profilePic = profilePic,
        primaryGoal = primaryGoal,
        equipmentAvailable = equipmentAvailable ?: emptyList(),
        authProvider = authProvider
    )
}
