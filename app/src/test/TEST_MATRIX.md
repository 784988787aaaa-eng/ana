# TEST MATRIX — SmartLedger

## Required invariants

- I01: A rate has a direction.
- I02: Reverse direction is reciprocal of the stored canonical relationship.
- I03: A missing pair is never 1:1.
- I04: No pair is derived through a third currency.
- I05: Exchange conversion is independent of currency rank/order.
- I06: Exchanged transactions retain original currency + historical rate + equivalent + base.
- I07: Unexchanged foreign transactions retain foreign amount and do not enter the local total.
- I08: Changing default currency changes perspective, not historical source facts.
- I09: Fixed recurring templates are immutable financial snapshots.
- I10: Generated recurring transactions copy the template snapshot, not current market rates.
- I11: A due occurrence is not generated twice.
- I12: Trash changes record state, not financial facts.
- I13: Restoring trash does not revalue.
- I14: Backup contains all financial and recurring facts.
- I15: Restore is a snapshot restore, not current-rate recalculation.
- I16: Active reports exclude trash.
- I17: Reports do not collapse independent currencies into an unsupported total.
- I18: BigDecimal is the calculation primitive; Double is never the accounting source of truth.

## Test count

67 Kotlin test files + 2 test architecture documents, including dedicated security responsiveness contracts added in Phase 6. The count includes broad-stack financial scenario tests for YER/SAR/USD under every default currency, historical-vs-new data, reciprocal directions, and source-level build hygiene.

## Required execution

Run the complete `testDebugUnitTest` suite repeatedly. Any failure is a release blocker until the root cause is understood and the implementation or test contract is corrected deliberately.

## Mandatory golden scenarios

1. Default YER:
   - 1 USD = 550 YER; 100 USD = 55,000 YER.
   - 1 SAR = 139.5 YER; 100 SAR = 13,950 YER.
   - Combined balance = 68,950 YER.
2. Default SAR:
   - 1 USD = 3.75 SAR; 100 USD = 375 SAR.
   - 1 YER = 0.0072 SAR; 10,000 YER = 72 SAR.
   - Combined balance = 447 SAR.
   - USD→YER must remain missing when only USD→SAR and YER→SAR exist.
3. Default USD:
   - 1 SAR = 0.266666666667 USD; 100 SAR = 26.666666666700 USD.
   - 1 YER = 0.001818181818 USD; 55,000 YER = 99.999999990000 USD.
4. Historical/new data:
   - Old transaction keeps its historical base and equivalent after changing the default currency.
   - New transaction uses the new default as its base.
5. Unexchanged foreign data remains in its own currency and never silently enters the default-currency total.
6. Every direct pair has a reciprocal read; no third-currency cross-rate is synthesized.
7. Every scenario must be exercised through persistence/DAO aggregation and customer-balance calculation, not only through a standalone conversion function.


## Keyboard / IME input-surface matrix

The keyboard contract applies only to intentional text-entry surfaces, not arbitrary windows or read-only dialogs.

Covered input surfaces (19 source components): currency rate, backup search, business profile name/phone, add customer, add/edit transaction, customer edit, customer-history search, exchange-rate entry, add/rename category, date/time dial entry, main search, license activation/token, security PIN/recovery, currency-symbol settings, and trash search.

For every covered text field the contract requires:
- explicit `keyboardOptions`;
- explicit `keyboardActions`;
- an intentional IME action (`Next`, `Done`, or `Search`);
- no global `SOFT_INPUT_STATE_ALWAYS_VISIBLE` behavior;
- automatic keyboard opening only through `RequestFocusAndShowKeyboard(..., autoShow = true)` at an explicitly intentional entry point.

The add-transaction form is a single shared component. Both the main add-transaction entry point and the customer-details/history entry point must use `AddTransactionPopup`, which owns one `AddTransactionFormFields` instance. This prevents keyboard behavior from diverging between visually equivalent transaction windows.

Runtime device acceptance must additionally verify the matrix on representative Android phones across small/normal/large screens and portrait/landscape, because source contracts cannot prove actual IME rendering or OEM keyboard behavior.

## Phase 3 — startup, windows, backups, quota and lifecycle integrity

### Window open/close and motion
- W01: Every intentional input/dialog surface opens from a single deterministic state; no half-rendered content is visible to the user.
- W02: Enter motion is short (160ms for standard dialogs) and exit motion is shorter (120ms); dismissal is delayed until exit completes.
- W03: Back, outside-dismiss, explicit close and successful submit all execute the same keyboard/focus cleanup path.
- W04: Repeated open→close→open does not accumulate duplicate overlays, focus requests, keyboard controllers, or stale state.
- W05: Rotation/background-resume does not reopen a dismissed window or replay its entrance indefinitely.

