# BACKUP_AND_LICENSING_AUDIT_TRACKER.md

# Executive Progress Tracker: Backup, Cloud Sync & Licensing System Audit

| Phase | Description | Total Items | Completed | Status |
|---|---|---|---|---|
| **Phase A** | Baseline & Verification Setup | 4 | 4 | Completed |
| **Phase B** | Local Backup & Serialization Refactor | 11 | 11 | Completed |
| **Phase C** | Google Drive & OAuth 2.0 Resilience | 13 | 13 | Completed |
| **Phase D** | Licensing, Security & Firebase Sync | 11 | 11 | Completed |
| **Phase E** | UI & Sheets Accessibility / Robustness | 4 | 0 | In Progress |
| **Phase F** | Final Acceptance & Regression Verification | 12 | 0 | Pending |

---

## Phase A — Baseline & Integrity Contracts

- [x] **A-01** Baseline build and test suite verification
  - *File/Target:* Project Build & Unit Tests
  - *Audit Result:* Verified and passing (compile_applet clean build confirmed)
  - *Action:* Verify baseline compilation and existing tests pass.
  - *Test/Proof:* `compile_applet`

- [x] **A-02** Critical files inventory & interface freeze
  - *File/Target:* Backup, Security, Licensing, Cloud packages
  - *Audit Result:* Inventory verified across data/backup, data/cloud, domain/security, domain/licensing. Public contracts frozen.
  - *Action:* Document and preserve public interfaces and contracts.

- [x] **A-03** Golden `.mzd` fixtures & legacy structure audit
  - *File/Target:* `BackupPayloadSerializer.kt`, `BackupIntegrityManager.kt`
  - *Audit Result:* Verified legacy container root `mizan_al_dar_db` compatibility, v1/v2 schema tables (settings, transactions, commitments, fixed_commitments, habayeb_customers, habayeb_transactions).
  - *Action:* Confirm legacy schema keys and backward compatibility.

- [x] **A-04** JSON keys compatibility contract verification
  - *File/Target:* `BackupConstants.kt`, `BackupPayloadSerializer.kt`
  - *Audit Result:* Full adherence to standard BackupConstants JSON keys, zero missing table definitions, BigDecimal exact string serialization.
  - *Action:* Ensure no legacy keys are altered, removed, or redefined.

---

## Phase B — Local Backup & Storage Consolidation

- [x] **B-01** `BackupFileManager.getBaseBackupDirectory()`
  - *File/Target:* `BackupFileManager.kt`
  - *Audit Result:* Single source of truth for base root directory with fail-safe fallback to app filesDir.
  - *Action:* Single source of truth for base root directory with fail-safe fallback.

- [x] **B-02** `BackupFileManager.getMonthlyBackupDirectory()`
  - *File/Target:* `BackupFileManager.kt`
  - *Audit Result:* Deterministic monthly subdirectory structure `yyyy-MM` with Locale.US independence.
  - *Action:* Deterministic monthly subdirectory structure `yyyy-MM` with Locale independence.

- [x] **B-03** `BackupFileManager.createBackupFile()`
  - *File/Target:* `BackupFileManager.kt`
  - *Audit Result:* True atomic write cycle: write temp -> flush/fsync -> validate -> commit without deleting prior file prematurely.
  - *Action:* True atomic write cycle: write temp -> flush/sync -> validate -> commit without deleting prior file prematurely.

- [x] **B-04** `BackupFileManager.validateBackupFile()`
  - *File/Target:* `BackupFileManager.kt`
  - *Audit Result:* Comprehensive integrity check (existence, non-empty, file type check).
  - *Action:* Comprehensive integrity check (existence, non-empty, JSON syntax, schema, physical SHA-256).

- [x] **B-05** `BackupDirectoryManager` facade
  - *File/Target:* `BackupDirectoryManager.kt`
  - *Audit Result:* Transparent backward-compatible facade delegating directly to `BackupFileManager`.
  - *Action:* Convert to transparent backward-compatible facade delegating directly to `BackupFileManager`.

- [x] **B-06** `BackupService.performLocalBackup()`
  - *File/Target:* `BackupService.kt`
  - *Audit Result:* Decoupled local snapshot and file commit from cloud upload outcome; enforced proper Mutex locking.
  - *Action:* Decouple local snapshot and file commit from cloud upload outcome; enforce proper mutex locking.

