package com.smartledger.aldaftar.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.components.AppNavigationDrawer
import com.smartledger.aldaftar.ui.components.MainAppContent
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.screens.habayeb.components.FloatingSearchBubble
import com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.launch

/** Minimal shell: one normal content surface (Habayeb) and the navigation drawer. */
@Composable
fun MainAppLayout(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityViewModel,
    businessProfileViewModel: BusinessProfileViewModel,
    settings: AppSettings,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isFloatingSearchActive by remember { mutableStateOf(viewModel.isFloatingSearchActive()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isHistoryOverlayActive by remember { mutableStateOf(false) }
    var isHistorySearchActive by remember { mutableStateOf(false) }
    var habayebFabOverlay by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val currentScreen = Screen.HABAYEB
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onExit()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
            AppNavigationDrawer(
                currentScreen = currentScreen,
                settings = settings,
                versionName = versionName,
                onHomeClick = { scope.launch { drawerState.close() } },
                onThemeToggle = {
                    val nextMode = if (settings.themeMode == 2) 1 else 2
                    viewModel.saveSettings(settings.copy(themeMode = nextMode))
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            MainAppContent(
                currentScreen = currentScreen,
                habayebViewModel = habayebViewModel,
                securityViewModel = securityViewModel,
                businessProfileViewModel = businessProfileViewModel,
                onMenuClick = { scope.launch { drawerState.open() } },
                onExit = onExit,
                isDrawerOpen = drawerState.isOpen,
                isFloatingSearchActive = isFloatingSearchActive,
                onFloatingSearchActiveChanged = {
                    isFloatingSearchActive = it
                    viewModel.setFloatingSearchActive(it)
                },
                isSearchActive = isSearchActive,
                onSearchActiveChanged = { isSearchActive = it },
                isHistoryOverlayActive = isHistoryOverlayActive,
                onHistoryOverlayActiveChanged = { isHistoryOverlayActive = it },
                isHistorySearchActive = isHistorySearchActive,
                onHistorySearchActiveChanged = { isHistorySearchActive = it },
                onFabOverlayChanged = { habayebFabOverlay = it },
                modifier = Modifier.fillMaxSize()
            )

            habayebFabOverlay?.invoke()

            val hideBubble = if (isHistoryOverlayActive) isHistorySearchActive else isSearchActive
            if (isFloatingSearchActive && !hideBubble) {
                FloatingSearchBubble(
                    activeThemeColor = MaterialTheme.colorScheme.primary,
                    persisted = viewModel.floatingSearchState(),
                    onPersist = viewModel::saveFloatingSearchState,
                    onSearchClick = {
                        if (isHistoryOverlayActive) isHistorySearchActive = true else isSearchActive = true
                    }
                )
            }
        }
    }
}
