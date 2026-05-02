package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Kaizen button system. Wraps Material 3 button primitives so we get
 * state layers and ripples for free, while standardizing radius,
 * height, label typography, and an optional leading icon + loading state.
 *
 * Variants:
 *  • [KaizenButton.Primary]      — filled brand action (one per screen)
 *  • [KaizenButton.Secondary]    — outlined alternate action
 *  • [KaizenButton.Tonal]        — quiet neutral action on busy surfaces
 *  • [KaizenButton.Destructive]  — confirm-delete; uses error tones
 *  • [KaizenButton.TextOnly]     — link-weight action; no chrome
 *
 * Buttons honor a 56dp height by default — comfortable for one-handed
 * gym use. Pass [compact] = true for inline / sheet-action buttons.
 */
object KaizenButton {

    @Composable
    fun Primary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true,
        loading: Boolean = false,
        compact: Boolean = false
    ) {
        val scheme = MaterialTheme.colorScheme
        Button(
            onClick = onClick,
            modifier = modifier.height(if (compact) 44.dp else 56.dp),
            enabled = enabled && !loading,
            shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
                disabledContainerColor = scheme.primary.copy(alpha = 0.38f),
                disabledContentColor = scheme.onPrimary.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { ButtonInner(text = text, leadingIcon = leadingIcon, loading = loading, contentColor = scheme.onPrimary) }
    }

    @Composable
    fun Secondary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true,
        compact: Boolean = false
    ) {
        val scheme = MaterialTheme.colorScheme
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(if (compact) 44.dp else 56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onSurface),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (enabled) scheme.outline else scheme.outline.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { ButtonInner(text = text, leadingIcon = leadingIcon, loading = false, contentColor = scheme.onSurface) }
    }

    @Composable
    fun Tonal(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true,
        compact: Boolean = false
    ) {
        val scheme = MaterialTheme.colorScheme
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier.height(if (compact) 44.dp else 56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { ButtonInner(text = text, leadingIcon = leadingIcon, loading = false, contentColor = scheme.onSurface) }
    }

    @Composable
    fun Destructive(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true,
        loading: Boolean = false,
        compact: Boolean = false
    ) {
        val scheme = MaterialTheme.colorScheme
        Button(
            onClick = onClick,
            modifier = modifier.height(if (compact) 44.dp else 56.dp),
            enabled = enabled && !loading,
            shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.errorContainer,
                contentColor = scheme.error
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { ButtonInner(text = text, leadingIcon = leadingIcon, loading = loading, contentColor = scheme.error) }
    }

    @Composable
    fun TextOnly(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true,
        destructive: Boolean = false
    ) {
        val scheme = MaterialTheme.colorScheme
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (destructive) scheme.error else scheme.primary
            )
        ) { ButtonInner(text = text, leadingIcon = leadingIcon, loading = false, contentColor = if (destructive) scheme.error else scheme.primary) }
    }
}

@Composable
private fun ButtonInner(
    text: String,
    leadingIcon: ImageVector?,
    loading: Boolean,
    contentColor: androidx.compose.ui.graphics.Color
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
