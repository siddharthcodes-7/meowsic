# 🐱 Meowsic

<p align="center">
  <b>A sleek, AMOLED Liquid Glass music streaming & local playback player for Android.</b>
  <br />
  <i>Built with Jetpack Compose, Media3 ExoPlayer, NewPipe Extractor, and Room Database.</i>
</p>

---

## ✨ Features

- **Liquid Glass & AMOLED Design System**:
  - Optical backdrop blur powered by `dev.chrisbanes.haze:haze`.
  - True `#000000` AMOLED dark canvas for maximum battery savings.
  - Specular rim highlights, glass refraction borders, and organic ambient light orbs.
  - Interactive physics with spring animations for tab bars, sheets, and player controls.

- **Seamless Audio Streaming**:
  - YouTube audio search and streaming powered by **NewPipe Extractor**.
  - Intelligent stream resolution preferring high-bitrate M4A audio with fallback.
  - In-memory 20-minute audio stream caching with automatic URL expiration handling.

- **Audio Engine**:
  - Background playback powered by AndroidX **Media3 ExoPlayer**.
  - Robust queue management: repeat modes (Off, All, One), shuffle, seek scrubbing, and mini-player capsule.
  - Full-screen player modal with dynamic artwork scaling and tactile controls.

- **Local Library & Playlists**:
  - Offline favorite songs tracking using **Room Database**.
  - Custom playlist creation and track management.
  - Reactive state flows with Kotlin Coroutines and StateFlow.

---

## 🛠️ Architecture & Tech Stack

```
meowsic/
├── app/src/main/java/com/surya/meowsic/
│   ├── data/
│   │   ├── local/          # Room DB, DAOs, entities & mappers
│   │   ├── repository/     # LibraryRepository & database operations
│   │   └── stream/         # NewPipeDownloader & YouTubeStreamResolver
│   ├── domain/             # Immutable models (Song)
│   ├── presentation/
│   │   ├── home/           # Feed & queue previews
│   │   ├── search/         # YouTube search & SearchViewModel (MVI state hub)
│   │   ├── library/        # Playlists, liked songs & detail screens
│   │   └── player/         # MiniPlayer, FullScreenPlayer & QueueBottomSheet
│   └── ui/
│       ├── components/     # LiquidGlass & Obsidian UI components
│       ├── glass/          # Haze glass surfaces, buttons, sliders & tabs
│       └── theme/          # AMOLED palette, typography & glass tokens
└── docs/                   # Architecture, migration & design documentation
```

### Key Libraries:
- **UI**: Jetpack Compose, Material 3, Haze Blur (`1.5.1`)
- **Audio Engine**: AndroidX Media3 ExoPlayer (`1.5.1`)
- **Extraction**: NewPipe Extractor (`0.25.2`) + OkHttp3
- **Local Persistence**: AndroidX Room (`2.6.1`) with KSP
- **Image Loading**: Coil 3 (`3.0.4`)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or higher
- **Android SDK**: `compileSdk 35`, `minSdk 26`

### Building from Source

```bash
# Clone the repository
git clone https://github.com/siddharthcodes-7/meowsic.git
cd meowsic

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 📄 Documentation

- [Liquid Glass Design System](docs/LIQUID_GLASS.md)
- [Architecture & Data Flow](docs/ARCHITECTURE.md)
- [Contributing Guidelines](docs/CONTRIBUTING.md)

---

## 📜 License

Meowsic is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
