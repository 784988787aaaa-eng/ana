package com.smartledger.aldaftar.data.backup

import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import android.net.Uri
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** دورة النسخ اليومية: نسخة محلية مؤكدة أولاً، ثم رفع النسخة نفسها إلى السحابة. */
class AutomaticBackupCoordinator(
    private val engine: BackupEngine,
    private val cloud: CloudArchiveStore,
    private val publicStore: PublicBackupStore
) {
    private val running = AtomicBoolean(false)

    suspend fun runDaily(): Result {
        if (!running.compareAndSet(false, true)) return Result.AlreadyRunning
        return try {
            val local = engine.createAutomatic()
            val publicUri = runCatching { publicStore.publish(local) }.getOrNull()
            if (!cloud.connected()) return Result.LocalOnly(local, null, publicUri)

            runCatching { cloud.upload(local, local.name) }
                .fold(
                    onSuccess = { Result.LocalAndCloud(local, publicUri) },
                    onFailure = { Result.LocalOnly(local, it, publicUri) }
                )
        } finally {
            running.set(false)
        }
    }

    sealed interface Result {
        data class LocalAndCloud(val file: File, val publicUri: Uri? = null) : Result
        data class LocalOnly(val file: File, val cloudError: Throwable? = null, val publicUri: Uri? = null) : Result
        data object AlreadyRunning : Result
    }
}
