# Phase 7 — Release Build Fix

This phase fixes the concrete Kotlin compilation errors reported by the Release build screenshot.

## Fixed compile blockers

- PDF renderers passed `Float` card widths to `drawArabicText`, which requires an `Int`. Dynamic card widths are now rounded explicitly before text layout.
- `CurrencySettingsDialog` now declares its focus manager and software keyboard controller inside the composable that owns the rate input field.
- `BackupRestoreBottomSheet` search header now owns its focus manager.
- `HabayebHeaderSearchBar` now declares the keyboard controller before the search field uses it.
- `SecurityActivePanel` imports `BorderStroke` explicitly.
- Deprecated `rememberRipple` was replaced by the current `ripple` API.
- The security performance contract was updated to assert the current ripple API.
- The fixed 200 ms biometric delay was removed; the first-frame boundary is used instead so the security screen does not introduce an artificial wait.

## Verification status

Static verification was performed against every compiler error shown in the supplied Release build log, and the affected references/types were corrected.

A full Gradle Release compilation could not be executed in this sandbox because the project wrapper requires Gradle 9.3.1 and the environment cannot reach `services.gradle.org` to download it. Therefore this phase does **not** claim a runtime/build PASS that was not actually executed.
