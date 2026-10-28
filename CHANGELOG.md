# Changelog

Changes are recorded using the Keep a Changelog categories. No released versions are inferred.

## [Unreleased]

### Dose history and treatment archive — 2026-10-27
- Add an account-scoped dose-history screen for confirmed, skipped and obsolete events.
- Replace treatment deletion from Home with non-destructive archiving: archived treatments stop
  appearing in active lists and generating reminders while their dose and inventory records stay
  available to history and encrypted backups.
- Add Room migration v13→v14, archive persistence in backups, and regression coverage for
  archive isolation and historical doses. Android/iOS compilation and domain/data tests pass;
  no APK was generated.

### Custom schedules and reminder snooze — 2026-10-27
- Add validated custom daily medication times, persisted through Room migration v11→v12 and
  used by reminder planning while retaining the former evenly spaced schedule as fallback.
- Add a persistent, conditional ten-minute snooze for pending doses. It retains the scheduled
  dose and stock state, reschedules notifications from the stored value, and is protected from
  conflicting resolution transitions.
- Add Room migration v12→v13 and real iOS historical-database migration coverage. Domain/data
  tests plus Android and iOS Kotlin compilation pass; no APK was generated.

### Historical database migration — 2026-10-26
- Add non-destructive Room migrations from every exported v1--v4 schema to v5, then through v11.
- Preserve medication ownership when historical text user IDs are converted to the current integer IDs;
  v1/v2 accounts receive an empty password because those schemas did not store one.
- Exercise v1--v8 files with the production Room builder, schema validator, post-migration writes,
  reopen and foreign-key checks on iOS. Validate 77 exercised iOS checks; no APK generated.

### Local authentication — 2026-10-26
- Replace the simulated account source with local Room-backed registration and password login.
- Hash new local passwords with PBKDF2, upgrade valid legacy plaintext passwords on login, and
  reject invalid credentials or duplicate registrations.
- Connect one-time recovery-code enrollment in Profile and local password reset in the UI.
- Remove the simulated email-recovery wording from the user interface and documentation.
- Validate 69 Android JVM checks, 73 exercised iOS checks, Android presentation and app Kotlin
  compilation, without generating an APK.

### Migration and reminder validation — 2026-10-25
- Validate v5/v6/v7/v8→v11 by opening temporary historical SQLite files with the production Room builder on iOS, then writing and reopening them. Validate account/medication preservation and foreign keys.
- Verify an unsupported v4 database remains intact after Room rejects opening it.
- Derive portable migration fixtures from the committed Room exports; add a generator with a --check mode without inventing schema hashes.
- Fix a reminder synchronization race: re-read the enabled preference after planning, and cancel alarms when disabled even if planning fails. Add six shared regression checks.
- Validation: 68 JVM checks, 72 exercised iOS checks and 11 auxiliary SQLite checks pass. Keychain still needs signed-app acceptance; Android upgrade/device acceptance remains pending. No APK generated.

### Integration update — 2026-10-25
- Preserve the current simulated authentication source; adapt session persistence to stable local user IDs so medication, doses and inventory survive logout.
- Integrate pending medication, reminder, stock, scanner and design changes; keep local recovery-code UI and DI disabled for compatibility with simulated access.
- Generate Room v11 with Android/iOS KSP and restore historical generated v6/v7 schemas for migration checks.
- Fix Compose cross-module nullable stock reads, recovery preview compatibility, missing Android backup resource and scanner dependency registration.
- Run SQLite migration tests on iOS/Android devices; the Android host runner cannot load the Android SQLite JNI library on macOS.
- Validation: 62 JVM tests, 61 exercised iOS checks and 11 SQLite checks pass. One additional iOS Keychain case returns without checking storage when Keychain is unavailable; signed-app validation remains pending.
- No new APK generated. Device camera/catalogue/notification/accessibility acceptance remains pending.
- Earlier local recovery/authentication entries below describe the previous implementation, not the currently selected simulated access flow.

### Security
- Scope stock counts, movements and updates to the active local account; reject negative/overflowing balances and silent unit/product replacement.
- Commit first Taken transition, inventory debit and unique event consumption together; rollback all three on failure.
- Restrict catalogue requests to validated CN and the CIMA HTTPS host, without session credentials, photos or package serials; disclose scanner SDK metrics.
- Check event and medication ownership against the active account before reading or resolving a dose.
- Use generic reminder text and identifier-only payloads; re-read current schedules before accepting actions.
- Add local recovery-code enrollment from Profile with current-password verification; persist only a salted PBKDF2 hash.
- Consume recovery codes and replace passwords in one conditional SQL update; clear the session after successful recovery.
- Avoid overwriting recovery codes when upgrading a legacy password hash during login.
- Remove destructive migration fallback for unsupported database versions 1–4.
- Check active account ownership before reading, creating, updating or deleting medication through the repository.
- Limit general medication listings to the active account.
- Clear previous session persistence on successful login and registration; require Remember me for session restoration.

