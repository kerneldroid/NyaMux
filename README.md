> **NOTE**: This project is temporarily frozen. It is not in the archive, but Issues and PR will not be considered. 

# NyaMux

Terminal emulator for Android with a Linux command line environment. Fork of [termux/termux-app](https://github.com/termux/termux-app) — entire UI rewritten in Kotlin + Jetpack Compose.

## What's different

- **100% Kotlin** — all production code across all 4 modules (app, terminal-emulator, terminal-view, termux-shared)
- **Jetpack Compose UI** — main screen, session drawer, extra keys, toolbar, context menu, settings — all Compose Material 3
- **Material You** — dynamic colors (Monet) + custom color mode derived from the terminal color scheme
- **Material 3 Expressive** shapes and motion in settings and UI elements
- **TAPI / Nightzuku** — `kerneldroid.nightzuku.TAPI_SUPPORT` for Nightzuku integration
- Full compatibility with plugin apps (Api, Boot, Float, Styling, Tasker, Widget) built against the `com.nyamux` namespace

Terminal rendering remains a native View (`TerminalView`) embedded via `AndroidView` — correct approach for Canvas-based cell rendering.

## Build

```bash
./gradlew assembleRelease
```

Requires signing config in `local.properties` (not tracked). Release builds on GitHub Actions are signed from repository secrets.

## Stack

Kotlin 2.4.10 · Compose BOM 2026.08.00 · AGP 9.3.2 · compileSdk 37 · minSdk 24

## License

[GPLv3](LICENSE.md) · Based on [Termux](https://termux.dev)
