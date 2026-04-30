package com.example.kaizenfrontend.feature.auth.domain.validation

import java.util.Locale

object AuthInputValidator {
    const val MAX_EMAIL_LENGTH = 255
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 20

    private val emailRegex = Regex(
        pattern = "^[a-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+$"
    )

    fun normalizeEmail(rawEmail: String): String {
        return rawEmail.trim().lowercase(Locale.ROOT)
    }

    fun validateEmail(normalizedEmail: String): String? {
        if (normalizedEmail.isBlank()) return "Email is required"
        if (normalizedEmail.length > MAX_EMAIL_LENGTH) {
            return "Email cannot exceed $MAX_EMAIL_LENGTH characters"
        }
        if (!emailRegex.matches(normalizedEmail)) return "Please enter a valid email"

        val domainTld = normalizedEmail.substringAfterLast('.', missingDelimiterValue = "")
        if (domainTld.length < 2) return "Please enter a valid email"

        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < MIN_PASSWORD_LENGTH) {
            return "Password must be at least $MIN_PASSWORD_LENGTH characters"
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            return "Password cannot exceed $MAX_PASSWORD_LENGTH characters"
        }
        return null
    }
}
