package com.example.ui.main

/*
 * =====================================================================================
 * حزمة التخطيط الهيكلي الرئيسي للتطبيق (Main Layout Architecture Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على الهيكل البنائي الأساسي لواجهات التطبيق، حيث تدير الربط بين
 * القائمة الجانبية (Drawer)، شريط التنقل السفلي العائم، النوافذ السفلية المنبثقة،
 * ومعالجة زر الرجوع الفيزيائي للنظام (Back Navigation).
 * =====================================================================================
 */

import androidx.compose.material3.MaterialTheme
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.ledger.components.DeviceActivationDialog
import com.example.ui.screens.BackupRestoreBottomSheet
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import com.example.ui.viewmodel.BackupSyncViewModel
import com.example.ui.viewmodel.FinanceConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * =====================================================================================
 * التخطيط المعماري الرئيسي للتطبيق (MainAppLayout)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * المكون الهيكلي الشامل لحاوية التطبيق الأساسية:
 * 1. احتواء القائمة الجانبية للتنقل والإعدادات (ModalNavigationDrawer & AppNavigationDrawer).
 * 2. احتواء هيكل Scaffold الرئيسي وشريط التنقل السفلي العائم (MainBottomNavigation).
 * 3. إدارة عقود أندرويد لخدمات التخزين (Storage Access Framework - SAF) لتصدير واستيراد النسخ الاحتياطية.
 * 4. إدارة أحداث زر الرجوع الفيزيائي (BackHandler) للتبديل بين الشاشة الافتراضية، إغلاق القائمة، أو تأكيد الخروج.
 * 5. عرض النوافذ العائمة التفاعلية (التنشيط، النسخ الاحتياطي، التقارير الشاملة، والفقاعة العائمة للبحث).
 *
 * [المُدخلات]:
 * - viewModel: نموذج بيانات المعاملات المالية ودفتر اليومية.
 * - habayebViewModel: نموذج بيانات حسابات وعملاء الحبايب.
 * - securityViewModel: نموذج بيانات الأمان والترخيص وحماية التطبيق.
 * - backupSyncViewModel: نموذج بيانات إدارة النسخ الاحتياطي السحابي والمحلي.
 * - settings: إعدادات التطبيق الحالية (العملة، التفضيلات، الأمان).
 * - onExit: دالة إنهاء النشاط وإغلاق التطبيق.
 * =====================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    /*
     * ---------------------------------------------------------------------------------
     * استخراج معلومات الإصدار وتنسيق التاريخ للنسخ الاحتياطي
     * ---------------------------------------------------------------------------------
     */
    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: FinanceConstants.DEFAULT_FALLBACK_VERSION
        } catch (e: Exception) {
            FinanceConstants.DEFAULT_FALLBACK_VERSION
        }
    }
    val sdfName = remember { java.text.SimpleDateFormat(FinanceConstants.BACKUP_DATE_FORMAT, java.util.Locale.US) }

    /*
     * ---------------------------------------------------------------------------------
     * مراقبة حالات التنشيط والترخيص ووجهة البدء الافتراضية
     * ---------------------------------------------------------------------------------
     */
    val defaultStartDest by viewModel.defaultStartDestinationState.collectAsStateWithLifecycle()
    val isActivated by securityViewModel.isActivatedState.collectAsStateWithLifecycle()
    val deviceId by securityViewModel.deviceIdState.collectAsStateWithLifecycle()
    
    var showActivationDialog by remember { mutableStateOf(false) }
    var isActivationAutoTriggered by remember { mutableStateOf(false) }

    val showSecurityActivationRequired by securityViewModel.showActivationRequired.collectAsStateWithLifecycle()
    val showHabayebActivationRequired by habayebViewModel.showActivationRequired.collectAsStateWithLifecycle()
    val showBackupActivationRequired by backupSyncViewModel.showActivationRequired.collectAsStateWithLifecycle()

    /*
     * مراقبة طلبات التنشيط التلقائية من مختلف الشاشات عند محاولة استخدام ميزة مدفوعة
     */
    LaunchedEffect(showSecurityActivationRequired) {
        if (showSecurityActivationRequired) {
            isActivationAutoTriggered = true
            showActivationDialog = true
            securityViewModel.showActivationRequired.value = false
        }
    }

    LaunchedEffect(showHabayebActivationRequired) {
        if (showHabayebActivationRequired) {
            isActivationAutoTriggered = true
            showActivationDialog = true
            habayebViewModel.resetActivationRequired()
        }
    }

    LaunchedEffect(showBackupActivationRequired) {
        if (showBackupActivationRequired) {
            isActivationAutoTriggered = true
            showActivationDialog = true
            backupSyncViewModel.showActivationRequired.value = false
        }
    }

    var showComprehensiveReportDialog by remember { mutableStateOf(false) }
    val initialStartScreen = remember(defaultStartDest) {
        try {
            Screen.valueOf(defaultStartDest)
        } catch (e: Exception) {
            Screen.HABAYEB
        }
    }
    var currentScreen by remember { mutableStateOf(initialStartScreen) }
    var hasInitializedStartScreen by remember { mutableStateOf(false) }

    /*
     * تهيئة الشاشة الابتدائية للتطبيق وفق تفضيل المستخدم المحفوظ في الإعدادات
     */
    LaunchedEffect(defaultStartDest) {
        if (!hasInitializedStartScreen) {
            val targetScreen = try {
                Screen.valueOf(defaultStartDest)
            } catch (e: Exception) {
                Screen.HABAYEB
            }
            if (currentScreen != targetScreen) {
                currentScreen = targetScreen
            }
            hasInitializedStartScreen = true
        }
    }

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    var showCurrencyBallSelector by remember { mutableStateOf(false) }

    /*
     * ---------------------------------------------------------------------------------
     * إعدادات وحالات البحث العائم والتراكبات (Floating Search & Overlays)
     * ---------------------------------------------------------------------------------
     */
    val floatingSearchPrefs = remember(context) { 
        context.getSharedPreferences(FinanceConstants.PREFS_FLOATING_SEARCH, android.content.Context.MODE_PRIVATE) 
    }
    var isFloatingSearchActive by remember {
        mutableStateOf(floatingSearchPrefs.getBoolean(FinanceConstants.KEY_FLOATING_SEARCH_ACTIVE, false))
    }
    var isSearchActive by remember { mutableStateOf(false) }
    var isHistoryOverlayActive by remember { mutableStateOf(false) }
    var isHistorySearchActive by remember { mutableStateOf(false) }
    var habayebFabOverlay by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    /*
     * معالجة نية الانتقال المباشر للنسخ الاحتياطي عبر الاختصارات أو الإشعارات
     */
    val activity = context as? android.app.Activity
    LaunchedEffect(activity) {
        val navigateTo = activity?.intent?.getStringExtra(FinanceConstants.EXTRA_NAVIGATE_TO)
        if (navigateTo == FinanceConstants.DEST_BACKUP_SETTINGS) {
            showBackupRestoreSheet = true
            activity?.intent?.removeExtra(FinanceConstants.EXTRA_NAVIGATE_TO)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    /*
     * ---------------------------------------------------------------------------------
     * عقود اختيار الملفات لنظام أندرويد (Storage Access Framework Launchers)
     * ---------------------------------------------------------------------------------
     */
    // عقد تصدير وإنشاء ملف نسخة احتياطية محلياً بتنسيق JSON
    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(FinanceConstants.MIME_TYPE_JSON)
    ) { uri ->
        if (uri != null) {
            backupSyncViewModel.getBackupJsonForClipboard { jsonStr ->
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(jsonStr.toByteArray())
                            }
                        }
                        Toast.makeText(context, context.getString(R.string.toast_backup_export_success), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // عقد فتح واستيراد ملف نسخة احتياطية من جهاز المستخدم
    val safRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonText = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    }
                    if (jsonText.isNotBlank()) {
                        backupSyncViewModel.executeMasterRestore(jsonText, context) { success, _ ->
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.toast_restore_success), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_restore_invalid_file), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * معالج زر الرجوع الفيزيائي ونظام الإيماءات (BackHandler)
     * ---------------------------------------------------------------------------------
     * الأولويات:
     * 1. إغلاق القائمة الجانبية إذا كانت مفتوحة.
     * 2. العودة إلى شاشة البدء الافتراضية إذا كان المستخدم في شاشة أخرى.
     * 3. إظهار نافذة تأكيد الخروج أو الخروج المباشر وفق التفضيلات.
     * ---------------------------------------------------------------------------------
     */
    BackHandler {
        val defaultStart = try {
            Screen.valueOf(defaultStartDest)
        } catch (e: Exception) {
            Screen.HABAYEB
        }
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

    /*
     * ---------------------------------------------------------------------------------
     * القائمة الجانبية للتطبيق (ModalNavigationDrawer)
     * ---------------------------------------------------------------------------------
     */
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen == Screen.HABAYEB || currentScreen == Screen.LEDGER,
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
                isActivated = isActivated,
                onActivateProClick = {
                    scope.launch { drawerState.close() }
                    isActivationAutoTriggered = false
                    showActivationDialog = true
                },
                settings = settings,
                securityViewModel = securityViewModel,
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
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            /*
             * -------------------------------------------------------------------------
             * هيكل واجهة المستخدم مع شريط التنقل السفلي العائم
             * -------------------------------------------------------------------------
             */
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                bottomBar = {
                    MainBottomNavigation(
                        currentScreen = currentScreen,
                        isVisible = currentScreen == Screen.HABAYEB || currentScreen == Screen.LEDGER,
                        onNavigate = { currentScreen = it }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // مضيف الشاشات ومحتوى التطبيق الرئيسي
                    MainAppContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        backupSyncViewModel = backupSyncViewModel,
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
                            floatingSearchPrefs.edit().putBoolean(FinanceConstants.KEY_FLOATING_SEARCH_ACTIVE, it).apply()
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

            // زر العمل العائم المخصص لشاشة الحبايب
            if (currentScreen == Screen.HABAYEB) {
                habayebFabOverlay?.invoke()
            }

            /*
             * فقاعة البحث العائمة السريعة
             */
            val hideBubble = if (currentScreen == Screen.HABAYEB) {
                if (isHistoryOverlayActive) isHistorySearchActive else isSearchActive
            } else {
                isSearchActive
            }
            if (isFloatingSearchActive && !hideBubble && (currentScreen == Screen.HABAYEB || currentScreen == Screen.LEDGER)) {
                com.example.ui.screens.habayeb.components.FloatingSearchBubble(
                    activeThemeColor = MaterialTheme.colorScheme.primary,
                    onSearchClick = {
                        if (currentScreen == Screen.HABAYEB) {
                            if (isHistoryOverlayActive) {
                                isHistorySearchActive = true
                            } else {
                                isSearchActive = true
                            }
                        } else if (currentScreen == Screen.LEDGER) {
                            isSearchActive = true
                        }
                    }
                )
            }
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * النوافذ المنبثقة التفاعلية ومربعات الحوار الشاملة
     * ---------------------------------------------------------------------------------
     */
    // نافذة تأكيد الخروج من التطبيق
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

    // الورقة السفلية للنسخ الاحتياطي واستعادة البيانات
    if (showBackupRestoreSheet) {
        BackupRestoreBottomSheet(
            settings = settings,
            backupSyncViewModel = backupSyncViewModel,
            onExportMzd = {
                val dateStr = sdfName.format(java.util.Date())
                safExportLauncher.launch("${FinanceConstants.BACKUP_FILE_PREFIX}$dateStr${FinanceConstants.BACKUP_FILE_EXTENSION}")
            },
            onImportMzd = {
                safRestoreLauncher.launch(arrayOf(FinanceConstants.MIME_TYPE_ALL_APP))
            },
            onDismiss = { showBackupRestoreSheet = false }
        )
    }

    // نافذة تنشيط التطبيق وإدخال مفتاح الترخيص
    if (showActivationDialog) {
        DeviceActivationDialog(
            deviceId = deviceId,
            viewModel = securityViewModel,
            backupSyncViewModel = backupSyncViewModel,
            onDismiss = { showActivationDialog = false },
            isAutoTriggered = isActivationAutoTriggered
        )
    }

    // نافذة التقرير المالي الشامل للعملاء والحسابات
    if (showComprehensiveReportDialog) {
        val habayebCustomersState by habayebViewModel.customersUiState.collectAsStateWithLifecycle()
        val selectedCustomerIds by habayebViewModel.selectedCustomerIdsState.collectAsStateWithLifecycle()
        com.example.ui.screens.habayeb.components.ComprehensiveReportDialog(
            customers = habayebCustomersState.customers,
            currencySymbol = settings.currencySymbol,
            activeThemeColor = MaterialTheme.colorScheme.primary,
            onDismiss = { showComprehensiveReportDialog = false },
            selectedCustomerIds = selectedCustomerIds
        )
    }
}

