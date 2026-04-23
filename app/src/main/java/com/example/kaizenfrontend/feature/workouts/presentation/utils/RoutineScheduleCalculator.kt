package com.example.kaizenfrontend.feature.workouts.presentation.utils

import com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object RoutineScheduleCalculator {

    fun calculateNextOccurrence(routine: Routine, plan: TrainingPlan?): String? {
        if (plan == null) return null // Standalone routines shouldn't exist anymore anyway

        val today = LocalDate.now()
        val intervalConfig = PlanIntervalConfig.fromBackend(plan.interval, plan.cycleLength)

        val nextDate = when (intervalConfig.type) {
            PlanIntervalType.FREQUENCY -> {
                val weekDays = parseWeekDays(routine.schedulingValue)
                if (weekDays.isNotEmpty()) {
                    calculateNextForWeekly(weekDays, today)
                } else {
                    val cycleDays = parseCycleDays(routine.schedulingValue)
                    if (cycleDays.isEmpty()) null
                    else calculateNextForCycle(cycleDays, parseDateOrToday(plan.startingDate, today), intervalConfig.cycleLengthDays.coerceAtLeast(1), today)
                }
            }
            PlanIntervalType.CYCLE -> {
                if (intervalConfig.cycleMode == CycleMode.WEEKLY) {
                    val weekDays = parseWeekDays(routine.schedulingValue)
                    if (weekDays.isNotEmpty()) {
                        calculateNextForWeekly(weekDays, today)
                    } else {
                        val normalizedWeekDays = parseCycleDays(routine.schedulingValue)
                            .mapNotNull { day ->
                                runCatching { DayOfWeek.of(day.coerceIn(1, 7)) }.getOrNull()
                            }
                            .toSet()
                        if (normalizedWeekDays.isEmpty()) null
                        else calculateNextForWeekly(normalizedWeekDays, today)
                    }
                } else {
                    val cycleDays = parseCycleDays(routine.schedulingValue)
                    if (cycleDays.isEmpty()) null
                    else calculateNextForCycle(
                        cycleDays,
                        parseDateOrToday(plan.startingDate, today),
                        intervalConfig.cycleLengthDays.coerceAtLeast(1),
                        today
                    )
                }
            }
        } ?: return null

        return formatNextOccurrence(nextDate, today)
    }

    private fun calculateNextForWeekly(weekDays: Set<DayOfWeek>, today: LocalDate): LocalDate? {
        if (weekDays.isEmpty()) return null
        
        val todayDow = today.dayOfWeek.value // 1..7
        
        val nextMatchingDayThisWeek = weekDays.map { it.value }.sorted().firstOrNull { it >= todayDow }
        return if (nextMatchingDayThisWeek != null) {
            today.plusDays((nextMatchingDayThisWeek - todayDow).toLong())
        } else {
            val firstDayNextWeek = weekDays.map { it.value }.minOrNull() ?: 1
            val daysUntilEndOfWeek = 7 - todayDow
            today.plusDays((daysUntilEndOfWeek + firstDayNextWeek).toLong())
        }
    }

    private fun calculateNextForCycle(
        scheduledDays: List<Int>,
        planStartDate: LocalDate,
        cycleLength: Int,
        today: LocalDate
    ): LocalDate? {
        if (scheduledDays.isEmpty()) return null

        // If today is before the plan even starts, simply return the first occurrence relative to the start date
        if (today.isBefore(planStartDate)) {
            val firstScheduledDay = scheduledDays.minOrNull() ?: 1
            return planStartDate.plusDays((firstScheduledDay - 1).toLong())
        }

        val daysSinceStart = ChronoUnit.DAYS.between(planStartDate, today)
        val dayInCycle = (daysSinceStart % cycleLength).toInt() + 1 // 1-indexed

        // Look for the next scheduled day in the CURRENT loop of the cycle
        val nextMatchingDayInCurrentLoop = scheduledDays.sorted().firstOrNull { it >= dayInCycle }

        return if (nextMatchingDayInCurrentLoop != null) {
            today.plusDays((nextMatchingDayInCurrentLoop - dayInCycle).toLong())
        } else {
            // Loop past the current cycle to the first day of the NEXT cycle
            val firstScheduledDay = scheduledDays.minOrNull() ?: 1
            val daysRemainingInCurrentCycle = cycleLength - dayInCycle
            today.plusDays(daysRemainingInCurrentCycle.toLong() + firstScheduledDay)
        }
    }

    fun parseWeekDays(schedulingValue: String?): Set<DayOfWeek> {
        if (schedulingValue.isNullOrBlank()) return emptySet()

        return schedulingValue
            .split(',')
            .mapNotNull { token ->
                val normalized = token.trim().uppercase()
                runCatching { DayOfWeek.valueOf(normalized) }.getOrNull()
            }
            .toSet()
    }

    fun parseCycleDays(schedulingValue: String?): List<Int> {
        if (schedulingValue.isNullOrBlank()) return emptyList()
        // Format is often JSON array like "[1, 4]" or standard string "MONDAY, WEDNESDAY"
        val cleanValue = schedulingValue
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "")
            .trim()

        // If it looks like days of week, convert to standard cycle integers (1..7, Monday=1)
        val isWeekDays = java.time.DayOfWeek.values().any { cleanValue.contains(it.name, ignoreCase = true) }
        if (isWeekDays) {
            return cleanValue.split(",")
                .map { it.trim().uppercase() }
                .mapNotNull { dayString ->
                    try {
                        DayOfWeek.valueOf(dayString).value
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
        }

        // Generic cycle days (1, 3, 5, etc.)
        return cleanValue.split(",")
            .map { it.trim() }
            .mapNotNull { it.toIntOrNull() }
    }

    fun parseRestDays(schedulingValue: String?): Int {
        return schedulingValue?.trim()?.toIntOrNull()?.takeIf { it >= 1 } ?: 1
    }

    private fun parseDateOrToday(dateStr: String?, today: LocalDate): LocalDate {
        if (dateStr.isNullOrBlank()) return today
        // Adjust for potential timezone or time appended, "2026-03-24T00:00:00Z"
        val dateOnlyString = if (dateStr.contains("T")) dateStr.substringBefore("T") else dateStr
        return try {
            LocalDate.parse(dateOnlyString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            today
        }
    }

    private fun formatNextOccurrence(nextDate: LocalDate, today: LocalDate): String {
        val daysDiff = ChronoUnit.DAYS.between(today, nextDate)
        
        return when {
            daysDiff == 0L -> "Today"
            daysDiff == 1L -> "Tomorrow"
            daysDiff > 1L -> "in $daysDiff days"
            else -> "Today" // Logic loops past dates forward, so daysDiff should be >= 0, but fallback just in case
        }
    }

    fun getDisplayStringOrFallback(routine: Routine, plan: TrainingPlan?): String {
        val intervalConfig = plan?.let { PlanIntervalConfig.fromBackend(it.interval, it.cycleLength) }
        val nextStr = calculateNextOccurrence(routine, plan)
        
        return if (nextStr == null) {
            if (intervalConfig?.type == PlanIntervalType.FREQUENCY && routine.lastPerformedDate.isNullOrBlank()) {
                "Not started yet"
            } else {
                "Not scheduled"
            }
        } else {
            nextStr
        }
    }
}
