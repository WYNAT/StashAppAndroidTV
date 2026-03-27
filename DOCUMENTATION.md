# StashAppAndroidTV - Technical Documentation

## Project Overview
 StashAppAndroidTV is a native Android TV client for the Stash server. The project is currently in a transition phase from a classic Leanback-based user interface to a modern Jetpack Compose interface.
 
 ---
 
## Build Infrastructure

The project uses the following build stack:

*   **Gradle**: 9.3.0
*   **Android Gradle Plugin (AGP)**: 9.0.0
*   **Kotlin**: 2.3.0
*   **compileSdk / targetSdk**: 36
*   **Build file**: `app/build.gradle.kts` (Kotlin DSL)

> **Note:** Migration to `android.builtInKotlin=true` (AGP 9.0 built-in Kotlin) is blocked by the `com.google.protobuf` plugin (v0.9.6), which is incompatible with the new AGP Extension API. The `org.jetbrains.kotlin.android` plugin remains in use until the protobuf plugin is updated.

### Static Analysis
*   **Detekt**: Upgraded to **2.0.0-alpha.2**. Configuration: `detekt.yml`.

### String Generation
*   Server-side UI strings are generated at build time from `stash-server/ui/v2.5/src/locales/en-GB.json` via the `generateStrings` Gradle task (`buildSrc/ParseStashStrings.kt`). Output: `app/build/generated/res/server/values/stash_strings.xml`.

---

## Image Preloading (v0.8.16)

To reduce the perception of blank cards while scrolling, background image preloading has been implemented for grid views.

### How It Works
*   Each `StashPresenter` subclass implements `getPreloadUrl(item): String?` to expose its primary image URL.
*   `StashDataGridFragment.preloadImages(position)` is called on every position change and issues `StashGlide.withMemoryCaching().preload()` for the next **10 items** ahead of the current position.
*   Preloaded images are stored in Glide's memory cache and are available immediately when a card is bound.

### Supported Types
Scenes, Performers, Studios, Tags, Groups, Galleries, Images, Markers.

### Configuration
The lookahead count is controlled by `IMAGE_PRELOAD_COUNT = 20` in `StashDataGridFragment`.

---


 ## UI Architecture & Path Separation (Detailed View)

## UI Architecture & Path Separation (Detailed View)

The project maintains two parallel UI implementations. When developing new features or adapting existing logic, **implementations must always be done on both levels simultaneously** to ensure parity. A feature is only considered completed ("Done") when it works identically in both interfaces.

### Comparison of Paths & Components

| Feature / Component | Old UI (Leanback / XML) | New UI (Jetpack Compose) |
| :--- | :--- | :--- |
| **Base Directory** | `app/src/main/java/.../stashapp/` | `app/src/main/java/.../stashapp/ui/` |
| **Main Page / Home** | `MainFragment.kt` | `ui/nav/NavScaffold.kt` & `ui/pages/SearchPage.kt` |
| **Grid Views** | `StashDataGridFragment.kt` | `ui/components/StashGrid.kt` |
| **Cards** | `presenters/ScenePresenter.kt` | `ui/cards/SceneCard.kt` |
| **Detail Pages** | `SceneDetailsFragment.kt` | `ui/pages/SceneDetailsPage.kt` |
| **Player Integration** | `playback/PlaybackSceneFragment.kt` | `ui/components/playback/PlaybackPageContent.kt` |
| **Presentation Logic** | `presenters/StashPresenter.kt` | `ui/components/ItemDetails.kt` |
| **Settings** | `SettingsFragment.kt` | `ui/pages/SettingsPage.kt` |

---

## Implementation Guidelines for Parity

To guarantee a consistent user experience across both UI modes, the following rules apply:

1.  **Logic Encapsulation**: Business logic, API calls (Apollo), and hardware control (HandyManager) must **not** reside directly in the UI components. They must be encapsulated in `util/`, `api/`, or shared `ViewModels` so that both UI levels use the same data foundation.
2.  **Feature Synchronicity**: If a new icon (e.g., the gamepad icon for interactive scenes) is added, this must be implemented in both `ScenePresenter.kt` (Leanback) and `SceneCard.kt` (Compose).
3.  **Navigation**: Navigation is controlled via fragment transactions in the Old UI and via the `Compose Navigator` in the New UI. Both paths must be correctly registered in `RootActivity.kt`.
4.  **Theming**: 
    *   **Leanback**: Uses `themes.xml` and `styles.xml`.
    *   **Compose**: Uses `ui/theme/Theme.kt` and `Color.kt`.
    Colors and spacings should be visually aligned.

