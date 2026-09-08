package com.smartledger.aldaftar

import android.content.Context
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.repository.*
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebCategoryUseCase

/** تركيب الاعتماديات طويلة العمر داخل جذر التطبيق فقط. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val categories = CategoryRepository(database.customCategoryDao())
    val settings = SettingsRepository(database.settingsDao())
    val commitments = CommitmentRepository(database.commitmentDao())
    val transactions = TransactionRepository(database.transactionDao())
    val categoryUseCase = HabayebCategoryUseCase(categories, HabayebCategoryDataRepository(database, categories))
    val habayeb = HabayebRepository(database, database.habayebDao())
    val trash = TrashRepository(database.trashDao())
    val maintenance = DataMaintenanceRepository(database, database.settingsDao(), database.commitmentDao(), database.transactionDao(), database.customCategoryDao(), database.trashDao(), database.habayebDao())
    val businessProfile = BusinessProfileRepository(database.businessProfileDao())
    val recurring = RecurringRepository(database, database.recurringConfigDao())
    val floatingUi = FloatingUiPreferencesRepository(context.applicationContext)
    val mutation = HabayebMutationRepository(database)
}
