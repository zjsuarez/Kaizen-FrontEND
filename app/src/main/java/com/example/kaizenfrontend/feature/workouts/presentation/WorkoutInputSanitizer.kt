package com.example.kaizenfrontend.feature.workouts.presentation

object WorkoutInputSanitizer {
    const val MAX_TITLE_LENGTH = 30
    const val MAX_DESCRIPTION_LENGTH = 300

    fun normalizeTitleInput(input: String): String =
        input.take(MAX_TITLE_LENGTH)

    fun normalizeDescriptionInput(input: String): String =
        input.take(MAX_DESCRIPTION_LENGTH)
}
