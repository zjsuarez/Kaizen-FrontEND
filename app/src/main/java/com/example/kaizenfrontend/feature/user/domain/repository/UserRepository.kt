package com.example.kaizenfrontend.feature.user.domain.repository

import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    /** Fetch the current user using [bearerToken] directly (e.g. "Bearer <jwt>").
     *  Use this immediately after login to guarantee the freshest token is used. */
    suspend fun getCurrentUserWithToken(bearerToken: String): Result<User>
    suspend fun updateUser(request: UserUpdateRequest): Result<User>
}
