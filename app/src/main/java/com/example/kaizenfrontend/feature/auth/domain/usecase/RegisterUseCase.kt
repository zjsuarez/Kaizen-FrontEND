package com.example.kaizenfrontend.feature.auth.domain.usecase

import com.example.kaizenfrontend.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<String> =
        repository.register(email, password)
}
