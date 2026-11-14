# Meowsic — iOS 26 Liquid Glass Architecture & Migration Plan

## 1. Source Codebase Analysis & Current State Audit

### 1.1 Existing Architecture
- **Application & Single-Point Initialization**: `MeowMusicApplication.kt` and `MainActivity.kt` initialize `YouTubeStreamResolver` safely on startup.
- **Audio & Stream Pipeline**: 
  - `domain/Song.kt`: Immutable data representation (`id`, `title`, `artist`, `thumbnailUrl`, `duration`).
  - `data/stream/NewPipeDownloader.kt`: OkHttp3 custom downloader implementing NewPipe's `Downloader` interface with automated User-Agent injection.
  - `data/stream/YouTubeStreamResolver.kt`: Resolves audio URLs via NewPipe Extractor (`v0.25.2`), preferring M4A streams with progressive fallback, 20-minute stream caching with URL expiration parsing.
  - `presentation/search/SearchViewModel.kt`: Central state hub managing Media3 `ExoPlayer`, search debouncing (500ms), queue management, repeat/shuffle modes, playback position tracking, and UI modal states (`isFullScreenOpen`).
- **Presentation & Navigation**:
  - `MainActivity.kt`: Root composable `MeoMusicApp` providing `MeowsicTheme` with `currentSongId`. 4-layer box structure: Canvas orbs → content (Home, Search, Library) → floating chrome (`MiniPlayer` + `LiquidGlassPill` tab bar) → `FullScreenPlayer` modal sheet.
  - `presentation/home/HomeScreen.kt`: Feed with "Made For You", "Your Queue", and "About" sections. Uses `LiquidGlassCard` and legacy `Obsidian*` headers/dividers.
  - `presentation/search/SearchScreen.kt`: YouTube search field, animated loading state, resolving/error banners, search results list with active track highlight.
  - `presentation/library/LibraryScreen.kt`: Empty library placeholder with `LiquidGlassCard`.
  - `presentation/player/FullScreenPlayer.kt`: Full-bleed player with artwork spring animations (`0.92f` -> `1.0f`), seek scrubber (`LiquidScrubber`), play/pause orb button (`LiquidOrbButton`), tactile icon buttons, and queue bottom sheet trigger.
  - `presentation/player/MiniPlayer.kt`: Floating capsule player with progress bar, artwork, marquee/track info, and play/skip buttons.
  - `presentation/player/QueueBottomSheet.kt`: Modal bottom sheet displaying up next list with active track highlight in `LiquidGlassCard`.

### 1.2 Design System Audit

| File | Components | Analysis | Real Backdrop Blur? |
|---|---|---|---|
| `ui/theme/LiquidGlassTokens.kt` | Color tokens, brush gradients, spring specs, `AmbientPalette` | Provides AMOLED black (`#000000`), glass gradient fills, specular rim borders, and track-derived ambient color palettes. | N/A |
| `ui/theme/ObsidianTheme.kt` | Spacing, radius, legacy dark tokens | Contains legacy dimensions and colors still referenced in older components. | N/A |
| `ui/components/LiquidGlass.kt` | `liquidGlassLens`, `AmbientLightOrbs`, `LiquidGlassCard`, `LiquidOrbButton`, `LiquidGlassPill` | Relies on gradient fills and an AGSL shader on API 33+. **Critically: the shader operates solely on the composable's own rendered pixels rather than refracting underlying content.** | ❌ No backdrop capture |
| `ui/components/ObsidianComponents.kt` | `ObsidianGlassSurface`, `ObsidianSectionHeader`, `ObsidianDivider`, `ObsidianSquareArtwork`, `ObsidianProgressBar`, `ObsidianTextButton` | Flat semi-transparent containers and custom canvas controls. | ❌ No backdrop capture |

### 1.3 Why a Complete Rebuild is Necessary
1. **Absence of Real Optical Backdrop Blur**: Existing glass components draw translucent gradient fills over a static black canvas. They do not sample or blur content scrolling beneath them.
2. **Disconnected Lens Shader**: The AGSL `RuntimeShader` in `LiquidGlass.kt` computes SDF lens distortion on internal content rather than the backdrop.
3. **Static Lighting**: `AmbientLightOrbs` draws 3 static radial circles; it lacks organic fluid motion or drift.
4. **Scattered Components**: Coexistence of `Obsidian*` and `LiquidGlass*` created inconsistent visual hierarchies.
5. **No Tab Bar Spring Physics**: Navigation tab switches perform flat color swaps without sliding lens morphing.

