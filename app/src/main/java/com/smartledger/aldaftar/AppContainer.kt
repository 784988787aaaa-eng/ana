package com.smartledger.aldaftar

import android.content.Context
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.repository.*
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebCategoryUseCase
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.backup.AutomaticBackupCoordinator
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
import com.smartledger.aldaftar.data.license.LicenseRepository

/** تركيب الاعتماديات طويلة العمر داخل جذر التطبيق فقط. */
class AppContainer(context: Context) {
    private val database by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppDatabase.getDatabase(context) }
    val categories by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CategoryRepository(database.customCategoryDao()) }
    val settings by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsRepository(database.settingsDao()) }
    val categoryUseCase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HabayebCategoryUseCase(categories, HabayebCategoryDataRepository(database, categories)) }
    val habayeb by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HabayebRepository(database, database.habayebDao(), license) }
    val trash by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TrashRepository(database.trashDao()) }
    val maintenance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { DataMaintenanceRepository(database, database.settingsDao(), database.customCategoryDao(), database.trashDao(), database.habayebDao()) }
    val businessProfile by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BusinessProfileRepository(database.businessProfileDao()) }
    val recurring by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RecurringRepository(database, database.recurringConfigDao(), license) }
    val floatingUi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FloatingUiPreferencesRepository(context.applicationContext) }
    val mutation by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HabayebMutationRepository(database) }
    val backupEngine by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BackupEngine(context.applicationContext, database) }
    val cloudArchiveStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CloudArchiveStore(context.applicationContext) }
    val automaticBackup by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AutomaticBackupCoordinator(backupEngine, cloudArchiveStore) }
    val license by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { LicenseRepository(context.applicationContext) }
    val unifiedAccount by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { UnifiedAccountSessionRepository(context.applicationContext, license, cloudArchiveStore) }
}
