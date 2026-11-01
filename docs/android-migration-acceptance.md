# Android migration acceptance

This procedure validates an **existing copy** of an app database on a physical Android device.
It does not uninstall the app, clear its data, modify the original database, upload data, or
generate an APK. Keep the original file until the checks below have succeeded.

## Preconditions

- A development build already installed on an Android device and USB debugging authorized.
- Android Platform Tools (`adb`) available on the computer.
- A copied database from a version v1--v11 installation, including its `-wal` and `-shm` files
  when they exist. Obtain the copy only through an authorized development/debug workflow; a
  production app's private database is deliberately not readable by `adb`.
- Test data only. A medicine name, dose, stock movement and recovery code are sensitive health
  and account data.

## Acceptance steps

1. Record the copied file's SHA-256 checksum and retain it unchanged as the rollback source.
2. Place a second copy where the installed development build opens its database. Do not replace
   the original. Close the app before copying all three files (database, `-wal`, `-shm`).
3. Start the app and sign in with the copied local account. Confirm its treatments, decimal
   quantities, units, notes, paused/indefinite flags and catalogue attribution.
4. Open Home and Stock. Confirm dose history and stock accounts/movements have not been invented
   or discounted during upgrade. Confirm an old treatment can be edited and saved.
5. Close and reopen the app. Confirm data remains and the database opens again. On a debug build,
   inspect `PRAGMA user_version` (expected `11`) and `PRAGMA foreign_key_check` (no rows).
6. Repeat with representative v1--v4 copies, including an account with a non-numeric historical
   identifier. Those identifiers are converted to a current integer ID; all associated treatments
   must remain visible to the migrated account.
7. Restore the untouched original copy to return to the starting point. Report only schema version,
   row counts and failure messages; never attach a real health-data database to an issue.

## Evidence to record

Record device model, Android version, starting schema version, app revision, checksum of the
source copy, final `user_version`, row counts and the outcome of `foreign_key_check`. This is
enough to reproduce a migration defect without disclosing the database contents.
