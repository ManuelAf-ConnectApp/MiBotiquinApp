# Mi Botiquín App

<img src="commonResources/src/commonMain/composeResources/drawable/app_logo.png" width="96" alt="Mi Botiquín App logo">

[Español](README.es.md) · [Changelog](CHANGELOG.md) · [License](LICENSE) · [Notices](NOTICE)

An Android/iOS personal medication organizer built with Kotlin Multiplatform and Compose.
Accounts, treatments, dose events and inventory are stored locally. Optional Spanish medicine
identification uses CIMA/AEMPS after an explicit lookup. This app records a user's treatment;
it does not diagnose, prescribe or recommend splitting tablets.

## Current implementation and validation

| Area | Implemented in source |
|---|---|
| Local accounts | Registration/login, salted PBKDF2 password hashes, platform secure session storage, Remember me, logout and account deletion |
| Recovery | Enroll a single-use recovery code in Profile after password verification; reset locally with email, code and a new password |
| Medication | Exact tablet quarters and mL amounts, dates, inclusive duration, indefinite/paused treatments, optional dose text and notes |
| Doses | Pending events, detail, Confirm/Skip, actual record time, owner checks and idempotent resolution |
| Notifications | Native Android/iOS scheduling and event-specific tap routing, with continuation after login |
| Stock | Explicit initial count, replenishments, reasoned count adjustments, low-stock threshold, history and first-uncovered-dose forecast |
| Spain scanner | Android Google Code Scanner / iOS AVFoundation, supported Spanish EAN/NTIN extraction, manual CN fallback, optional CIMA lookup and match confirmation |
| UI | Screenshot-led light/dark design and shared accessible form components |

**Validation is partial:** 46 Kotlin/JUnit checks and 11 SQLite checks pass. The iOS camera
adapter and public HTTP client also compile separately with Kotlin/Native. The user confirmed
an earlier authentication build. The complete current Gradle/Compose/KSP build, new Android
scanner dependency, real camera/CIMA integration and native acceptance flows remain unverified.
Do not interpret source implementation as a completed native acceptance test.

## Platforms, stack and project layout

Android minimum SDK is 26; compile/target versions are in [the version catalog](gradle/libs.versions.toml).
Use Android Studio and a JDK compatible with the checked-in Gradle/AGP versions. iOS requires
macOS/Xcode and the deployment/signing settings in `iosApp/iosApp.xcodeproj`.

- `domain`: models, validation, scheduling/forecast rules and repository contracts.
- `data`: Room/SQLite persistence, repositories, secure storage and native notifications/HTTP.
- `presentation`: Compose UI, ViewModels, navigation, resources access and native camera adapters.
- `composeApp`: Android/iOS entry points and Koin bindings.
- `commonResources`: translated strings, dimensions, logo and shared resources.

The existing MVI-style state/intent/effect architecture, Navigation3, coroutines, kotlinx.datetime,
serialization and Koin are retained. Android scanning adds
`com.google.android.gms:play-services-code-scanner:16.1.0`; it has separate Google terms.

## Build and run

Open this checkout in Android Studio, let Gradle resolve dependencies and run `composeApp`.

```sh
./gradlew :composeApp:assembleDebug
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
```

For iOS, open `iosApp/iosApp.xcodeproj`, choose the application scheme/device and configure
local signing. Simulator checks:

```sh
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
```

The full build must generate and review the current Room **v11** schema. Only schemas actually
produced by KSP should be committed; no schema identity hash is fabricated.

## Local data, recovery and privacy

There is no cloud account or treatment synchronization. Each local account has isolated
medication, dose and stock operations. New passwords and recovery codes are stored as salted
PBKDF2 hashes; legacy clear-text passwords are upgraded after a successful login. Session
values use Android Keystore-backed storage or iOS Keychain. This is not a claim that the
entire health database is encrypted. Android backup is disabled in the existing configuration.

After registration, open **Profile → Recovery code**, verify your current password, and keep
the displayed code privately outside the device. Generating another replaces it. A successful
reset consumes it and ends the session without deleting medication. Email is a local account
identifier, not proof of mailbox ownership. Without password and a previously enrolled code,
access cannot be recovered; uninstalling or clearing app data can lose local information.

Profile can explicitly delete the account and its associated data. Medication deletion also
removes its dose/stock history after confirmation. Do not put real health data or secrets into
logs, bug reports or screenshots. The privacy screen is factual product information, not a
legally reviewed compliance policy.

## Stock rules

Configure an initial count from what is physically available **now**. It gets a reference time;
old confirmed doses are not backfilled. Unconfigured stock stays unknown, not zero.
Replenishments and adjustments create traceable movements; adjustments require a reason.
Operation identifiers prevent duplicate retries.