### Changed
- Require explicit stock review/replenishment on insufficient tracked stock; preserve already confirmed untracked doses without retroactive debit.
- Record CIMA source/retrieval time while the verified name is retained; drop attribution for renamed saved medicines.
- Update deletion confirmation to include dose/inventory history and preserve existing license/legacy attribution notices.
- Adopt screenshot-led petrol-teal light/dark palettes, shared shapes, pill actions, outlined auth fields and responsive form layouts.
- Redesign Login, Register, local Recovery and Settings; apply the visual system to Profile, Home, medication and dose detail.
- Use contextual surface app bars, consume scaffold insets and remove duplicate page titles; match Android system-bar contrast to the app theme.
- Restyle real bottom-navigation destinations without adding placeholder tabs or social authentication.
- Route Settings security to Profile and show factual local-data information from Privacy.

### Fixed
- Read the installed platform version in Settings instead of a stale hardcoded value.
- Use medication Upsert instead of REPLACE so editing medication preserves its dose events.
- Separate scheduled dose times from advance-reminder times; cancel legacy Android alarms when switching to event alarms.
- Recreate date/time picker state when opening a dialog, avoiding stale selections across medication forms.
- Accept the same date for start and end; stop silently changing invalid date selections.
- Display dose units in the current interface language and distinguish indefinite/paused treatments.
- Replace the nonfunctional email recovery form with local code-based password recovery and explicit offline limitations.
- Return an explicit unavailable error instead of reporting successful email recovery.

### Added
- Room v11 stock accounts/movements and optional catalogue provenance without inferring legacy inventory.
- Stock management with explicit initial count, reasoned adjustments, idempotent replenishments, threshold badges and bounded shortage forecast.
- Spain-specific EAN/NTIN parsing, native Android/iOS camera adapters, manual CN fallback and optional confirmed CIMA lookup.
- Scan bottom-bar destination and catalogue name-only prefill with manual quantity/unit/frequency selection.
- English README, faithful README.es.md, canonical NOTICE and stock/scanner validation documents.
- Stock/forecast/catalogue regression tests and isolated Kotlin/Native adapter check script.
- Reusable design components, light/dark/compact preview definitions and docs/design-system.md mapping real screenshots to the implementation.
- Room v10 dose events with stable occurrence identity, pending/taken/skipped state and actual resolution time.
- Dose details with exact physical quantity, current medication instructions and idempotent Confirm/Skip actions.
- Android notification intents and iOS userInfo response routing, preserving the event destination through login.
- Home pending/upcoming-dose links and a native acceptance matrix in docs/reminder-validation.md.
- Kotlin repository/navigation tests and concurrent SQL event-resolution tests.
- Exact dose quantities stored in integer thousandths with stable tablet/ml unit codes and tablet quarter selection.
- Explicit indefinite treatment, pause/resume of reminder scheduling, optional saved instructions and separate pharmacological dose text.
- Room 8→9 migration retaining historical quantities without rounding or guessing their units.
- Shared medication validation for dates, duration, frequency and dose precision; same-day treatments count as one day.
- JVM domain and persistence checks runnable with cached compiler/JUnit dependencies.
- Room 7→8 migration adding nullable recoveryHash without replacing accounts or medication.
- Profile recovery-code dialog, password reset form and registration enrollment hint in Spanish and English.
- Kotlin recovery/migration tests and a standalone SQLite verification script.
- Regression tests for medication isolation, session persistence and unavailable recovery.

### Validation
- 46 Kotlin/JUnit checks and 11 SQLite checks pass, including stock rollback and 16 concurrent consumption transactions with exactly one debit.
- iOS camera and HTTP adapters compile in isolation with Kotlin/Native; no real camera/live CIMA/full application validation is inferred.
- Full current Gradle/Compose/KSP build, Room v11 export and Android Google scanner dependency remain unverified in this environment.
- Five required root documents exist. LICENSE is preserved; byte-for-byte comparison with the official URL was blocked by network restrictions. Transitive notice/copyright release TODOs remain explicit.
- Redesign: resource XML/localization parity, diff formatting and static contrast checks pass. Rendered preview, full build and native accessibility verification remain pending.
- Android tests/build could not start in the restricted execution environment: Gradle was denied access to its global lock and, using a temporary user home, to its internal socket.
- iOS tests and device flows remain unverified.
- Eight SQLite verification tests pass, including migration to v10 and 16 concurrent event resolutions with exactly one winner.
- 29 Kotlin/JUnit tests pass for exact amounts, scheduling, persistence, dose repository behavior and notification reference handling using the standalone cached compiler check.
- Resource XML is valid with matching Spanish/English key sets; git diff --check passes.
- Android reminder adapters also compile against cached Android SDK/Core/Koin dependencies; no device execution is inferred.
- The user confirmed compilation of phase 1; the generated Room v8 schema is present.
- The current blocks still require the full Gradle/Compose/KSP build, Room v11 export and native acceptance matrix. Migrations for versions 1–4, broader scheduling, snoozing/export/archive, OCR/general GTIN mapping and release legal review remain pending.
