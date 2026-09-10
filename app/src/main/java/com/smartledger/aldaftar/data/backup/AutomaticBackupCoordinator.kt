package com.smartledger.aldaftar.data.backup

import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import java.util.concurrent.atomic.AtomicBoolean

/** دورة النسخ اليومية: نسخة محلية داخلية، ثم نسخة سحابية عند توفر الربط. */
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
            runCatching { cloud.upload(local.readBytes(), local.name) }
                .fold(
                    onSuccess = { Result.LocalAndCloud(local) },
                    onFailure = { Result.LocalOnly(local) }
                )
        } finally {
            running.set(false)
        }
    }

    sealed interface Result {
        data class LocalAndCloud(val file: java.io.File) : Result
        data class LocalOnly(val file: java.io.File) : Result
        data object AlreadyRunning : Result
    }
}
