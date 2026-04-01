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
            PlanIntervalType.CYCLE -> "CYCLE"
        }
    }

    fun toBackendCycleLength(): Int? {
        return when (type) {
            PlanIntervalType.FREQUENCY -> null
            PlanIntervalType.CYCLE -> {
                if (cycleMode == CycleMode.WEEKLY) {
                    7
                } else {
                    cycleLengthDays.coerceAtLeast(1)
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

        fun fromBackend(interval: String?, cycleLength: Int?): PlanIntervalConfig {
            if (interval.isNullOrBlank()) return defaultCycleWeekly()

            val normalized = interval.trim().uppercase()

            // Backward compatibility with previously encoded values.
            if (normalized.contains(':')) {
                return fromLegacyEncodedValue(normalized)
            }

            if (normalized == "FREQUENCY") return defaultFrequency()

            if (normalized == "CYCLE" || normalized == "WEEKLY") {
                val resolvedLength = cycleLength?.coerceAtLeast(1) ?: 7
                return if (resolvedLength == 7) {
                    defaultCycleWeekly()
                } else {
                    PlanIntervalConfig(
                        type = PlanIntervalType.CYCLE,
                        cycleMode = CycleMode.CUSTOM,
                        cycleLengthDays = resolvedLength
                    )
                }
            }

            return defaultCycleWeekly()
        }

        fun fromBackendValue(raw: String?): PlanIntervalConfig {
            return fromBackend(interval = raw, cycleLength = null)
        }

        private fun fromLegacyEncodedValue(normalized: String): PlanIntervalConfig {
            if (normalized == "CYCLE:WEEKLY") return defaultCycleWeekly()

            if (normalized.startsWith("CYCLE:CUSTOM:")) {
                val days = normalized.substringAfter("CYCLE:CUSTOM:").toIntOrNull()?.coerceAtLeast(1) ?: 7
                return PlanIntervalConfig(
                    type = PlanIntervalType.CYCLE,
                    cycleMode = if (days == 7) CycleMode.WEEKLY else CycleMode.CUSTOM,
                    cycleLengthDays = days
                )
            }

            return defaultCycleWeekly()
        }
    }
}
