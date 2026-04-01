package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.KaizenWidgetContainer
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.dashboard.model.WidgetConfig
import com.example.kaizenfrontend.feature.dashboard.model.WidgetSize
import com.example.kaizenfrontend.feature.dashboard.model.WidgetType
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.AvgTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.CalendarWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.LastSessionWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.NextWorkoutWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.OneRmWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrMock
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrsWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecoveryTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.StreakWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.WeightTrendWidget
import com.example.kaizenfrontend.feature.statistics.presentation.StatisticsScreen
import com.example.kaizenfrontend.feature.user.presentation.settings.SettingsScreen
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutsScreen

// ──────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val title: String, val date: String, val workoutPlan: String) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

// ──────────────────────────────────────────────────────────────
// Dashboard Widget Layout — hardcoded order for the grid engine
// ──────────────────────────────────────────────────────────────

private val dashboardWidgets = listOf(
    // LARGE — full-width, tall
    WidgetConfig(type = WidgetType.NEXT_WORKOUT,  size = WidgetSize.FULL_WIDTH, heightDp = 200.dp),
    // THIN — full-width, short
    WidgetConfig(type = WidgetType.WEIGHT_TREND,  size = WidgetSize.FULL_WIDTH, heightDp = 80.dp),
    WidgetConfig(type = WidgetType.RECOVERY_TIME, size = WidgetSize.FULL_WIDTH, heightDp = 80.dp),
    WidgetConfig(type = WidgetType.LAST_SESSION,  size = WidgetSize.FULL_WIDTH, heightDp = 80.dp),
    // SMALL — half-width, square (side by side)
    WidgetConfig(type = WidgetType.STREAK,        size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    WidgetConfig(type = WidgetType.AVG_TIME,      size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    WidgetConfig(type = WidgetType.ONE_RM,        size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    // Placeholder to keep the grid even (2 cols)
    WidgetConfig(type = WidgetType.ONE_RM,        size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    // LARGE — full-width, tall
    WidgetConfig(type = WidgetType.CALENDAR, size = WidgetSize.FULL_WIDTH, heightDp = 320.dp),
    WidgetConfig(type = WidgetType.RECENT_PRS,    size = WidgetSize.FULL_WIDTH, heightDp = 250.dp),
)

// ──────────────────────────────────────────────────────────────
// Main Screen
// ──────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    uiState: DashboardUiState = DashboardUiState.Success(
        title = "Welcome!",
        date = "16 / 02",
        workoutPlan = "Upper"
    ),
    onWorkoutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Onyx,
        bottomBar = {
            KaizenBottomNavigation(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> when (uiState) {
                    is DashboardUiState.Loading -> {
                        CircularProgressIndicator(
                            color = CrayolaBlue,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is DashboardUiState.Success -> {
                        DashboardWidgetGrid(
                            successState = uiState,
                            onWorkoutClick = onWorkoutClick
                        )
                    }
                    is DashboardUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(ShadowGrey, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = uiState.message, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
                1 -> WorkoutsScreen()
                2 -> StatisticsScreen()
                3 -> SettingsScreen(onLogoutClick = onLogoutClick)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Widget Grid Engine
// ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardWidgetGrid(
    successState: DashboardUiState.Success,
    onWorkoutClick: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Header (spans full width) ────────────────────────
        item(span = { GridItemSpan(2) }) {
            DashboardHeader(
                title = successState.title,
                date = successState.date
            )
        }

        // ── Widget items ─────────────────────────────────────
        itemsIndexed(
            items = dashboardWidgets,
            span = { _, config -> GridItemSpan(config.size.span) }
        ) { index, config ->
            val widgetModifier = Modifier
                .fillMaxWidth()
                .height(config.heightDp)

            when (config.type) {
                // ── Small widgets (real UI) ───────────────
                WidgetType.STREAK -> StreakWidget(
                    streakDays = 5,
                    modifier = widgetModifier
                )
                WidgetType.AVG_TIME -> AvgTimeWidget(
                    minutes = 62,
                    trendDiffMinutes = -3,
                    modifier = widgetModifier
                )
                WidgetType.ONE_RM -> {
                    if (index == 6) {
                        OneRmWidget(
                            exercise = "Bench Press",
                            weight = 105.0,
                            isNewPr = true,
                            weightIncrease = 2.5,
                            modifier = widgetModifier
                        )
                    } else {
                        OneRmWidget(
                            exercise = "Squat",
                            weight = 140.0,
                            isNewPr = false,
                            weightIncrease = 0.0,
                            modifier = widgetModifier
                        )
                    }
                }

                // ── Thin widgets (real UI) ────────────────
                WidgetType.WEIGHT_TREND -> WeightTrendWidget(
                    currentWeight = 82.5,
                    trendLabel = "-0.5 kg this week",
                    isPositive = true,
                    modifier = widgetModifier
                )
                WidgetType.RECOVERY_TIME -> RecoveryTimeWidget(
                    hours = 48,
                    modifier = widgetModifier
                )
                WidgetType.LAST_SESSION -> LastSessionWidget(
                    routineName = "Pull Day",
                    timeLabel = "Yesterday",
                    modifier = widgetModifier
                )

                // ── Large widgets (real UI) ───────────────
                WidgetType.NEXT_WORKOUT -> NextWorkoutWidget(
                    routineName = "Pull Day",
                    modifier = widgetModifier
                )
                WidgetType.CALENDAR -> CalendarWidget(
                    trainingDays = listOf(1, 3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26, 29),
                    modifier = widgetModifier
                )
                WidgetType.RECENT_PRS -> RecentPrsWidget(
                    prs = listOf(
                        RecentPrMock("Bench Press", "105 kg", "+2.5 kg", "2 days ago"),
                        RecentPrMock("Squat", "140 kg", "+5 kg", "5 days ago"),
                        RecentPrMock("Deadlift", "180 kg", "+2.5 kg", "1 week ago")
                    ),
                    modifier = widgetModifier
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Header (preserved from original)
// ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(title: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date,
                color = LightGrey,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ShadowGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = LightGrey
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Bottom Navigation
// ──────────────────────────────────────────────────────────────

@Composable
private fun KaizenBottomNavigation(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        Pair("Dashboard", Icons.Default.Home),
        Pair("Workouts", Icons.Default.FitnessCenter),
        Pair("Statistics", Icons.Default.BarChart),
        Pair("Settings", Icons.Default.Settings)
    )

    NavigationBar(containerColor = Onyx, tonalElevation = 0.dp) {
        items.forEachIndexed { index, pair ->
            val selected = selectedTabIndex == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = { Icon(imageVector = pair.second, contentDescription = pair.first) },
                label = { Text(text = pair.first, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Onyx,
                    unselectedIconColor = LightGrey,
                    selectedTextColor = CrayolaBlue,
                    unselectedTextColor = LightGrey,
                    indicatorColor = CrayolaBlue
                )
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme { DashboardScreen() }
}
