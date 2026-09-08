# TuneCraft Music Player 🎵

<div align="center">

[![Android SDK](https://img.shields.io/badge/Android-API%2024--36-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/UI-Compose%20M3-purple.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)]()
[![Release](https://img.shields.io/badge/Release-v1.0.0-orange.svg)]()

**A modern, offline-first Android music player with 14 themes, equalizer, car mode, and Android Auto support.**

</div>

---

## ✨ Features

### 🎧 Playback
- **Media3 ExoPlayer engine** — gapless-ready, high-quality audio
- **10-band Equalizer** with 13 presets + unlimited custom presets
- **DSP Effects**: Bass Boost, 3D Virtualizer, Loudness Enhancer, Reverb (6 environments)
- **Playback speed**: 0.5x–2.0x with pitch preservation
- **Sleep timer** (15/30/45/60 min or custom)
- **4 shuffle modes**: Standard, Balanced Artists, Favorites Priority, Fresh Tracks
- **3 repeat modes**: Off, All, One
- **Audio bookmarks** — save positions to jump back later
- **Swipe gestures** — drag left/right to skip tracks with live preview card

### 📚 Library
- **Auto-scan** device storage on launch
- **Smart playlists**: Recently Played, Most Played, Recently Added, Favorites
- **Custom playlists** with rename, delete, reorder
- **Tag editing** — title, artist, album, genre, year, lyrics
- **Audio trimmer** — create ringtones/clips
- **JSON backup & restore** — never lose your favorites and play counts
- **Search** with 10 filter types (title, artist, album, genre, year, bitrate, file size)

### 🎨 UI & Personalization
- **Bilingual**: English & Persian (full RTL support)
- **14 color themes**: Dark Violet, Cyberpunk, Gold, Lava, Sunset, Forest, Ocean, Rose, AMOLED, Sakura, Mint, Warm Peach, Nordic, Material You
- **5 Glance widgets**: Minimal Player, Full Player, Car Mode, Queue, Stats
- **4 visualizer modes**: waveform, spectrum, circular, bars
- **Custom animations**: Spring, Slide, Fade, Bounce — adjustable speed
- **Font scaling** for accessibility
- **Adaptive layout**: Phone portrait, tablet landscape, car head units

### 🚗 Car Mode
- **Ultra-large touch targets** — 92dp play button for safe driving
- **Voice search** — say song or artist name to play hands-free
- **High contrast AMOLED theme** — eliminates glare in vehicles
- **Quick drive shortcuts**: Bass Boost, Favorites, Recently Played, Shuffle All
- **Live clock** display

### 🔌 Platform Integration
- **Android Auto** via MediaLibraryService
- **System Media Panel** — Android 10+ lock screen & quick settings
- **Bluetooth auto-pause** on disconnect
- **Notification controls** with album art
- **External intent handling** — open audio files from any file manager

---

## 📸 Screenshots

<p align="center">
  <i>Screenshots coming soon — build the app to see it in action!</i>
</p>

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Kotlin** 100% |
| UI | **Jetpack Compose** + **Material Design 3** |
| Audio | **Media3 ExoPlayer** + Android AudioEffect DSP |
| Database | **Room** (KSP) + **DataStore Preferences** |
| Images | **Coil** (Compose-optimized) |
| Architecture | **MVVM** + Repository pattern (manual DI) |
| CI/CD | GitHub Actions |
| Testing | Robolectric, Roborazzi, JUnit |

---

## 🏗 Architecture

```
MainActivity (Single Activity)
├── NavHost (11 routes)
│   ├── HomeScreen ─── HorizontalPager (3 tabs)
│   ├── SearchScreen, StatisticsScreen, SettingsScreen
│   ├── EqualizerScreen, CarModeScreen, AboutScreen
│   ├── FavoritesDetailScreen, AllSongsDetailScreen
├── MiniPlayer (animated bottom bar)
├── NowPlayingSheet (modal bottom sheet — 3 tabs)
└── Dialogs (TagEdit, AddToPlaylist, AudioTrimmer)

TuneCraftMediaService (MediaLibraryService)
├── ExoPlayer (playback engine)
└── AudioFxManager (5 DSP effects — debounced)

Data Layer
├── Room Database (6 entities, 5 DAOs)
├── DataStore Preferences (30+ settings)
└── MusicRepository (scanning + CRUD + backup)
```

---

## 🚀 Building for Release

### Prerequisites
- **Android Studio** Ladybug (2024.2) or newer
- **JDK 17**
- **Android SDK** 34+

### Debug Build
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/`

### Release Build
1. Generate a signing key:
   ```bash
   keytool -genkey -v -keystore my-upload-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```

2. Set environment variables (or the build will fall back to debug keystore):
   ```bash
   export KEYSTORE_PATH=/path/to/my-upload-key.jks
   export STORE_PASSWORD=your_store_password
   export KEY_PASSWORD=your_key_password
   ```

3. Build:
   ```bash
   ./gradlew assembleRelease
   ```
   APK: `app/build/outputs/apk/release/`
   AAB: `app/build/outputs/bundle/release/`

### Running Tests
```bash
./gradlew test                       # Unit tests
./gradlew connectedAndroidTest       # Instrumented tests
```

---

## 📁 Project Structure

```
app/src/main/java/ir/danialchoopan/tunecraftmusicplayer/
├── MainActivity.kt          # Single-activity host + navigation
├── TuneCraftApplication.kt   # Application class (manual DI)
├── data/
│   ├── local/
│   │   ├── TuneCraftDatabase.kt   # Room DB (v2, migration-aware)
│   │   ├── dao/                    # 5 DAO interfaces
│   │   └── entity/                 # 6 Room entities
│   ├── preferences/                # DataStore (30+ settings)
│   └── repository/                 # MusicRepository
├── service/
│   ├── TuneCraftMediaService.kt   # Media3 playback service
│   └── AudioFxManager.kt          # DSP pipeline
├── ui/
│   ├── components/        # Reusable composables
│   ├── navigation/        # Screen routes
│   ├── screens/           # Feature screens (9 total)
│   └── theme/             # 14 color schemes + typography
├── util/                  # Battery helper, audio trimmer
└── widgets/               # 5 Glance widgets
```

---

## 📋 Future Roadmap

- [ ] Cloud sync (playlists & preferences)
- [ ] Gapless playback
- [ ] Crossfade transitions
- [ ] Android Auto full browsing (folder/genre/artist)
- [ ] Song deduplication
- [ ] Collaborative playlists
- [ ] Cover art downloader

---

## 📄 License

```
MIT License

Copyright (c) 2026 Danial Choopan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

---

<div align="center">
  <b>Developed by <a href="https://github.com/danialchoopan">Danial Choopan</a></b><br>
  Built with ❤️ for music lovers
</div>