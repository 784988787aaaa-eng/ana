package com.smartledger.aldaftar.ui.viewmodel

import com.smartledger.aldaftar.data.license.LicenseRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    application: Application,
    private val licenseRepository: LicenseRepository,
    private val settingsRepository: com.smartledger.aldaftar.data.repository.SettingsRepository,
    private val categoriesRepository: com.smartledger.aldaftar.data.repository.CategoryRepository,
    private val habayebRepository: com.smartledger.aldaftar.data.repository.HabayebRepository,
    private val trashRepository: com.smartledger.aldaftar.data.repository.TrashRepository,
    private val maintenanceRepository: com.smartledger.aldaftar.data.repository.DataMaintenanceRepository,
    private val floatingUiRepository: com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
) : AndroidViewModel(application) {

    companion object {
        private const val CLEANUP_PERIOD_NEVER = "never"
        private const val PREFIX_HABAYEB = "habayeb_"
    }

    private val app = application

    fun floatingSearchState() = floatingUiRepository.search()
    fun saveFloatingSearchState(state: com.smartledger.aldaftar.data.repository.FloatingSearchState) = floatingUiRepository.saveSearch(state)
    fun floatingAddState() = floatingUiRepository.add()
    fun saveFloatingAddState(state: com.smartledger.aldaftar.data.repository.FloatingAddState) = floatingUiRepository.saveAdd(state)
    fun isFloatingSearchActive() = floatingUiRepository.floatingSearchActive()
    fun setFloatingSearchActive(active: Boolean) = floatingUiRepository.setFloatingSearchActive(active)

    private val _autoCleanupPeriod = MutableStateFlow(CLEANUP_PERIOD_NEVER)
    val autoCleanupPeriod: StateFlow<String> = _autoCleanupPeriod.asStateFlow()

    private val _uiEventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    private fun sendUiEvent(event: UiEvent) {
        _uiEventChannel.trySend(event)
    }

    private val _themeModeState = MutableStateFlow(0)
    val themeModeState: StateFlow<Int> = _themeModeState.asStateFlow()

    val isSettingsLoaded = MutableStateFlow(false)

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .onEach {
            isSettingsLoaded.value = true
            if (it != null && _themeModeState.value != it.themeMode) {
                _themeModeState.value = it.themeMode
            }
        }
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val customCategoriesState: StateFlow<List<CustomCategory>> = categoriesRepository.customCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = trashRepository.deletedItemsFlow

    val totalTransactionsCount: StateFlow<Int> = habayebRepository.getHabayebTransactionsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun hasShownOnboarding(): Boolean = settingsState.value.onboardingShown

    fun markOnboardingShown() {
        viewModelScope.launch { settingsRepository.saveSettings(settingsState.value.copy(onboardingShown = true)) }
    }

    fun saveSettings(settings: AppSettings) {
        if (_themeModeState.value != settings.themeMode) {
            _themeModeState.value = settings.themeMode
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettings(settings)
        }
    }

    fun isEligibleToCreate(): Boolean = licenseRepository.isEligibleToCreate()
    fun triggerLicensePrompt() = licenseRepository.triggerLicenseRequired()

    fun permanentlyDeleteDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.removeDeletedItem(item)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun permanentlyDeleteMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items.forEach { trashRepository.removeDeletedItem(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun restoreMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items.forEach { trashRepository.restoreDeletedItem(it) }
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.restoreDeletedItem(item)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreSingleTransactionFromBundle(itemId: String, txId: String, item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.restoreSingleTransactionFromBundle(itemId, txId)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun updateAutoCleanupPeriod(period: String) {
        _autoCleanupPeriod.value = period
        viewModelScope.launch(Dispatchers.IO) {
            val context = app
            settingsRepository.saveSettings(settingsState.value.copy(trashAutoCleanupPeriod = period))

            com.smartledger.aldaftar.TrashCleanupWorker.schedulePeriodicCleanup(context, period)

            if (period != CLEANUP_PERIOD_NEVER) {
                try {
                    val ageInMillis = com.smartledger.aldaftar.TrashCleanupWorker.getPeriodDurationMillis(period)
                    if (ageInMillis > 0L) {
                        val thresholdTime = System.currentTimeMillis() - ageInMillis
                        val items = trashRepository.getAllDeletedItemsDirect()
                        val expiredItems = items.filter { it.deletedAt < thresholdTime }
                        expiredItems.forEach { item ->
                            trashRepository.removeDeletedItem(item)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                }
            }
        }
    }

    fun cleanLedgerTrashItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = app.getString(R.string.source_system_habayeb)
                val allItems = trashRepository.getAllDeletedItemsDirect()
                val nonHabayebItems = allItems.filter {
                    it.sourceSystem != systemHabayeb && !it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                nonHabayebItems.forEach {
                    trashRepository.removeDeletedItem(it)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = app.getString(R.string.source_system_habayeb)
                val allItems = trashRepository.getAllDeletedItemsDirect()
                val habayebItems = allItems.filter {
                    it.sourceSystem == systemHabayeb || it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                habayebItems.forEach {
                    trashRepository.removeDeletedItem(it)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun saveCustomCategory(name: String, tabType: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.saveCustomCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = emoji))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_save_failed))
            }
        }
    }

    fun deleteCustomCategory(customCategory: CustomCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.deleteCustomCategory(customCategory)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                maintenanceRepository.deleteAllData()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }
}
