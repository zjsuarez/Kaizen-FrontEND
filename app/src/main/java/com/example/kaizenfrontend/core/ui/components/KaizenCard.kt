package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.core.ui.theme.spacing

/**
 * Visual variants for a Kaizen card.
 *
 *  • [Tonal]     — default: filled `surfaceContainer`, no border. Standard widgets.
 *  • [Outlined]  — transparent fill, 1dp `outlineVariant` border. Lower visual
 *                   weight; use for read-only metric strips inside busy screens.
 *  • [Elevated]  — same fill as Tonal but on `surfaceContainerHigh` for chips
 *                   that need to read above another card (e.g. PR rows).
 */
enum class KaizenCardStyle { Tonal, Outlined, Elevated }

/**
 * Kaizen card chrome. Replaces the legacy `KaizenWidgetContainer` and
 * the ad-hoc `Card` / `Surface` calls scattered through the screens.
 *
 * Caller controls width/height via [modifier]; this composable handles
 * radius, fill, border, padding, and (optional) tap behavior.
 */
@Composable
fun KaizenCard(
    modifier: Modifier = Modifier,
    style: KaizenCardStyle = KaizenCardStyle.Tonal,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.md),
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape: Shape = RoundedCornerShape(cornerRadius)

    val container = when (style) {
        KaizenCardStyle.Tonal -> scheme.surfaceContainer
        KaizenCardStyle.Outlined -> Color.Transparent
        KaizenCardStyle.Elevated -> scheme.surfaceContainerHigh
    }
    val borderStroke: BorderStroke? = when (style) {
        KaizenCardStyle.Outlined -> BorderStroke(1.dp, scheme.outlineVariant)
        else -> null
    }

    val composedModifier = modifier
        .clip(shape)
        .background(container, shape)
        .let { base -> if (borderStroke != null) base.border(borderStroke, shape) else base }
        .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
        .padding(contentPadding)

    Box(modifier = composedModifier) { content() }
}
