package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.repository.*
import com.smartledger.aldaftar.ui.screens.TrashFilterType
import com.smartledger.aldaftar.ui.screens.trash.components.TrashWrapper
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color


import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
class FinanceViewModel(
    application: Application,
    private val licenseRepository: LicenseRepository,
    private val settingsRepository: SettingsRepository,
    private val categoriesRepository: CategoryRepository,
    private val habayebRepository: HabayebRepository,
    private val trashRepository: TrashRepository,
    private val maintenanceRepository: com.smartledger.aldaftar.data.repository.DataMaintenanceRepository,
    private val floatingUiRepository: com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
) : AndroidViewModel(application) {

    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _themeModeState = MutableStateFlow(0)
    val themeModeState: StateFlow<Int> = _themeModeState.asStateFlow()

    private val _isSettingsLoaded = MutableStateFlow(false)
    val isSettingsLoaded: StateFlow<Boolean> = _isSettingsLoaded.asStateFlow()

    private val _isPasscodeEnabled = MutableStateFlow(false)
    val isPasscodeEnabled: StateFlow<Boolean> = _isPasscodeEnabled.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(false)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()
    
    val autoCleanupPeriod: StateFlow<String> = settingsState.map { it.trashAutoCleanupPeriod }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NEVER")

    val customCategoriesState: StateFlow<List<CustomCategory>> = categoriesRepository.customCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = trashRepository.deletedItemsFlow

    val totalTransactionsCount: StateFlow<Int> = habayebRepository.getHabayebTransactionsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    fun isFloatingSearchActive(): Boolean = floatingUiRepository.floatingSearchActive()
    
    fun setFloatingSearchActive(active: Boolean) {
        floatingUiRepository.setFloatingSearchActive(active)
    }

    fun floatingSearchState(): com.smartledger.aldaftar.data.repository.FloatingSearchState = floatingUiRepository.search()
    
    fun saveFloatingSearchState(state: com.smartledger.aldaftar.data.repository.FloatingSearchState) {
        floatingUiRepository.saveSearch(state)
    }

    init {
        viewModelScope.launch {
            settingsState.collect { settings ->
                _themeModeState.value = settings.themeMode
                _isSettingsLoaded.value = true
                _isPasscodeEnabled.value = settings.isPasscodeEnabled
                _isFirstLaunch.value = settings.isFirstLaunch
            }
        }
    }

    fun hasShownOnboarding(): Boolean = settingsState.value.onboardingShown

    /**
     * Completes first-run onboarding in one atomic settings write. Keeping
     * both flags in the same write prevents an async race from restoring
     * onboardingShown=false after the dialog has already been dismissed.
     */
    fun completeOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettings(
                settingsState.value.copy(
                    isFirstLaunch = false,
                    onboardingShown = true
                )
            )
        }
    }

    /** @deprecated Use completeOnboarding() so both first-run flags change together. */
    @Deprecated("Use completeOnboarding()")
    fun markOnboardingShown() {
        completeOnboarding()
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
    
    fun getPagedTrashItems(query: String, filter: TrashFilterType): Flow<PagingData<TrashWrapper>> {
        val tableFilter = when(filter) {
            TrashFilterType.ALL -> ""
            TrashFilterType.TRANSACTIONS -> "habayeb_transactions"
            TrashFilterType.CUSTOMERS -> "habayeb_customers"
        }
        return Pager(PagingConfig(pageSize = 50)) {
            trashRepository.getPagedTrashItems(query, tableFilter)
        }.flow.map { pagingData ->
            pagingData.map { entity ->
                TrashWrapper(
                    entity = entity,
                    parsed = TrashItemParser.parse(
                        item = entity,
                        customersList = emptyList(),
                        currencySymbol = "ر.ي",
                        strings = TrashStrings("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
                        primaryColor = Color.Black,
                        secondaryColor = Color.Gray,
                        errorColor = Color.Red,
                        outlineColor = Color.LightGray
                    )
                )
            }
        }.cachedIn(viewModelScope)
    }

    fun cleanLedgerTrashItems() {
                viewModelScope.launch(Dispatchers.IO) {
            val period = settingsState.value.trashAutoCleanupPeriod
            val threshold = when (period) {
                "1_DAY" -> System.currentTimeMillis() - 86400000L
                "7_DAYS" -> System.currentTimeMillis() - 86400000L * 7
                "30_DAYS" -> System.currentTimeMillis() - 86400000L * 30
                else -> return@launch
            }
            trashRepository.removeExpiredBefore(threshold)
        }
    }
    
    fun updateAutoCleanupPeriod(period: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettings(settingsState.value.copy(trashAutoCleanupPeriod = period))
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            trashRepository.clearDeletedItems()
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_trash_emptied))
        }
    }

    fun restoreDeletedItem(entity: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            trashRepository.restoreDeletedItem(entity)
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_restored))
        }
    }

    fun permanentlyDeleteDeletedItem(entity: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            trashRepository.removeDeletedItem(entity)
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_permanently_deleted))
        }
    }

    fun restoreMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { trashRepository.restoreDeletedItem(it) }
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_items_restored))
        }
    }

    fun permanentlyDeleteMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { trashRepository.removeDeletedItem(it) }
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_items_permanently_deleted))
        }
    }
    
    fun restoreSingleTransactionFromBundle(bundleId: String, txId: String, entity: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            trashRepository.restoreSingleTransactionFromBundle(bundleId, txId)
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_restored))
        }
    }

    

    

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            maintenanceRepository.deleteAllData()
        }
    }
    
    companion object {
        const val TEST = 1
    }
}
