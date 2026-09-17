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

23 Kotlin test files + 2 test architecture documents.

## Required execution

Run the complete `testDebugUnitTest` suite repeatedly. Any failure is a release blocker until the root cause is understood and the implementation or test contract is corrected deliberately.