---

## Funscript Feature (The Handy Integration)

The Funscript feature allows synchronization of "The Handy" devices directly via the app, without requiring an additional plugin installed on the Stash server.

### Implementation Details
*   **Manager:** `com.github.damontecres.stashapp.util.HandyManager.kt` (Singleton).
*   **API:** Uses **The Handy API v3** (`https://api.handyfeeling.com/bundle/v3`).
*   **Communication Flow:**
    1.  When a video starts, it checks if a Funscript path exists in the metadata.
    2.  `HandyManager.setup(url)` validates the protocol (http/https).
    3.  **Cloud Bridge (Local IP Workaround):** If a local IP is detected (192.168.x.x etc.) and the option is enabled, the script is automatically uploaded to the Handy hosting API to generate a temporary public URL.
    4.  The (possibly bridged) script is configured on the device.
    5.  `HandyManager.play(position)` synchronizes playback.
    6.  Error and status messages are output via detailed dialogs (Leanback & Compose) with error codes and URLs.

### Configuration
*   **Enable/Disable Toggle:** Global switch in Settings (Old UI XML & Compose), and via a Gamepad icon in the Player UI.
*   **Persistent Logic:** The integration remains enabled even if `setup()` fails due to transient network or API errors. This allows retries without manual re-activation in Settings.
*   **Connection Key:** The Handy key must be entered in the UI settings.
*   **Handy Cloud Bridge:** Enables the use of Funscripts on local Stash servers via automatic hosting relay.
*   **Sync:** Provides functions for testing the connection and adjusting the server time (offset correction).

#### Known Issues & Troubleshooting
*   **Unsupported URL / local IP:** Since the Handy Cloud (HSSP) downloads the Funscript from the provided link, it must be accessible from the outside (internet). Local IPs like `192.168.x.x` will not work without the **Cloud Bridge** enabled.
*   **Upload Errors:** If the upload to the hosting API fails, a fallback to the original URL is attempted (which will fail for local IPs).

---
 
 ## Performance Optimization (v0.8.14)
 
 To ensure a fast and responsive startup experience on low-end Android TV devices, several optimization patterns have been implemented:
 
 1.  **Stale-While-Revalidate (Server Connection)**:
     -   The `ServerViewModel` and `StashServer` now support optimistic UI entry. If a server has been previously configured and its settings are cached in `SharedPreferences`, the app will set the `currentServer` and hide the loading state immediately.
     -   The mandatory metadata refresh (`updateServerPrefs`) still occurs in the background, ensuring the UI eventually updates with the latest server configuration without blocking the user.
 
 2.  **Lazy Database Initialization**:
     -   Room database initialization (`AppDatabase`) has been moved from `StashApplication.onCreate` to a lazy property in the companion object. The database is only built on its first actual access (e.g., when a DAO is requested), reducing the initial overhead of `StashApplication`.
 
 3.  **Asynchronous App Upgrade Handling**:
     -   `AppUpgradeHandler` now runs in a background thread. This prevents migration logic (which often involves synchronous I/O) from blocking the main thread during the first start after an app update.
 
 ---
 
 ## Architecture Patterns & Conventions

*   **Dependency Injection:** Manual DI or use of singletons for global managers (e.g., `HandyManager`, `ApolloClient`).
*   **API Communication:** GraphQL via Apollo Android.
*   **Video Player:** ExoPlayer with mpv support via JNI for extended codec support.
*   **Design Decisions:** 
    *   Compose is preferred as the standard for new features.
    *   Critical errors are optionally logged to the Stash server (companion plugin required).

---

## Technical Debt / Roadmap
*   Migration of the remaining Leanback fragments (e.g., `SettingsUiFragment`) to Compose.
*   Unification of player logic between Leanback and Compose.
*   Expansion of Funscript support to other hardware protocols (HDSP/HAMP).
*   Alignment of Handy latency compensation between both UI paths.

 ---
 
-
+## Changelog
+
+| Version | Typ | Beschreibung | Auswirkung (Impact) |
+| :--- | :--- | :--- | :--- |
+| **v0.8.16** | `Feature` | Build-Infrastruktur Upgrade (Gradle 9.3, AGP 9.0, JDK 25, Detekt 2.0) | High (Infrastructure) |
+| **v0.8.14** | `Fix` | Performance Optimierungen (Lazy DB, Startup Speed) | Medium |
+
+---
