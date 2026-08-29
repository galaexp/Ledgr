package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.example.data.model.ThemePalette

/**
 * Semantic tokens extending Material3's [androidx.compose.material3.ColorScheme].
 * Holds luxury-only roles — bento, glow, shimmer, gradients — that Material doesn't model.
 */
data class CustomThemeColors(
    // Bento & cards
    val bentoCardBg: Color,
    val bentoCardElevated: Color,
    val bentoBorder: Color,
    val bentoBorderSubtle: Color,
    val bentoBorderStrong: Color,

    // Glows & auras
    val glowColor: Color,
    val glowColorStrong: Color,

    // Text
    val textPrimary: Color,
    val textMuted: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // Status (financial indicators)
    val success: Color,
    val successDeep: Color,
    val danger: Color,
    val dangerDeep: Color,
    val warning: Color,
    val warningDeep: Color,
    val info: Color,

    // Shimmer
    val shimmerBase: Color,
    val shimmerHighlight: Color,

    // Meta
    val isDark: Boolean
) {
    /** Vertical obsidian fade for hero surfaces. */
    val heroGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(bentoCardElevated, bentoCardBg)
        )

    /** Brand mint → cyan sweep. */
    val brandMintBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(GradientMintStart, GradientMintEnd)
        )

    /** Indigo → rose statement sweep. */
    val brandIndigoBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(GradientIndigoStart, GradientIndigoEnd)
        )

    /** Gold → rose premium sweep. */
    val brandGoldBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(GradientGoldStart, GradientGoldEnd)
        )

    /** Animated shimmer sweep (clamp-tiled). */
    fun shimmerBrush(progress: Float = 0f): Brush = Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = androidx.compose.ui.geometry.Offset(progress, 0f),
        end = androidx.compose.ui.geometry.Offset(progress + 600f, 0f),
        tileMode = TileMode.Clamp
    )
}

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        bentoCardBg        = ObsidianSurface,
        bentoCardElevated  = ObsidianElevated,
        bentoBorder        = ObsidianBorder,
        bentoBorderSubtle  = ObsidianBorderSubtle,
        bentoBorderStrong  = ObsidianBorderStrong,
        glowColor          = EmeraldGlow,
        glowColorStrong    = EmeraldGlowStrong,
        textPrimary        = TextPrimaryDark,
        textMuted          = TextSecondaryDark,
        textTertiary       = TextTertiaryDark,
        textDisabled       = TextDisabledDark,
        success            = IncomeGreen,
        successDeep        = IncomeGreenDeep,
        danger             = ExpenseRose,
        dangerDeep         = ExpenseRoseDeep,
        warning            = WarningAmber,
        warningDeep        = WarningAmberDeep,
        info               = InfoIndigo,
        shimmerBase        = ShimmerBaseDark,
        shimmerHighlight   = ShimmerHighlightDark,
        isDark             = true
    )
}

/** Access custom luxury tokens anywhere a [MaterialTheme] is in scope. */
val MaterialTheme.customColors: CustomThemeColors
    @Composable @ReadOnlyComposable get() = LocalCustomColors.current

// ──────────────────────────────────────────────────────────────
// Color scheme factories
// ──────────────────────────────────────────────────────────────
fun createDarkColorScheme(palette: ThemePalette): androidx.compose.material3.ColorScheme {
    val primaryColor = Color(palette.primaryHex)
    val accentColor  = Color(palette.accentHex)
    return darkColorScheme(
        primary            = primaryColor,
        onPrimary          = ObsidianBg,
        primaryContainer   = ObsidianElevated,
        onPrimaryContainer = primaryColor,
        secondary          = accentColor,
        onSecondary        = ObsidianBg,
        secondaryContainer = ObsidianSurface,
        onSecondaryContainer = TextPrimaryDark,
        tertiary           = CyanAccent,
        onTertiary         = ObsidianBg,
        background         = ObsidianBg,
        onBackground       = TextPrimaryDark,
        surface            = ObsidianSurface,
        onSurface          = TextPrimaryDark,
        surfaceVariant     = ObsidianElevated,
        onSurfaceVariant   = TextSecondaryDark,
        surfaceTint        = primaryColor,
        inverseSurface     = ObsidianHigh,
        inverseOnSurface   = TextPrimaryDark,
        outline            = ObsidianBorder,
        outlineVariant     = ObsidianBorderSubtle,
        error              = ExpenseRose,
        onError            = Color.White,
        errorContainer     = ExpenseRoseDeep,
        onErrorContainer   = Color.White,
        scrim              = ObsidianScrim
    )
}

