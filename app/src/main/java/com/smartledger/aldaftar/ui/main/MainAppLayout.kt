package com.smartledger.aldaftar.ui.main

import androidx.compose.material3.MaterialTheme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.components.*
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.screens.BackupRestoreBottomSheet
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import com.smartledger.aldaftar.ui.screens.license.LicenseDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityViewModel,
    backupSyncViewModel: BackupSyncViewModel,
    businessProfileViewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel,
    licenseViewModel: LicenseViewModel,
    settings: AppSettings,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: FinanceConstants.DEFAULT_FALLBACK_VERSION
        } catch (e: Exception) {
            FinanceConstants.DEFAULT_FALLBACK_VERSION
        }
    }
    var showComprehensiveReportDialog by remember { mutableStateOf(false) }
    var showCurrencySettingsDialog by remember { mutableStateOf(false) }
    var showBusinessProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.HABAYEB) }
    var hasInitializedStartScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasInitializedStartScreen) {
            currentScreen = Screen.HABAYEB
            hasInitializedStartScreen = true
        }
    }

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    var showCurrencyBallSelector by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var forceLicenseDialog by remember { mutableStateOf(false) }
    val licenseSnapshot by licenseViewModel.snapshot.collectAsStateWithLifecycle()

    var isFloatingSearchActive by remember { mutableStateOf(viewModel.isFloatingSearchActive()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isHistoryOverlayActive by remember { mutableStateOf(false) }
    var isHistorySearchActive by remember { mutableStateOf(false) }
    var habayebFabOverlay by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val activity = context as? android.app.Activity
    LaunchedEffect(activity) {
        val navigateTo = activity?.intent?.getStringExtra(FinanceConstants.EXTRA_NAVIGATE_TO)
        if (navigateTo == FinanceConstants.DEST_BACKUP_SETTINGS) {
            showBackupRestoreSheet = true
            activity?.intent?.removeExtra(FinanceConstants.EXTRA_NAVIGATE_TO)
        }
    }

    LaunchedEffect(Unit) {
        licenseViewModel.licenseRequiredEvent.collect {
            showLicenseDialog = true
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler {
        val defaultStart = Screen.HABAYEB
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != defaultStart) {
            currentScreen = defaultStart
        } else {
            if (settings.doubleCheckExit) {
                showExitConfirmDialog = true
            } else {
                onExit()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen == Screen.HABAYEB,
        drawerContent = {
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
            AppNavigationDrawer(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                    scope.launch { drawerState.close() }
                },
                onBackupClick = {
                    scope.launch { drawerState.close() }
                    showBackupRestoreSheet = true
                },
                settings = settings,
                securityViewModel = securityViewModel,
                licenseViewModel = licenseViewModel,
                onLicenseClick = { scope.launch { drawerState.close() }; forceLicenseDialog = false; showLicenseDialog = true },
                businessProfileViewModel = businessProfileViewModel,
                onSaveSettings = { updated, targetCurrency, newRate, revalueHistorical ->
                    viewModel.saveSettings(updated)
                    if (revalueHistorical && targetCurrency.isNotEmpty() && newRate > 0.0) {
                        habayebViewModel.revalueHistoricalTransactions(updated.currencySymbol, targetCurrency, java.math.BigDecimal.valueOf(newRate))
                    }
                },
                versionName = versionName,
                onComprehensiveReportClick = {
                    scope.launch { drawerState.close() }
                    showComprehensiveReportDialog = true
                },
                onBusinessProfileClick = {
                    scope.launch { drawerState.close() }
                    showBusinessProfileDialog = true
                },
                onCurrencySettingsClick = {
                    scope.launch { drawerState.close() }
                    showCurrencySettingsDialog = true
                },
                onSecurityClick = {
                    scope.launch { drawerState.close() }
                    showSecurityDialog = true
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MainAppContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        backupSyncViewModel = backupSyncViewModel,
                        businessProfileViewModel = businessProfileViewModel,
                        settings = settings,
                        contentPadding = innerPadding,
                        onNavigate = { currentScreen = it },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        isDrawerOpen = drawerState.isOpen,
                        onExit = {
                            if (settings.doubleCheckExit) {
                                showExitConfirmDialog = true
                            } else {
                                onExit()
                            }
                        },
                        onHeaderDoubleClick = { showComprehensiveReportDialog = true },
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
                }
            }

            if (currentScreen == Screen.HABAYEB) {
                habayebFabOverlay?.invoke()
            }

            val hideBubble = if (isHistoryOverlayActive) isHistorySearchActive else isSearchActive
            if (isFloatingSearchActive && !hideBubble && currentScreen == Screen.HABAYEB) {
                com.smartledger.aldaftar.ui.screens.habayeb.components.FloatingSearchBubble(
                    activeThemeColor = MaterialTheme.colorScheme.primary,
                    persisted = viewModel.floatingSearchState(),
                    onPersist = viewModel::saveFloatingSearchState,
                    onSearchClick = {
                        if (isHistoryOverlayActive) {
                            isHistorySearchActive = true
                        } else {
                            isSearchActive = true
                        }
                    }
                )
            }
        }
    }

    ExitConfirmDialog(
        show = showExitConfirmDialog,
        onDismiss = { showExitConfirmDialog = false },
        onConfirm = { dontShowAgain ->
            if (dontShowAgain) {
                viewModel.saveSettings(settings.copy(doubleCheckExit = false))
            }
            showExitConfirmDialog = false
            onExit()
        }
    )

    if (showLicenseDialog) {
        LicenseDialog(viewModel = licenseViewModel, onDismiss = { showLicenseDialog = false })
    }

    if (showBackupRestoreSheet) {
        BackupRestoreBottomSheet(
            backupSyncViewModel = backupSyncViewModel,
            onDismiss = { showBackupRestoreSheet = false },
            onRestoreSuccess = viewModel::saveSettings
        )
    }

    if (showComprehensiveReportDialog) {
        val habayebCustomersState by habayebViewModel.customersUiState.collectAsStateWithLifecycle()
        val selectedCustomerIds by habayebViewModel.selectedCustomerIdsState.collectAsStateWithLifecycle()
        val businessProfile by businessProfileViewModel.profile.collectAsStateWithLifecycle()
        com.smartledger.aldaftar.ui.screens.habayeb.components.ComprehensiveReportDialog(
            customers = habayebCustomersState.customers,
            currencySymbol = settings.currencySymbol,
            activeThemeColor = MaterialTheme.colorScheme.primary,
            onDismiss = { showComprehensiveReportDialog = false },
            selectedCustomerIds = selectedCustomerIds,
            businessProfile = businessProfile,
            loadTransactionsForReport = habayebViewModel::transactionsForReport,
            reportCoroutineScope = scope
        )
    }

    if (showCurrencySettingsDialog) {
        CurrencySettingsDialog(
            settings = settings,
            onSaveSettings = { updated, targetCurrency, newRate, revalueHistorical ->
                viewModel.saveSettings(updated)
                if (revalueHistorical && targetCurrency.isNotEmpty() && newRate > 0.0) {
                    habayebViewModel.revalueHistoricalTransactions(updated.currencySymbol, targetCurrency, java.math.BigDecimal.valueOf(newRate))
                }
            },
            onDismiss = { showCurrencySettingsDialog = false }
        )
    }

    if (showBusinessProfileDialog) {
        com.smartledger.aldaftar.ui.screens.BusinessProfileDialog(
            viewModel = businessProfileViewModel,
            onDismiss = { showBusinessProfileDialog = false }
        )
    }

    if (showSecurityDialog) {
        com.smartledger.aldaftar.ui.screens.SecurityDialog(
            settings = settings,
            viewModel = securityViewModel,
            onDismiss = { showSecurityDialog = false }
        )
    }
}
