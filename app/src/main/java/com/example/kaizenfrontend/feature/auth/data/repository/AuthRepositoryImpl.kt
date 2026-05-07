package com.example.kaizenfrontend.feature.auth.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.auth.data.remote.AuthApiService
import com.example.kaizenfrontend.feature.auth.data.remote.dto.LoginRequest
import com.example.kaizenfrontend.feature.auth.data.remote.dto.RegisterRequest
import com.example.kaizenfrontend.feature.auth.domain.validation.AuthInputValidator
import com.example.kaizenfrontend.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    private fun normalizeToken(rawToken: String): String =
        rawToken.trim().removePrefix("Bearer ").removePrefix("bearer ")

    private fun normalizeEmail(rawEmail: String): String =
        AuthInputValidator.normalizeEmail(rawEmail).take(AuthInputValidator.MAX_EMAIL_LENGTH)

    override suspend fun login(email: String, password: String): Result<String> {
        val normalizedEmail = normalizeEmail(email)
        return try {
            val response = api.loginUser(LoginRequest(normalizedEmail, password))
            if (response.isSuccessful && response.body() != null) {
                val token = normalizeToken(response.body()!!.token)
                sessionManager.saveTokenAndAwait(token)
                Result.success(token)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<String> {
        val normalizedEmail = normalizeEmail(email)
        return try {
            val username = "Kaizenuser-" + normalizedEmail.replace("@", "-").replace(".", "-")
            val registerResponse = api.registerUser(RegisterRequest(username, normalizedEmail, password))

            if (registerResponse.isSuccessful) {
                // Auto-login after successful registration
                login(normalizedEmail, password)
            } else {
                Result.failure(Exception(registerResponse.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleLogin(idToken: String): Result<String> {
        return try {
            val response = api.googleLogin(com.example.kaizenfrontend.feature.auth.data.remote.dto.GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val token = normalizeToken(response.body()!!.token)
                sessionManager.saveTokenAndAwait(token)
                Result.success(token)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Google Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
