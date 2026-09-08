package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartledger.aldaftar.AppContainer

/** مصنع موحد لحقن اعتماديات واجهة التطبيق. */
class AppViewModelFactory(
    private val application: Application,
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        FinanceViewModel::class.java -> FinanceViewModel(application, container.settings, container.commitments, container.transactions, container.categories, container.habayeb, container.trash, container.maintenance, container.floatingUi) as T
        HabayebFinanceViewModel::class.java -> HabayebFinanceViewModel(application, container.categoryUseCase, container.habayeb, container.transactions, container.categories, container.settings, container.recurring, container.mutation, container.floatingUi) as T
        LedgerViewModel::class.java -> LedgerViewModel(application, container.settings, container.transactions, container.categories, container.trash) as T
        SecurityViewModel::class.java -> SecurityViewModel(application, container.settings) as T
        BackupSyncViewModel::class.java -> BackupSyncViewModel(application, container.maintenance) as T
        BusinessProfileViewModel::class.java -> BusinessProfileViewModel(container.businessProfile) as T
        else -> throw IllegalArgumentException("Unsupported ViewModel: ${modelClass.name}")
    }
}
