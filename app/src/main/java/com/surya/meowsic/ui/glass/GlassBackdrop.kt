package com.surya.meowsic.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Provides a [HazeState] to child composables so that all glass surfaces
 * share ONE backdrop layer per screen — never create per-item.
 *
 * Usage:
 * ```
 * GlassBackdropHost { hazeState ->
 *     Box(Modifier.haze(hazeState)) {    // content that acts as the blurred backdrop
 *         ScreenContent()
 *     }
 *     GlassSurface(hazeState = hazeState) { ... }  // chrome on top
 * }
 * ```
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
fun GlassBackdropHost(
    content: @Composable (hazeState: HazeState) -> Unit
) {
    val hazeState = remember { HazeState() }
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        content(hazeState)
    }
}

/**
 * Convenience Modifier extension: marks a composable as the backdrop source.
 * Equivalent to `Modifier.haze(hazeState)` but reads from [LocalHazeState].
 */
@Composable
fun Modifier.glassBackdrop(): Modifier {
    val hazeState = LocalHazeState.current ?: return this
    return this.hazeSource(hazeState)
}
