# Room migration validation

The iOS simulator tests open temporary SQLite files with the production
`createRoomDatabase` builder and KSP-generated `AppDatabase_Impl`. They do not open
or modify the installed app database and do not create an APK.

## Verified paths

| Starting schema | Result |
|---|---|
| v1 | Room migrates to v11, retaining account and medication ownership; the missing legacy password becomes empty |
| v2 | Room migrates to v11, retaining account and medication ownership; the missing legacy password becomes empty |
| v3 | Room migrates to v11, retaining account and medication ownership |
| v4 | Room migrates to v11, retaining account and medication ownership |
| v5 | Room migrates to v11 and validates the complete resulting schema |
| v6 | Room migrates to v11 and validates the complete resulting schema |
| v7 | Room migrates to v11 and validates the complete resulting schema |
| v8 | Room migrates to v11, preserving the existing recovery hash |

Every supported path preserves the account email/password (where present) and medication owner,
legacy decimal quantity and unit. Exact quantity stays unknown until explicitly saved;
no inventory is invented. Versions 1--4 replace text identifiers with deterministic integer
identifiers because the current schema requires them, updating every medication owner reference
in the same migration. The migrated file accepts writes to new columns, closes and reopens
successfully with the updated Room identity, and passes SQLite foreign_key_check.

`data/src/iosTest/kotlin/com/connectapp/data/database/RoomMigrationTest.kt` contains the
real Room tests. Historical DDL and identity hashes come directly from the committed
Room exports, copied into portable Kotlin fixtures by:

```sh
python3 scripts/generate_migration_fixtures.py
python3 scripts/generate_migration_fixtures.py --check
```

The generator does not generate new Room schemas or invent hashes. New current schemas
must still be produced by KSP. The checked fixtures cover exported versions 1–8.

## Commands and evidence

```sh
./gradlew :data:testAndroidHostTest :domain:testAndroidHostTest :data:iosSimulatorArm64Test :domain:iosSimulatorArm64Test
python3 scripts/verify_recovery_sql.py
python3 scripts/generate_migration_fixtures.py --check
```

On 2026-10-26: 69 JVM checks and 77 exercised iOS checks passed, plus 11 auxiliary
SQLite checks. The iOS suite records one additional Keychain case that returns early
when Keychain is unavailable; it does not establish Keychain acceptance.

## Remaining acceptance

- Run upgrade acceptance on Android and on copies of representative installed databases.
- The reproducible physical-device protocol is in [android-migration-acceptance.md](android-migration-acceptance.md).
- Actual legacy user databases may contain cases not covered by synthetic exported-schema fixtures.
- No new independent exports exist for v9/v10; they are tested as intermediate steps in the supported paths.
- Recovery-hash preservation supports the local code-recovery flow. It still requires the user
  to have generated and stored a code before losing the password.
- Native camera, notification delivery, permissions, Keychain and accessibility still need device acceptance.
