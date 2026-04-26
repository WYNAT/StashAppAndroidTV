# StashAppAndroidTV Agents Guide

This document serves as a guide for AI agents and developers working on this project.

## Agent Roles

### 1. UI Migration Agent (Compose)
*   **Focus:** Migration of Leanback fragments to Jetpack Compose.
*   **Knowledge:** 
    *   Knowledge of `androidx.leanback` vs `androidx.compose.ui.tooling.preview`.
    *   Use of `ComposeUiConfig` for configuration transfer.
    *   Design specifications from `ui/theme/`.
*   **Goals:** Replacement of `StashDataGridFragment` by `ui/pages/GridPage` (generic).

### 2. Playback & Hardware Agent
*   **Focus:** Video playback, codecs, and external hardware (Funscripts).
*   **Knowledge:**
    *   `ExoPlayer` integration & mpv fallback.
    *   `HandyManager.kt` API communication (REST/HSSP).
    *   Synchronization logic (Server-Time Offset).
*   **Goals:** Optimization of Funscript sync latency and support for additional devices.

### 3. API & Data Agent (GraphQL)
*   **Focus:** Communication with the Stash server.
*   **Knowledge:**
    *   Apollo GraphQL Client.
    *   Handling fragments (`SlimSceneData`, `PerformerData`).
    *   Schema validation against the Stash core.
*   **Goals:** More efficient caching and prefetching of metadata.

## Working Principles (Mandates)

1.  **Architecture Fidelity:** New UI features must be implemented in `app/src/main/java/com/github/damontecres/stashapp/ui/`.
2.  **Backward Compatibility:** The "Old UI" (Leanback) must remain functional and be updated synchronously with the "New UI" (Compose) for EVERY change to features or logic.
3.  **UI Parity (Mandate):** Changes to icons, texts, colors, or interaction logic MUST be implemented in both paths (Leanback & Compose) simultaneously. A feature is only considered "Done" when it is available in both UIs.
4.  **Documentation Obligation:** Any change to the architecture or new features (like Funscript) must be documented in `DOCUMENTATION.md` at the same time.
5.  **Hardware Security:** API keys (such as the Handy key) must never appear in logs or commits (use Android Preferences).
6.  **Compilation:** For compilation, always use the following command and overwrite the file: `.\gradlew.bat compileDebugKotlin > gradle_out.txt 2>&1`
7.  **Verification Mandate:** Unit tests and a full build test (`./gradlew testDebugUnitTest`) MUST be performed after every implementation to ensure stability and prevent regressions across both UI stacks.

## Coding Best Practices

To maintain code quality and support the dual-UI architecture (Leanback and Compose), agents must adhere to the following best practices:

1.  **Reduce Redundancy (DRY):**
    *   Never duplicate business logic, API calls, or state management between the Old UI and New UI. Consolidate logic into shared `ViewModels`, `UseCase` classes, or utilities (like `HandyManager`).
    *   If you find identical code logic in `app/src/main/java/.../stashapp/` and `app/src/main/java/.../stashapp/ui/`, extract it into a shared central class.
2.  **Extract Functionality:**
    *   UI components (Fragments, Compose functions) should be "dumb". They must only handle UI rendering and user interactions.
    *   Extract heavy data processing, Apollo GraphQL mapping (e.g., `SlimSceneData` to UI models), and hardware communication out of the UI layer.
    *   Use Kotlin Coroutines and Flows for unified, testable asynchronous state management across both UI stacks.

## Functional Testing Guidelines

Testing is crucial to verify that both user interfaces work flawlessly and operate on the same underlying logic.

1.  **Dual-Path Verification:** Every feature or bugfix must be functionally tested in both the Jetpack Compose UI world and the classic Leanback XML path.
2.  **External Integrations & Hardware:** When testing features that rely on external APIs, network services, or physical hardware, always utilize mocked endpoints, test environments, or software simulators rather than depending on physical devices or production APIs.
3.  **Data Consistency & Caching:**
    *   Ensure any data schema changes or migrations maintain backward compatibility.
    *   Validate proper caching behaviors and consistent state synchronization across the app when mutating data.
4.  **UI State & Error Handling:** Verify that all UI states—including loading indicators, empty states, error dialogs, and timeouts—are triggered correctly and gracefully handled in both UI paradigms.

## Feature Parity Checklist for Agents

Before marking a task as completed, check:
- [ ] Has the change been implemented in the Compose world (`ui/`)?
- [ ] Has the change been implemented in the Leanback path (Fragments/Presenters)?
- [ ] Does the logic (API, ViewModel, Utils) use shared classes to avoid divergence?
- [ ] Do visual elements (icons, labels) look identical in both UIs?
- [ ] Have all Unit Tests passed (`./gradlew testDebugUnitTest`)?
- [ ] Has the project been successfully built (Build Test)?

## Helpful Entry Points for Agents
*   `RootActivity.kt`: Entry point of the app (Routing between Old/New UI).
*   `util/Constants.kt`: Central preference keys and URL constants.
*   `ui/nav/NavScaffold.kt`: Entry point for the Compose world.
