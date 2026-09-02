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
import androidx.compose.ui.graphics.luminance
import com.example.data.model.ThemePalette

/**
 * Semantic tokens extending Material3's [androidx.compose.material3.ColorScheme].
 * Holds NeoPOP-only roles — extrusion shadows, glows, shimmer — that Material
 * doesn't model on its own.
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

    // NeoPOP extrusion — the "slab" a card/button sits on, and the thin
    // highlight sliver traced along its top edge. Flat, no blur, offset only.
    val extrusionShadow: Color,
    val extrusionHighlight: Color,

    // Meta
    val isDark: Boolean
) {
    /** Vertical charcoal fade for hero surfaces. */
    val heroGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(bentoCardElevated, bentoCardBg)
        )

    /** Brand lime → cyan sweep — used sparingly, NeoPOP favors flat fills. */
    val brandMintBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(GradientMintStart, GradientMintEnd)
        )

    /** Violet → magenta statement sweep. */
    val brandIndigoBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(GradientIndigoStart, GradientIndigoEnd)
        )

    /** Yellow → magenta premium sweep. */
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
        extrusionShadow    = NeoPopShadowDark,
        extrusionHighlight = NeoPopHighlightDark,
        isDark             = true
    )
}

/** Access custom NeoPOP tokens anywhere a [MaterialTheme] is in scope. */
val MaterialTheme.customColors: CustomThemeColors
    @Composable @ReadOnlyComposable get() = LocalCustomColors.current

/**
 * Returns pure black or pure white — whichever reads legibly on top of
 * this color. NeoPOP fills (lime, yellow, cyan, magenta, violet) vary a lot
 * in brightness, so foreground text/icon color must be computed per-fill
 * rather than hardcoded — this is what prevents "white text on yellow"
 * style contrast failures.
 */
fun Color.neoPopOnColor(): Color =
    if (this.luminance() > 0.5f) NeoPopPureBlack else NeoPopPureWhite

// ──────────────────────────────────────────────────────────────
// Color scheme factories
// ──────────────────────────────────────────────────────────────
fun createDarkColorScheme(palette: ThemePalette): androidx.compose.material3.ColorScheme {
    val primaryColor = Color(palette.primaryHex)
    val accentColor  = Color(palette.accentHex)
    return darkColorScheme(
        primary            = primaryColor,
        onPrimary          = NeoPopPureBlack,
        primaryContainer   = ObsidianElevated,
        onPrimaryContainer = primaryColor,
        secondary          = accentColor,
        onSecondary        = NeoPopPureBlack,
        secondaryContainer = ObsidianSurface,
        onSecondaryContainer = TextPrimaryDark,
        tertiary           = CyanAccent,
        onTertiary         = NeoPopPureBlack,
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
        onError            = NeoPopPureWhite,
        errorContainer     = ExpenseRoseDeep,
        onErrorContainer   = NeoPopPureWhite,
        scrim              = ObsidianScrim
    )
}

fun createLightColorScheme(palette: ThemePalette): androidx.compose.material3.ColorScheme {
    val primaryColor = Color(palette.primaryHex)
    val accentColor  = Color(palette.accentHex)
    return lightColorScheme(
        primary            = primaryColor,
        onPrimary          = NeoPopPureBlack,
        primaryContainer   = LightElevated,
        onPrimaryContainer = primaryColor,
        secondary          = accentColor,
        onSecondary        = NeoPopPureBlack,
        secondaryContainer = LightElevated,
        onSecondaryContainer = TextPrimaryLight,
        tertiary           = CyanAccent,
        onTertiary         = NeoPopPureBlack,
        background         = LightBg,
        onBackground       = TextPrimaryLight,
        surface            = LightSurface,
        onSurface          = TextPrimaryLight,
        surfaceVariant     = LightElevated,
        onSurfaceVariant   = TextSecondaryLight,
        surfaceTint        = primaryColor,
        inverseSurface     = TextPrimaryLight,
        inverseOnSurface   = NeoPopPureWhite,
        outline            = LightBorder,
        outlineVariant     = LightBorderSubtle,
        error              = ExpenseRoseDeep,
        onError            = NeoPopPureWhite,
        errorContainer     = CredRoseSoft,
        onErrorContainer   = NeoPopPureBlack,
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
            glowColor          = primaryHex.copy(alpha = 0.22f),
            glowColorStrong    = primaryHex.copy(alpha = 0.5f),
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
            extrusionShadow    = NeoPopShadowDark,
            extrusionHighlight = NeoPopHighlightDark,
            isDark             = true
        )
    } else {
        CustomThemeColors(
            bentoCardBg        = LightSurface,
            bentoCardElevated  = LightElevated,
            bentoBorder        = LightBorder,
            bentoBorderSubtle  = LightBorderSubtle,
            bentoBorderStrong  = LightBorderStrong,
            glowColor          = primaryHex.copy(alpha = 0.14f),
            glowColorStrong    = primaryHex.copy(alpha = 0.32f),
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
            extrusionShadow    = NeoPopShadowLight,
            extrusionHighlight = NeoPopHighlightLight,
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
