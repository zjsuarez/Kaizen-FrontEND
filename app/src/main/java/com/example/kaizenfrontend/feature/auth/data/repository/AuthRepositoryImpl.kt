package com.example.kaizenfrontend.feature.auth.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.network.RetrofitClient
import com.example.kaizenfrontend.feature.auth.data.remote.dto.LoginRequest
import com.example.kaizenfrontend.feature.auth.data.remote.dto.RegisterRequest
import com.example.kaizenfrontend.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val sessionManager: SessionManager
) : AuthRepository {

    private val api = RetrofitClient.authService

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.loginUser(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                sessionManager.saveToken(token)
                Result.success(token)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<String> {
        return try {
            val username = "Kaizenuser-" + email.replace("@", "-").replace(".", "-")
            val registerResponse = api.registerUser(RegisterRequest(username, email, password))

            if (registerResponse.isSuccessful) {
                // Auto-login after successful registration
                login(email, password)
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
                val token = response.body()!!.token
                sessionManager.saveToken(token)
                Result.success(token)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Google Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
