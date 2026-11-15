package com.surya.meowsic.ui.theme

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GlassTokens {
    val CardCornerRadius: Dp = 24.dp
    val SheetCornerRadius: Dp = 32.dp
    val MiniPlayerCornerRadius: Dp = 32.dp
    val PillCornerRadius: Dp = 50.dp
    val SongItemCornerRadius: Dp = 22.dp
    val SongArtCornerRadius: Dp = 12.dp
    val DefaultBlurRadius: Dp = 24.dp
    val BorderWidth: Dp = 1.dp

    val supportsBlur: Boolean
        get() = false
}

object LiquidGlassTokens {
    val CardShape = RoundedCornerShape(24.dp)
    val PillShape = CircleShape
    val SheetTopShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    fun InteractiveSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    @Immutable
    data class AmbientPalette(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val surfaceTint: Color,
        val accent: Color,
        val onPrimary: Color = Color(0xFF1E1234),
        val primaryContainer: Color = Color(0xFF3B235A),
        val onPrimaryContainer: Color = Color(0xFFF3E5F5),
        val surfaceContainer: Color = Color(0xFF1F1630),
        val surfaceContainerHigh: Color = Color(0xFF2B2042)
    )

    val DefaultPalette = AmbientPalette(
        primary = Color(0xFFD0BCFF),
        secondary = Color(0xFFF06292),
        tertiary = Color(0xFFFF8A65),
        surfaceTint = Color(0xFF2A1F40),
        accent = Color(0xFFCE93D8),
        onPrimary = Color(0xFF2A1246),
        primaryContainer = Color(0xFF422866),
        onPrimaryContainer = Color(0xFFF5EAFF),
        surfaceContainer = Color(0xFF1B132B),
        surfaceContainerHigh = Color(0xFF271D3D)
    )

    private val paletteCache = object : LinkedHashMap<String, AmbientPalette>(40, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AmbientPalette>?): Boolean {
            return size > 32
        }
    }

    @Composable
    fun rememberAmbientPalette(thumbnailUrl: String?): State<AmbientPalette> {
        val context = LocalContext.current
        var targetPalette by remember { mutableStateOf(DefaultPalette) }

        LaunchedEffect(thumbnailUrl) {
            if (thumbnailUrl.isNullOrBlank()) {
                targetPalette = DefaultPalette
                return@LaunchedEffect
            }
            val cached = synchronized(paletteCache) { paletteCache[thumbnailUrl] }
            if (cached != null) {
                targetPalette = cached
                return@LaunchedEffect
            }
            val extracted = withContext(Dispatchers.IO) {
                runCatching {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .size(64, 64)
                        .allowHardware(false)
                        .build()
                    val result = loader.execute(request)
                    val bitmap = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) extractFromBitmap(bitmap) else DefaultPalette
                }.getOrDefault(DefaultPalette)
            }
            synchronized(paletteCache) {
                paletteCache[thumbnailUrl] = extracted
            }
            targetPalette = extracted
        }

        val animSpec = tween<Color>(durationMillis = 550, easing = FastOutSlowInEasing)
        val primary by animateColorAsState(targetPalette.primary, animSpec, label = "palPrimary")
        val secondary by animateColorAsState(targetPalette.secondary, animSpec, label = "palSecondary")
        val tertiary by animateColorAsState(targetPalette.tertiary, animSpec, label = "palTertiary")
        val surfaceTint by animateColorAsState(targetPalette.surfaceTint, animSpec, label = "palSurface")
        val accent by animateColorAsState(targetPalette.accent, animSpec, label = "palAccent")
        val onPrimary by animateColorAsState(targetPalette.onPrimary, animSpec, label = "palOnPrimary")
        val primaryContainer by animateColorAsState(targetPalette.primaryContainer, animSpec, label = "palPrimaryContainer")
        val onPrimaryContainer by animateColorAsState(targetPalette.onPrimaryContainer, animSpec, label = "palOnPrimaryContainer")
        val surfaceContainer by animateColorAsState(targetPalette.surfaceContainer, animSpec, label = "palSurfaceContainer")
        val surfaceContainerHigh by animateColorAsState(targetPalette.surfaceContainerHigh, animSpec, label = "palSurfaceContainerHigh")

        return remember(
            primary, secondary, tertiary, surfaceTint, accent,
            onPrimary, primaryContainer, onPrimaryContainer, surfaceContainer, surfaceContainerHigh
        ) {
            mutableStateOf(
                AmbientPalette(
                    primary = primary,
                    secondary = secondary,
                    tertiary = tertiary,
                    surfaceTint = surfaceTint,
                    accent = accent,
                    onPrimary = onPrimary,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    surfaceContainer = surfaceContainer,
                    surfaceContainerHigh = surfaceContainerHigh
                )
            )
        }
    }

    private fun extractFromBitmap(bitmap: Bitmap): AmbientPalette {
        val w = bitmap.width.coerceAtLeast(1)
        val h = bitmap.height.coerceAtLeast(1)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0L
        var vibrantColor: Color? = null
        var maxSat = 0f

        val stepX = (w / 12).coerceAtLeast(1)
        val stepY = (h / 12).coerceAtLeast(1)
        val hsv = FloatArray(3)

        for (y in 0 until h step stepY) {
            for (x in 0 until w step stepX) {
                val argb = bitmap.getPixel(x, y)
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF
                rSum += r
                gSum += g
                bSum += b
                count++

                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                val sat = hsv[1]
                val v = hsv[2]
                if (sat > maxSat && v in 0.25f..0.95f) {
                    maxSat = sat
                    vibrantColor = Color(r, g, b)
                }
            }
        }

        if (count == 0L) return DefaultPalette
        val avgR = (rSum / count).toInt()
        val avgG = (gSum / count).toInt()
        val avgB = (bSum / count).toInt()
        val base = vibrantColor ?: Color(avgR, avgG, avgB)

        val lightPrimary = lerpColor(base, Color.White, 0.38f)
        val darkContainer = lerpColor(base, Color(0xFF140D22), 0.62f)
        val surfaceContainer = lerpColor(base, Color(0xFF120C1E), 0.78f)
        val surfaceContainerHigh = lerpColor(base, Color(0xFF1C142E), 0.70f)
        val onPri = if (lightPrimary.luminance() > 0.45f) Color(0xFF1D102B) else Color.White

        return AmbientPalette(
            primary = lightPrimary,
            secondary = lerpColor(base, MeowsicPink, 0.35f),
            tertiary = lerpColor(base, MeowsicOrange, 0.35f),
            surfaceTint = darkContainer,
            accent = lightPrimary,
            onPrimary = onPri,
            primaryContainer = darkContainer,
            onPrimaryContainer = lerpColor(lightPrimary, Color.White, 0.55f),
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh
        )
    }

    private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = start.red + (stop.red - start.red) * f,
            green = start.green + (stop.green - start.green) * f,
            blue = start.blue + (stop.blue - start.blue) * f,
            alpha = start.alpha + (stop.alpha - start.alpha) * f
        )
    }
}
