# Inventory implementation and validation

## Data and transaction rules

Room v11 adds stock_accounts and stock_movements, retaining medication/dose/account rows.
Migration does not infer initial inventory or consume previously confirmed doses. The user
counts the current physical stock; the initial row records that reference time, balance,
unit and user-chosen low threshold. Zero is valid; absence of an account means unknown.

Amounts are exact thousandths of the physical unit, with quarter multiples for tablets.
The balance is maintained alongside a traceable ledger in the same immediate transaction.
Each movement stores kind, signed delta, requested count, reason/time and an operation id.
Replenishment ids are replay-safe; count adjustments require a nonblank reason. A unique
nullable eventId on movements prevents more than one consumption for an occurrence.

Dose confirmation verifies ownership/current schedule and available inventory. The first
PENDING→TAKEN transition, balance change and consumption insert share the same transaction.
Insufficient balance or any write failure leaves both dose and inventory unchanged. Skip,
opening, dismissing and repeated final actions do not consume. A dose confirmed while stock
is untracked remains untracked historically; later configuration does not backfill it. A still
pending dose confirmed after configuration consumes stock under the normal current rule.

Inventory unit and product identity cannot silently change through medication editing.
Notes, dose quantities, timing and pause can be edited; a different product/presentation needs
its own medication. Explicit medication/account deletion cascades to stock and dose history.
No archive/export or inventory push alert is implemented. Low stock is a visible threshold badge.

## Forecast definition

The estimate is the first future dose that current stock cannot cover, using the current
fixed quantity/schedule and device timezone. It does not use advance notification times,
assume consumption of overdue pending events or mutate stock. It assumes no future omitted
doses/replenishments and has a 365-day / bounded-occurrence horizon. Paused, unknown and
unconfigured states have explicit no-estimate outcomes. A finite treatment may end with enough
stock. The daily figure is planned physical consumption, not confirmed administration.

## Executed checks

The shared cached-compiler suite now contains 46 checks across the application; stock coverage
includes explicit initialization, no backfill, retries, reasons, thresholds, isolation, exact
fractions, overflow, unit/product lock, competing confirmations, skip behavior and rollback.
Eleven SQLite checks include migration 10→11, balance/ledger consistency, unique event debit,
16 competing consumption transactions and failure-trigger rollback of dose plus inventory.

The Kotlin data-source test double serializes transactions and restores ledger/event/balance
snapshots on failure. The SQL tests separately use the actual production statements and SQLite
foreign keys. They do not replace Room KSP migration validation or native device testing.

## Manual matrix — not executed here

1. Upgrade a populated v8/v9/v10 database. Verify original rows, no auto-created inventory,
   and no discounts for already confirmed events. Generate/review the Room v11 export.
2. In Home → Stock, configure a count and threshold in tablets and in mL. Check zero,
   quarter quantities, decimal comma, invalid precision and negative values.
3. Replenish; repeat the submit/retry; verify one movement and one balance change. Adjust
   to an absolute count with a reason and confirm the signed difference/history.
4. Configure exactly ½ tablet, confirm a ½-tablet dose and verify zero. Reopen/confirm/skip
   again and ensure no extra movement. Confirm another pending dose with zero stock:
   it must stay pending. Replenish explicitly and retry.
5. Open/dismiss/skip a reminder without taking; the inventory must stay unchanged.
6. Change users and attempt an old dose/stock destination; no foreign data/actions are exposed.
7. Try to change a tracked unit/product identity; saving must fail without altering stock.
   Edit only notes/schedule/quantity and verify preserved history and updated future forecast.
8. Compare the forecast for same-day finite, indefinite, paused and missing/legacy schedules.
   Check the explicit assumptions, horizon and low-stock threshold labels.
9. Verify history link → recorded dose → Home, Stock from a dose and return, large fonts,
   keyboard, light/dark theme and both translations on Android/iOS.
