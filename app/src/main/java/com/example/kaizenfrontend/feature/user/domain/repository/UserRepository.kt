package com.example.kaizenfrontend.feature.user.domain.repository

import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateUser(request: UserUpdateRequest): Result<User>
}
