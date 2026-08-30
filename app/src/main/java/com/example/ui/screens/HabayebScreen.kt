package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة شاشة الحبايب والعملاء (Habayeb Customers & Ledger Screen Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المنسق الرئيسي لواجهة الحبايب (العملاء والحسابات المدينة والدائنة)،
 * وتدير الفلترة والتصنيفات، البحث، التحديد المتعدد، كشف الحساب التفصيلي، وإدارة الحوارات.
 * =====================================================================================
 */

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.HabayebCustomer
import com.example.domain.model.TransactionType
import com.example.ui.screens.habayeb.HabayebDialogHost
import com.example.ui.screens.habayeb.HabayebDialogState
import com.example.ui.screens.habayeb.HabayebFabHost
import com.example.ui.screens.habayeb.components.CustomerHistoryOverlay
import com.example.ui.screens.habayeb.components.CustomerMultiSelectFloatingBar
import com.example.ui.screens.habayeb.components.HabayebFilterToolbar
import com.example.ui.screens.habayeb.components.HabayebFinanceHeader
import com.example.ui.screens.habayeb.components.HabayebListSection
import com.example.ui.state.CustomerUiState
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.HabayebUiEvent
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

/*
 * إعادة تصدير حالة الحوارات (HabayebDialogState) لتجنب انقطاع التوافقية
 */
typealias HabayebDialogState = HabayebDialogState

/*
 * =====================================================================================
 * شاشة الحبايب وإدارة الحسابات (HabayebScreen)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * المنسق المعماري الشامل لواجهة الحبايب ودفتر الأستاذ للعملاء:
 * 1. ترويسة مالية تلخص إجمالي ما لنا (له) وما علينا (عليه) مع دعم وضع الخصوصية.
 * 2. شريط أدوات الفلترة والترتيب وتخصيص وإعادة ترتيب التصنيفات المخصصة.
 * 3. قائمة حسابات مرنة تدعم التثبيت والتمييز البصري والإضافة السريعة والتحديد المتعدد.
 * 4. طبقة تراكبية (Overlay) لكشف الحساب التاريخي مع رسوم حركية سلسة.
 * 5. إدارة موحدة لكافة النوافذ المنبثقة من خلال (HabayebDialogHost).
 *
 * [المُدخلات]:
 * - viewModel: نموذج بيانات الإدارة المالية للحبايب والعملاء.
 * - securityViewModel: نموذج بيانات الأمان والترخيص ووضع الخصوصية.
 * - onMenuClick: فتح القائمة الجانبية للتطبيق.
 * - onClose: إغلاق الشاشة أو الرجوع.
 * - contentPadding: هوامش التباعد الداخلية من نظام التشغيل.
 * - isDrawerOpen: حالة فتح القائمة الجانبية لمنع اعتراض زر الرجوع.
 * =====================================================================================
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HabayebScreen(
    viewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    onMenuClick: () -> Unit,
    onClose: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
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
    onFabOverlayChanged: (((@Composable () -> Unit)?) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val activeThemeColor = MaterialTheme.colorScheme.primary
    val activeSubColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceBackgroundColor = MaterialTheme.colorScheme.background

    /*
     * ---------------------------------------------------------------------------------
     * جمع تدفقات الحالة من نماذج البيانات (State Ingestion)
     * ---------------------------------------------------------------------------------
     */
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val isPrivacyMode by securityViewModel.isPrivacyModeEnabled.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val isDark = remember(settingsState.themeMode, systemDark) {
        when (settingsState.themeMode) {
            1 -> false
            2 -> true
            else -> systemDark
        }
    }
    val currencySymbol = settingsState.currencySymbol

    // حالات التحديد المتعدد للحسابات والمعاملات
    val selectedCustomerIds = remember { mutableStateListOf<String>() }
    var isMultiSelectActive by remember { mutableStateOf(false) }
    var isHistoryTxMultiSelectActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { selectedCustomerIds.toList() }
            .distinctUntilChanged()
            .collect { list ->
                viewModel.updateSelectedCustomerIds(list)
            }
    }

    // إدارة الحوار المنبثق النشط والعميل المفتوح كشف حسابه
    var activeDialogState by remember { mutableStateOf<HabayebDialogState>(HabayebDialogState.None) }
    var activeCustomerForHistory by remember { mutableStateOf<HabayebCustomer?>(null) }
    var stableCustomer by remember { mutableStateOf<HabayebCustomer?>(null) }

    LaunchedEffect(activeCustomerForHistory) {
        if (activeCustomerForHistory != null) {
            stableCustomer = activeCustomerForHistory
            viewModel.updateSearchQuery("")
            onSearchActiveChanged(false)
            focusManager.clearFocus()
        } else {
            isHistoryTxMultiSelectActive = false
        }
        onHistoryOverlayActiveChanged(activeCustomerForHistory != null)
    }

    // التحقق من طلب تفعيل النسخة المرخصة
    val showActivationRequired by viewModel.showActivationRequired.collectAsStateWithLifecycle()
    LaunchedEffect(showActivationRequired) {
        if (showActivationRequired) {
            activeDialogState = HabayebDialogState.DeviceActivation
            viewModel.resetActivationRequired()
        }
    }

    // حالة تمرير القائمة مع إعادة التعيين عند تغير الفلاتر
    val listState = remember(uiState.selectedCategory, uiState.selectedFilterTab, uiState.financialSortMode, uiState.historicalSortMode) {
        LazyListState(0, 0)
    }
    var highlightedCustomerId by remember { mutableStateOf<String?>(null) }
    var pendingTargetAccountId by remember { mutableStateOf<String?>(null) }
    var pendingTargetToken by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    /*
     * الانتقال الفوري والتمرير التلقائي إلى حساب محدد
     */
    val jumpToAccount: (String) -> Unit = remember(coroutineScope, listState) {
        { accountId ->
            highlightedCustomerId = accountId
            pendingTargetAccountId = accountId
            pendingTargetToken = System.currentTimeMillis()
            if (isSearchActive) {
                viewModel.updateSearchQuery("")
                onSearchActiveChanged(false)
            }
            coroutineScope.launch {
                listState.scrollToItem(0, 0)
            }
        }
    }

    // إزالة التمييز البصري للحساب بعد فترة وجيزة
    LaunchedEffect(highlightedCustomerId) {
        if (highlightedCustomerId != null) {
            delay(1800)
            highlightedCustomerId = null
        }
    }

    // التمرير لأعلى القائمة عند بدء كتابة نص بحث
    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isNotEmpty() && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)) {
            listState.scrollToItem(0, 0)
        }
    }

    // الاستماع لأحداث واجهة المستخدم من الـ ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEventFlow.collect { event ->
            when (event) {
                is HabayebUiEvent.ScrollToAccount -> jumpToAccount(event.accountId)
                is HabayebUiEvent.ResetScrollToTop -> listState.scrollToItem(0, 0)
            }
        }
    }

    // التمرير للحساب المستهدف عند جهوزية القائمة
    LaunchedEffect(uiState.filteredCustomers, pendingTargetToken) {
        val targetId = pendingTargetAccountId
        if (targetId != null) {
            val targetIdx = uiState.filteredCustomers.indexOfFirst { it.id == targetId }
            if (targetIdx >= 0) {
                listState.scrollToItem(targetIdx, 0)
            }
        }
    }

    LaunchedEffect(pendingTargetToken) {
        if (pendingTargetToken > 0L) {
            delay(2000)
            pendingTargetAccountId = null
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    /*
     * ---------------------------------------------------------------------------------
     * معالجة زر الرجوع الفيزيائي حسب الأولوية والتسلسل الهرمي
     * ---------------------------------------------------------------------------------
     */
    BackHandler(enabled = !isDrawerOpen) {
        if (activeDialogState !is HabayebDialogState.None) {
            activeDialogState = HabayebDialogState.None
        } else if (isMultiSelectActive) {
            selectedCustomerIds.clear()
            isMultiSelectActive = false
        } else if (isSearchActive) {
            viewModel.updateSearchQuery("")
            onSearchActiveChanged(false)
        } else if (activeCustomerForHistory != null) {
            activeCustomerForHistory = null
        } else if (uiState.selectedCategory != null) {
            viewModel.updateSelectedCategoryFilter(null)
        } else if (uiState.selectedFilterTab != 0) {
            viewModel.updateSelectedFilterTab(0)
        } else {
            onClose()
        }
    }

    /*
     * معالجة النقر على بطاقة العميل
     */
    val onCustomerClickRemembered = remember(isMultiSelectActive) {
        { customer: CustomerUiState ->
            if (isMultiSelectActive) {
                if (selectedCustomerIds.contains(customer.id)) {
                    selectedCustomerIds.remove(customer.id)
                    if (selectedCustomerIds.isEmpty()) isMultiSelectActive = false
                } else {
                    selectedCustomerIds.add(customer.id)
                }
            } else {
                activeCustomerForHistory = customer.originalCustomer
            }
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /*
     * معالجة النقر المطول على بطاقة العميل (بدء التحديد أو فتح قائمة السياق)
     */
    val onCustomerLongClickRemembered = remember(isMultiSelectActive, uiState.filteredCustomers) {
        { customerId: String ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (isMultiSelectActive) {
                if (!selectedCustomerIds.contains(customerId)) selectedCustomerIds.add(customerId)
            } else {
                val cust = uiState.filteredCustomers.find { it.id == customerId }
                if (cust != null) activeDialogState = HabayebDialogState.ContextMenu(cust)
            }
        }
    }

    /*
     * معالجة الإضافة السريعة لمعاملة جديدة على حساب العميل
     */
    val onQuickAddRemembered = remember {
        { customer: CustomerUiState ->
            val defaultType = if (customer.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) >= 0) {
                TransactionType.OWED_BY_THEM.value
            } else {
                TransactionType.OWED_TO_THEM.value
            }
            activeDialogState = HabayebDialogState.AddTransaction(customer.originalCustomer, defaultType)
        }
    }

    val onScrollToTopRemembered = remember(listState, coroutineScope) {
        {
            coroutineScope.launch { listState.animateScrollToItem(0) }
            Unit
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * بناء الهيكل البصري للشاشة بالاتجاه العربي (RTL)
     * ---------------------------------------------------------------------------------
     */
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceBackgroundColor)
                .testTag("habayeb_screen_root")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceBackgroundColor)
                ) {
                    // الترويسة المالية، أزرار البحث، ووضع الخصوصية
                    HabayebFinanceHeader(
                        isSearchActive = isSearchActive,
                        onSearchActiveChanged = onSearchActiveChanged,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = viewModel::updateSearchQuery,
                        onMenuClick = onMenuClick,
                        haptic = haptic,
                        totalOwedByThem = uiState.totalOwedByThem,
                        totalOwedToThem = uiState.totalOwedToThem,
                        selectedFilterTab = uiState.selectedFilterTab,
                        onFilterTabSelected = viewModel::updateSelectedFilterTab,
                        isPrivacyMode = isPrivacyMode,
                        onTogglePrivacy = securityViewModel::togglePrivacyMode,
                        currencySymbol = currencySymbol,
                        onHeaderDoubleClick = onHeaderDoubleClick,
                        isFloatingActive = isFloatingSearchActive,
                        onToggleFloatingClick = { onFloatingSearchActiveChanged(!isFloatingSearchActive) },
                        activeThemeColor = activeThemeColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // شريط أدوات الفلترة والترتيب وإدارة التصنيفات
                    HabayebFilterToolbar(
                        selectedCategory = uiState.selectedCategory,
                        customCategories = uiState.customCategories,
                        orderedCategories = uiState.orderedCategories,
                        categoryCounts = uiState.categoryCounts,
                        activeCustomersCount = uiState.activeCustomersCount,
                        financialSortMode = uiState.financialSortMode,
                        onFinancialSortModeChanged = viewModel::updateFinancialSortMode,
                        historicalSortMode = uiState.historicalSortMode,
                        onHistoricalSortModeChanged = viewModel::updateHistoricalSortMode,
                        activeThemeColor = activeThemeColor,
                        activeSubColor = activeSubColor,
                        haptic = haptic,
                        onScrollToTop = onScrollToTopRemembered,
                        onCategorySelected = viewModel::updateSelectedCategoryFilter,
                        onAddCategoryClick = { activeDialogState = HabayebDialogState.AddCategory },
                        onRenameCategory = viewModel::renameCustomCategory,
                        onDeleteCategory = viewModel::deleteCustomCategoryWithChoice,
                        onMoveCategoryLeft = viewModel::moveCategoryLeft,
                        onMoveCategoryRight = viewModel::moveCategoryRight,
                        onReorderCategories = viewModel::reorderCategories,
                        closedCategoryName = uiState.closedCategoryName,
                        onRenameClosedCategory = viewModel::renameClosedCategory
                    )
                }

                // قسم قائمة الحسابات والعملاء
                HabayebListSection(
                    listState = listState,
                    filteredCustomers = uiState.filteredCustomers,
                    selectedFilterTab = uiState.selectedFilterTab,
                    selectedCategory = uiState.selectedCategory,
                    selectedCustomerIds = selectedCustomerIds,
                    isPrivacyMode = isPrivacyMode,
                    pinnedCustomerIds = uiState.pinnedCustomerIds,
                    isMultiSelectActive = isMultiSelectActive,
                    activeThemeColor = activeThemeColor,
                    activeSubColor = activeSubColor,
                    currencySymbol = currencySymbol,
                    haptic = haptic,
                    onCustomerClick = onCustomerClickRemembered,
                    onCustomerLongClick = onCustomerLongClickRemembered,
                    onQuickAdd = onQuickAddRemembered,
                    getCustomerCategory = viewModel::getCustomerCategory,
                    onRemoveFromCategory = { customerId -> viewModel.assignCategoryToCustomers(listOf(customerId), null) },
                    highlightedCustomerId = highlightedCustomerId,
                    modifier = Modifier.weight(1f)
                )
            }

            // الأزرار العائمة لإضافة عميل أو إضافة معاملة
            HabayebFabHost(
                targetCustomer = if (activeCustomerForHistory != null) (stableCustomer ?: activeCustomerForHistory) else null,
                contentPadding = contentPadding,
                activeThemeColor = activeThemeColor,
                activeSubColor = activeSubColor,
                haptic = haptic,
                isMultiSelectActive = isMultiSelectActive,
                isHistoryTxMultiSelectActive = isHistoryTxMultiSelectActive,
                onAddCustomerClick = { activeDialogState = HabayebDialogState.AddCustomer },
                onAddTransactionForCustomer = { c ->
                    activeDialogState = HabayebDialogState.AddTransaction(c)
                },
                onFabOverlayChanged = onFabOverlayChanged
            )

            // شريط الإجراءات العائم للتحديد المتعدد (حذف جماعي أو تعيين تصنيف)
            if (isMultiSelectActive && selectedCustomerIds.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CustomerMultiSelectFloatingBar(
                        selectedCount = selectedCustomerIds.size,
                        activeThemeColor = activeThemeColor,
                        onBulkDelete = { activeDialogState = HabayebDialogState.DeleteConfirm },
                        onBulkAssignCategory = { activeDialogState = HabayebDialogState.BulkAssignCategory }
                    )
                }
            }

            // طبقة تراكبية لعرض كشف الحساب التفصيلي للعميل المختار
            AnimatedVisibility(
                visible = activeCustomerForHistory != null,
                enter = slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(160, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(140)),
                exit = slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(140, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(120)),
                modifier = Modifier.zIndex(10f)
            ) {
                stableCustomer?.let { customer ->
                    CustomerHistoryOverlay(
                        customer = customer,
                        viewModel = viewModel,
                        onDismiss = { activeCustomerForHistory = null },
                        activeThemeColor = activeThemeColor,
                        activeSubColor = activeSubColor,
                        currencySymbol = currencySymbol,
                        contentPadding = contentPadding,
                        isSearchActive = isHistorySearchActive,
                        onSearchActiveChanged = onHistorySearchActiveChanged,
                        onTxMultiSelectActiveChanged = { isHistoryTxMultiSelectActive = it }
                    )
                }
            }

            // مضيف كافة النوافذ والحوارات المنبثقة
            HabayebDialogHost(
                activeDialogState = activeDialogState,
                viewModel = viewModel,
                securityViewModel = securityViewModel,
                activeThemeColor = activeThemeColor,
                activeSubColor = activeSubColor,
                selectedCategory = uiState.selectedCategory,
                customCategories = uiState.customCategories,
                selectedCustomerIds = selectedCustomerIds,
                onMultiSelectActiveChanged = { isMultiSelectActive = it },
                listState = listState,
                coroutineScope = coroutineScope,
                isSearchActive = isSearchActive,
                onSearchActiveChanged = onSearchActiveChanged,
                onJumpToAccount = jumpToAccount,
                onDismissDialog = { activeDialogState = HabayebDialogState.None },
                onOpenEditCustomer = { customer ->
                    activeDialogState = HabayebDialogState.EditCustomer(customer)
                },
                onOpenDeleteConfirm = {
                    activeDialogState = HabayebDialogState.DeleteConfirm
                }
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }
}

