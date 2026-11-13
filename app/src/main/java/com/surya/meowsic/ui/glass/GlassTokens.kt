package com.surya.meowsic.ui.glass

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glass design system tokens.
 * Extends LiquidGlassTokens with blur-specific constants and tier resolution.
 */
object GlassTokens {

    // ── Blur radii ──────────────────────────────────────────────────────────
    /** Blur radius for primary chrome (tab bar, mini player) */
    val BlurRadiusPrimary: Dp = 28.dp

    /** Blur radius for elevated chrome (bottom sheet, prominent card) */
    val BlurRadiusElevated: Dp = 40.dp

    /** Blur radius for subtle chrome (search bar, dividers) */
    val BlurRadiusSubtle: Dp = 16.dp

    // ── Glass surface tints ─────────────────────────────────────────────────
    /** Primary glass tint — light veil over backdrop */
    val TintPrimary: Color = Color(0x1AFFFFFF)   // 10% white

    /** Elevated glass tint — slightly more opaque */
    val TintElevated: Color = Color(0x26FFFFFF)  // 15% white

    /** Sheet glass tint — darkened AMOLED-friendly */
    val TintSheet: Color = Color(0x0D000000)     // 5% black (AMOLED-safe)

    // ── Fallback tints (API < 31, no real blur available) ───────────────────
    /** Tier C fallback: opaque tinted surface for API 26-30 */
    val FallbackSurface: Color = Color(0xFF0D0D11)

    /** Tier C fallback tint on top of opaque surface */
    val FallbackTint: Color = Color(0x33FFFFFF)  // 20% white — more visible without blur

    // ── Corner radii ────────────────────────────────────────────────────────
    val CornerTabBar: Dp = 32.dp
    val CornerMiniPlayer: Dp = 24.dp
    val CornerCard: Dp = 22.dp
    val CornerButton: Dp = 50.dp   // capsule
    val CornerSheet: Dp = 28.dp

    // ── Spring specs ────────────────────────────────────────────────────────
    fun <T> pressSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> tabSelectionSpring() = spring<T>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow
    )

    val PressScaleSpring get() = pressSpring<Float>()
    val TabSelectionSpring get() = tabSelectionSpring<Float>()

    // ── API tier resolution ──────────────────────────────────────────────────
    /** True if device supports RenderEffect-based blur (API 31+) */
    val supportsBlur: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** True if device supports AGSL RuntimeShader (API 33+) */
    val supportsAGSL: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * Device graphics capability tier categorization:
     * - Tier A (API 33+ / Android 13+): Full hardware backdrop blur via RenderEffect + AGSL runtime shader refraction.
     * - Tier B (API 31-32 / Android 12): Hardware backdrop blur via RenderEffect with standard alpha blending.
     * - Tier C (API 26-30): Graceful fallback to opaque dark AMOLED surfaces with subtle scrim highlights.
     */
    sealed class Tier {
        /** API 33+: backdrop blur + AGSL lens refraction */
        object A : Tier()
        /** API 31-32: backdrop blur only */
        object B : Tier()
        /** API 26-30: opaque tinted surface fallback */
        object C : Tier()
    }

    /**
     * Resolves the active graphics tier based on the running Android OS version.
     */
    fun currentTier(): Tier = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Tier.A
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> Tier.B
        else -> Tier.C
    }
}
