package com.example.kaizenfrontend.feature.workouts.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric
import com.example.kaizenfrontend.feature.workouts.domain.model.MuscleTarget

data class ExerciseRequest(
    val name: String,
    val description: String?,
    val muscleTarget: String,
    val metrics: String,
    val type: String,
    val isCustom: Boolean = true
)

data class ExerciseResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val muscleTarget: String? = null,
    val metrics: String? = null,
    val type: String? = null,
    val isCustom: Boolean = false,
    @SerializedName(value = "createdByUserId", alternate = ["createdByUserId_FK", "createdByUserIdFk"])
    val createdByUserId: String? = null
)

fun ExerciseResponse.toDomain(): Exercise {
    val muscleList = muscleTarget
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        .orEmpty()

    return Exercise(
        id = id,
        name = name,
        muscleTarget = muscleTarget.toPrimaryTarget(),
        equipmentType = type.toEquipmentType(),
        gifUrl = null,
        isCustom = isCustom,
        description = description,
        selectedMuscles = muscleList,
        metric = metrics.toExerciseMetric(),
        createdByUserId = createdByUserId
    )
}

private fun String?.toPrimaryTarget(): MuscleTarget {
    val normalized = this
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?.lowercase()
        .orEmpty()

    return when (normalized) {
        "chest" -> MuscleTarget.CHEST
        "biceps", "triceps", "forearms", "arms" -> MuscleTarget.ARMS
        "shoulders", "delts", "deltoids" -> MuscleTarget.SHOULDERS
        "quads", "hamstrings", "glutes", "calves", "legs" -> MuscleTarget.LEGS
        "core", "abs" -> MuscleTarget.CORE
        else -> MuscleTarget.BACK
    }
}

private fun String?.toEquipmentType(): EquipmentType {
    return when (this?.uppercase()) {
        EquipmentType.BAND.name -> EquipmentType.BAND
        EquipmentType.CARDIO.name -> EquipmentType.CARDIO
        EquipmentType.MACHINE.name -> EquipmentType.MACHINE
        EquipmentType.DUMBBELL.name -> EquipmentType.DUMBBELL
        EquipmentType.BARBELL.name -> EquipmentType.BARBELL
        EquipmentType.CABLE.name -> EquipmentType.CABLE
        EquipmentType.KETTLEBELL.name -> EquipmentType.KETTLEBELL
        EquipmentType.SMITH_MACHINE.name -> EquipmentType.SMITH_MACHINE
        EquipmentType.BODYWEIGHT.name -> EquipmentType.BODYWEIGHT
        else -> EquipmentType.BODYWEIGHT
    }
}

private fun String?.toExerciseMetric(): ExerciseMetric {
    return when (this?.trim()?.lowercase()) {
        "set", "sets" -> ExerciseMetric.SETS
        "duration" -> ExerciseMetric.DURATION
        "distance", "distance_km" -> ExerciseMetric.DISTANCE
        "simplecheckoff", "simple_check_off", "simple-check-off", "simple check off" -> ExerciseMetric.SIMPLE_CHECK_OFF
        else -> ExerciseMetric.SETS
    }
}
