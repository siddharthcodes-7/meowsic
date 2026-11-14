# Contributing to Meowsic

Thank you for your interest in contributing to Meowsic!

## 🌿 Branching Strategy
- `main`: Production and stable releases.
- `feature/<feature-name>`: New UI components, extractors, or enhancements.
- `bugfix/<issue-name>`: Fixes for audio playback, crash reports, or UI glitches.

## 🎨 Code Style & Quality Standards
- **Kotlin**: Follow official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Compose**: Maintain stateless composables where possible and hoist state to ViewModels.
- **AMOLED Compliance**: Ensure all new screens preserve the true AMOLED `#000000` canvas background.
- **Glass Components**: Always use `GlassSurface`, `GlassButton`, or tokens from `com.surya.meowsic.ui.glass` for consistent blur and rim highlights.

## 🧪 Testing & Verification
Before submitting a PR:
1. Build debug APK: `./gradlew assembleDebug`
2. Verify linting & formatting: `./gradlew lint`
3. Test audio playback and stream extraction on physical device or emulator.
