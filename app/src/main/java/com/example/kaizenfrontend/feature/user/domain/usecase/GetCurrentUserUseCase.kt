package com.example.kaizenfrontend.feature.user.domain.usecase

import com.example.kaizenfrontend.feature.user.domain.model.User
import com.example.kaizenfrontend.feature.user.domain.repository.UserRepository

class GetCurrentUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<User> = repository.getCurrentUser()
}
