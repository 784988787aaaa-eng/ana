# BUILD FIX — FINAL CUMULATIVE AUDIT

Date: 2026-09-18

## Build errors addressed from CI screenshots

### 1. PdfCustomerSummaryRenderer.kt — Paint color shadowing

The KPI loop destructured a field as `color`, then used `Paint(...).apply { color = Color.parseColor(color) }`.
Inside `Paint.apply`, `color` resolves to the receiver's `Paint.color: Int`, not the lambda value. This produced the reported `Int`/`String` mismatch and assignment diagnostics around lines 265–267.

Fix: renamed the lambda field to `valueColorHex` and passed that string to `Color.parseColor`.

### 2. PdfPageRenderer.kt — missing import

`HabayebMathHelper.formatSmart(...)` was used by the new customer-report introduction block without importing `HabayebMathHelper`.

Fix: added the explicit import from `com.smartledger.aldaftar.ui.helper.HabayebMathHelper`.

### 3. KSP build tooling

The CI log also showed a KSP-side `ApplicationManager.getApplication()` NPE before the Kotlin compilation diagnostics. The project used KSP 2.3.5 with Kotlin 2.2.10. KSP was upgraded to 2.3.10, which is documented as supporting Kotlin 2.2.10–2.3.x and AGP 8.12+ / Gradle 8.13+.

This is a build-tooling correction, not an application-code workaround.

## Regression protection

Added `PdfRendererBuildContractTest` to prevent the exact Paint receiver-shadowing regression and to ensure the math helper import remains present while the customer intro uses it.

## Verification limitation

A full Gradle build was attempted in this environment, but Gradle 9.3.1 was not locally installed and the environment could not download it from services.gradle.org. Therefore no false Build PASS is claimed here. The source-level fixes above directly address the compiler errors shown in the supplied CI screenshots.
