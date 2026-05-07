package com.example.kaizenfrontend.feature.workouts.presentation

object WorkoutInputSanitizer {
    const val MAX_TITLE_LENGTH = 30
    const val MAX_DESCRIPTION_LENGTH = 300
    const val MAX_SESSION_NUMBER_DIGITS = 6
    const val MAX_WORKOUT_NOTES_LENGTH = 200
    const val MAX_EFFORT_VALUE = 10

    fun normalizeTitleInput(input: String): String =
        input.take(MAX_TITLE_LENGTH)

    fun normalizeDescriptionInput(input: String): String =
        input.take(MAX_DESCRIPTION_LENGTH)

    fun normalizeSessionNumberInput(input: String): String =
        input.filter(Char::isDigit).take(MAX_SESSION_NUMBER_DIGITS)

    fun normalizeNotesInput(input: String): String =
        input.take(MAX_WORKOUT_NOTES_LENGTH)

    fun normalizeEffortInput(input: String, previousValue: String): String {
        val digitsOnly = input.filter(Char::isDigit).take(MAX_EFFORT_VALUE.toString().length)

        if (digitsOnly.isBlank()) {
            return ""
        }

        val parsedValue = digitsOnly.toIntOrNull() ?: return previousValue

        return if (parsedValue <= MAX_EFFORT_VALUE) {
            parsedValue.toString()
        } else {
            previousValue
        }
    }
}
