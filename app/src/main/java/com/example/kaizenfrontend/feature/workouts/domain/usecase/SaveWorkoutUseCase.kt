package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.request.WorkoutRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.request.WorkoutSetRequest
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaveWorkoutUseCase(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(state: ActiveWorkoutState): Result<Unit> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val startTimeStr = dateFormat.format(Date(state.startTime))
        val endTimeStr = dateFormat.format(Date(state.startTime + state.elapsedTimeGlobal * 1000L))

        val sets = mutableListOf<WorkoutSetRequest>()
        state.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                // Only send completed sets or everything? Usually we send completed sets or all sets but with data.
                // We'll send sets that have at least weight or reps.
                if (set.isCompleted || (!set.weight.isNullOrBlank() || !set.reps.isNullOrBlank())) {
                    sets.add(
                        WorkoutSetRequest(
                            customExerciseId = if (exercise.isCustom) exercise.id else null,
                            builtinExerciseKey = if (!exercise.isCustom) exercise.id else null,
                            setNumber = set.setNumber,
                            weightKg = set.weight?.toDoubleOrNull(),
                            isPR = false, // PR is calculated/handled in backend or requires extra logic
                            reps = set.reps?.toIntOrNull(),
                            rpe = set.rir?.toIntOrNull(), // Backend expects RPE, mapping RIR roughly if we want or just sending as is. It's stored in rpe.
                            type = set.type.name
                        )
                    )
                }
            }
        }

        val request = WorkoutRequest(
            routineId = state.routineId,
            startTime = startTimeStr,
            endTime = endTimeStr,
            notes = state.notes,
            sets = sets
        )

        val result = repository.saveWorkout(request)
        return if (result.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error saving workout"))
        }
    }
}
