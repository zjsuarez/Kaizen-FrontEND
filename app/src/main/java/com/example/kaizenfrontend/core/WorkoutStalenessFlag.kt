package com.example.kaizenfrontend.core

object WorkoutStalenessFlag {
    @Volatile var isStatisticsStale: Boolean = false
}
