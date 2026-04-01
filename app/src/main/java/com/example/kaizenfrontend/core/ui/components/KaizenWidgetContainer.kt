package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey

private val WidgetShape = RoundedCornerShape(16.dp)
private val BorderColor = ShadowGrey.copy(alpha = 0.6f)

/**
 * Reusable container for every dashboard widget.
 *
 * Provides the standard Kaizen card aesthetic:
 * dark surface background, subtle border, rounded corners.
 *
 * The caller is responsible for setting the overall size
 * (height / span) via [modifier]; this container only handles
 * the internal decoration and padding.
 */
@Composable
fun KaizenWidgetContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(WidgetShape)
            .background(ShadowGrey, WidgetShape)
            .border(width = 1.dp, color = BorderColor, shape = WidgetShape)
            .padding(16.dp)
    ) {
        content()
    }
}
