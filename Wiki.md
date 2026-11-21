# Mi Botiquín Wiki

## Purpose

Mi Botiquín is a cross-platform app for organising treatments, recording doses, and tracking
medication stock. Information is stored locally on the device: the app does not synchronise data
to the cloud and does not replace advice from a healthcare professional.

## Features

- Local sign-up, sign-in, and one-time-code account recovery.
- Treatment creation and editing with dose, unit, frequency, dates, schedules, and notes.
- Indefinite, paused, and archived treatments.
- Reminders and pending-dose management: confirm, skip, or snooze a dose.
- The Home screen shows the number of pending doses for each medication.
- Per-medication inventory: initial count, replenishments, adjustments, low-stock threshold, and
  history.
- Optional lookup of Spanish medicines through their national code and CIMA/AEMPS.

## Basic usage

1. Create a local account or sign in.
2. Add a medication and enter its amount, unit, schedule, and dates.
3. Set up stock if you want to track available medication.
4. Use Home to check the pending-dose count for each treatment.
5. Open a dose from a reminder or the history to confirm, skip, or snooze it.

Confirming a dose deducts stock only when stock is configured and sufficient. Skipping a dose
does not change stock.

## Privacy and data

Accounts, treatments, doses, and stock movements are kept in the local database. The session
uses each platform's secure storage. CIMA lookup only runs after an explicit action and sends the
requested national code, not account data or medication history.

See [README.md](README.md) for complete limitations and details, and
[docs/privacy-and-data.md](docs/privacy-and-data.md) for privacy information.

## Architecture

| Module | Responsibility |
|---|---|
| `domain` | Models, validation, schedules, and repository contracts. |
| `data` | Room/SQLite, repositories, session, notifications, and catalogue. |
| `presentation` | Compose screens, ViewModels, and navigation. |
| `composeApp` | Android/iOS entry points and dependency injection. |
| `commonResources` | Translations, dimensions, and shared resources. |

The project uses Kotlin Multiplatform, Compose Multiplatform, Room, Koin, and coroutines.

## Development

Main requirements: JDK 17 or later, Android Studio, Android SDK API 37 for Android, and Xcode
for iOS.

```sh
./gradlew :composeApp:assembleDebug
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
```

For iOS, open `iosApp/iosApp.xcodeproj`, select a target, and configure local signing.

## Related documentation

- [Contributing guide](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)
- [Open tasks](docs/TASKS.md)
- [Reminder validation](docs/reminder-validation.md)
- [Stock validation](docs/stock-validation.md)
- [Spanish scanner and catalogue](docs/scanner-spain.md)
- [Backup and restore](docs/backup-and-restore.md)
- [License](LICENSE) and [third-party notices](NOTICE)
