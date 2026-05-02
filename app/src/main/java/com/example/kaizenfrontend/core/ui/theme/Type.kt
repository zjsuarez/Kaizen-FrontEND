package com.example.kaizenfrontend.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// ──────────────────────────────────────────────────────────────
// Type families.
// Phase 1 ships with FontFamily.Default (system Roboto) — visual
// rhythm comes from the populated scale and tabular figures, not
// the typeface. A follow-up commit swaps in Inter via Google Fonts
// once the cert provider is validated end-to-end.
// ──────────────────────────────────────────────────────────────

private val DisplayFamily = FontFamily.Default
private val TextFamily = FontFamily.Default

/**
 * Numeric figures that occupy equal horizontal space. Required for
 * weight/duration/PR columns where decimal alignment matters.
 */
val KaizenNumericFamily = FontFamily.Monospace

// ──────────────────────────────────────────────────────────────
// Material 3 type scale — every token populated.
// Screens must reference MaterialTheme.typography.*; never inline
// Text(fontSize = X.sp).
// ──────────────────────────────────────────────────────────────

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 6.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.6.sp
    )
)

// ──────────────────────────────────────────────────────────────
// Kaizen numeric variants.
// Use these for any column or row of numbers (weights, reps,
// durations, PR values, set counts, percentages). Tabular monospace
// keeps decimal points aligned across rows.
// ──────────────────────────────────────────────────────────────

object KaizenTypography {
    val numericDisplay = TextStyle(
        fontFamily = KaizenNumericFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    )

    val numericLarge = TextStyle(
        fontFamily = KaizenNumericFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    val numericMedium = TextStyle(
        fontFamily = KaizenNumericFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    val numericSmall = TextStyle(
        fontFamily = KaizenNumericFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    /**
     * Uppercase, letter-spaced micro-label used for section headers
     * ("THIS WEEK", "STRENGTH & HEALTH"). Standardizes the variants
     * scattered through the existing screens (Statistics 11sp/1.2sp,
     * Workouts 11sp/1.8sp, Calibration 12sp/normal).
     */
    val sectionLabel = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.6.sp
    )
}
