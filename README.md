[![Android CI](https://github.com/GenericUniqueness/medlog-android/actions/workflows/android-ci.yml/badge.svg)](https://github.com/GenericUniqueness/medlog-android/actions/workflows/android-ci.yml)

# MedLog — Personal Health Tracker

A private, offline-first Android application for tracking medications, conditions, appointments, and more. All data is stored locally on your device — no network calls, no cloud, no accounts.

## Features

- **Multi-Profile Support** — Track health data for multiple family members
- **Medication Tracking** — Log intake, view history, track dosage changes
- **Condition Monitoring** — Track symptoms with notes over time
- **Appointment Management** — Schedule visits with reminders
- **Health Journal** — Record how you feel with mood tracking
- **Clutter Pad** — Quick scratch notes
- **Custom Sections** — Organize data your way
- **Global Search** — Find anything across all your data
- **PDF Export** — Generate reports to share with your doctor
- **Reminders** — Medication and appointment notifications

## Download

| Build | Link |
|-------|------|
| 🧪 Nightly (latest debug) | [Download from Nightly Release](https://github.com/GenericUniqueness/medlog-android/releases/tag/nightly) |
| 📦 Stable releases | [Releases page](https://github.com/GenericUniqueness/medlog-android/releases) |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Database**: Room (SQLite)
- **Architecture**: MVVM with unidirectional data flow
- **Navigation**: Jetpack Navigation Compose
- **Notifications**: AlarmManager + WorkManager

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 35 (compileSdk)
- Minimum device: Android 8.0 (API 26)

## Build & Run

1. Clone the repository
2. Open in Android Studio: File → Open → select the `medlog-android` folder
3. Let Gradle sync complete
4. Connect an Android device or start an emulator (API 26+)
5. Click Run ▶️ or use: `./gradlew installDebug`

## Project Structure

```
app/src/main/java/com/medlog/app/
├── data/
│   ├── local/
│   │   ├── entity/          → Room entity classes (13)
│   │   ├── dao/             → DAO interfaces
│   │   ├── converter/       → Type converters
│   │   └── MedLogDatabase   → Room database
│   └── repository/          → Repository classes
├── di/
│   └── AppContainer         → Manual DI container
├── ui/
│   ├── theme/               → Material 3 theme
│   ├── components/          → Shared composables
│   ├── navigation/          → Nav graph and routes
│   └── features/
│       ├── profile/         → Profile management
│       ├── medication/      → Medication tracking
│       ├── condition/       → Condition monitoring
│       ├── appointment/     → Appointment management
│       ├── journal/         → Health journal
│       ├── clutter/         → Scratch pad
│       ├── section/         → Custom sections
│       ├── dashboard/       → Home dashboard
│       ├── search/          → Global search
│       ├── settings/        → App settings
│       └── onboarding/      → First-launch flow
├── service/                  → AlarmReceiver, BootReceiver, NotificationHelper
├── worker/                   → RescheduleWorker
├── MedLogApp                → Application class
└── MainActivity             → Main activity with Scaffold
```

## Key Design Decisions

- **Offline-first**: Zero network calls. Every feature works in airplane mode.
- **Profile-scoped data**: Switching profiles switches ALL data. No cross-profile leakage.
- **Manual DI**: Simpler than Hilt for a single-module offline app.
- **AlarmManager for reminders**: Exact timing that survives device restart.
- **Material 3 / Material You**: Modern Android design with dynamic color support.

## Permissions

- `POST_NOTIFICATIONS` — Medication/appointment reminders
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — Exact reminder timing
- `RECEIVE_BOOT_COMPLETED` — Re-schedule reminders after reboot
- `CAMERA` — Photo capture for attachments
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` — File attachments

## CI/CD

- **Every push/PR**: Builds debug APK + runs lint → artifacts available for 30 days
- **Version tags (`v*`)**: Builds debug + release APKs → creates GitHub Release with APKs attached
- **Nightly**: Latest `main` build always available at the `nightly` pre-release tag

## Version

1.0.0
