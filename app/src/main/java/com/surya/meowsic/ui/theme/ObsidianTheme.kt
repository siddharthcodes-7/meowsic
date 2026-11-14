package com.surya.meowsic.ui.theme

object Obsidian {
    // === COLORS ===
    val Background = androidx.compose.ui.graphics.Color(0xFF000000)
    val SurfacePrimary = androidx.compose.ui.graphics.Color(0xFF080808)
    val SurfaceSecondary = androidx.compose.ui.graphics.Color(0xFF101010)
    val SurfaceElevated = androidx.compose.ui.graphics.Color(0xFF151515)
    val SurfaceHighlight = androidx.compose.ui.graphics.Color(0xFF1C1C1E) // iOS-style elevated

    val TextPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFFA3A3A3)
    val TextTertiary = androidx.compose.ui.graphics.Color(0xFF666666)
    val TextDisabled = androidx.compose.ui.graphics.Color(0xFF3A3A3A)

    val GlassFill = androidx.compose.ui.graphics.Color(0x0DFFFFFF)       // 5% white
    val GlassHighlight = androidx.compose.ui.graphics.Color(0x14FFFFFF)  // 8% white
    val GlassBorder = androidx.compose.ui.graphics.Color(0x1AFFFFFF)     // 10% white
    val GlassBorderStrong = androidx.compose.ui.graphics.Color(0x1EFFFFFF) // 12% white
    val GlassTopReflection = androidx.compose.ui.graphics.Color(0x12FFFFFF) // 7% white

    val ProgressTrack = androidx.compose.ui.graphics.Color(0xFF454545)
    val ProgressActive = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val Accent = androidx.compose.ui.graphics.Color(0xFFD8D8D8)

    val Separator = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
    val IconTint = androidx.compose.ui.graphics.Color(0xFFB8B8B8)
    val IconTintActive = androidx.compose.ui.graphics.Color(0xFFFFFFFF)

    // === DIMENSIONS ===
    val SpacingXxs = 4
    val SpacingXs = 8
    val SpacingSm = 12
    val SpacingMd = 16
    val SpacingLg = 20
    val SpacingXl = 24
    val SpacingXxl = 32
    val Spacing3xl = 40
    val Spacing4xl = 48

    val RadiusSm = 8
    val RadiusMd = 12
    val RadiusLg = 16
    val RadiusXl = 20
    val RadiusXxl = 24
    val RadiusPill = 9999

    val GlassRadius = 24
    val MiniPlayerRadius = 24
    val ArtworkRadius = 24

    val MiniPlayerHeight = 72
    val MiniPlayerHorizontalMargin = 14
    val MiniPlayerArtworkSize = 48

    val PlayButtonSize = 76
    val SkipButtonSize = 50
    val SecondaryControlSize = 40

    val ArtworkWidthFraction = 0.84f

    // === ANIMATION ===
    val DurationMicro = 120
    val DurationButton = 150
    val DurationNav = 300
    val DurationMiniPlayer = 350
    val DurationArtwork = 450
    val DurationAmbient = 800
}
