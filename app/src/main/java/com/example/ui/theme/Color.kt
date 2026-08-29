package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * LEDGR — NeoPOP Design System
 *
 * High-contrast fusion of Neomorphism + Pop Art: near-black charcoal fields,
 * sharp unrounded-feeling geometry, saturated neon accents, and tactile
 * "extruded" surfaces that visibly press down on tap.
 *
 * Token names are preserved from the previous palette so every screen,
 * component and dialog that already imports them keeps compiling —
 * only the values (and a few additive NeoPOP-only tokens at the bottom)
 * have changed.
 */

// ──────────────────────────────────────────────────────────────
// 1. Charcoal Surfaces — Base + extrusion stack
// ──────────────────────────────────────────────────────────────
val ObsidianBg            = Color(0xFF0A0A0C) // True near-black stage
val ObsidianSurface       = Color(0xFF141417) // Base extruded surface
val ObsidianElevated      = Color(0xFF1C1C21) // Raised card face
val ObsidianHigh          = Color(0xFF242429) // Highest tier / pressed highlight
val ObsidianBorder        = Color(0xFF2E2E35) // Crisp hairline edge
val ObsidianBorderSubtle  = Color(0xFF1E1E23) // Subtle divider
val ObsidianBorderStrong  = Color(0xFF38383F) // Emphasized edge
val ObsidianCardGlow      = Color(0xFF1F1F26) // Radial highlight
val ObsidianScrim         = Color(0xB3000000) // Modal scrim

// ──────────────────────────────────────────────────────────────
// 2. Light Surfaces — Paper-white NeoPOP counterpart
// ──────────────────────────────────────────────────────────────
val LightBg            = Color(0xFFF4F3EF)
val LightSurface       = Color(0xFFFFFFFF)
val LightElevated      = Color(0xFFEDEBE4)
val LightHigh          = Color(0xFFE2E0D8)
val LightBorder        = Color(0xFF111111)
val LightBorderSubtle  = Color(0xFFD9D7CE)
val LightBorderStrong  = Color(0xFF000000)
val LightScrim         = Color(0x4D000000)

// ──────────────────────────────────────────────────────────────
// 3. Pop Accents — Signature NeoPOP neons
// ──────────────────────────────────────────────────────────────
// Acid Lime (primary brand punch)
val CredMint             = Color(0xFFC6FF3D)
val CredMintSoft         = Color(0xFFDCFF8C)
val CredMintDeep         = Color(0xFF8FCC00)
val CredMintGlow         = Color(0x40C6FF3D)
val CredMintGlowStrong   = Color(0x80C6FF3D)

// Electric Violet
val CredIndigo           = Color(0xFF8C5CFF)
val CredIndigoSoft       = Color(0xFFB39CFF)
val CredIndigoDeep       = Color(0xFF5A32CC)
val CredIndigoGlow       = Color(0x408C5CFF)
val CredIndigoGlowStrong = Color(0x808C5CFF)

// Pop Yellow
val CredGold             = Color(0xFFFFD400)
val CredGoldSoft         = Color(0xFFFFE666)
val CredGoldDeep         = Color(0xFFCCA800)
val CredGoldGlow         = Color(0x40FFD400)
val CredGoldGlowStrong   = Color(0x80FFD400)

// Hot Magenta
val CredRose             = Color(0xFFFF2E88)
val CredRoseSoft         = Color(0xFFFF7AB3)
val CredRoseDeep         = Color(0xFFCC1560)
val CredRoseGlow         = Color(0x40FF2E88)
val CredRoseGlowStrong   = Color(0x80FF2E88)

// Laser Cyan
val CredCyan             = Color(0xFF2EE6FF)
val CredCyanSoft         = Color(0xFF7EF2FF)
val CredCyanDeep         = Color(0xFF00B8CC)
val CredCyanGlow         = Color(0x402EE6FF)
val CredCyanGlowStrong   = Color(0x802EE6FF)

// ──────────────────────────────────────────────────────────────
// 4. Brand primary aliases (kept for backward compatibility)
// ──────────────────────────────────────────────────────────────
val EmeraldPrimary     = CredMint
val EmeraldAccent      = CredMintSoft
val EmeraldLight       = CredMintSoft
val EmeraldDeep        = CredMintDeep
val EmeraldGlow        = CredMintGlow
val EmeraldGlowStrong  = CredMintGlowStrong

// ──────────────────────────────────────────────────────────────
// 5. Text Tokens
// ──────────────────────────────────────────────────────────────
val TextPrimaryDark   = Color(0xFFF5F5F2) // Off-white ink
val TextSecondaryDark = Color(0xFFA0A0A8) // Muted steel
val TextTertiaryDark  = Color(0xFF64646C) // Faint slate
val TextDisabledDark  = Color(0xFF3E3E44) // Disabled

val TextPrimaryLight   = Color(0xFF0A0A0A)
val TextSecondaryLight = Color(0xFF45454A)
val TextTertiaryLight  = Color(0xFF7A7A80)
val TextDisabledLight  = Color(0xFFB8B8BC)

// ──────────────────────────────────────────────────────────────
// 6. Semantic / Financial Indicator Colors
// ──────────────────────────────────────────────────────────────
val IncomeGreen      = CredMint
val IncomeGreenDeep  = CredMintDeep
val ExpenseRose      = CredRose
val ExpenseRoseDeep  = CredRoseDeep
val WarningAmber     = CredGold
val WarningAmberDeep = CredGoldDeep
val InfoIndigo       = CredIndigo
val InfoIndigoDeep   = CredIndigoDeep
val CyanAccent       = CredCyan

// ──────────────────────────────────────────────────────────────
// 7. Shimmer / Skeleton Loading
// ──────────────────────────────────────────────────────────────
val ShimmerBaseDark       = ObsidianElevated
val ShimmerHighlightDark  = Color(0xFF2A2A31)
val ShimmerBaseLight      = LightElevated
val ShimmerHighlightLight = Color(0xFFFFFFFF)

// ──────────────────────────────────────────────────────────────
// 8. Gradients (used sparingly in NeoPOP — flat fills are default)
// ──────────────────────────────────────────────────────────────
val GradientMintStart      = CredMint
val GradientMintEnd        = CredCyan
val GradientIndigoStart    = CredIndigo
val GradientIndigoEnd      = CredRose
val GradientGoldStart      = CredGold
val GradientGoldEnd        = CredRoseSoft
val GradientObsidianStart  = ObsidianSurface
val GradientObsidianEnd    = ObsidianBg

// ──────────────────────────────────────────────────────────────
// 9. NeoPOP-only tokens — hard extrusion shadows & pure black/white
// ──────────────────────────────────────────────────────────────
// NeoPOP surfaces are "pressable blocks": a flat top face sitting on a
// solid-color side/shadow slab with zero blur, offset down-right.
val NeoPopPureBlack        = Color(0xFF000000)
val NeoPopPureWhite        = Color(0xFFFFFFFF)
val NeoPopShadowDark       = Color(0xFF000000) // extrusion slab, dark theme
val NeoPopShadowLight      = Color(0xFF000000) // extrusion slab, light theme (NeoPOP always uses black shadow blocks)
val NeoPopHighlightDark    = Color(0xFF303038) // top-edge highlight sliver, dark theme
val NeoPopHighlightLight   = Color(0xFFFFFFFF) // top-edge highlight sliver, light theme
