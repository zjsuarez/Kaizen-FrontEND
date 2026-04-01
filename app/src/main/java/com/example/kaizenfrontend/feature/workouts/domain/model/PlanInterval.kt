package com.example.kaizenfrontend.feature.workouts.domain.model

enum class PlanIntervalType {
    CYCLE,
    FREQUENCY
}

enum class CycleMode {
    WEEKLY,
    CUSTOM
}

data class PlanIntervalConfig(
    val type: PlanIntervalType,
    val cycleMode: CycleMode = CycleMode.WEEKLY,
    val cycleLengthDays: Int = 7
) {
    fun toBackendValue(): String {
        return when (type) {
            PlanIntervalType.FREQUENCY -> "FREQUENCY"
            PlanIntervalType.CYCLE -> {
                if (cycleMode == CycleMode.WEEKLY) {
                    "CYCLE:WEEKLY"
                } else {
                    "CYCLE:CUSTOM:${cycleLengthDays.coerceAtLeast(1)}"
                }
            }
        }
    }

    fun toDisplayLabel(): String {
        return when (type) {
            PlanIntervalType.FREQUENCY -> "Frequency"
            PlanIntervalType.CYCLE -> {
                if (cycleMode == CycleMode.WEEKLY) {
                    "Cycle (Weekly)"
                } else {
                    "Cycle (${cycleLengthDays.coerceAtLeast(1)} days)"
                }
            }
        }
    }

    companion object {
        fun defaultCycleWeekly(): PlanIntervalConfig = PlanIntervalConfig(
            type = PlanIntervalType.CYCLE,
            cycleMode = CycleMode.WEEKLY,
            cycleLengthDays = 7
        )

        fun defaultFrequency(): PlanIntervalConfig = PlanIntervalConfig(
            type = PlanIntervalType.FREQUENCY
        )

        fun fromBackendValue(raw: String?): PlanIntervalConfig {
            if (raw.isNullOrBlank()) return defaultCycleWeekly()

            val normalized = raw.trim().uppercase()
            if (normalized == "FREQUENCY") return defaultFrequency()

            if (normalized.startsWith("CYCLE:CUSTOM:")) {
                val days = normalized.substringAfter("CYCLE:CUSTOM:").toIntOrNull()?.coerceAtLeast(1) ?: 7
                return PlanIntervalConfig(
                    type = PlanIntervalType.CYCLE,
                    cycleMode = CycleMode.CUSTOM,
                    cycleLengthDays = days
                )
            }

            if (normalized == "WEEKLY" || normalized == "CYCLE" || normalized == "CYCLE:WEEKLY") {
                return defaultCycleWeekly()
            }

            return defaultCycleWeekly()
        }
    }
}