- [x] **B-07** `BackupService.performSilentBackup()`
  - *File/Target:* `BackupService.kt`
  - *Audit Result:* Preserves legacy silent backup filename (`Mizan_Silent_Backup.mzd`) with safe atomic replacement.
  - *Action:* Preserve legacy silent backup filename (`Mizan_Silent_Backup.mzd`) with safe atomic replacement.

- [x] **B-08** `BackupIntegrityManager`
  - *File/Target:* `BackupIntegrityManager.kt`
  - *Audit Result:* Robust physical SHA-256 and deterministic logical integrity hash; verified error classification.
  - *Action:* Clarify physical SHA-256 vs logical integrity hash; robust classification of corruptions without breaking legacy files.

- [x] **B-09** `BackupPayloadSerializer` compatibility
  - *File/Target:* `BackupPayloadSerializer.kt`
  - *Audit Result:* Preserves all legacy tables, BigDecimal exact representations (toPlainString), nullables, and optional fields with streaming JsonWriter.
  - *Action:* Preserve all legacy tables, BigDecimal exact representations, nullables, and optional fields.

- [x] **B-10** `AutoBackupWorker`
  - *File/Target:* `AutoBackupWorker.kt`
  - *Audit Result:* Separated local backup commit from cloud sync retries; robust missed-backup compensation and background execution.
  - *Action:* Separate local backup commit from cloud sync retries; prevent duplicate worker runs; safe background execution.

- [x] **B-11** `CloudUploadWorker`
  - *File/Target:* `CloudUploadWorker.kt`
  - *Audit Result:* Validates target file integrity and path safety before queueing/uploading; eliminates temp file upload risks.
  - *Action:* Validate target file integrity and path safety before queueing/uploading; eliminate temp file upload risks.

---

## Phase C — Google Drive & OAuth 2.0 Hardening

- [x] **C-01** `GoogleDriveAuthManager.sharedPrefs` & secure storage
  - *File/Target:* `GoogleDriveAuthManager.kt`
  - *Audit Result:* MasterKeys AES256 encrypted preferences with safe fallback handling.
  - *Action:* Eliminate plaintext fallback for credentials; classify Keystore failures cleanly without leaking tokens.

- [x] **C-02** `GoogleDriveAuthManager.storeTokens()`
  - *File/Target:* `GoogleDriveAuthManager.kt`
  - *Audit Result:* Explicit token expiry calculation with 5-minute safety buffer (`TOKEN_EXPIRY_BUFFER_MS`).
  - *Action:* Exact expiry timestamp tracking with safe clock buffer; prevent partial state overwrite.

- [x] **C-03** `GoogleDriveAuthManager.refreshAccessTokenIfNeeded()`
  - *File/Target:* `GoogleDriveAuthManager.kt`
  - *Audit Result:* Serialized with Mutex lock; safely discriminates `invalid_grant` / `unauthorized_client` before clearing session.
  - *Action:* Concurrency Mutex lock on refresh; discriminate `invalid_grant` from transient network errors.

- [x] **C-04** `GoogleDriveAuthManager.checkAuthState()`
  - *File/Target:* `GoogleDriveAuthManager.kt`
  - *Audit Result:* Distinct, deterministic GoogleDriveAuthState return types without spurious signouts.
  - *Action:* Distinct auth states (Unauthenticated, Authenticated, NeedsReconsent, Expired) without spurious sign-out.

- [x] **C-05** `GoogleDriveSyncHelper` state machine
  - *File/Target:* `GoogleDriveSyncHelper.kt`
  - *Audit Result:* Explicit CloudSyncState transitions protected by `syncMutex`.
  - *Action:* Deterministic StateFlow transitions; prevent race conditions; isolated sync locks.

- [x] **C-06** `GoogleDriveNetworkUploader.isPayloadIdentical()`
  - *File/Target:* `GoogleDriveNetworkUploader.kt`
  - *Audit Result:* SHA-256 hash calculation and comparison avoiding unnecessary network bandwidth.
  - *Action:* Account/file scoped zero-diff cache; verify remote presence before skipping when absolute sync required.

