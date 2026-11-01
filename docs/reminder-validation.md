# Dose events and notification navigation

## Implemented behavior

Room v10 adds `dose_events`. The event key combines the medication id and scheduled UTC
instant; the unique `(medicationId, scheduledAt)` index prevents duplicate events for
that occurrence. Rows are created before platform reminders are scheduled. The shared
planner prepares at most 60 upcoming reminders, ordered across the active user's medicines.
The scheduled dose time is distinct from the earlier notification time.

Notification payloads contain only the event reference. Visible reminder text is generic.
Android uses distinct URI identities for alarm and activity PendingIntents; notification tags
are the event ids. `MainActivity` handles both initial intents and `onNewIntent`. iOS stores
the reference in `userInfo` and forwards notification responses through its retained delegate.
Dismissal does not resolve a dose.

The shared pending-navigation holder retains a tap until Splash/session restoration finishes
or login succeeds. The resulting back stack is Home → Dose details. Further reminder taps
replace the detail destination instead of piling up detail screens. The detail repository
checks the active local account and reloads the medication and schedule inside a Room write
transaction. A missing/deleted event or a different account returns no details. A paused or
changed schedule returns an obsolete state with no confirmation actions.

The detail shows the physical quantity and unit, optional pharmacological dose, saved notes,
scheduled time, pending/taken/skipped/obsolete state and the actual resolution time. Confirm
and Skip use a conditional `PENDING` transition inside an immediate transaction. Reopening,
double taps and competing requests return the first final state without changing its time.
Pending details use current medication data; final events retain the quantity and notes that
were recorded when resolved. Medication upserts preserve events. Explicit medication/account
deletion cascades to events; the medication deletion confirmation explains this consequence.

Home lists pending and upcoming events and provides a route to the same detail without
notification permissions. Snoozing remains unimplemented. In v11, confirming with configured
stock atomically adds one debit; skip/open/dismiss never debit. Without configured inventory,
the dose is recorded without backfilling stock. See stock-validation.md.

## Executed checks

- The full data/domain Gradle suites pass on Android JVM host and the iOS simulator:
  68 JVM checks and 72 exercised iOS checks. One additional Keychain test returns early
  when the unsigned test environment lacks Keychain; signed-app acceptance remains pending.
- Six synchronization regressions verify enabled events, cancellation without a session or
  when disabled, account changes during planning, preference changes during planning,
  cancellation despite planning failure, and retaining alarms on transient failure while enabled.
- Fixed a race where disabling notifications during planning could still schedule the old
  result. Synchronization now rechecks the preference before updating platform reminders.
- Eleven auxiliary SQLite checks pass for migrations, foreign keys, owner filtering,
  immutable dose resolution and concurrent stock/event transactions.
- Room KSP generated v11; real Room validates migration fixtures in iOS. See
  [migration validation](migration-validation.md) for evidence and limits.

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest
./gradlew :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
python3 scripts/verify_recovery_sql.py
python3 scripts/generate_migration_fixtures.py --check
```

These commands do not generate an APK. Native notification delivery and UI acceptance
remain separate from repository, migration and synchronization tests.

## Manual acceptance matrix — Android and iOS

Use a test account with at least two medicines, exact physical quantities and different
near-future reminder times. Do not use real health information in screenshots or logs.

| Scenario | Expected result |
|---|---|
| Tap with the app open | Correct medication, amount, unit and actual scheduled time appear |
| Tap with the app in background | Same dose detail opens, with Home as its Back destination |
| Tap after closing the app normally | Splash restores the session or login appears; then the same event opens |
| Tap with Remember me disabled after a restart | Login is required before any dose data is displayed |
| Sign into a different account after the tap | Unavailable state; no original account details or actions |
| Two different reminders | Each opens its own event; no repeated detail-screen stack |
| Confirm twice, reopen, then attempt Skip | One final state and one actual timestamp |
| Skip then reopen | Skipped state; no conversion to Taken |
| Open or dismiss without an action | Event stays pending |
| Edit amount/notes without changing the occurrence | Pending detail reloads the current data |
| Pause, change the occurrence time or delete the medicine | Obsolete/unavailable state; no confirmation |
| Edit quantity/notes after confirming | Previously recorded quantity/notes remain unchanged |
| Deny notification permission | No expectation of a system alert; events remain accessible from Home |
| Reboot/change device clock/timezone on Android | Future alarms are recalculated; stale events cannot be confirmed |
| Resume the iOS app after a long absence | The bounded upcoming notification window is replenished |
| Upgrade a v8 database with medicines | User/medication data survive migrations through v9/v10 to v11 |

## Limits and remaining work

- iOS planning is bounded to 60 pending alerts. Beyond that window the app must be opened
  again; delivery is never assumed to imply a taken dose. Android replenishes its window
  from the alarm receiver as well as normal synchronization.
- OS permission denial, approximate alarm mode, force-stop and background restrictions can
  delay or prevent delivery. Force-stopping an Android app is not an ordinary cold start.
- Device-current timezone determines new planned times; old notifications are revalidated
  against that schedule. Timezone/DST changes require native verification.
- The existing schedule still distributes 1–10 doses per day; custom weekdays and arbitrary
  times remain pending. The pending inbox is bounded and is not a complete dose history.
- Scheduling permission/error presentation still needs further UI work. Native notification
  add failures are not yet exposed as a persistent diagnostic screen.
- Stock ledger integration is implemented in v11: it consumes only a first successful
  Taken transition and records a unique event id in the same transaction. Native acceptance
  remains pending; Room KSP and iOS migration validation pass with historical-schema fixtures.
