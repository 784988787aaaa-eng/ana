package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.screens.HabayebScreen
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel

/**
 * The application's only normal content surface: the Habayeb customer/debt workspace.
 * Navigation to former ledger/settings/trash screens intentionally no longer exists.
 */
@Composable
fun MainAppContent(
    currentScreen: Screen,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityViewModel,
    businessProfileViewModel: BusinessProfileViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    onMenuClick: () -> Unit,
    onExit: () -> Unit,
    isDrawerOpen: Boolean = false,
    isFloatingSearchActive: Boolean = false,
    onFloatingSearchActiveChanged: (Boolean) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    isHistoryOverlayActive: Boolean = false,
    onHistoryOverlayActiveChanged: (Boolean) -> Unit = {},
    isHistorySearchActive: Boolean = false,
    onHistorySearchActiveChanged: (Boolean) -> Unit = {},
    onFabOverlayChanged: (((@Composable () -> Unit)?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val businessProfile by businessProfileViewModel.profile.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        // Keep the guard explicit so a stale navigation value can never resurrect a removed screen.
        if (currentScreen == Screen.HABAYEB) {
            HabayebScreen(
                viewModel = habayebViewModel,
                securityViewModel = securityViewModel,
                businessProfile = businessProfile,
                onMenuClick = onMenuClick,
                onClose = onExit,
                contentPadding = contentPadding,
                isDrawerOpen = isDrawerOpen,
                isFloatingSearchActive = isFloatingSearchActive,
                onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                isSearchActive = isSearchActive,
                onSearchActiveChanged = onSearchActiveChanged,
                isHistoryOverlayActive = isHistoryOverlayActive,
                onHistoryOverlayActiveChanged = onHistoryOverlayActiveChanged,
                isHistorySearchActive = isHistorySearchActive,
                onHistorySearchActiveChanged = onHistorySearchActiveChanged,
                onFabOverlayChanged = onFabOverlayChanged
            )
        }
    }
}
