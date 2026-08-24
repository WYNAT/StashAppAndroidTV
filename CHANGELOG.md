# Changelog

All significant changes to the Stash App Android TV project are documented in this file (according to the Documentation & Architecture Guidelines).

| Version | Type | Description | Impact |
| :--- | :--- | :--- | :--- |
| **Maintenance** | `Fix` | **Player-Dialog Timeout & Server Compensation Buttons behoben:** 1. Behebung der automatischen Menüausblendung nach 5s beim Öffnen der erweiterten Menüs (Drei Punkte, Einstellungen, Untertitel, Audio etc.): `ControllerViewState` hält nun die Steuerungselemente (`hasActiveDialog`) dauerhaft sichtbar, solange ein Dialog geöffnet ist. Erst nach Schließen des Dialogs startet der Ausblend-Timer wieder. 2. Behebung der Nicht-Bedienbarkeit der Server-Compensation Tasten (-100, -25, 0, +25, +100ms): Fokus wird beim Öffnen des Dialogs nun automatisch mittels `FocusRequester` auf die Tasten gelegt, sodass diese per Android TV-Fernbedienung (D-Pad) sofort ausgewählt und betätigt werden können. | Low |
| **Maintenance** | `Fix` | **Vermeidung von falscher Videowiedergabe nach Filteränderung/Standby:** Behebung des Problems, dass nach Filterwechsel oder Aufwachen aus dem Standby/Energiesparmodus das falsche Video abgespielt wurde, obwohl die Vorschau korrekt war. Ursache: `FilterViewModel` behielt beim Filterwechsel alte `ComposePager`-Instanzen bei und `PlaylistPlaybackPage` nutzte statische ViewModel-Keys (`playlistData`), wodurch alte Pager-Daten vor Abschluss der Neuinitialisierung abgespielt wurden. Behoben durch sofortiges Zurücksetzen (`pager.value = null`) bei Filteränderung, dynamische ViewModel-Keys, Filter-Gleichheitsprüfung in `LaunchedEffect(pager, filterArgs)` und Weitergabe von `initialSceneId` in `Destination.Playlist` zur exakten Zielverifikation. | Medium |
| **Maintenance** | `Improvement` | **Scroll-Performance & Bild-Dekodierungs-Optimierung im Raster:** Stabile Key-Zuordnung (`key = { it }`) in `LazyVerticalGrid` zur Vermeidung unnötiger Recompositionen beim Scrollen. Caching der `ImageRequest` via `remember(imageUrl)` in `CardImage`, Deaktivierung von globalem Crossfade für snappy Thumbnail-Darstellung ohne GPU-Alpha-Animationen, Vergrößerung des In-Memory-Bildcaches auf 35% RAM und Erhöhung des GraphQL-Seitencaches (`ComposePager`) von 8 auf 32 Seiten (800 Elemente). | Medium |
| **Maintenance** | `Fix` | **Szenen-Raster Back-Navigation nach Vorschau:** Behebung des Problems, dass beim Zurückgehen aus der Szenen-Detail/Voransicht das zuvor abgespielte Video statt der ausgewählten Szene fokussiert/angezeigt wurde. Die Zielposition wird jetzt beim Navigieren zur Vorschau (`Destination.Item`) sauber auf dem Backstack für `Destination.Filter` hinterlegt, ohne während des Scrollens unnötige Backstack-Ersetzungen oder Recompositionen auszulösen. | Medium |
| **Maintenance** | `Feature` | **Funscript-Verzögerung direkt im Video-Player anpassen:** Slider-Dialog zur On-The-Fly-Kompensation der Funscript-Latenz (-2000ms bis +2000ms sowie Schnellauswahltasten für -100, -25, 0, +25, +100ms) im Player-Optionsmenü und per Long-Click auf den Gamepad-Button hinzugefügt. SliderBar für negative Wertebereiche korrigiert. | Low |
| **Maintenance** | `Improvement` | **The Handy Silent-Mode & Non-Blocking Verbindungsaufbau:** Völlig lautlose und unterbrechungsfreie Behandlung von Handy-Verbindungen im Hintergrund: Netzwerk-Timeout auf 5s verkürzt, kein Pausieren des Players (`player.pause()`) beim Starten, Unterdrückung sämtlicher Toasts und Stacktraces bei Offline-Geräten, saubere Cancellation-Behandlung und automatische, stille Deaktivierung mit gedimmtem Gamepad-Button. | Low |
| **Maintenance** | `Feature` | **Scene-Bewertungen direkt im Player:** Eine Option "Rate" im Drei-Punkte-Menü des Players öffnet nun ein Dialog-Overlay mit der Stash-Bewertungsanzeige (Sterne/Dezimal). Barrierefreiheit für Fernbedienungen (D-Pad) wurde durch automatische Fokusweiterleitung (`FocusRequester`) und einen expliziten "Cancel"-Button verbessert. Ermöglicht komfortables Bewerten während laufender Wiedergabe. | Low |
| **Maintenance** | `Improvement` | **Video-Vorschau und Fokus-Synchronisierung bei Zurück-Taste:** Wenn eine Playlist im Player verlassen wird (Zurück-Taste), scrollt das Szenen-Raster zum zuletzt abgespielten Video und fokussiert dieses. Die Hintergrund-Vorschau startet dadurch direkt für die aktuelle Szene. Umgesetzt via dynamischer Backstack-Synchronisierung im `NavController` (`setNewBackstack` / `NavAction.Replace`). | Low |
| **Maintenance** | `Improvement` | **Untertitel-Schriftart im Player angepasst:** Leicht transparenter Hintergrund und schwarze Zeichenumrandung (Outline) zur Verbesserung der Lesbarkeit hinzugefügt. Betrifft sowohl die Compose-Player-Ansicht als auch den klassischen XML-basierten Player (`StashPlayerView`). | Low |
| **Maintenance** | `Fix` | **Funscript-Wiedergabe bei Duplikaten & Standby-Fix:** Falls ein Video-Duplikat keine Funscript-Datei hat, sucht die App nun per phash/oshash/checksum nach interaktiven Duplikaten auf dem Server und spielt dessen Funscript ab. Zudem wird der ExoPlayer nach dem Standby mittels `LifecycleStartEffect` neu erstellt, um Hänger zu vermeiden. Die Wiedergabe wird an der korrekten Stelle (`currentPlaylistIndex`) fortgesetzt. | Medium |
| **Maintenance** | `Fix` | **Kritische Playlist-Startlatenz behoben:** Root cause war das sequentielle Laden aller Szenen von Index 0 bis `startIndex` (~34 Netzwerkanfragen bei Index 808). Neue Architektur: Playlist wird jetzt nur in einem Fenster von ±10 Elementen um den `startIndex` aufgebaut (`PLAYLIST_WINDOW=10`). Initiale Anfragen reduziert von O(startIndex) auf O(1). `CodecSupport`- und `StreamDecision`-Cache im `FilterViewModel` implementiert. "Building playlist..." Ladeanzeige ergänzt. | Critical |
| **Maintenance** | `Fix` | Behebung von Git-Tag-Konflikten (`develop`) und Festlegung von `main` als primärem Target-Branch. | Low |
| **v0.8.26** | `Feature` | Video-Wiederholung (Loop): Neuer Button im Querformat & Menü-Option im Hochformat für nahtlose Wiedergabe. | Medium |
| **v0.8.25** | `Fix` | Player Overlay: Erhöhung der Basishöhe (256dp) & Korrektur Marker-Bar Layout zur Vermeidung von Titel-Clipping und Reduzierung von unerwünschtem Scrollen. | Medium |
| **v0.8.24** | `Fix` | Mobile Player: Korrektur der Orientierungs-Logik. Im Querformat werden alle Steuerungen direkt angezeigt, im Hochformat werden sekundäre Buttons in das "Mehr"-Menü verschoben. | Medium |
| **v0.8.17** | `Improvement` | Beschleunigung der Ladezeit beim ersten Öffnen durch Auslagerung von synchronen SharedPreferences-Aufrufen (Commit zu Apply) und Verschiebung des App-Upgrade-Checks in einen Hintergrund-Thread. | Low |
| **v0.8.16** | `Fix` / `Improvement` | Fixed inconsistent card sizes in home slider: info area now has a fixed height (88dp) and icon row visibility is controlled dynamically. Added background image preloading for the next 10 items while scrolling in grid views (Scenes, Performers, Studios, Tags, Groups, Galleries, Images, Markers). | Medium |
| **v0.8.15** | `Fix` | Fixed Handy/Funscript loading: Removed auto-disable logic from setup path which was permanently disabling the integration in SharedPreferences on transient API/network errors. Fixed Game symbol toggle reliability. | Low |
| **v0.8.14** | `Feature` | Optimized startup performance: implemented stale-while-revalidate for server connection, lazy database initialization, and asynchronous app upgrade handling to significantly reduce startup time on slow devices. | Medium |
| **v0.8.13** | `Fix` | Fixed Handy auto-disable persistence by ensuring the integration is deactivated on all failure paths (timeouts/mode-errors) and adding a guard to skip loading if disabled. | Low |
| **v0.8.12** | `Fix` | Fixed Dark Mode visibility in the Handy error dialog by ensuring theme-aware text colors (onSurface/error) on dark backgrounds. | Low |
| **v0.8.11** | `Fix` | ACHIEVED ZERO-WARNING BUILD. Systematically resolved 200+ compiler warnings (unchecked casts, deprecated Focus/SDK_INT APIs, Resolution enums, and redundant null-checks). | Low |
| **v0.8.10** | `Feature` | Finalized The Handy integration: Global*   **Enable/Disable Toggle:** Global switch in Settings (Old UI XML & Compose), and via a Gamepad icon in the Player UI.
*   **Persistent Logic:** The integration remains enabled even if `setup()` fails due to transient network or API errors. This allows retries without manual re-activation in Settings.
*   **Connection Key:** The Handy key must be entered in the UI settings. (both Old & New UI) and detailed error reporting for Handy (Toasts include HTTP/API error details). | Low |
| **v0.8.9** | `Feature` | Added Hardware Test Slider in Settings (both Old & New UI) and detailed error reporting for Handy (Toasts include HTTP/API error details). | Low |
| **v0.8.8** | `Documentation` | Comprehensive documentation update: README.md (Funscript fork), AGENTS.MD (Testing/Coding standards), and translated all comments/docs to English. | Low |
| **v0.8.4** | `Feature` | Added Interactive gamepad icons to Scene Cards and 15s loading Timeout-Toast for Funscripts on Video Start (Playback). | Low |
| **v0.8.3** | `Fix` / `Feature` | Extended error information for The Handy API connection test in both UIs (Compose & XML). Added missing Handy settings to the old UI. | Low |
| **v0.8.2** | `Feature` | Direct The Handy API (Funscripts) integration into ExoPlayer. Introduced HandyManager for REST communication. | Low |

