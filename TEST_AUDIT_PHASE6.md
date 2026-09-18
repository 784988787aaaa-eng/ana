# Phase 6 — Full Test & Responsiveness Audit

## Scope

This audit covers the cumulative Phase 5 project: financial correctness, currencies (YER/SAR/USD), historical rates, reports, PDF, the existing CSV/XLSX export path, keyboard/input surfaces, startup, dialogs, backups, trial quota, recurring scheduling, trash/restore, business profile, security/PIN, haptics and responsiveness.

## Existing coverage found in the project

- Reports: PDF/report layout, currency catalog, foreign totals, header spacing, currency invariants, report coroutine lifecycle, PDF share lifecycle, and CSV/XLSX integrity contracts.
- Input: keyboard/IME matrix for 19 intentional input surfaces, lifecycle, passive/read-only exclusion, performance source contracts, and one instrumentation IME runtime contract.
- Startup: rendering/no-flash and onboarding lifecycle/source contracts.
- Transactions: money semantics, customer history calculation, precision boundaries, currency direction, historical/default-currency invariance, stress/cycle scenarios, and a financial golden scenario integration contract.
- Recurring scheduling: configuration, snapshot immutability, schedule calculation, exhaustive schedule coverage, idempotency, execution and integration contracts.
- Backup: encryption, path management, atomic/tamper protection, round trip, restore integration, daily scheduling, and trash/recurring integration.
- Trash: persistence, serialization, restore round trip, financial snapshot and exhaustive state contracts.
- Trial quota: lifecycle and source contracts, including active/trash semantics.
- Security: hash utility unit tests exist, but prior to Phase 6 there was no dedicated security UI responsiveness contract suite.

## Phase 6 additions

- Dedicated security responsiveness/visual feedback contract suite.
- Lock keypad pressed-state feedback and touch indication.
- Removed heavy per-digit bouncy header animation from the lock screen.
- Preserved haptic keypress/success/error feedback and Android vibrator fallback paths.
- Security setup hashing remains strong PBKDF2 and runs away from the UI thread; independent PIN/recovery derivations can run concurrently.
- Removed an accidental edit-mode true→false toggle in the security screen.
- Security action buttons now explicitly use application surfaces instead of relying on an opaque/default white container.

## Important limitations

Source contracts can prove architectural intent and catch regressions, but cannot prove OEM keyboard latency, actual frame timing, or physical haptic strength. Those require release-build instrumentation on representative devices.

The Gradle wrapper requires Gradle 9.3.1. If the execution environment cannot download that distribution, a full Gradle suite must be reported as not executed rather than reported as passing.

## Release acceptance gates

1. `testDebugUnitTest` passes completely.
2. Android instrumentation IME tests pass on representative API/device sizes.
3. Release build cold/warm start has no visible default-state flash.
4. Lock keypad digit tap has immediate visual response and tactile feedback; no artificial UI delay.
5. Four-digit verification keeps the UI responsive while strong verification runs off the main thread.
6. Security setup/save shows a clear saving state until persistence actually completes.
7. Report totals remain separated by YER/SAR/USD and never perform unsupported cross-currency addition.
8. Historical exchange rates remain attached to historical transactions after default-currency changes.
9. Existing CSV/XLSX export remains the only export path; no parallel Excel feature is introduced.
10. The exported workbook recalculates formulas when editable source cells are changed, while formula/result cells are protected against accidental edits without a password.
