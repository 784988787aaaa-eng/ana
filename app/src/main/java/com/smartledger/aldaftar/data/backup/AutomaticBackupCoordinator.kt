package com.smartledger.aldaftar.data.backup

import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** دورة النسخ اليومية: نسخة محلية مؤكدة أولاً، ثم رفع النسخة نفسها إلى السحابة. */
class AutomaticBackupCoordinator(
    private val engine: BackupEngine,
    private val cloud: CloudArchiveStore
) {
    private val running = AtomicBoolean(false)

    suspend fun runDaily(): Result {
        if (!running.compareAndSet(false, true)) return Result.AlreadyRunning
        return try {
            val local = engine.createAutomatic()
            if (!cloud.connected()) return Result.LocalOnly(local)

            runCatching { cloud.upload(local, local.name) }
                .fold(
                    onSuccess = { Result.LocalAndCloud(local) },
                    onFailure = { Result.LocalOnly(local, it) }
                )
        } finally {
            running.set(false)
        }
    }

    sealed interface Result {
        data class LocalAndCloud(val file: File) : Result
        data class LocalOnly(val file: File, val cloudError: Throwable? = null) : Result
        data object AlreadyRunning : Result
    }
}