### Startup / no-flash data rendering
- S01: Splash remains until settings are loaded; the app never exposes the default empty settings frame first.
- S02: First meaningful screen is rendered from loaded state; no placeholder-to-real-data flash is accepted for the main ledger.
- S03: Search/list/report surfaces do not briefly display stale or empty data before their authoritative Flow/Paging state arrives.
- S04: Cold start, warm start and process recreation are tested separately.

### Welcome onboarding
- O01: Fresh install: welcome appears once.
- O02: Dismissal persists `isFirstLaunch=false` and `onboardingShown=true` in one settings write.
- O03: Relaunch after dismissal: welcome never appears.
- O04: App update over the same data: welcome never appears again.
- O05: Restore a backup with onboarding flags from another installation: current installation lifecycle flags win; welcome is never reset by restore.
- O06: Rotation/recomposition while welcome is open cannot create a second copy.

### Daily local/cloud backup
- B01: One unique daily WorkManager job exists after scheduling repeatedly.
- B02: Local archive is created before cloud upload.
- B03: Cloud disconnected: local backup remains successful and usable.
- B04: Temporary cloud/network failure: local backup remains; WorkManager retries the cloud path.
- B05: Concurrent triggers cannot create two daily archives for the same run.
- B06: Backup is encrypted, authenticated, atomically written, and contains the complete financial snapshot.
- B07: Restoring backup never resets trial usage or installation-local onboarding state.
- B08: Daily schedule is recalculated from local device date/time and survives process/device restart through WorkManager.

### Trial quota / trash state machine
The authoritative rule is: **one logical created record = one slot**. An arithmetic expression is evaluated before persistence and, when saved as one transaction, consumes exactly one slot.

State machine for a transaction:
`ACTIVE (1) → TRASH (1) → PERMANENTLY_DELETED (0)`.

Required invariants:
- Q01: Creation at usage 99 succeeds and ends at 100.
- Q02: Any creation at usage 100 is rejected immediately before persistence and emits the license-required event.
- Q03: A transaction moved to trash does not decrement usage.
- Q04: A transaction permanently removed from trash decrements usage exactly once.
- Q05: Restoring a transaction moves its representation from trash to active without changing total usage.
- Q06: Restoring one transaction from a customer bundle keeps total usage unchanged; the remaining bundle count decreases by one while active count increases by one.
- Q07: Restoring the final bundle removes the trash bundle and leaves usage unchanged.
- Q08: Customer + opening transaction reserves exactly two slots atomically; if insufficient capacity exists, neither record is created.
- Q09: Concurrent create requests cannot oversubscribe the 100-slot trial because the authorization check and mutation are serialized.
- Q10: A soft-deleted logical record is not counted once as active and again as trash in normal state transitions.
- Q11: Recurring creation uses the same quota gate and cannot partially apply when the available capacity is insufficient.
- Q12: Paid licenses bypass the trial gate without changing the trial mirror semantics.

### Device acceptance matrix
Run the UI/instrumentation subset on representative Android devices/emulators:
- API 24, 28, 30, 33, 35/36.
- Small, normal and large screens; portrait + landscape.
- 60Hz and 90/120Hz where available.
- Arabic RTL enabled.
- Gboard plus at least one OEM keyboard where the device provides it.
- Cold start after force-stop; warm start; process recreation; low-memory recreation.
- Open/close every intentional input/dialog surface 10 consecutive times; no duplicate overlay, stuck keyboard, visible blank frame, or state regression is accepted.
- Capture frame timing with Macrobenchmark/JankStats in the release candidate. Acceptance target: no startup visible-content flash; dialog transitions should stay within the defined motion budget and avoid missed-frame bursts under normal device conditions.


## Phase 6 — Security responsiveness and tactile feedback

- SEC01: Lock keypad has immediate pressed-state visual feedback; no invisible tap state.
- SEC02: Lock keypad emits a lightweight keypress haptic on digit/delete/functional actions where supported.
- SEC03: The lock screen does not add an artificial fixed delay before user interaction.
- SEC04: Four-digit verification runs strong PIN verification off the UI thread.
- SEC05: Security setup performs strong PIN/recovery hashing off the UI thread and does not mark save complete before persistence completes.
- SEC06: Security setup and active-security actions do not fall back to an unintended opaque white button surface.
- SEC07: Editing security does not immediately toggle edit mode off due to a duplicate state assignment.
- SEC08: Security UI remains responsive while cryptographic work is in progress; final device acceptance requires release-build frame/latency measurement.
