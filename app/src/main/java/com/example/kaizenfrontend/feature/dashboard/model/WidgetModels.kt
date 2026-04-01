package com.example.kaizenfrontend.feature.dashboard.model

import androidx.compose.ui.unit.Dp

/**
 * Defines how many columns a widget spans in the 2-column LazyVerticalGrid.
 */
enum class WidgetSize(val span: Int) {
    FULL_WIDTH(span = 2),
    HALF_WIDTH(span = 1)
}

/**
 * The 9 planned dashboard widgets for the Kaizen Hub.
 */
enum class WidgetType {
    NEXT_WORKOUT,
    CALENDAR,
    RECENT_PRS,
    WEIGHT_TREND,
    RECOVERY_TIME,
    LAST_SESSION,
    STREAK,
    AVG_TIME,
    ONE_RM
}

/**
 * Configuration for a single widget slot in the dashboard grid.
 *
 * @param type     Which widget to render in this slot.
 * @param size     Column span (FULL_WIDTH = 2 cols, HALF_WIDTH = 1 col).
 * @param heightDp Fixed height for this widget slot.
 */
data class WidgetConfig(
    val type: WidgetType,
    val size: WidgetSize,
    val heightDp: Dp
)
