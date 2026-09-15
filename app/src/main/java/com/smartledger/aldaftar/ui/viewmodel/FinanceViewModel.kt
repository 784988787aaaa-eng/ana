package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FloatingSearchState
import com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
import com.smartledger.aldaftar.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Small shell ViewModel: app appearance/preferences used by the single Habayeb workspace. */
class FinanceViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val floatingUiRepository: FloatingUiPreferencesRepository
) : AndroidViewModel(application) {

    private val _themeModeState = MutableStateFlow(0)
    val themeModeState: StateFlow<Int> = _themeModeState.asStateFlow()
    val isSettingsLoaded = MutableStateFlow(false)

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .onEach {
            isSettingsLoaded.value = true
            if (it != null) _themeModeState.value = it.themeMode
        }
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun saveSettings(settings: AppSettings) {
        _themeModeState.value = settings.themeMode
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettings(settings)
        }
    }

    fun floatingSearchState(): FloatingSearchState = floatingUiRepository.search()
    fun saveFloatingSearchState(state: FloatingSearchState) = floatingUiRepository.saveSearch(state)
    fun isFloatingSearchActive(): Boolean = floatingUiRepository.floatingSearchActive()
    fun setFloatingSearchActive(active: Boolean) = floatingUiRepository.setFloatingSearchActive(active)
}
