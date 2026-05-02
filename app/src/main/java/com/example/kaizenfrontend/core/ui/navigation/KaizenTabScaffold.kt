package com.example.kaizenfrontend.core.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.kaizenfrontend.core.ui.components.KaizenScreenHeader

/**
 * Standard chrome for the four primary tab destinations
 * (Dashboard / Workouts / Statistics / Settings).
 *
 * Provides:
 *  • A consistent KaizenScreenHeader (replaces the four divergent
 *    header treatments in the legacy code)
 *  • The KaizenBottomBar tied to [navController]
 *  • Brand surface background
 *
 * The screen content composable receives the inner [PaddingValues]
 * so it can route them into LazyColumn `contentPadding` and avoid
 * being clipped by the bottom bar.
 */
@Composable
fun KaizenTabScaffold(
    navController: NavController,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            KaizenScreenHeader(
                title = title,
                subtitle = subtitle,
                actions = headerActions
            )
        },
        bottomBar = { KaizenBottomBar(navController) },
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = { padding -> content(padding) }
    )
}
