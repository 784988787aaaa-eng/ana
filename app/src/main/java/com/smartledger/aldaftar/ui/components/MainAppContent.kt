package com.smartledger.aldaftar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.screens.*
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityAndLicenseViewModel
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel

@Composable
fun MainAppContent(
    currentScreen: Screen,
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    contentPadding: PaddingValues = PaddingValues(),
    onNavigate: (Screen) -> Unit,
    onMenuClick: () -> Unit,
    onExit: () -> Unit,
    isDrawerOpen: Boolean = false,
    onHeaderDoubleClick: () -> Unit = {},
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
    val drawerStateHolder = rememberMainAppContentState(
        isDrawerOpen = isDrawerOpen,
        onMenuClick = onMenuClick
    )

    Box(modifier = modifier.fillMaxSize()) {
        val navFadeSpec = remember {
            spring<Float>(
                dampingRatio = 0.9f,
                stiffness = 500f
            )
        }
        val navOffsetSpec = remember {
            spring<IntOffset>(
                dampingRatio = 0.9f,
                stiffness = 500f
            )
        }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isInitialSub = initialState == Screen.SETTINGS || initialState == Screen.TRASH || initialState == Screen.BUSINESS_PROFILE || initialState == Screen.SECURITY
                val isTargetSub = targetState == Screen.SETTINGS || targetState == Screen.TRASH || targetState == Screen.BUSINESS_PROFILE || targetState == Screen.SECURITY

                if (isTargetSub && !isInitialSub) {
                    // Entering secondary/settings screen: Vertical spring entrance with subtle fade
                    val slideIn = slideInVertically(animationSpec = navOffsetSpec) { (it * 0.12f).toInt() } +
                            fadeIn(animationSpec = navFadeSpec)
                    val slideOut = fadeOut(animationSpec = navFadeSpec)
                    slideIn togetherWith slideOut
                } else if (isInitialSub && !isTargetSub) {
                    // Exiting secondary screen back to main: Subtle fade & slide down
                    val slideIn = fadeIn(animationSpec = navFadeSpec)
                    val slideOut = slideOutVertically(animationSpec = navOffsetSpec) { (it * 0.12f).toInt() } +
                            fadeOut(animationSpec = navFadeSpec)
                    slideIn togetherWith slideOut
                } else {
                    // Lateral tab switching (Ledger <-> Habayeb): Horizontal translation with clean fade
                    val isForward = targetState.ordinal > initialState.ordinal
                    val slideIn = if (isForward) {
                        slideInHorizontally(animationSpec = navOffsetSpec) { width -> (width * 0.2f).toInt() } +
                        fadeIn(animationSpec = navFadeSpec)
                    } else {
                        slideInHorizontally(animationSpec = navOffsetSpec) { width -> (-width * 0.2f).toInt() } +
                        fadeIn(animationSpec = navFadeSpec)
                    }
                    val slideOut = if (isForward) {
                        slideOutHorizontally(animationSpec = navOffsetSpec) { width -> (-width * 0.2f).toInt() } +
                        fadeOut(animationSpec = navFadeSpec)
                    } else {
                        slideOutHorizontally(animationSpec = navOffsetSpec) { width -> (width * 0.2f).toInt() } +
                        fadeOut(animationSpec = navFadeSpec)
                    }
                    slideIn togetherWith slideOut
                }
            },
            label = "ScreenSwitch"
        ) { screen ->
            when (screen) {
                Screen.HABAYEB -> {
                    HabayebScreen(
                        viewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        onMenuClick = { drawerStateHolder.handleMenuClick() },
                        onClose = onExit,
                        contentPadding = contentPadding,
                        isDrawerOpen = drawerStateHolder.isDrawerOpen,
                        onHeaderDoubleClick = onHeaderDoubleClick,
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
                Screen.LEDGER -> {
                    MainLedgerView(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        settings = settings,
                        onBackIntercept = {},
                        onMenuClick = { drawerStateHolder.handleMenuClick() },
                        isDrawerOpen = drawerStateHolder.isDrawerOpen,
                        isFloatingSearchActive = isFloatingSearchActive,
                        onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                        isSearchActive = isSearchActive,
                        onSearchActiveChanged = onSearchActiveChanged,
                        contentPadding = contentPadding
                    )
                }
                Screen.SETTINGS -> {
                    SettingsView(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        backupSyncViewModel = backupSyncViewModel,
                        settings = settings,
                        onNavigateToSecurity = { onNavigate(Screen.SECURITY) },
                        contentPadding = contentPadding
                    )
                }
                Screen.TRASH -> {
                    TrashScreen(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        onBack = { onNavigate(Screen.HABAYEB) },
                        contentPadding = contentPadding
                    )
                }
                Screen.BUSINESS_PROFILE -> {
                    BusinessProfileScreen(
                        onBack = { onNavigate(Screen.HABAYEB) },
                        contentPadding = contentPadding
                    )
                }
                Screen.SECURITY -> {
                    SecurityScreen(
                        settings = settings,
                        viewModel = securityViewModel,
                        onBack = { onNavigate(Screen.LEDGER) },
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }
}
