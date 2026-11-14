package com.surya.meowsic.ui.glass

import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/**
 * Glass intensity levels. Used to downgrade glass quality for power saving or user preference.
 */
enum class GlassIntensity {
    /** Full quality: backdrop blur + AGSL lens + animated orbs */
    FULL,
    /** Reduced: backdrop blur + no AGSL lens + static orbs */
    REDUCED,
    /** Minimal: opaque tinted surface, no blur, no animations (Tier C fallback) */
    MINIMAL
}

/**
 * Resolves the effective [GlassIntensity] at runtime.
 * Respects power save mode and API tier limitations.
 */
@Composable
fun rememberEffectiveGlassIntensity(
    userPreference: GlassIntensity = GlassIntensity.FULL
): GlassIntensity {
    val context = LocalContext.current
    return remember(userPreference) {
        val powerManager = context.getSystemService<PowerManager>()
        val isPowerSaving = powerManager?.isPowerSaveMode == true
        when {
            isPowerSaving -> GlassIntensity.MINIMAL
            !GlassTokens.supportsBlur -> GlassIntensity.MINIMAL
            userPreference == GlassIntensity.MINIMAL -> GlassIntensity.MINIMAL
            userPreference == GlassIntensity.REDUCED -> GlassIntensity.REDUCED
            else -> GlassIntensity.FULL
        }
    }
}
