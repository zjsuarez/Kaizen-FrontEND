package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutSetResponseDto
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseHistoryUiState(
    val isLoading: Boolean = false,
    val workouts: List<com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto> = emptyList(),
    val errorMessage: String? = null
)

data class ExerciseHistorySetUi(
    val setNumber: Int,
    val weightKg: Double?,
    val reps: Int?,
    val rpe: Int?
)

data class ExerciseHistoryWorkoutUi(
    val workoutId: String,
    val workoutLabel: String,
    val formattedDate: String,
    val formattedTime: String,
    val isMostRecent: Boolean,
    val sets: List<ExerciseHistorySetUi>
)

@HiltViewModel
class ExerciseHistoryViewModel
@Inject
constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseHistoryUiState(isLoading = true))
    val uiState: StateFlow<ExerciseHistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = workoutRepository.getWorkouts()
            result.fold(
                onSuccess = { workouts ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            workouts = workouts,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not load workout history"
                        )
                    }
                }
            )
        }
    }

    fun getExerciseHistory(
        exerciseId: String,
        isCustomExercise: Boolean,
        exerciseName: String
    ): List<ExerciseHistoryWorkoutUi> {
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        val sessions = uiState.value.workouts
            .asSequence()
            .mapNotNull { workout ->
                val matchingSets = workout.sets
                    .filter { matchesExercise(it, exerciseId, isCustomExercise, exerciseName) }
                    .sortedBy { it.setNumber }
                if (matchingSets.isEmpty()) return@mapNotNull null

                val instant = parseToInstant(workout.startTime)
                val localDate = parseToLocalDate(workout.startTime)
                val zonedDateTime = instant?.atZone(ZoneId.systemDefault())

                val formattedDate = when {
                    zonedDateTime != null -> zonedDateTime.format(dateFormatter)
                    localDate != null -> localDate.format(dateFormatter)
                    else -> "Unknown date"
                }
                val formattedTime = zonedDateTime?.format(timeFormatter) ?: "Unknown time"
                val sortMillis = instant?.toEpochMilli() ?: parseToEpochMillis(workout.startTime)

                sortMillis to ExerciseHistoryWorkoutUi(
                    workoutId = workout.id,
                    workoutLabel = workout.routineName?.takeIf { it.isNotBlank() } ?: "Workout",
                    formattedDate = formattedDate,
                    formattedTime = formattedTime,
                    isMostRecent = false,
                    sets = matchingSets.map { set ->
                        ExerciseHistorySetUi(
                            setNumber = set.setNumber,
                            weightKg = set.weightKg,
                            reps = set.reps,
                            rpe = set.rpe
                        )
                    }
                )
            }
            .toList()

        return sessions
            .sortedByDescending { it.first }
            .map { it.second }
            .mapIndexed { index, session ->
                session.copy(isMostRecent = index == 0)
            }
    }

    private fun matchesExercise(
        set: WorkoutSetResponseDto,
        exerciseId: String,
        isCustomExercise: Boolean,
        exerciseName: String
    ): Boolean {
        val idMatch = if (isCustomExercise) {
            set.customExerciseId == exerciseId
        } else {
            set.builtinExerciseKey == exerciseId
        }

        if (idMatch) return true

        return !set.exerciseName.isNullOrBlank() &&
            set.exerciseName.equals(exerciseName, ignoreCase = true)
    }

    private fun parseToLocalDate(rawDate: String?): LocalDate? {
        val value = rawDate?.trim().orEmpty()
        if (value.isBlank()) return null

        return runCatching { OffsetDateTime.parse(value).toLocalDate() }.getOrNull()
            ?: runCatching {
                Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.getOrNull()
            ?: runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
    }

    private fun parseToInstant(rawDate: String?): Instant? {
        val value = rawDate?.trim().orEmpty()
        if (value.isBlank()) return null

        return runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
            ?: runCatching { Instant.parse(value) }.getOrNull()
    }

    private fun parseToEpochMillis(rawDate: String?): Long {
        val value = rawDate?.trim().orEmpty()
        if (value.isBlank()) return 0L

        return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
            ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
            ?: 0L
    }
}