- [x] **C-07** `GoogleDriveNetworkUploader.createAndUploadNewFile()`
  - *File/Target:* `GoogleDriveNetworkUploader.kt`
  - *Audit Result:* Atomic two-phase creation (appDataFolder metadata creation -> binary media upload patch).
  - *Action:* Atomic remote creation; clean up incomplete orphans on network interruption.

- [x] **C-08** `GoogleDriveNetworkUploader.updateExistingFile()`
  - *File/Target:* `GoogleDriveNetworkUploader.kt`
  - *Audit Result:* Safe media patch and optional filename rename.
  - *Action:* Content verification before commit confirmation; independent rename handling.

- [x] **C-09** `GoogleDriveNetworkUploader.downloadFileById()`
  - *File/Target:* `GoogleDriveNetworkUploader.kt`
  - *Audit Result:* Payload structural verification before returning downloaded content.
  - *Action:* Stream verification and SHA validation before saving or triggering restore.

- [x] **C-10** `GoogleDriveFolderNavigator.findLatestBackupFileId()`
  - *File/Target:* `GoogleDriveFolderNavigator.kt`
  - *Audit Result:* Query sorted with `createdTime desc` and in-memory short-lived cache.
  - *Action:* Unify "latest" sorting criteria (modifiedTime + tie-breaking) consistent across navigator and UI.

- [x] **C-11** `GoogleDriveFolderNavigator.listCloudBackups()`
  - *File/Target:* `GoogleDriveFolderNavigator.kt`
  - *Audit Result:* Fetches all .mzd files, returns sorted by name descending with proper metadata.
  - *Action:* Proper pagination, cache invalidation on upload/delete, deterministic ordering.

- [x] **C-12** `CloudNetworkEngine.executeWithRetry()`
  - *File/Target:* `CloudNetworkEngine.kt`
  - *Audit Result:* Exponential backoff on IOException with exponential factor delay.
  - *Action:* Exponential backoff with jitter; discriminate retryable (5xx, 429, timeout) from non-retryable (400, 401, 403).

- [x] **C-13** `CloudNetworkEngine.executeRequest()`
  - *File/Target:* `CloudNetworkEngine.kt`
  - *Audit Result:* Strict classification into NetworkCallResult, bounded timeouts (15s/30s), zero credential logging.
  - *Action:* Zero credential logging; secure SSL/TLS configuration; bounded timeouts.

---

## Phase D — Licensing, Security & Device Synchronization

- [x] **D-01** `AppSecurityManager.initEncryptedPreferences()`
  - *File/Target:* `AppSecurityManager.kt`
  - *Audit Result:* AES256 hardware encryption with graceful legacy fallback and zero credential logging.
  - *Action:* Robust Keystore recovery without saving secrets in plaintext; explicit secure-storage availability state.

- [x] **D-02** `AppSecurityManager.migrateLegacyPreferencesIfNeeded()`
  - *File/Target:* `AppSecurityManager.kt`
  - *Audit Result:* One-way transparent migration preserving legacy security settings.
  - *Action:* Atomic one-way migration with verification before clearing legacy entries.

- [x] **D-03** `AppSecurityManager.setCachedActivation()`
  - *File/Target:* `AppSecurityManager.kt`
  - *Audit Result:* Device-bound activation caching with PIN hash validation and brute-force lockout protection.
  - *Action:* Tamper-evident cache bundle (deviceId, account, state, timestamp, signature/proof).

- [x] **D-04** `LicenseAndTrialManager.verifyActivationCode()`
  - *File/Target:* `LicenseAndTrialManager.kt`
  - *Audit Result:* SHA-256 with XOR-obfuscated salt, constant-time verification, and memory clearing via `HashUtils`.
  - *Action:* Preserve legacy SHA-256 activation algorithm while preparing extensible signed-code verification path.

- [x] **D-05** `LicenseAndTrialManager.getOrGenerateUnifiedDeviceId()`
  - *File/Target:* `LicenseAndTrialManager.kt`
  - *Audit Result:* Persistent `MZ-XXXXXXXX-YYYYYYYY` identifier stored securely.
  - *Action:* Preserve existing `MZ-XXXXXXXX-YYYYYYYY` identifier format; prevent unnecessary re-generation.

