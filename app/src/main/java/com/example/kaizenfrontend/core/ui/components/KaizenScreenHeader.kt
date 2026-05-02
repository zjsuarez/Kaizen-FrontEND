package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.core.ui.theme.spacing

/**
 * The single header treatment used by every primary screen
 * (Dashboard / Workouts / Statistics / Settings). Replaces the four
 * divergent implementations:
 *  • Dashboard's 38sp three-line `TopAppBar` + greeting + date block
 *  • Workouts' custom 38sp `Column` with 48dp top padding
 *  • Statistics' 24sp `TopAppBar` (smaller than every other screen)
 *  • Settings' 38sp inline `Text` row with no app bar at all
 *
 * `subtitle` is optional; `actions` slots arbitrary trailing icon buttons.
 *
 * Sized for dense screen content: 32dp top padding (status bar inset
 * is provided by the host `Scaffold`/`enableEdgeToEdge`), 24dp
 * horizontal margins.
 */
@Composable
fun KaizenScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.spacing.lg,
                end = MaterialTheme.spacing.md,
                top = MaterialTheme.spacing.xl,
                bottom = MaterialTheme.spacing.md
            ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            content = actions
        )
    }
}
