package com.smartledger.aldaftar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartledger.aldaftar.data.license.LicenseRepository

class LicenseVerificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val repo = LicenseRepository(applicationContext)
        if (repo.snapshot().type == com.smartledger.aldaftar.domain.license.LicenseType.ACCOUNT) repo.verifyAccountOnline()
        Result.success()
    } catch (_: java.io.IOException) { Result.retry() }
      catch (_: Exception) { Result.failure() }
}
