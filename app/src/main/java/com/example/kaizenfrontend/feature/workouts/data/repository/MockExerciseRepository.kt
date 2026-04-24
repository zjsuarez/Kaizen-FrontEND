package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.CreateCustomExerciseCommand
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.MuscleTarget
import com.example.kaizenfrontend.feature.workouts.domain.repository.ExerciseRepository

class MockExerciseRepository : ExerciseRepository {

    override suspend fun getExercises(): Result<List<Exercise>> {
        val merged = synchronized(lock) {
            builtinExercises + customExercises
        }
        return Result.success(merged)
    }

    override suspend fun createCustomExercise(command: CreateCustomExerciseCommand): Result<Exercise> {
        val trimmedName = command.name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise name is required"))
        }

        val custom = Exercise(
            id = "custom_${System.currentTimeMillis()}",
            name = trimmedName,
            muscleTarget = mapPrimaryTarget(command.selectedMuscles),
            equipmentType = command.equipmentType,
            gifUrl = null,
            isCustom = true,
            description = command.description?.trim().takeUnless { it.isNullOrBlank() },
            selectedMuscles = command.selectedMuscles,
            metric = command.metrics.toExerciseMetric()
        )

        synchronized(lock) {
            customExercises = customExercises + custom
        }

        return Result.success(custom)
    }

    private fun mapPrimaryTarget(selectedMuscles: List<String>): MuscleTarget {
        val normalized = selectedMuscles.map { it.trim().lowercase() }
        return when {
            normalized.any { it in setOf("chest") } -> MuscleTarget.CHEST
            normalized.any { it in setOf("biceps", "triceps", "forearms", "arms") } -> MuscleTarget.ARMS
            normalized.any { it in setOf("shoulders", "delts", "deltoids") } -> MuscleTarget.SHOULDERS
            normalized.any { it in setOf("quads", "hamstrings", "glutes", "calves", "legs") } -> MuscleTarget.LEGS
            normalized.any { it in setOf("core", "abs") } -> MuscleTarget.CORE
            else -> MuscleTarget.BACK
        }
    }

    private fun String.toExerciseMetric(): com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric {
        return when (trim().lowercase()) {
            "set", "sets" -> com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric.SETS
            "duration" -> com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric.DURATION
            "distance", "distance_km" -> com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric.DISTANCE
            "simplecheckoff", "simple_check_off", "simple-check-off", "simple check off" -> com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric.SIMPLE_CHECK_OFF
            else -> com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric.SETS
        }
    }

    companion object {
        private val lock = Any()
        private var customExercises: List<Exercise> = emptyList()

        private val builtinExercises = listOf(
        Exercise(
            id = "ex_001",
            name = "Barbell Bench Press",
            muscleTarget = MuscleTarget.CHEST,
            equipmentType = EquipmentType.BARBELL,
            gifUrl = null
        ),
        Exercise(
            id = "ex_002",
            name = "Incline Dumbbell Press",
            muscleTarget = MuscleTarget.CHEST,
            equipmentType = EquipmentType.DUMBBELL,
            gifUrl = null
        ),
        Exercise(
            id = "ex_003",
            name = "Lat Pulldown",
            muscleTarget = MuscleTarget.BACK,
            equipmentType = EquipmentType.CABLE,
            gifUrl = null
        ),
        Exercise(
            id = "ex_004",
            name = "Seated Cable Row",
            muscleTarget = MuscleTarget.BACK,
            equipmentType = EquipmentType.CABLE,
            gifUrl = null
        ),
        Exercise(
            id = "ex_005",
            name = "Back Squat",
            muscleTarget = MuscleTarget.LEGS,
            equipmentType = EquipmentType.BARBELL,
            gifUrl = null
        ),
        Exercise(
            id = "ex_006",
            name = "Leg Press",
            muscleTarget = MuscleTarget.LEGS,
            equipmentType = EquipmentType.MACHINE,
            gifUrl = null
        ),
        Exercise(
            id = "ex_007",
            name = "Dumbbell Shoulder Press",
            muscleTarget = MuscleTarget.SHOULDERS,
            equipmentType = EquipmentType.DUMBBELL,
            gifUrl = null
        ),
        Exercise(
            id = "ex_008",
            name = "Lateral Raise",
            muscleTarget = MuscleTarget.SHOULDERS,
            equipmentType = EquipmentType.DUMBBELL,
            gifUrl = null
        ),
        Exercise(
            id = "ex_009",
            name = "Cable Triceps Pushdown",
            muscleTarget = MuscleTarget.ARMS,
            equipmentType = EquipmentType.CABLE,
            gifUrl = null
        ),
        Exercise(
            id = "ex_010",
            name = "Plank",
            muscleTarget = MuscleTarget.CORE,
            equipmentType = EquipmentType.BODYWEIGHT,
            gifUrl = null
        )
        )
    }
}
