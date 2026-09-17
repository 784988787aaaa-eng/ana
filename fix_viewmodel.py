import re

with open('app/src/main/java/com/smartledger/aldaftar/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Add DataMaintenanceRepository and FloatingUiPreferencesRepository to constructor
content = re.sub(r'class FinanceViewModel\(\n    application: Application,\n    private val settingsRepository: SettingsRepository,\n    private val categoriesRepository: CategoryRepository,\n    private val trashRepository: TrashRepository,\n    private val habayebRepository: HabayebRepository,\n    private val licenseRepository: LicenseRepository\n\)', 
"""class FinanceViewModel(
    application: Application,
    private val licenseRepository: LicenseRepository,
    private val settingsRepository: SettingsRepository,
    private val categoriesRepository: CategoryRepository,
    private val habayebRepository: HabayebRepository,
    private val trashRepository: TrashRepository,
    private val maintenanceRepository: com.smartledger.aldaftar.data.repository.DataMaintenanceRepository,
    private val floatingUiRepository: com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
)""", content)


# Fix uiEventFlow
content = content.replace("class FinanceViewModel", "import kotlinx.coroutines.channels.Channel\nimport kotlinx.coroutines.flow.receiveAsFlow\nclass FinanceViewModel")
ui_event = """    private val _uiEventChannel = Channel<UiEvent>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()"""
content = content.replace("    val settingsState: StateFlow<AppSettings>", ui_event + "\n\n    val settingsState: StateFlow<AppSettings>")


# Fix floating properties
floating_props = """
    fun isFloatingSearchActive(): Boolean = floatingUiRepository.floatingSearchActive()
    
    fun setFloatingSearchActive(active: Boolean) {
        floatingUiRepository.setFloatingSearchActive(active)
    }

    fun floatingSearchState(): com.smartledger.aldaftar.data.repository.FloatingSearchState = floatingUiRepository.search()
    
    fun saveFloatingSearchState(state: com.smartledger.aldaftar.data.repository.FloatingSearchState) {
        floatingUiRepository.saveSearch(state)
    }"""
content = re.sub(r'    private val _isFloatingSearchActive = MutableStateFlow.*?    fun saveFloatingSearchState\(query: String\) \{\n        _floatingSearchState\.value = query\n    \}', floating_props, content, flags=re.DOTALL)


# Fix toasts
content = content.replace("trashRepository.clearDeletedItems()", "trashRepository.clearDeletedItems()\n            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_trash_emptied))")
content = content.replace("trashRepository.restoreDeletedItem(entity)", "trashRepository.restoreDeletedItem(entity)\n            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_restored))")
content = content.replace("trashRepository.removeDeletedItem(entity)", "trashRepository.removeDeletedItem(entity)\n            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_permanently_deleted))")
content = content.replace("items.forEach { trashRepository.restoreDeletedItem(it) }", "items.forEach { trashRepository.restoreDeletedItem(it) }\n            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_items_restored))")
content = content.replace("items.forEach { trashRepository.removeDeletedItem(it) }", "items.forEach { trashRepository.removeDeletedItem(it) }\n            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_items_permanently_deleted))")


# Fix restoreSingleTransactionFromBundle
content = re.sub(r'fun restoreSingleTransactionFromBundle\(.*?\)\s*\{\s*viewModelScope\.launch\(Dispatchers\.IO\)\s*\{\s*trashRepository\.restoreSingleTransactionFromBundle\(bundleId, txId\)\s*_uiEventChannel\.send\(UiEvent\.ShowToast\(R\.string\.toast_item_restored\)\)\s*\}\s*\}', 
"""fun restoreSingleTransactionFromBundle(bundleId: String, txId: String, entity: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            trashRepository.restoreSingleTransactionFromBundle(bundleId, txId)
            _uiEventChannel.send(UiEvent.ShowToast(R.string.toast_item_restored))
        }
    }""", content, flags=re.DOTALL)


# Add deleteAllData
content = re.sub(r'fun deleteAllData\(\)\s*\{\s*viewModelScope\.launch\(Dispatchers\.IO\)\s*\{\s*// Delete all data implementation\s*\}\s*\}', 
"""fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            maintenanceRepository.resetAllDataAndCaches()
        }
    }""", content, flags=re.DOTALL)


with open('app/src/main/java/com/smartledger/aldaftar/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
