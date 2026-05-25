package com.example.kaizenfrontend.feature.dashboard.model

import androidx.compose.ui.unit.Dp

enum class WidgetSize(val span: Int) {
    FULL_WIDTH(span = 2),
    HALF_WIDTH(span = 1)
}

enum class WidgetType {
    NEXT_WORKOUT,
    CALENDAR,
    RECENT_PRS,
    WEIGHT_TREND,
    LAST_SESSION,
    STREAK,
    AVG_TIME
}

data class WidgetConfig(
    val type: WidgetType,
    val size: WidgetSize,
    val heightDp: Dp? = null
)
