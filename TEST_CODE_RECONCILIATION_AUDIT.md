# Cumulative Test ↔ Code Reconciliation Audit

## Scope
This pass starts from `aldaftar-smartledger-final-keyboard-reports.zip` and treats tests as executable contracts that must match the production implementation. The focus is cumulative drift: earlier work added many tests before corresponding production behavior was hardened.

## Findings and actions

### 1. Keyboard helper was tested only through Compose's best-effort controller
The existing helper relied on `SoftwareKeyboardController.show()` after focus. That is not a hard guarantee on Android, especially around Dialog input-connection timing.

**Action:** `KeyboardFocus.kt` now keeps Compose's controller as the primary path and adds a bounded Android `InputMethodManager.showSoftInput()` fallback against the attached Compose host view. The fallback is non-blocking and does not use `SOFT_INPUT_STATE_ALWAYS_VISIBLE`.

### 2. Runtime test did not cover automatic initial focus
The existing runtime test first clicked the amount field, so it verified IME navigation but not the failure mode reported by users: opening the transaction editor without a tap.

**Action:** added `transactionFormRequestsInitialFocusWithoutUserTap()` using the real `AddTransactionFormFields` and its production focus requester/tag.

### 3. Duplicate keyboard-callsite contracts
`KeyboardLifecycleContractTest` and `KeyboardInputSurfaceMatrixContractTest` both independently checked that every callsite specified `autoShow`.

**Action:** removed the duplicate assertion from `KeyboardLifecycleContractTest`. The matrix test is now the single inventory authority and additionally rejects explicit `autoShow = false` on intentional automatic-input surfaces.

### 4. Transaction entry-point parity
The source contract already verifies that the FAB/host and customer-history paths use the same `AddTransactionPopup`, and that popup owns one `AddTransactionFormFields`.

**Action:** retained this as a single-source-of-truth contract and added the runtime initial-focus check at the shared form level. This avoids duplicating the entire popup integration test while still covering the common failure point.

### 5. Report spacing tests had weak boundary coverage
Existing tests checked selected values but did not explicitly protect the two-column boundary (1 vs 2 currencies) and the transition to a second row (2 vs 3).

**Action:** added boundary/invariant tests for `foreignCurrencySectionHeight()` and `comprehensiveSummaryCardHeight()` covering 0/1/2/3 currencies, monotonic growth, and the expected two-column behavior.

## Reconciliation result
- Production UI input surfaces discovered: **19**
- Matrix inventory: **19**
- Automatic keyboard helper callsites: **17**
- Explicit automatic callsites with `autoShow = true`: **17**
- Global `SOFT_INPUT_STATE_ALWAYS_VISIBLE`: **0**
- Blocking `Thread.sleep` / `runBlocking` in UI input surfaces: **0**
- Shared transaction form instances inside `AddTransactionPopup`: **1**
- Transaction popup references in the two required entry managers: **1 + 1**

## Build/test execution limitation
The project wrapper requests Gradle 9.3.1. This environment has no cached Gradle distribution and cannot resolve `services.gradle.org`, so a real Gradle test/build run could not be completed here. No claim of `BUILD PASS` is made.

Static source reconciliation and structural checks were completed after the edits. The added runtime test is intentionally device-side because actual IME visibility is platform behavior and cannot be proven by a JVM source contract alone.
