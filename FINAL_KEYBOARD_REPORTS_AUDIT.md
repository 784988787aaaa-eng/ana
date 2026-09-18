# Final Keyboard & Reports Audit — SmartLedger

## 1. Keyboard / IME

### Root cause found
The transaction popup already had `FocusRequester`s and IME actions, but it did **not** explicitly invoke the shared automatic focus/IME helper for its initial amount field. Therefore the two transaction entry routes could open the same UI while relying on platform focus timing, which is not deterministic inside a Compose `Dialog`.

A second issue was that the shared animated dialog did not explicitly configure the **actual Android Dialog window** for IME resize. Activity-level `adjustResize` alone does not guarantee identical behavior for a separate Compose dialog window.

### Final implementation
- `AddTransactionPopup` now explicitly gives initial IME ownership to `amountFocusRequester` with `autoShow = true`.
- This is located inside the dialog content, so it targets the actual dialog window.
- Both transaction entry routes remain on the same `AddTransactionPopup` / `AddTransactionFormFields` path.
- `MizanAnimatedDialog` now configures its Android dialog window with:
  - `SOFT_INPUT_ADJUST_RESIZE`
  - `SOFT_INPUT_STATE_UNSPECIFIED`
- The window configuration was extracted into `ConfigureDialogImeWindow()` to avoid duplicated window/IME code.
- The same dialog-window IME configuration is applied to the two custom input dialogs:
  - `CurrencySettingsDialog`
  - `ExchangeRateSetupDialog`
- The existing shared keyboard helper remains lifecycle-safe:
  - bounded retry (maximum 3)
  - frame-scale retry delay
  - no `SOFT_INPUT_STATE_ALWAYS_VISIBLE`
  - focus/IME cleanup on dismissal/disposal
- No blocking sleeps or `runBlocking` were introduced.

## 2. Transaction IME flow

The transaction form remains:
1. Amount — Decimal + `Next`
2. Description — `Done`
3. `Next` moves to Description
4. `Done` clears focus and hides the IME

The amount field is now also the explicit initial focus owner when the transaction dialog opens, including opening from customer details.

## 3. PDF report spacing audit

A real layout inconsistency was found in the comprehensive PDF report:

- The renderer places the table header at `summaryEndY + 10`.
- The table header is 24pt high.
- The first data row starts 30pt after the table header.
- The dry-run pagination calculation previously reserved only `10 + 26 + 4 = 40pt` after the summary card, while the real renderer consumed `10 + 24 + 30 = 64pt`.

This could cause the dry-run page count to underestimate space and produce inconsistent page-break decisions.

### Fixed
- Centralized table-header height and first-row offset in `PdfReportLayoutSpec`.
- Dry-run pagination now uses the same geometry as the real renderer.
- Actual rendering uses the same centralized first-row offset.

## 4. Comprehensive foreign-currency card whitespace

The comprehensive summary card previously reserved a fixed extra bottom region after its last foreign-currency card.

For one foreign-currency row, the actual card ended around 94pt from the card top, while the calculated card height was 122pt — leaving about 28pt of unnecessary empty space.

The sizing formula is now content-driven:
- one/two currencies: 100pt
- three/four currencies: 158pt
- grows by one row height only when another visual row is actually required
- retains a small designed bottom breathing room

This removes the oversized blank tail without compressing the visible content.

## 5. Individual foreign-currency summary

The bottom padding of the individual-customer foreign-currency section was reduced from 8pt to 4pt. The cards themselves keep their 68pt content height.

## 6. Excel report whitespace

Only explicit spacer rows were adjusted; row numbers and formulas were deliberately left unchanged.

Reduced spacer heights:
- Single-customer transactions/summary sheets: 10/12/14pt spacers → 6pt
- All-customers account/foreign/summary sheets: 10/12/14pt spacers → 6pt
- Empty-state trailing spacer: 24pt → 6pt

No data rows, formula references, merges, filters, or row indices were shifted.

## 7. Regression contracts added/updated

Added or strengthened source-level contracts for:
- Dialog window IME resize configuration
- Explicit transaction-popup initial keyboard ownership
- Compact comprehensive PDF card sizing
- Excel spacer-row compactness

Existing contracts continue to enforce:
- no global `ALWAYS_VISIBLE` IME
- explicit `autoShow` at every automatic keyboard call site
- shared transaction popup/form architecture
- two-step transaction IME flow
- 19-input-surface inventory

## 8. Verification

### Passed in this environment
- No empty production source files detected.
- Changed Kotlin files have balanced braces.
- All 17 automatic keyboard-helper call sites explicitly declare `autoShow`.
- No old comprehensive dry-run spacing formula remains.
- No old `tableHeaderY + 30f` hardcoded first-row offset remains.
- `PdfReportLayoutSpec.kt` compiled independently with `kotlinc`.
- `XlsxOpenXmlBuilder.kt` compiled independently with `kotlinc`.

### Not honestly claimable here
A complete Android Gradle build/test run could not be executed because the project wrapper requires Gradle 9.3.1 and this environment cannot reach `services.gradle.org`.

Therefore this package does **not** claim a full `assembleRelease` / `testDebugUnitTest` PASS. Device-level IME behavior also remains a runtime verification item, as required by the project's own test plan.

## Result

The implementation removes the identified duplicate/fragile IME ownership path, makes dialog-window resize explicit, fixes the transaction popup's missing initial IME request, and aligns PDF dry-run geometry with actual rendering. Report whitespace is reduced conservatively without changing data or formulas.
