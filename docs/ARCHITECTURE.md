# Meowsic Architecture & Data Flow

This document outlines the architectural patterns, data flow, and separation of concerns implemented across Meowsic.

---

## 1. Architectural Pattern: Clean Architecture + MVI

Meowsic follows Clean Architecture principles divided into three main layers:

```
[UI / Presentation] <---> [Domain] <---> [Data / Stream & Local Storage]
```

### 1.1 Presentation Layer
- **State Hub**: `SearchViewModel` acts as the primary MVI state container handling:
  - Audio playback state via `Media3 ExoPlayer`.
  - Search query debouncing (500ms debounce buffer).
  - Stream resolution state management (`Idle`, `Resolving`, `Ready`, `Error`).
  - Active queue and playback mode controls (repeat off/all/one, shuffle).
- **Navigation & Modals**: Root navigation in `MainActivity.kt` orchestrates 4-layer UI:
  1. Background ambient orbs canvas.
  2. Tabbed screens (`HomeScreen`, `SearchScreen`, `LibraryScreen`).
  3. Floating glass chrome (`MiniPlayer` + `GlassTabBar`).
  4. Overlay modals (`FullScreenPlayer`, `QueueBottomSheet`, `PlaylistDetailScreen`).

### 1.2 Domain Layer
- **Entities**:
  - `Song`: Core immutable data model representing audio items with metadata (`id`, `title`, `artist`, `thumbnailUrl`, `duration`).

### 1.3 Data Layer
- **Stream Extraction**:
  - `YouTubeStreamResolver`: Resolves high-bitrate audio URLs using NewPipe Extractor.
  - `NewPipeDownloader`: Custom HTTP client implementation using OkHttp3.
- **Persistence (Room Database)**:
  - `MeowsicDatabase`: Local database instance.
  - `SongDao`, `LikedSongDao`, `PlaylistDao`: Type-safe queries for offline tracks and custom playlists.
  - `LibraryRepository`: Central repository coordinating database queries and state updates.

---

## 2. Stream Resolution & Caching Lifecycle

```
User Selects Song
       │
       ▼
SearchViewModel (requests stream URL)
       │
       ▼
Check In-Memory Cache (20-min TTL)
       ├── Hit  ──> Return cached audio URL immediately
       └── Miss ──> Dispatch to NewPipe Extractor
                         │
                         ├── Parse audio stream formats (prefer M4A > 128kbps)
                         ├── Extract URL expiry timestamp (parameter: expire=)
                         ├── Save to Cache with safe expiration window
                         └── Feed MediaItem to ExoPlayer
```

---

## 3. UI Layering & Optical Backdrop Blur

The UI uses hardware-accelerated blur via `dev.chrisbanes.haze:haze:1.5.1`.

- **Root Source**: `Modifier.haze(state)` captures background content rendered beneath the glass.
- **Glass Surfaces**: `Modifier.hazeChild(state, style)` samples the background and renders blurred glass surfaces with specular highlights and specular rim borders.
- **Fallback**: On older Android versions without hardware blur, components gracefully fall back to semi-translucent AMOLED-tinted containers.
