package com.example.kaizenfrontend.core.ui.navigation

/**
 * Single source of truth for navigation route strings.
 *
 * Replaces the literal route strings scattered through `MainActivity`
 * (lines 60-173 of the legacy implementation) and the integer
 * `selectedTab` index inside the legacy DashboardScreen.
 *
 * Two route groups:
 *  • Auth/onboarding flow — full-screen, no bottom bar.
 *  • Tab destinations — wrapped in `KaizenTabScaffold` with the
 *    standard header + bottom bar.
 */
object KaizenDestinations {
    const val SPLASH = "splash"
    const val START = "start"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val CALIBRATION = "calibration"
    const val ONBOARDING = "onboarding"

    const val DASHBOARD = "dashboard"
    const val WORKOUTS = "workouts"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"

    /** Routes that show the bottom nav. */
    val tabRoutes: Set<String> = setOf(DASHBOARD, WORKOUTS, STATISTICS, SETTINGS)
}