fun createLightColorScheme(palette: ThemePalette): androidx.compose.material3.ColorScheme {
    val primaryColor = Color(palette.primaryHex)
    val accentColor  = Color(palette.accentHex)
    return lightColorScheme(
        primary            = primaryColor,
        onPrimary          = Color.White,
        primaryContainer   = LightElevated,
        onPrimaryContainer = primaryColor,
        secondary          = accentColor,
        onSecondary        = Color.White,
        secondaryContainer = LightElevated,
        onSecondaryContainer = TextPrimaryLight,
        tertiary           = CyanAccent,
        onTertiary         = Color.White,
        background         = LightBg,
        onBackground       = TextPrimaryLight,
        surface            = LightSurface,
        onSurface          = TextPrimaryLight,
        surfaceVariant     = LightElevated,
        onSurfaceVariant   = TextSecondaryLight,
        surfaceTint        = primaryColor,
        inverseSurface     = TextPrimaryLight,
        inverseOnSurface   = Color.White,
        outline            = LightBorder,
        outlineVariant     = LightBorderSubtle,
        error              = ExpenseRose,
        onError           = Color.White,
        errorContainer     = CredRoseSoft,
        onErrorContainer   = Color.White,
        scrim              = LightScrim
    )
}

// ──────────────────────────────────────────────────────────────
// Theme entry point
// ──────────────────────────────────────────────────────────────
@Composable
fun LedgrTheme(
    palette: ThemePalette = ThemePalette.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) createDarkColorScheme(palette)
                      else createLightColorScheme(palette)

    val primaryHex = Color(palette.primaryHex)
    val customColors = if (darkTheme) {
        CustomThemeColors(
            bentoCardBg        = ObsidianSurface,
            bentoCardElevated  = ObsidianElevated,
            bentoBorder        = ObsidianBorder,
            bentoBorderSubtle  = ObsidianBorderSubtle,
            bentoBorderStrong  = ObsidianBorderStrong,
            glowColor          = primaryHex.copy(alpha = 0.20f),
            glowColorStrong    = primaryHex.copy(alpha = 0.45f),
            textPrimary        = TextPrimaryDark,
            textMuted          = TextSecondaryDark,
            textTertiary       = TextTertiaryDark,
            textDisabled       = TextDisabledDark,
            success            = IncomeGreen,
            successDeep        = IncomeGreenDeep,
            danger             = ExpenseRose,
            dangerDeep         = ExpenseRoseDeep,
            warning            = WarningAmber,
            warningDeep        = WarningAmberDeep,
            info               = InfoIndigo,
            shimmerBase        = ShimmerBaseDark,
            shimmerHighlight   = ShimmerHighlightDark,
            isDark             = true
        )
    } else {
        CustomThemeColors(
            bentoCardBg        = LightSurface,
            bentoCardElevated  = LightElevated,
            bentoBorder        = LightBorder,
            bentoBorderSubtle  = LightBorderSubtle,
            bentoBorderStrong  = LightBorderStrong,
            glowColor          = primaryHex.copy(alpha = 0.12f),
            glowColorStrong    = primaryHex.copy(alpha = 0.28f),
            textPrimary        = TextPrimaryLight,
            textMuted          = TextSecondaryLight,
            textTertiary       = TextTertiaryLight,
            textDisabled       = TextDisabledLight,
            success            = IncomeGreenDeep,
            successDeep        = IncomeGreenDeep,
            danger             = ExpenseRoseDeep,
            dangerDeep         = ExpenseRoseDeep,
            warning            = WarningAmberDeep,
            warningDeep        = WarningAmberDeep,
            info               = InfoIndigoDeep,
            shimmerBase        = ShimmerBaseLight,
            shimmerHighlight   = ShimmerHighlightLight,
            isDark             = false
        )
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}