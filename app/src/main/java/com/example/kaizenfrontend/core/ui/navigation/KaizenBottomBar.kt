package com.example.kaizenfrontend.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kaizenfrontend.R

private data class TabItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
)

private val TabItems = listOf(
    TabItem(KaizenDestinations.DASHBOARD, Icons.Default.Home, R.string.dashboard_nav_dashboard),
    TabItem(KaizenDestinations.WORKOUTS, Icons.Default.FitnessCenter, R.string.dashboard_nav_workouts),
    TabItem(KaizenDestinations.STATISTICS, Icons.Default.BarChart, R.string.dashboard_nav_statistics),
    TabItem(KaizenDestinations.SETTINGS, Icons.Default.Settings, R.string.dashboard_nav_settings)
)

/**
 * Bottom navigation bar shown on the four primary tab routes.
 *
 * Replaces the private `KaizenBottomNavigation` previously embedded
 * inside the legacy DashboardScreen, which used a 10sp label (below
 * Material's 12sp floor) and an integer-index selectedTab state.
 *
 * Tab switching uses `popUpTo(startDestination)` + `saveState/restoreState`
 * so each tab keeps its own back stack and scroll/list state across switches.
 */
@Composable
fun KaizenBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val scheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = scheme.surface,
        tonalElevation = 0.dp
    ) {
        TabItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        // Use DASHBOARD as the tab-root anchor so each tab
                        // switch unwinds to a single shared root and saves the
                        // outgoing tab's state. Anchoring to graph.startDestination
                        // (splash) would not work — splash is no longer on the
                        // stack once the user has authenticated.
                        navController.navigate(item.route) {
                            popUpTo(KaizenDestinations.DASHBOARD) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = scheme.onPrimary,
                    selectedTextColor = scheme.primary,
                    unselectedIconColor = scheme.onSurfaceVariant,
                    unselectedTextColor = scheme.onSurfaceVariant,
                    indicatorColor = scheme.primary
                )
            )
        }
    }
}
