<div align="center">

<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />

  <h1>Noor Al-Quran · نور القرآن</h1>

  <p>Uthmani Mushaf reader, Juz/Surah navigation and background audio recitations — built with Jetpack Compose.</p>

</div>

---

## Features

| | |
|---|---|
| **604-page Madani Mushaf** | Authentic page boundaries, RTL paging, per-page zoom (font size) and bookmarks. |
| **Reader** | Uthmani Arabic + 11 translation languages, Sajdah markers, AI Tafsir (Gemini), copy/bookmark/share. |
| **Audio** | 30+ Qaris hosted on mp3quran.net, background playback with a media notification, repeat/shuffle, ambient sounds, offline downloads. |
| **Profile & Sync** | Reading progress, cloud backup/restore (Supabase), dark/light/system theme, daily reminders. |
| **Premium** | Plan selection and unlock flow (RevenueCat-ready). |

## Build

```bash
# Debug
./gradlew :app:assembleDebug

# Release (minified + resource-shrunk, R8 full mode)
./gradlew :app:assembleRelease

# Tests
./gradlew :app:testDebugUnitTest
```

A Gradle wrapper is not committed — generate one with `gradle wrapper --gradle-version 9.1` (or open the project in Android Studio, which provisions it automatically).

### Release signing

Credentials are never stored in the repository. `app/build.gradle.kts` reads them from the environment or from a git-ignored `keystore.properties` at the project root:

```properties
KEYSTORE_PATH=/absolute/path/to/upload-keystore.jks
STORE_PASSWORD=********
KEY_ALIAS=upload
KEY_PASSWORD=********
```

If no keystore is configured, the release build is signed with the debug key and Gradle prints a warning — convenient for CI smoke tests, **not** for store uploads.

### Secrets

`GEMINI_API_KEY` (and any other entry) is injected through the Secrets Gradle Plugin from a `.env` file at the project root; `.env.example` documents the expected keys. `.env` is git-ignored.

## Production hardening (this release)

### 1. Compose performance

* **Playback progress is decoupled from the UI state.** Position updates (2 Hz) are published on a dedicated `PlaybackProgress` flow and only the seek-bar composable collects them, so scrolling and navigation no longer recompose the whole tree twice a second.
* **`derivedStateOf` everywhere on hot paths.** `MainContainerScreen`, `MushafScreen` and `WelcomeScreen` observe individual fields (`derivedStateOf`) instead of the aggregate state object; the top bar, mini player, bottom bar and active screen each live in their own recomposition scope.
* **Lifecycle-aware collection** (`collectAsStateWithLifecycle`) stops state collection while the app is backgrounded.
* **Stable keys + content types** on every `LazyColumn`/`LazyRow`/`HorizontalPager` item; pager keeps one neighbour page composed (`beyondBoundsPageCount = 1`).
* **Memoised expensive work**: Uthmani text shaping (`buildAnnotatedString`), ayah grouping, `QuranUtils.formatAyahText` regex cleanup, and the reciter/popular-surah lists are all cached with `remember`.

### 2. Audio engine

* Playback runs on a shared **Media3/ExoPlayer** engine with a tuned `DefaultLoadControl` (20 s minimum buffer / 90 s max / 3 s rebuffer threshold, 30 s retained back buffer) and HTTP timeouts — no more rebuffering between verses on mobile networks.
* A **`SimpleCache`** (256 MB LRU) stores streamed surahs on disk so replays are instant and network-free.
* `PlaybackService` (a `MediaSessionService`) provides **foreground playback**, a media notification, lock-screen/Bluetooth controls, and holds a partial `PowerManager` wake lock **only while audio is playing** (plus `setWakeMode(WAKE_MODE_NETWORK)`); audio focus and headphone-unplug handling are wired through `setAudioAttributes(..., handleAudioFocus = true)` / `setHandleAudioBecomingNoisy(true)`.
* **All heavy I/O is off the main thread**: the 2 MB offline mushaf asset is parsed once on `Dispatchers.IO`, page loads/pre-fetching are deduplicated and dispatched to IO, Room queries are suspend-based, and the download progress callback is throttled to one update per percent.

### 3. First-run onboarding

The intro is shown **only on the very first launch**. Completing or skipping it persists `onboarding_completed` in **DataStore** (`AppPreferencesRepository`), with automatic migration of the legacy `has_seen_welcome` SharedPreferences flag. The welcome screen itself was rebuilt with a smoother settle-time animation, animated page indicators and a gradient call-to-action.

### 4. Release build

* R8 minification + resource shrinking enabled, with a full keep-rule set for kotlinx.serialization, Moshi/Retrofit, Room, Media3, Supabase, Firebase and RevenueCat.
* `configChanges`-free edge-to-edge UI, portrait/landscape agnostic layout, HTTPS-only network policy (`network_security_config.xml`), no cleartext traffic.
* Development/patch scripts and duplicate build artifacts were removed from the repository root; build outputs, keystores and `.env` files are git-ignored.

## Project layout

```
app/src/main/java/com/example/
├── audio/            # NoorAudioEngine (ExoPlayer + cache) and PlaybackService (media session)
├── data/
│   ├── local/        # Room database (ayahs, downloaded audio)
│   ├── model/        # Domain models (Surah, Ayah, Qari, …)
│   ├── preferences/  # DataStore-backed app preferences (onboarding)
│   ├── api/          # Retrofit services (alquran.cloud, Gemini)
│   └── repository/   # QuranData, MushafPageMapper, AudioRepository, Supabase sync
├── ui/
│   ├── components/   # Mushaf page rendering, location picker
│   ├── screens/      # Compose screens
│   ├── state/        # QuranViewModel, MushafViewModel
│   └── theme/        # Material 3 emerald & gold theme
└── util/             # Workers, network monitor, formatting helpers
```

## Credits

Qur'an text and translations are served by [alquran.cloud](https://alquran.cloud); recitations by [mp3quran.net](https://mp3quran.net). Please verify religious content against an authoritative printed Mushaf before publishing.