### Design & Structure Documentation (Maintenance)
- **Problem:**
  - Duplicate videos without a funscript file showed the handy/funscript icon (due to interactive status propagation or duplicate identification) but the funscript did not play because the URL was blank/404.
  - Waking up from standby on Android TV caused the player to get stuck because the ExoPlayer was released on stop but not recreated. Additionally, the player reset to `startIndex` rather than resuming at the correct playlist index.
- **Solution:**
  - Added query support for `fingerprints` on `VideoFile` so the client gets hashes (`phash`, `oshash`, `checksum`) of the current scene's files.
  - Implemented `findDuplicateFunscriptUrl` in `QueryEngine` to search for duplicate interactive scenes sharing the same file fingerprints and fetch their funscript.
  - Managed the player instance dynamically in Composable scope using `LifecycleStartEffect` to release/recreate the player on pause/stop and start/resume.
  - Linked player listeners and setup code to `LaunchedEffect(player)` and passed `currentPlaylistIndex` to preserve the correct playlist item when recovering.

### Design & Structure Documentation (v0.8.27)
- **Problem:** Eager stream resolution (calling `getStreamDecision` for every item in a fetched page) was causing ~10s startup latency when jumping to high playlist indices.
- **Solution:** "Demand-Driven" Media Loading.
- **Pattern:** `MediaItem` objects are initialized as "unresolved" placeholders. Real stream resolution is deferred until the item is within the immediate playback vicinity (Current ± 1). 
- **Implementation:** 
    - `resolveMediaItemAt(index)` uses `player.replaceMediaItem` to swap placeholders with resolved streams dynamically.
    - Hooked into `onMediaItemTransition` for proactive pre-fetching and `seekToIndex` for instant startup.
    - Added `buildUnresolvedMediaItem` in `StreamUtils.kt` to standardize placeholder creation.

### Design & Structure Documentation (v0.8.26)

- **Feature:** Video Repeat (Loop) Toggle.
- **Pattern:** Integration via `ExoPlayer.setRepeatMode`. Der Zustand wird über die `PlaybackAction.ToggleRepeat` an das `PlaybackPageContent` signalisiert und über einen reaktiven `isLooping` State an das UI (Overlay/Controls) weitergereicht.
- **UI-Parity:** Implementierung sowohl als direkter Button (Landscape) als auch als dynamischer Menüeintrag im Bottom-Dialog (Portrait) zur Wahrung der Konsistenz auf unterschiedlichen Bildschirmgrößen.
- **Refactoring:** Erhöhung der `PlaybackOverlay` Basishöhe und Entkopplung der `SceneMarkerBar` Höhe, um Layout-Stabilität bei variabler Button-Anzahl zu gewährleisten.

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
