# Changelog

All significant changes to the Stash App Android TV project are documented in this file (according to the Documentation & Architecture Guidelines).

| Version | Type | Description | Impact |
| :--- | :--- | :--- | :--- |
| **v1.x.x+7** | `Fix` | Eliminated all compiler warnings: removed always-true null guards, fixed `View?` type mismatches, suppressed DEPRECATION for `versionCode`/`setDiagnosticStackTraceEnabled` (pre-P branch & missing API), suppressed UNCHECKED_CAST for JSON structure casts, removed unnecessary `lateinit` and safe calls. | Low |
| **v1.x.x+6** | `Feature` | Added global Enable/Disable toggle for The Handy integration (Auto-disable on error) and Gamepad Player UI quick toggle for immediate script loading in both UIs. | Low |
| **v1.x.x+5** | `Documentation` | Added detailed Funscript fork feature documentation to README.md | Low |
| **v1.x.x+4** | `Documentation` | Enhanced AGENTS.MD with functional testing and coding best practices guidelines | Low |
| **v1.x.x+3** | `Documentation` | Translated MD files and comments into English | Low |
| **v0.8.6** | `Documentation` | Added technical documentation (`DOCUMENTATION.md`) and the `AGENTS.MD` guide. Detailed UI paths and the Funscript feature. | Low |
| **v0.8.5** | `Fix` / `Feature` | Fixed Interactive gamepad icon logic (using `interactive` flag). Corrected loading Toast and timeout for Funscripts in `PlaybackSceneFragment.kt`. Refactored `HandyManager.setup` to `suspend`. | Low |
| **v1.x.x+1** | `Feature` | Added Hardware Test Slider in Settings (both Old & New UI) | Low |
| **v1.x.x+2** | `Refactor` | Detailed error reporting for Handy (Toasts include HTTP/API error details) | Medium |
| **v0.8.4** | `Feature` | Added Interactive gamepad icons to Scene Cards and 15s loading Timeout-Toast for Funscripts on Video Start (Playback). | Low |
| **v0.8.3** | `Fix` / `Feature` | Extended error information for The Handy API connection test in both UIs (Compose & XML). Added missing Handy settings to the old UI. | Low |
| **v0.8.2** | `Feature` | Direct The Handy API (Funscripts) integration into ExoPlayer. Introduced HandyManager for REST communication. | Low |

### Design & Structure Documentation (v0.8.5)

- **Feature:** Corrected Interactive Funscript detection.
- **Pattern:** Using the `interactive` boolean field from GraphQL fragments (`SlimSceneData`, `VideoSceneData`, `FullSceneData`, `MinimalSceneData`) instead of checking for existing Funscript URLs. This prevents the gamepad icon from appearing on all scenes.
- **Refactoring:** Converted `HandyManager.setup` to a `suspend` function to properly wait for the API response during playback initialization with a 15-second timeout.
- **Build Fixes:** Added `interactive` field to the `Scene` data class (with a default value) and manual fragment instantiations in `Constants.kt` and `PreviewUtils.kt`.

### Design & Structure Documentation (v0.8.4)

- **Feature:** UI feedback for Interactive Funscripts.
- **Pattern:** Using `Toast` with a 15-second Coroutines timeout (`withTimeoutOrNull` and `Dispatchers.IO`) inside `PlaybackSceneFragment.kt` to ensure HandyManager initialization does not hang playback and informs the user.
- **Iconography:** Integrated `fa_gamepad` from FontAwesome to identify interactive scenes via `IconRowText` in `SceneCard.kt`.

### Design & Structure Documentation (v0.8.2)

- **Feature:** Local Funscript support during video playback. The app reads the `scene.funscriptUrl` (or `scene.paths.funscript` in GraphQL) field from the server.
- **Pattern (The Handy API):** The connection is managed entirely on the client side in StashAppAndroidTV (no plugin needed on the Stash server). A new singleton/manager `HandyManager.kt` uses `OkHttp` to synchronously send asynchronous requests (`/setup`, `/play`, `/stop`, `/servertime`) to `api.handyfeeling.com/api/handy/v2`.
- **Pattern (ExoPlayer Listener):**
    - Added `setupHandy` function in `PlaybackViewModel` with toast notifications and 15s timeout.
    - Added a `Player.Listener` to synchronize play/pause/seek events with The Handy API.
    - Added logic to pause the video during loading and resume after.
    - In the **new UI (Compose)**, loading notifications (Toast) and the 30-second timeout have been implemented. Additionally, the video is now paused during loading.
- Playback synchronization (Play/Pause/Seek) was added for the new UI.
- In the **old UI (Leanback)**, the toast messages and the timeout were corrected to 30s, and the video also pauses during loading.
- **HandyManager:** 
    - Timeouts for OkHttp increased to 30s.
    - Added `setMode(1)` (HSSP) before setup.
    - **New:** Hardware test option in the UI settings (moves 0 -> 100 in 3s).
    - Extended logging for setup errors.
- **Deviations / Technical Debt:** The hardware ID (Connection Key) is stored as a simple string in the settings. The Android TV Leanback Framework often has issues with native `EditTextPreference` in XML files. To avoid silent drops and caching issues, the input field for the Connection Key is bound programmatically to the playback category in `SettingsFragment.kt`.
