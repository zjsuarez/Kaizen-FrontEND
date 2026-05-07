package com.example.kaizenfrontend.feature.auth.domain.validation

import java.util.Locale

sealed class AuthValidationResult {
    object Valid : AuthValidationResult()
    object EmptyFields : AuthValidationResult()
    object InvalidEmail : AuthValidationResult()
    object EmailTooLong : AuthValidationResult()
    object PasswordTooShort : AuthValidationResult()
    object PasswordTooLong : AuthValidationResult()
    object PasswordsDoNotMatch : AuthValidationResult()
}

object AuthInputValidator {
    const val MAX_EMAIL_LENGTH = 255
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 20

    private val emailRegex = Regex(
        pattern = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"
    )

    fun normalizeEmail(input: String): String =
        input.trim().lowercase(Locale.ROOT)

    fun normalizeEmailInput(input: String): String =
        normalizeEmail(input).take(MAX_EMAIL_LENGTH)

    fun normalizePasswordInput(input: String): String =
        input.take(MAX_PASSWORD_LENGTH)

    fun validateLogin(email: String, password: String): AuthValidationResult {
        val normalizedEmail = normalizeEmail(email)

        return when {
            normalizedEmail.isBlank() || password.isBlank() -> AuthValidationResult.EmptyFields
            normalizedEmail.length > MAX_EMAIL_LENGTH -> AuthValidationResult.EmailTooLong
            !emailRegex.matches(normalizedEmail) -> AuthValidationResult.InvalidEmail
            password.length < MIN_PASSWORD_LENGTH -> AuthValidationResult.PasswordTooShort
            password.length > MAX_PASSWORD_LENGTH -> AuthValidationResult.PasswordTooLong
            else -> AuthValidationResult.Valid
        }
    }

    fun validateRegistration(
        email: String,
        password: String,
        confirmPassword: String
    ): AuthValidationResult {
        val loginValidation = validateLogin(email = email, password = password)

        return when {
            confirmPassword.isBlank() -> AuthValidationResult.EmptyFields
            loginValidation != AuthValidationResult.Valid -> loginValidation
            password != confirmPassword -> AuthValidationResult.PasswordsDoNotMatch
            else -> AuthValidationResult.Valid
        }
    }
}
