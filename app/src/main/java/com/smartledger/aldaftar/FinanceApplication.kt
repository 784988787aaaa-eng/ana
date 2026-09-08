package com.smartledger.aldaftar

import android.app.Application
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import android.content.Context
import com.smartledger.aldaftar.ui.viewmodel.AppViewModelFactory

class FinanceApplication : Application(), Configuration.Provider {
    private val container: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppContainer(this) }
    val viewModelFactory: AppViewModelFactory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppViewModelFactory(this, container)
    }

    override fun onCreate() {
        super.onCreate()
        container
        viewModelFactory
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
                if (workerClassName == TrashCleanupWorker::class.java.name) TrashCleanupWorker(appContext, workerParameters, container.settings, container.trash) else null
        }).build()
}
