# MedLog — Architecture Summary

## Language & UI Framework
Kotlin + Jetpack Compose. Kotlin is the idiomatic Android language with first-class Compose support; Compose eliminates XML layout boilerplate and enables declarative UI that integrates naturally with ViewModel state flows.

## Database
Room over SQLite. The spec requires offline-first with zero network calls and structured relational data across 13 entities with foreign-key relationships. Room provides compile-time query validation, type-safe DAOs, and Flow-based reactive queries — the standard Android ORM for this exact use case.

## Dependency Injection
Manual service locator via a singleton `AppContainer` created in `MedLogApp`. Hilt adds annotation-processing complexity and build-time cost for a single-module offline app; a hand-rolled container is simpler, debuggable, and sufficient for our scope.

## Navigation
Jetpack Navigation Compose with a `NavHost` in `MainActivity`. Type-safe route objects for each screen. Bottom navigation bar for primary sections (Dashboard, Medications, Conditions, Appointments, More) and a navigation drawer for secondary sections (Journal, Clutter, Sections, Search, Settings).

## Notification Scheduling
`AlarmManager` with `BroadcastReceiver` for exact-timing medication and appointment reminders, plus `WorkManager` for periodic re-scheduling after device reboot. Both persist across restarts; `AlarmManager` gives exact-time delivery that `WorkManager` alone cannot guarantee.

## File Storage
Internal app-specific storage (`context.filesDir`) for file attachments. Each profile gets a subdirectory. Files are referenced by relative path in the `FileAttachment` entity. No scoped storage complications — internal storage requires no permissions.

## State Management
Unidirectional data flow: `ViewModel` exposes `StateFlow<UiState>`, UI collects via `collectAsStateWithLifecycle()`, user events are dispatched as function calls on the ViewModel. Room returns `Flow<List<T>>` for reactive data; repositories transform these into domain-level flows.

## Project Structure
Feature-based package organization under `com.medlog.app`:
- `data/local/entity` — Room entity classes
- `data/local/dao` — DAO interfaces
- `data/local/converter` — Room type converters
- `data/repository` — Repository classes per domain area
- `di` — Dependency injection container
- `ui/features/{feature}` — ViewModel + Compose screens per feature
- `ui/navigation` — Nav graph and route definitions
- `ui/theme` — Material 3 theme
- `ui/components` — Shared composables
- `worker` — Background work (notifications, re-scheduling)
- `service` — Alarm receiver and related services
