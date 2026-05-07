package com.example.kaizenfrontend.core.data

import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.MuscleTarget

object BuiltinExerciseCatalog {

    val exercises: List<Exercise> = listOf(
        Exercise(id = "ex_001", name = "Barbell Bench Press",      muscleTarget = MuscleTarget.CHEST,     equipmentType = EquipmentType.BARBELL,    gifUrl = null),
        Exercise(id = "ex_002", name = "Incline Dumbbell Press",   muscleTarget = MuscleTarget.CHEST,     equipmentType = EquipmentType.DUMBBELL,   gifUrl = null),
        Exercise(id = "ex_003", name = "Lat Pulldown",             muscleTarget = MuscleTarget.BACK,      equipmentType = EquipmentType.CABLE,      gifUrl = null),
        Exercise(id = "ex_004", name = "Seated Cable Row",         muscleTarget = MuscleTarget.BACK,      equipmentType = EquipmentType.CABLE,      gifUrl = null),
        Exercise(id = "ex_005", name = "Back Squat",               muscleTarget = MuscleTarget.LEGS,      equipmentType = EquipmentType.BARBELL,    gifUrl = null),
        Exercise(id = "ex_006", name = "Leg Press",                muscleTarget = MuscleTarget.LEGS,      equipmentType = EquipmentType.MACHINE,    gifUrl = null),
        Exercise(id = "ex_007", name = "Dumbbell Shoulder Press",  muscleTarget = MuscleTarget.SHOULDERS, equipmentType = EquipmentType.DUMBBELL,   gifUrl = null),
        Exercise(id = "ex_008", name = "Lateral Raise",            muscleTarget = MuscleTarget.SHOULDERS, equipmentType = EquipmentType.DUMBBELL,   gifUrl = null),
        Exercise(id = "ex_009", name = "Cable Triceps Pushdown",   muscleTarget = MuscleTarget.ARMS,      equipmentType = EquipmentType.CABLE,      gifUrl = null),
        Exercise(id = "ex_010", name = "Plank",                    muscleTarget = MuscleTarget.CORE,      equipmentType = EquipmentType.BODYWEIGHT, gifUrl = null),
    )

    private val nameById: Map<String, String> = exercises.associate { it.id to it.name }

    fun resolveExerciseName(nameOrId: String): String = nameById[nameOrId] ?: nameOrId
}
