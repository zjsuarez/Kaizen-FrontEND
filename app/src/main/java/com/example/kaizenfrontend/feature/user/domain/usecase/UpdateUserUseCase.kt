package com.example.kaizenfrontend.feature.user.domain.usecase

import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.domain.model.User
import com.example.kaizenfrontend.feature.user.domain.repository.UserRepository

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(request: UserUpdateRequest): Result<User> =
        repository.updateUser(request)
}