For configured stock, the first successful Confirm transition, balance change and unique
consumption movement commit in one transaction. Reopening, duplicate confirmation, Skip and
dismissing a reminder do not debit again. Insufficient stock leaves the dose pending with an
error; review/replenish or correct the count explicitly. Untracked doses can be recorded
without stock consumption. Counts cannot become negative or overflow.

Amounts use integer thousandths; tablets must be quarter multiples. Units/product identity
cannot silently change once inventory exists. Create separate medication for a different
product/presentation. The forecast identifies the first future scheduled dose not covered by
current stock, within 365 days, assuming no replenishments or omissions. It excludes overdue
pending doses and never performs a debit. Unknown/paused schedules show no invented date.
See [stock validation](docs/stock-validation.md).

## Spanish code identification and optional online lookup

**Scan** reads supported EAN-13/NTIN Data Matrix identifiers or accepts a six-digit national
code manually. Unknown GTIN and unsupported GS1 variants require manual CN; no drug, dosage,
stock or divisibility is inferred. QR/OCR and general international GTIN lookup are not provided.

Pressing **Look up in CIMA** makes a public HTTPS request containing only the requested CN;
no profile, treatment history, photo, lot or serial is sent by the catalogue client. A failed or
offline lookup always leaves manual medication entry available. Confirm the exact presentation
before opening the form. Only name and catalogue provenance are prefilled; quantity, unit and
frequency must be chosen. Package strength is not used as a prescribed dose. Provenance/date
are retained while the verified name is retained; a manually renamed medication loses that
attribution on save.

Android delegates camera reading to Google Play services, without an app CAMERA permission;
the optional scanner module may need an initial download. The SDK is created only when the camera action is pressed.
Google may receive technical usage/performance metrics; see its
[terms and privacy](https://developers.google.com/ml-kit/terms). Manual entry avoids starting
that scanner. iOS requests camera access in context and uses AVFoundation; a missing/denied
camera has a manual fallback. Neither path sends camera images to CIMA.

Source: [AEMPS CIMA REST documentation](https://cima.aemps.es/cima/resources/docs/CIMA_REST_API.pdf),
[AEMPS identifier guidance](https://www.aemps.gob.es/informa/la-aemps-actualiza-la-informacion-a-incluir-en-la-aplicacion-de-notificacion-de-comercializacion-de-medicamentos-y-sobre-dispositivos-de-seguridad/).
Catalogue attribution and retrieval time are shown; no endorsement or dataset redistribution
is implied. See [scanner implementation and checks](docs/scanner-spain.md).

## Notifications and permissions

Android requests POST_NOTIFICATIONS on supported versions. Exact-alarm access is checked;
without it the scheduler uses an approximate mechanism. Boot/time/timezone changes reschedule
future events. Android replenishes its bounded window when alarms fire; iOS plans at most 60
pending alerts and needs the app reopened to replenish that window. OS force-stop, permission
denial and background limits can prevent or delay delivery. A shown notification never means
that a dose was taken. Payloads contain an event reference and visible text is generic.

A tap opens the corresponding detail, continuing after authentication when needed. Another
account cannot read or resolve it. Paused, changed, deleted and already resolved occurrences
are handled separately. [Native acceptance matrix](docs/reminder-validation.md).

## Design references and checks

The following supplied images are **design references**, not screenshots of a validated new build:
[Login](screenshots/login.png), [Register](screenshots/register.png),
[Recovery](screenshots/forgot_password.png), [Settings](screenshots/settings.png).

[Design system](docs/design-system.md) · Android Studio previews:
`presentation/src/commonMain/kotlin/com/connectapp/presentation/preview/DesignPreviews.kt`.

```sh
python3 scripts/run_domain_checks.py
python3 scripts/verify_recovery_sql.py
```

These cached-compiler/SQLite checks do not replace Gradle, Room KSP, Compose compilation or
device testing. The restricted agent environment cannot start Gradle's normal global lock/socket
services; use a normal terminal for the full build. New Google dependencies may need network.
Unsupported database versions 1–4 currently fail to open **without deleting their data**; do not
uninstall as a workaround. Migrations 5→11 are supplied and tested in SQLite; Room migration
validation and current schema export remain pending.

## Remaining work and contributing

Pending: native acceptance/build and license inventory; arbitrary weekday/time schedules;
snoozing, full history/export, archive semantics, notification diagnostics, migrations 1–4,
OCR/general GTIN mapping and a legally reviewed release privacy policy. No cloud sync is planned
by default. Keep changes incremental, preserve existing data, add meaningful domain/persistence
checks and update both READMEs and [CHANGELOG.md](CHANGELOG.md).

Project source uses [Apache License 2.0](LICENSE). Preserve [NOTICE](NOTICE) and inherited
[NOTICE.md](NOTICE.md). Google SDK, platform frameworks and AEMPS information retain their own
terms. Existing authorship/logo attribution and the complete transitive notice inventory need
confirmation before publication; see the explicit TODOs in NOTICE.
