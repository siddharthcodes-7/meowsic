package com.surya.meowsic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val LocalMeowsicDarkTheme = staticCompositionLocalOf { true }

val MeowsicDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = MeowsicPink,
    onSecondary = Color(0xFF49152B),
    secondaryContainer = Color(0xFF632B41),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = MeowsicOrange,
    onTertiary = Color(0xFF4C1F0D),
    tertiaryContainer = Color(0xFF683420),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF120D1D),
    onBackground = MeowsicWhite,
    surface = Color(0xFF161024),
    onSurface = MeowsicWhite,
    surfaceVariant = Color(0xFF2B213F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF0D0816),
    surfaceContainerLow = Color(0xFF171126),
    surfaceContainer = Color(0xFF1E1630),
    surfaceContainerHigh = Color(0xFF281E3E),
    surfaceContainerHighest = Color(0xFF33284D),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

val MeowsicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MeowsicTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    colorSchemeOverride: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        colorSchemeOverride != null -> colorSchemeOverride
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        else -> MeowsicDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(LocalMeowsicDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = MeowsicShapes,
            content = content
        )
    }
}
