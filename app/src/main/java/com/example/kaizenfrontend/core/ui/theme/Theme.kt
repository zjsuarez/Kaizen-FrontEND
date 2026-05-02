package com.example.kaizenfrontend.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ──────────────────────────────────────────────────────────────
// KaizenColorScheme — the single Material 3 mapping for the app.
//
// Kaizen is a dark-only product. There is no light scheme and no
// dynamicColor; every default Compose component (dialog, switch,
// snackbar, ripple, default Button) inherits brand chrome.
//
// Mapping rationale:
//   • primary           = CrayolaBlue (brand action)
//   • secondary         = MalachiteGreen (positive state, "fresh", recovery-ready)
//   • tertiary          = PrGold (achievement, PR highlights)
//   • error             = SubtleRed (destructive, fatigue, validation)
//   • surface stack     = Onyx → KaizenSurfaceLow → ShadowGrey →
//                          KaizenSurfaceHigh → KaizenSurfaceHighest
//                          (forms M3 surfaceContainer* ramp)
//   • outline           = visible 1dp borders, focus rings
//   • outlineVariant    = subtle dividers
// ──────────────────────────────────────────────────────────────

private val KaizenColorScheme = darkColorScheme(
    primary = CrayolaBlue,
    onPrimary = KaizenOnPrimary,
    primaryContainer = CrayolaBlue.copy(alpha = 0.18f).compositeOnto(Onyx),
    onPrimaryContainer = CrayolaBlue,
    inversePrimary = CrayolaBlue,

    secondary = MalachiteGreen,
    onSecondary = KaizenOnAccent,
    secondaryContainer = MalachiteGreen.copy(alpha = 0.16f).compositeOnto(Onyx),
    onSecondaryContainer = MalachiteGreen,

    tertiary = PrGold,
    onTertiary = KaizenOnAccent,
    tertiaryContainer = PrGold.copy(alpha = 0.16f).compositeOnto(Onyx),
    onTertiaryContainer = PrGold,

    error = SubtleRed,
    onError = KaizenOnAccent,
    errorContainer = SubtleRed.copy(alpha = 0.18f).compositeOnto(Onyx),
    onErrorContainer = SubtleRed,

    background = Onyx,
    onBackground = PureWhite,

    surface = Onyx,
    onSurface = PureWhite,
    surfaceVariant = KaizenSurface,
    onSurfaceVariant = LightGrey,
    surfaceTint = CrayolaBlue,
    inverseSurface = PureWhite,
    inverseOnSurface = Onyx,

    surfaceContainerLowest = Onyx,
    surfaceContainerLow = KaizenSurfaceLow,
    surfaceContainer = KaizenSurface,
    surfaceContainerHigh = KaizenSurfaceHigh,
    surfaceContainerHighest = KaizenSurfaceHighest,

    surfaceBright = KaizenSurfaceHighest,
    surfaceDim = Onyx,

    outline = KaizenOutline,
    outlineVariant = KaizenOutlineVariant,

    scrim = androidx.compose.ui.graphics.Color.Black
)

@Composable
fun KaizenFrontEndTheme(
    content: @Composable () -> Unit
) {
    // System bars: MainActivity calls enableEdgeToEdge() so the bars
    // are transparent. We only need to tell the system to render
    // light icons (since the app surface is always dark).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = KaizenColorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Internal helpers.
// ──────────────────────────────────────────────────────────────

/**
 * Composites a translucent color over an opaque base. M3 token
 * containers ([primaryContainer] etc.) need an opaque value; mixing
 * a 16-18% brand alpha over Onyx gives a perceptually muted accent
 * that reads against pure black without relying on `surfaceTint`.
 */
private fun androidx.compose.ui.graphics.Color.compositeOnto(
    base: androidx.compose.ui.graphics.Color
): androidx.compose.ui.graphics.Color {
    val a = alpha
    val r = red * a + base.red * (1 - a)
    val g = green * a + base.green * (1 - a)
    val b = blue * a + base.blue * (1 - a)
    return androidx.compose.ui.graphics.Color(r, g, b, 1f)
}
