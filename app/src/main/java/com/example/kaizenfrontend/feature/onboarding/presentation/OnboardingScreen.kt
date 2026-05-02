package com.example.kaizenfrontend.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.components.KaizenButton
import com.example.kaizenfrontend.core.ui.theme.spacing

/**
 * One-screen explainer shown post-calibration to teach the
 * Plan → Routine → Exercise hierarchy. Resolves the
 * "7-8 cognitive jumps for a new user to produce a workable routine"
 * gap identified in the audit.
 *
 * No ViewModel — single-shot persistence on continue.
 */
@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(MaterialTheme.spacing.lg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))

            Text(
                text = stringResource(id = R.string.onboarding_title),
                style = MaterialTheme.typography.displaySmall,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = stringResource(id = R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xxl))

            HierarchyStep(
                index = 1,
                icon = Icons.Outlined.Bookmark,
                title = stringResource(id = R.string.onboarding_plan_title),
                body = stringResource(id = R.string.onboarding_plan_body)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
            HierarchyStep(
                index = 2,
                icon = Icons.Outlined.Layers,
                title = stringResource(id = R.string.onboarding_routine_title),
                body = stringResource(id = R.string.onboarding_routine_body)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
            HierarchyStep(
                index = 3,
                icon = Icons.Outlined.FitnessCenter,
                title = stringResource(id = R.string.onboarding_exercise_title),
                body = stringResource(id = R.string.onboarding_exercise_body)
            )

            Spacer(modifier = Modifier.weight(1f))

            KaizenButton.Primary(
                text = stringResource(id = R.string.onboarding_continue),
                onClick = {
                    sessionManager.saveOnboardingCompleted(true)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
        }
    }
}

@Composable
private fun HierarchyStep(
    index: Int,
    icon: ImageVector,
    title: String,
    body: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(scheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "$index. $title",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
