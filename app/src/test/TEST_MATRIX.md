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

28 Kotlin test files + 2 test architecture documents. The count includes broad-stack financial scenario tests for YER/SAR/USD under every default currency, historical-vs-new data, reciprocal directions, and source-level build hygiene.

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

