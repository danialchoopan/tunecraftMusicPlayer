# TuneCraft Music Player

<div align="center">

[![Android SDK](https://img.shields.io/badge/Android-SDK%2024--36-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%20100%25-blue.svg)]()
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)]()

A modern, feature-rich Android music player built with Jetpack Compose, Material Design 3, and ExoPlayer.

</div>

## Features

### Playback
- **High-quality audio engine** powered by Media3 ExoPlayer
- **10-band Equalizer** with 13 presets + custom presets
- **DSP Effects**: Bass Boost, 3D Virtualizer, Loudness Enhancer, Reverb
- **Playback speed control** (0.5x to 2.0x) with pitch preservation
- **Sleep timer** with customizable countdown
- **Smart shuffle** modes (Standard, Balanced Artists, Favorites Priority, Fresh Tracks)
- **Repeat modes**: Off, All, One
- **Audio bookmarking** for quick navigation
- **Swipe gestures** to change tracks

### Library Management
- **Automatic media scanning** on startup
- **Smart playlists**: Recently Played, Most Played, Recently Added, Favorites
- **Custom playlist creation** with rename and delete
- **Tag editing** for title, artist, album, genre, year, lyrics
- **Audio trimming** to create ringtones/clips
- **Library backup & restore** via JSON export/import
- **Search** with 10 filter types (title, artist, album, genre, year, bitrate, etc.)

### UI & Experience
- **Bilingual interface**: English & Persian (RTL support)
- **14 color themes**: Dark Violet, Cyberpunk, Royal Gold, Crimson Lava, Sunset Amber, Emerald Forest, Ocean Sapphire, Rose Velvet, AMOLED Pitch Black, Sakura Blossom, Mint Breeze, Warm Peach, Nordic Slate, Material You
- **Adaptive layout**: Phone, Tablet, and Car Head Unit support
- **Car Mode**: Large touch targets (92dp), voice search, high contrast
- **Lyrics viewer** with LRC synchronized display
- **Listening statistics** with weekly charts and top artists/genres
- **5 Glance widgets**: Minimal Player, Full Player, Car Mode, Queue, Stats
- **Visualizer** with 4 modes (waveform, spectrum, circular, bars)
- **Animations**: Spring, Slide, Fade, Bounce — configurable speed
- **Font scaling** for accessibility

### Platform Integration
- **Android Auto** support via MediaLibraryService
- **System Media Panel** (Android 10+ lock screen & quick settings)
- **Bluetooth** automatic pause on disconnect
- **Notification playback controls**
- **External intent handling** (open audio files from file managers)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 100% |
| UI | Jetpack Compose + Material Design 3 |
| Audio Engine | Media3 ExoPlayer + AudioEffect DSP |
| Local Storage | Room Database + DataStore Preferences |
| Image Loading | Coil |
| Architecture | MVVM + Repository pattern |
| DI | Manual singleton (lightweight) |
| CI/CD | GitHub Actions |
| Testing | Robolectric, Roborazzi, JUnit |

## Architecture

```
MainActivity (Single Activity)
├── NavHost (8 routes)
│   ├── HomeScreen (HorizontalPager with 3 tabs)
│   ├── SearchScreen
│   ├── StatisticsScreen
│   ├── SettingsScreen
│   ├── EqualizerScreen
│   ├── CarModeScreen
│   ├── AboutScreen
│   ├── FavoritesDetailScreen
│   └── AllSongsDetailScreen
├── MiniPlayer (bottom bar)
├── NowPlayingSheet (modal bottom sheet)
└── Dialogs (TagEdit, AddToPlaylist, AudioTrimmer)

TuneCraftMediaService (MediaLibraryService)
├── ExoPlayer (playback engine)
└── AudioFxManager (DSP effects pipeline)

Data Layer
├── Room Database (6 entities, 5 DAOs)
├── DataStore Preferences (30+ settings)
└── MusicRepository (media scanning + CRUD)
```

## Building

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 34+

### Steps

```bash
# Clone the repository
git clone https://github.com/danialchoopan/TuneCraftMusicPlayer.git

# Open in Android Studio, then sync Gradle

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Project Structure

```
app/
├── src/main/java/ir/danialchoopan/tunecraftmusicplayer/
│   ├── MainActivity.kt          # Single activity host
│   ├── TuneCraftApplication.kt   # Application class
│   ├── data/
│   │   ├── local/
│   │   │   ├── TuneCraftDatabase.kt
│   │   │   ├── dao/              # Room DAOs
│   │   │   └── entity/           # Room entities
│   │   ├── preferences/          # DataStore preferences
│   │   └── repository/           # MusicRepository
│   ├── service/
│   │   ├── TuneCraftMediaService.kt  # Media3 playback service
│   │   └── AudioFxManager.kt        # DSP effects manager
│   ├── ui/
│   │   ├── components/           # Reusable composables
│   │   ├── navigation/           # NavRoutes
│   │   ├── screens/              # Feature screens
│   │   └── theme/                # Colors, typography, themes
│   ├── util/                     # Utilities
│   └── widgets/                  # Glance widgets
├── src/test/                     # Unit tests
└── src/androidTest/              # Instrumented tests
```

## Future Roadmap

- [ ] Cloud sync for playlists and preferences
- [ ] Gapless playback
- [ ] Crossfade transitions
- [ ] Android Auto browsing (folder/genre/artist hierarchy)
- [ ] Song deduplication
- [ ] Playlist auto-save on app close
- [ ] Cover art downloader
- [ ] Collaborative playlists

## License

Developed by [Danial Choopan](https://github.com/danialchoopan). Built with passion for music lovers.