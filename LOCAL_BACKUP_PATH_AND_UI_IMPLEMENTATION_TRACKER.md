# LOCAL_BACKUP_PATH_AND_UI_IMPLEMENTATION_TRACKER.md

## Implementation Tracker for Local Backup Path & UI Refactoring

---

### Phase A — Central Path Resolution (`BackupPathResolver`)
- [x] File: `app/src/main/java/com/example/data/backup/BackupPathResolver.kt`
  - Function: `getPublicBackupRoot()`, `getCurrentMonthlyDirectory()`, `getMonthlyDirectory()`, `ensureDirectory()`
  - Change: Centralize public directory resolution `/storage/emulated/0/Documents/الدفتر الذكي/[yyyy-MM]/` with path traversal validation.
  - Verification: Unit check & compilation.
  - Result: PASSED.

### Phase B — Engine & Repository Layer Unification
- [x] File: `app/src/main/java/com/example/data/backup/BackupFileManager.kt`
  - Function: `getBaseBackupDirectory()`, `getMonthlyBackupDirectory()`, `createBackupFile()`
  - Change: Delegate to `BackupPathResolver`, remove private storage fallback, enforce public monthly target.
  - Verification: Build & file creation path validation.
  - Result: PASSED.

- [x] File: `app/src/main/java/com/example/data/backup/BackupService.kt`
  - Function: `performLocalBackup()`, `performSilentBackup()`
  - Change: Use central public monthly directory resolver, strict atomic writes and integrity checks.
  - Verification: Build verification.
  - Result: PASSED.

- [x] File: `app/src/main/java/com/example/data/repository/BackupRepository.kt`
  - Function: `createLocalBackup()`, `createSilentBackup()`, `restoreFromFile()`
  - Change: Route all backup creations strictly through `BackupService`.
  - Verification: Build verification.
  - Result: PASSED.

### Phase C — ViewModel & Background Workers
- [x] File: `app/src/main/java/com/example/ui/viewmodel/BackupSyncViewModel.kt`
  - Function: `createLocalBackup()`, `exportLocalBackup()`, `triggerSilentLocalBackup()`, `localBackups`
  - Change: Eliminate direct `file.writeText`, delegate to repository, remove deprecated UI list flows, return saved path.
  - Verification: Build & ViewModel execution check.
  - Result: PASSED.

- [x] File: `app/src/main/java/com/example/AutoBackupWorker.kt`
  - Function: `doWork()`
  - Change: Ensure automated daily backup routes through `BackupService` into the public monthly directory.
  - Verification: Worker execution check.
  - Result: PASSED.

### Phase D — UI Hardening & Strings
- [x] File: `app/src/main/res/values/strings.xml`
  - Change: Update `settings_backup_portable_title` to "النسخ الاحتياطي المحلي", add restore confirmation warning and path feedback strings.
  - Verification: Strings resource compilation.
  - Result: PASSED.

- [x] File: `app/src/main/java/com/example/ui/screens/settings/components/FileTransferManager.kt`
  - Function: `FileTransferManager` Composable
  - Change: Remove the local backup list display entirely, update title, implement direct export with exact saved path feedback.
  - Verification: UI rendering & compilation.
  - Result: PASSED.

- [x] File: `app/src/main/java/com/example/ui/screens/settings/components/QuadBackupCard.kt`
  - Function: `QuadBackupCard` Composable
  - Change: Remove `localBackups` references, implement SAF initial directory targeting `Documents/الدفتر الذكي`, add MANDATORY pre-restore confirmation dialog.
  - Verification: UI & SAF flow verification.
  - Result: PASSED.

### Phase E — Acceptance & Full System Pass
- [x] Full Compilation & Static Audit
  - Verification: `compile_applet` and verification of all acceptance criteria.
  - Result: PASSED.

