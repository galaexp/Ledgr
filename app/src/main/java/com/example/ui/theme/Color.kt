package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * LEDGR — CRED Luxury Obsidian & Neon Mint Design System
 *
 * A palette inspired by dark luxury fintech aesthetics: deep obsidian surfaces
 * paired with electric jewel-tone accents. The system is anchored on CRED's
 * signature neon mint, supported by indigo, gold, rose, and cyan.
 *
 * Tokens are layered:
 *   1. Obsidian surfaces (dark theme)
 *   2. Light surfaces (editorial monolith)
 *   3. Jewel accents with soft / deep / glow variants
 *   4. Emerald palette aliases (default brand primary)
 *   5. Text tokens
 *   6. Semantic / financial indicator colors
 *   7. Shimmer / skeleton
 *   8. Curated luxury gradients
 */

// ──────────────────────────────────────────────────────────────
// 1. Obsidian Surfaces — Deep luxury charcoal stack
// ──────────────────────────────────────────────────────────────
val ObsidianBg            = Color(0xFF06080C) // Deepest obsidian black
val ObsidianSurface       = Color(0xFF0E121B) // Sleek charcoal surface
val ObsidianElevated      = Color(0xFF151B27) // Elevated card background
val ObsidianHigh          = Color(0xFF1A2230) // Highest elevation tier
val ObsidianBorder        = Color(0xFF222B3D) // Crisp metallic border
val ObsidianBorderSubtle  = Color(0xFF18202E) // Subtle divider border
val ObsidianBorderStrong  = Color(0xFF2D3848) // Emphasized border
val ObsidianCardGlow      = Color(0xFF1C2436) // Radial highlight
val ObsidianScrim         = Color(0x99000000) // Modal scrim

// ──────────────────────────────────────────────────────────────
// 2. Light Surfaces — Clean editorial monolith
// ──────────────────────────────────────────────────────────────
val LightBg            = Color(0xFFF1F4F9)
val LightSurface       = Color(0xFFFFFFFF)
val LightElevated      = Color(0xFFE8EDF5)
val LightHigh          = Color(0xFFDDE3EE)
val LightBorder        = Color(0xFFD4DDE8)
val LightBorderSubtle  = Color(0xFFE2E8F0)
val LightBorderStrong  = Color(0xFFCBD5E1)
val LightScrim         = Color(0x33000000)

// ──────────────────────────────────────────────────────────────
// 3. Jewel Accents — Signature CRED chromatics
// ──────────────────────────────────────────────────────────────
// Neon Mint
val CredMint             = Color(0xFF00F5A0)
val CredMintSoft         = Color(0xFF55FFBA)
val CredMintDeep         = Color(0xFF00C77E)
val CredMintGlow         = Color(0x3300F5A0)
val CredMintGlowStrong   = Color(0x6600F5A0)

// Electric Indigo
val CredIndigo           = Color(0xFF6C5CE7)
val CredIndigoSoft       = Color(0xFF9183F0)
val CredIndigoDeep       = Color(0xFF4B3FBC)
val CredIndigoGlow       = Color(0x336C5CE7)
val CredIndigoGlowStrong = Color(0x666C5CE7)

// Champagne Gold
val CredGold             = Color(0xFFFFD166)
val CredGoldSoft         = Color(0xFFFFE3A8)
val CredGoldDeep         = Color(0xFFE0A82E)
val CredGoldGlow         = Color(0x33FFD166)
val CredGoldGlowStrong   = Color(0x66FFD166)

// Cyber Rose
val CredRose             = Color(0xFFFF3366)
val CredRoseSoft         = Color(0xFFFF6B8A)
val CredRoseDeep         = Color(0xFFD61F4D)
val CredRoseGlow         = Color(0x33FF3366)
val CredRoseGlowStrong   = Color(0x66FF3366)

// Laser Cyan
val CredCyan             = Color(0xFF00E5FF)
val CredCyanSoft         = Color(0xFF5AF1FF)
val CredCyanDeep         = Color(0xFF00B3CC)
val CredCyanGlow         = Color(0x3300E5FF)
val CredCyanGlowStrong   = Color(0x6600E5FF)

// ──────────────────────────────────────────────────────────────
// 4. Emerald palette aliases (default brand primary)
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
val TextPrimaryDark   = Color(0xFFF8FAFC) // Crisp platinum white
val TextSecondaryDark = Color(0xFF94A3B8) // Brushed steel silver
val TextTertiaryDark  = Color(0xFF526077) // Muted slate
val TextDisabledDark  = Color(0xFF3A4458) // Disabled

val TextPrimaryLight   = Color(0xFF090D16)
val TextSecondaryLight = Color(0xFF4A5568)
val TextTertiaryLight  = Color(0xFF7E8B9B)
val TextDisabledLight  = Color(0xFFB4BFD0)

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
val ShimmerHighlightDark  = Color(0xFF1F2738)
val ShimmerBaseLight      = LightElevated
val ShimmerHighlightLight = Color(0xFFF0F4FA)

// ──────────────────────────────────────────────────────────────
// 8. Curated Luxury Gradients
// ──────────────────────────────────────────────────────────────
val GradientMintStart      = CredMint
val GradientMintEnd        = CredCyan
val GradientIndigoStart    = CredIndigo
val GradientIndigoEnd      = CredRose
val GradientGoldStart      = CredGold
val GradientGoldEnd        = CredRoseSoft
val GradientObsidianStart  = ObsidianSurface
val GradientObsidianEnd    = ObsidianBg