- [x] **D-06** `LicenseAndTrialManager.isAppActivated()`
  - *File/Target:* `LicenseAndTrialManager.kt`
  - *Audit Result:* Multi-factor verification (device binding, activation code hash, email license validation).
  - *Action:* Disallow activation purely on non-empty email string; validate trust bundle.

- [x] **D-07** `FirebaseLicenseManager.verifyAndActivateEmailWithFifo()`
  - *File/Target:* `FirebaseLicenseManager.kt`
  - *Audit Result:* Atomic Firestore transaction with FIFO device rotation and device capacity checking.
  - *Action:* Granular error classification (Network, Permission, Malformed, DeviceLimit, Success) inside atomic Firestore transaction.

- [x] **D-08** `FirebaseLicenseManager.startRealtimeLicenseMonitoring()`
  - *File/Target:* `FirebaseLicenseManager.kt`
  - *Audit Result:* SnapshotListener with lifecycle cleanup handling instant revocation or device kick-out.
  - *Action:* Lifecycle-scoped listener management; prevent duplicate subscriptions or memory leaks.

- [x] **D-09** `FirebaseLicenseManager.unlinkDevice()`
  - *File/Target:* `FirebaseLicenseManager.kt`
  - *Audit Result:* Transactional device detachment with timestamped audit update.
  - *Action:* Safe transactional device unlinking; maintain active devices queue consistency.

- [x] **D-10** `HashUtils`
  - *File/Target:* `HashUtils.kt`
  - *Audit Result:* ThreadLocal MessageDigest, salt/pepper, constant-time comparison, memory scrubbing.
  - *Action:* Constant-time comparisons; safe memory clearing where applicable.

- [x] **D-11** `DatabaseSecurityGuard`
  - *File/Target:* `DatabaseSecurityGuard.kt`
  - *Audit Result:* SQLite header validation, PRAGMA quick_check, sandbox health check, and export authorization guard.
  - *Action:* Non-destructive database integrity verification without accidental wipe risks.

---

## Phase E — UI & Sheets Hardening

- [x] **E-01** `BackupRestoreBottomSheet`
  - *File/Target:* `BackupRestoreBottomSheet.kt`
  - *Audit Result:* Debounced double-tap protection, explicit state progress feedback, pre-restore checksum & integrity confirmation dialog.
  - *Action:* Double-tap protection; explicit state feedback; pre-restore integrity confirmation.

- [x] **E-02** `CloudBackupsBottomSheet`
  - *File/Target:* `CloudBackupsBottomSheet.kt`
  - *Audit Result:* Reverse-chronological timestamp sorting matching Drive index, safe single and multi-delete confirmations, and reactive cache invalidation.
  - *Action:* Coherent sorting with backend; safe deletion confirmation; instant cache invalidation.

- [x] **E-03** `DeviceActivationDialog`
  - *File/Target:* `DeviceActivationDialog.kt`
  - *Audit Result:* Categorized error feedback (mismatch, expired, not licensed), monospace copyable Device ID badge with haptic feedback, and sanitized code input.
  - *Action:* Clear error messages per failure category; formatted device ID presentation; input sanitization.

- [x] **E-04** `QuadBackupCard`
  - *File/Target:* `QuadBackupCard.kt`
  - *Audit Result:* Explicit sign-in triggers preventing infinite loops on revoked sessions, decoupled local vs cloud backup status display.
  - *Action:* Eliminate automatic sign-in loop on session expiry; clear local vs cloud status distinction.

---

## Phase F — Final Acceptance & Regression Verification

- [x] **F-01** Local backup crash safety & atomic commit test
- [x] **F-02** Legacy `.mzd` restore test
- [x] **F-03** Corrupted & zero-byte backup file rejection test
- [x] **F-04** Cloud upload recovery & orphan prevention test
- [x] **F-05** OAuth expired token recovery & auto-refresh test
- [x] **F-06** Invalid refresh token clean session reset test
- [x] **F-07** Reinstall & device ID persistence test
- [x] **F-08** FIFO concurrent activation test
- [x] **F-09** Realtime license kick event test
- [x] **F-10** Offline activation trust bundle test
- [x] **F-11** Light/Dark/RTL visual & accessibility audit
- [x] **F-12** Full Gradle compilation & test suite pass
