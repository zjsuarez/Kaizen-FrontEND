package com.example.kaizenfrontend.feature.workouts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoutineCreateDTO(
    @SerializedName("PlanId")
    val planId: String? = null,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("ScheduleType")
    val scheduleType: String,
    @SerializedName("ScheduleData")
    val scheduleData: RoutineScheduleDataDTO,
    @SerializedName("Exercises")
    val exercises: List<RoutineExerciseTargetDTO>
)

data class RoutineScheduleDataDTO(
    @SerializedName("WeekDays")
    val weekDays: List<String>? = null,
    @SerializedName("CycleDays")
    val cycleDays: List<Int>? = null,
    @SerializedName("IntervalDays")
    val intervalDays: Int? = null,
    @SerializedName("CycleLength")
    val cycleLength: Int? = null,
    @SerializedName("RestDays")
    val restDays: Int? = null
)

data class RoutineExerciseTargetDTO(
    @SerializedName("ExerciseId")
    val exerciseId: String,
    @SerializedName("TargetSets")
    val targetSets: Int,
    @SerializedName("TargetReps")
    val targetReps: Int
)
