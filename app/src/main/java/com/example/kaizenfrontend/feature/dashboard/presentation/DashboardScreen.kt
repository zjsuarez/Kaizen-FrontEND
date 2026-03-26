package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.statistics.presentation.StatisticsScreen
import com.example.kaizenfrontend.feature.user.presentation.settings.SettingsScreen
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutsScreen

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val title: String, val date: String, val workoutPlan: String) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

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
                        DashboardContent(
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

@Composable
private fun DashboardContent(
    successState: DashboardUiState.Success,
    onWorkoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = successState.title,
                    color = PureWhite,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = successState.date,
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

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWorkoutClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ShadowGrey)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Start today's workout", color = LightGrey, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = successState.workoutPlan,
                    color = PureWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme { DashboardScreen() }
}
