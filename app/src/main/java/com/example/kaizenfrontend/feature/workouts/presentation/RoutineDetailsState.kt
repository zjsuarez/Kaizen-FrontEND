package com.example.kaizenfrontend.feature.workouts.presentation

import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import java.time.DayOfWeek

data class RoutineDetailsState(
    val routineId: String,
    val title: String,
    val description: String,
    val exercises: List<RoutineExercise>,
    val planIntervalConfig: PlanIntervalConfig? = null,
    val selectedWeekDays: Set<DayOfWeek> = emptySet(),
    val selectedCycleDays: Set<Int> = emptySet(),
    val restDaysBetweenWorkouts: Int = 1,
    val schedulingValueString: String? = null,
    val isEditMode: Boolean = false
)
