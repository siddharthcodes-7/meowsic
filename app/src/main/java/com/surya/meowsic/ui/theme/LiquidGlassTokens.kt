package com.surya.meowsic.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

/**
 * AMOLED Liquid Glass Design System Tokens
 *
 * Implements an optical architecture where 75%+ of the screen is true AMOLED #000000 void,
 * refracted by localized ambient light emitters placed behind frosted glass chrome.
 */
object LiquidGlassTokens {

    // ------------------------------------------------------------------------
    // 1. CANVAS & OLED SURFACES
    // ------------------------------------------------------------------------

    /** Pure AMOLED black — 0% pixel illumination on OLED panels */
    val AmoledVoid = Color(0xFF000000)

    /** Near-opaque OLED surface fallback for accessibility & low-power mode */
    val SurfaceOledFallback = Color(0xFF0D0D11)

    /** Primary Glass Surface (linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.03) 100%)) */
    val SurfaceGlassPrimary = Brush.linearGradient(
        colors = listOf(
            Color(0x1FFFFFFF), // rgba(255, 255, 255, 0.12)
            Color(0x08FFFFFF)  // rgba(255, 255, 255, 0.03)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Elevated Glass Surface (linear-gradient(135deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.05) 100%)) */
    val SurfaceGlassElevated = Brush.linearGradient(
        colors = listOf(
            Color(0x2EFFFFFF), // rgba(255, 255, 255, 0.18)
            Color(0x0DFFFFFF)  // rgba(255, 255, 255, 0.05)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Active / Pressed Glass Surface (rgba(255, 255, 255, 0.22)) */
    val SurfaceGlassActive = Color(0x38FFFFFF)

    /** Pill/Sheet Drag Handle & Accent Indicators (rgba(255, 255, 255, 0.30)) */
    val DragHandleColor = Color(0x4DFFFFFF)

    // ------------------------------------------------------------------------
    // 2. SPECULAR 3D RIM LIGHTING (Refraction & Liquid Bevels)
    // ------------------------------------------------------------------------

    /** Specular top/left light-catching edge gradient (rgba(255, 255, 255, 0.24) -> 0.04) */
    val SpecularRimGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x3DFFFFFF), // rgba(255, 255, 255, 0.24)
            Color(0x0AFFFFFF)  // rgba(255, 255, 255, 0.04)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Solid subtle specular rim border (rgba(255, 255, 255, 0.24)) */
    val SpecularRimBorder = Color(0x3DFFFFFF)

    /** Inner refraction top bevel highlight (rgba(255, 255, 255, 0.55)) */
    val SpecularBevelHighlight = Color(0x8CFFFFFF)

    /** Inner refraction bottom shadow (rgba(255, 255, 255, 0.08)) */
    val SpecularBevelShadow = Color(0x14FFFFFF)

    /** Deep outer elevation shadow color (rgba(0, 0, 0, 0.85)) */
    val ElevationShadow = Color(0xD9000000)

    // ------------------------------------------------------------------------
    // 3. AMBIENT LIGHT EMITTERS (Dynamic Palette & Fallbacks)
    // ------------------------------------------------------------------------

    /** Fallback Dominant Color: Electric Cyan */
    val ElectricCyan = Color(0xFF00F0FF)

    /** Fallback Secondary Color: Deep Violet */
    val DeepViolet = Color(0xFF7000FF)

    /** Fallback Accent Color: Crimson */
    val Crimson = Color(0xFFFF0055)

    /** Brand Primary Accent: Electric Lime */
    val ElectricLime = Color(0xFFB9F36A)

    /**
     * Ambient color triplet container for localized radial light emitters
     */
    data class AmbientPalette(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color
    )

    val DefaultPalette = AmbientPalette(
        primary = ElectricCyan,
        secondary = DeepViolet,
        tertiary = Crimson
    )

    /**
     * Algorithmic, deterministic palette generator based on song metadata.
     * Always provides vibrant, high-contrast emitters tailored to the track,
     * harmonizing with the Electric Cyan / Deep Violet / Crimson design spec.
     */
    fun getAmbientPaletteForSong(seed: String?): AmbientPalette {
        if (seed.isNullOrBlank()) return DefaultPalette
        val hash = seed.hashCode()

        val palettes = listOf(
            AmbientPalette(ElectricCyan, DeepViolet, Crimson),
            AmbientPalette(Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFFFF007F)),
            AmbientPalette(Color(0xFF3A86FF), Color(0xFF8338EC), Color(0xFFFF006E)),
            AmbientPalette(ElectricLime, Color(0xFF00B4D8), Color(0xFF7209B7)),
            AmbientPalette(Color(0xFFFF9E00), Color(0xFFFF0055), Color(0xFF7000FF)),
            AmbientPalette(Color(0xFF00F0FF), Color(0xFF10B981), Color(0xFF6366F1))
        )
        val index = (hash and 0x7FFFFFFF) % palettes.size
        return palettes[index]
    }

    // ------------------------------------------------------------------------
    // 4. TYPOGRAPHY & CONTRAST
    // ------------------------------------------------------------------------

    /** Pure white header & title text with optical tracking */
    val TextPrimary = Color(0xFFFFFFFF)

    /** Secondary metadata text (rgba(255, 255, 255, 0.68)) */
    val TextSecondary = Color(0xADFFFFFF)

    /** Muted timestamps & inactive controls (rgba(255, 255, 255, 0.42)) */
    val TextMuted = Color(0x6BFFFFFF)

    /** Optical tracking for headers: -0.02em */
    val HeaderLetterSpacing: TextUnit = (-0.02).em

    // ------------------------------------------------------------------------
    // 5. MOTION PHYSICS
    // ------------------------------------------------------------------------

    /** Motion curve: cubic-bezier(0.22, 1, 0.36, 1) */
    val LiquidCubicBezier = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

    /** Responsive spring for interactive press and scale effects (Float) */
    val TactileSpringSpecFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Responsive spring for Dp animations (e.g. Scrubber thickness) */
    val TactileSpringSpecDp = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Smooth spring for slide transitions (IntOffset) */
    val SlideSpringSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Smooth spring for layout and size expansions (Float) */
    val SmoothSpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}