---

## 2. Chosen Backdrop Architecture

### 2.1 Technology Evaluation
1. **`io.github.kyant0:backdrop`**: Tested conceptually; tightly coupled to experimental Compose snapshot APIs that conflict with current Compose BOM and compileSdk 37.
2. **`dev.chrisbanes.haze:haze:1.5.1` (CHOSEN)**:
   - Battle-tested on Android Jetpack Compose.
   - Automatically utilizes hardware-accelerated `RenderEffect.createBlurEffect` on API 31+.
   - Clean separation of backdrop capture (`Modifier.haze(state)` / `hazeSource`) and glass child rendering (`Modifier.hazeChild(state, style)` / `hazeEffect`).
   - Supports noise grain overlays to prevent color banding on dark OLED displays.
   - Elegant graceful degradation when blur is unsupported or disabled.
3. **DIY `GraphicsLayer` + `RenderEffect`**: Reinvents Haze's internal layer tree with higher maintenance overhead.

### 2.2 Three-Tier Degradation Architecture

```
                       ┌──────────────────────────────┐
                       │  Device Capabilities Check   │
                       └──────────────┬───────────────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            ▼                         ▼                         ▼
   ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
   │     Tier A      │       │     Tier B      │       │     Tier C      │
   │    (API 33+)    │       │   (API 31-32)   │       │   (API 26-30 /  │
   │                 │       │                 │       │   Power Save)   │
   ├─────────────────┤       ├─────────────────┤       ├─────────────────┤
   │ • Real Backdrop │       │ • Real Backdrop │       │ • High-opacity  │
   │   Blur (Haze)   │       │   Blur (Haze)   │       │   OLED fallback │
   │ • AGSL Rim      │       │ • Tint & Noise  │       │   (alpha 0.85)  │
   │   Refraction    │       │ • Specular Rim  │       │ • Specular Rim  │
   │ • Vibrant Tint  │       │ • Spring Motion │       │ • Drop Shadow   │
   │ • Kinetic Orbs  │       │ • Kinetic Orbs  │       │ • Static Orbs   │
   └─────────────────┘       └─────────────────┘       └─────────────────┘
```

---

## 3. `ui/glass/` Design System Architecture

```
ui/glass/
├── GlassTokens.kt           # Blur radii, tints, corner radii, generic pressSpring(), tier detection
├── GlassBackdrop.kt         # GlassBackdropHost { }, LocalHazeState, Modifier.glassBackdrop()
├── GlassSurface.kt          # GlassSurface composable & Modifier.glass(), multi-tier rendering
├── GlassButton.kt           # GlassIconButton, GlassButton, GlassProminentButton (play/pause orb)
├── GlassTabBar.kt           # Floating capsule navigation bar with sliding selection indicator
├── GlassSheet.kt            # Glass bottom sheet with backdrop blur and specular rim highlight
├── GlassSlider.kt           # Seek slider with animated thumb radius, glow, and palette gradient
├── AmbientBackground.kt     # Multi-orb Canvas with continuous organic drift animations (20-40s)
└── GlassSettings.kt         # Runtime intensity resolution respecting PowerManager.isPowerSaveMode
```

---

## 4. Phase Execution & Rebuild Roadmap

- **Phase 1: Source Audit & Architecture Documentation (`docs/LIQUID_GLASS.md`)**  
  Thorough code reading, dependency analysis, and complete architectural documentation.
- **Phase 2: Foundation & Core Glass Surface**  
  `GlassTokens`, `GlassBackdropHost`, `GlassSurface` with Tier A/B/C support, and `GlassPreviewScreen`.
- **Phase 3: Interactive Glass Controls**  
  `GlassButton`, `GlassProminentButton`, `GlassSlider`, and `AmbientBackground` with kinetic orbs.
- **Phase 4: Navigation Shell & MiniPlayer**  
  `GlassTabBar` integration in `MainActivity`, bottom inset padding (`160dp`), and modern `MiniPlayer`.
- **Phase 5: FullScreenPlayer & Queue Sheet**  
  `FullScreenPlayer` rebuild with `AmbientBackground`, `GlassProminentButton`, `GlassSlider`, and `GlassSheet`.
- **Phase 6: Primary Screen Integration**  
  Reconstruction of `HomeScreen`, `SearchScreen`, and `LibraryScreen`, eliminating `Obsidian*` dependencies.
- **Phase 7: Polish & Verification**  
  Haptic feedback, power-save fallbacks, dead code removal, and build verification across Android API targets.
