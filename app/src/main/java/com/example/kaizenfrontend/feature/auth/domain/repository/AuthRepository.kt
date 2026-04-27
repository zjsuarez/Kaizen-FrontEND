package com.example.kaizenfrontend.feature.auth.domain.repository

/**
 * Contract for authentication operations.
 */
interface AuthRepository {
    /** Returns the JWT token on success. */
    suspend fun login(email: String, password: String): Result<String>

    /** Registers a new user, then auto-logs in and returns the JWT token. */
    suspend fun register(email: String, password: String): Result<String>

    /** Logs in using a Google ID Token. */
    suspend fun googleLogin(idToken: String): Result<String>
}
