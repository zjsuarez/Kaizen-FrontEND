package com.example.kaizenfrontend.core.ui.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────
// Kaizen brand palette — single source of truth.
// ──────────────────────────────────────────────────────────────
//
// Five canonical tokens drive every screen:
//   • Onyx          — the OLED-friendly base
//   • ShadowGrey    — primary raised surface
//   • CrayolaBlue   — brand primary, used only for actionable elements
//   • PureWhite     — primary text on dark surfaces
//   • LightGrey     — secondary text and inactive UI
//
// Three semantic accents complete the system:
//   • MalachiteGreen — success / "fresh" / completed
//   • PrGold         — achievement / personal record
//   • SubtleRed      — destructive / error / fatigue
//
// Anything beyond these tokens is a derivation expressed below as a
// neutral-ramp surface (KaizenSurface*) or M3 outline tone. Screens
// must never hardcode hex literals — consume MaterialTheme.colorScheme
// or the named tokens here.

val Onyx = Color(0xFF0B0A0F)
val ShadowGrey = Color(0xFF242328)
val CrayolaBlue = Color(0xFF2979FF)
val PureWhite = Color.White
val LightGrey = Color(0xFFAAAAAA)
val SubtleRed = Color(0xFFCF6679)
val PrGold = Color(0xFFFFD740)
val MalachiteGreen = Color(0xFF00E676)

// ──────────────────────────────────────────────────────────────
// Neutral elevation ramp.
// Used to populate Material 3's surfaceContainer* tokens so default
// components (dialogs, menus, sheets, switches) inherit brand chrome.
// ──────────────────────────────────────────────────────────────

val KaizenSurfaceLowest = Onyx                       // page background
val KaizenSurfaceLow = Color(0xFF131217)             // subtle raise (e.g. divider blocks)
val KaizenSurface = ShadowGrey                       // standard card / sheet
val KaizenSurfaceHigh = Color(0xFF2E2D33)            // raised over a card (chips on cards)
val KaizenSurfaceHighest = Color(0xFF38373D)         // dialog interior, tooltip
val KaizenOutline = Color(0xFF3A3940)                // visible borders (focus rings, segmented dividers)
val KaizenOutlineVariant = Color(0xFF1F1E23)         // subtle 1dp dividers
val KaizenOnPrimary = Onyx                           // text/icon on CrayolaBlue
val KaizenOnAccent = Onyx                            // text/icon on Gold/Green/Red

// ──────────────────────────────────────────────────────────────
// Deprecated — auth-flow palette.
// Phase 3 migrates LoginScreen/SignUpScreen/CustomTextField call sites
// to MaterialTheme.colorScheme; until then these resolve to the
// canonical tokens so the visual identity unifies immediately.
// ──────────────────────────────────────────────────────────────

@Deprecated(
    message = "Use Onyx (or MaterialTheme.colorScheme.background)",
    replaceWith = ReplaceWith("Onyx", "com.example.kaizenfrontend.core.ui.theme.Onyx")
)
val DarkBackground = Onyx

@Deprecated(
    message = "Use ShadowGrey (or MaterialTheme.colorScheme.surface)",
    replaceWith = ReplaceWith("ShadowGrey", "com.example.kaizenfrontend.core.ui.theme.ShadowGrey")
)
val InputFieldColor = ShadowGrey

@Deprecated(
    message = "Use LightGrey (or MaterialTheme.colorScheme.onSurfaceVariant)",
    replaceWith = ReplaceWith("LightGrey", "com.example.kaizenfrontend.core.ui.theme.LightGrey")
)
val LightGrayText = LightGrey

// ──────────────────────────────────────────────────────────────
// Deprecated — calibration-flow palette.
// CalibrationScreen migrates to KaizenSegmentedControl + the canonical
// tokens in Phase 3.
// ──────────────────────────────────────────────────────────────

@Deprecated(
    message = "Use KaizenSurface (ShadowGrey)",
    replaceWith = ReplaceWith("KaizenSurface", "com.example.kaizenfrontend.core.ui.theme.KaizenSurface")
)
val SurfaceColor = ShadowGrey

@Deprecated(
    message = "Use KaizenSurfaceLow",
    replaceWith = ReplaceWith("KaizenSurfaceLow", "com.example.kaizenfrontend.core.ui.theme.KaizenSurfaceLow")
)
val SurfaceDarker = KaizenSurfaceLow

@Deprecated(
    message = "Use LightGrey",
    replaceWith = ReplaceWith("LightGrey", "com.example.kaizenfrontend.core.ui.theme.LightGrey")
)
val TextGray = LightGrey

@Deprecated(
    message = "Use LightGrey.copy(alpha = 0.7f) or define a semantic token",
    replaceWith = ReplaceWith("LightGrey", "com.example.kaizenfrontend.core.ui.theme.LightGrey")
)
val LabelGray = Color(0xFF808090)
