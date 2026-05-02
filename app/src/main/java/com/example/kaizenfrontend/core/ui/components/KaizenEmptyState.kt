package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.core.ui.theme.spacing

/**
 * Standard "nothing here yet" state. Replaces the four divergent
 * empty-state implementations across Workouts (`WorkoutsEmptyState`),
 * Dashboard (`DashboardEmptyState`), exercise catalog, and history sheet.
 *
 * If [actionText]/[onAction] is provided, a primary CTA is rendered
 * below the description.
 */
@Composable
fun KaizenEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(scheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
            KaizenButton.Primary(
                text = actionText,
                onClick = onAction
            )
        }
    }
}
