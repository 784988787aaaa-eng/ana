package com.smartledger.aldaftar

import android.app.Application
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.Constraints
import java.util.concurrent.TimeUnit
import android.content.Context
import com.smartledger.aldaftar.ui.viewmodel.AppViewModelFactory

class FinanceApplication : Application(), Configuration.Provider {
    companion object {
        private const val DAILY_BACKUP_WORK_NAME = "smartledger_daily_backup"
        private const val LICENSE_VERIFICATION_WORK_NAME = "smartledger_license_verification"
    }
    private val container: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppContainer(this) }
    val viewModelFactory: AppViewModelFactory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppViewModelFactory(this, container)
    }

    override fun onCreate() {
        super.onCreate()
        container
        viewModelFactory
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            DAILY_BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<com.smartledger.aldaftar.work.DailyBackupWorker>(24, TimeUnit.HOURS).build()
        )
        workManager.enqueueUniquePeriodicWork(
            LICENSE_VERIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<com.smartledger.aldaftar.work.LicenseVerificationWorker>(24, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()).build()
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
                when (workerClassName) {
                    TrashCleanupWorker::class.java.name -> TrashCleanupWorker(appContext, workerParameters, container.settings, container.trash)
                    com.smartledger.aldaftar.work.LicenseVerificationWorker::class.java.name -> com.smartledger.aldaftar.work.LicenseVerificationWorker(appContext, workerParameters)
                    com.smartledger.aldaftar.work.DailyBackupWorker::class.java.name -> com.smartledger.aldaftar.work.DailyBackupWorker(appContext, workerParameters, container.automaticBackup)
                    else -> null
                }
        }).build()
}
