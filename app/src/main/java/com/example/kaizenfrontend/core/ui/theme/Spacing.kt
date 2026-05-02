package com.example.kaizenfrontend.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Kaizen spacing scale.
 *
 * Every screen-level layout consumes these tokens — never hardcoded `.dp`
 * literals. The 8dp rhythm matches Material 3 guidance; xxs (4dp) is
 * reserved for icon-text tightening only.
 *
 * Typical usage:
 *  • inline gap between adjacent labels       → xs (8)
 *  • content padding inside a card            → md (16)
 *  • screen-edge horizontal padding           → lg (24)
 *  • vertical breathing between sections      → xl (32)
 *  • hero spacing on first-impression screens → xxl (48)
 */
data class Spacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
