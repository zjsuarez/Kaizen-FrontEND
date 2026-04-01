package com.example.kaizenfrontend.feature.auth.domain.usecase

import com.example.kaizenfrontend.feature.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password).map { }
}
