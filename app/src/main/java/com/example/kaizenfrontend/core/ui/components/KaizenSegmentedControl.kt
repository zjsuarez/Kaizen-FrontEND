package com.example.kaizenfrontend.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single-select segmented control. Replaces the four ad-hoc segmented
 * controls in the codebase (calibration KG/LB and RIR/RPE/NONE,
 * settings unit toggle, statistics time-range selector).
 *
 * Equal-width segments. Selected segment fills with brand primary;
 * unselected segments use the surrounding card surface so the control
 * works inside a card *or* on the page background.
 */
@Composable
fun <T : Any> KaizenSegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    height: Dp = 48.dp,
    cornerRadius: Dp = 12.dp
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(scheme.surfaceContainerLow),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) scheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                label = "segment_bg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
                label = "segment_fg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(cornerRadius - 2.dp))
                    .background(containerColor)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}
