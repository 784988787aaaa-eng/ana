package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.screens.habayeb.components.AddCustomerPopup
import com.example.ui.screens.habayeb.components.AddTransactionPopup
import com.example.ui.screens.habayeb.components.CustomerContextBottomSheet
import com.example.ui.screens.habayeb.components.CustomerHistoryOverlay
import com.example.ui.screens.habayeb.components.CustomerMultiSelectFloatingBar
import com.example.ui.screens.habayeb.components.DeleteConfirmDialog
import com.example.ui.screens.habayeb.components.EditCustomerDialog
import com.example.ui.screens.habayeb.components.HabayebBulkAssignDialog
import com.example.ui.screens.habayeb.components.HabayebFab
import com.example.ui.screens.habayeb.components.HabayebFilterTabs
import com.example.ui.screens.habayeb.components.HabayebFilterToolbar
import com.example.ui.screens.habayeb.components.HabayebHeaderTopBar
import com.example.ui.screens.habayeb.components.HabayebListSection
import com.example.ui.screens.habayeb.components.MicroAddCategoryDialog
import com.example.ui.screens.ledger.components.DeviceActivationDialog
import com.example.ui.state.CustomerUiState
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface HabayebDialogState {
    object None : HabayebDialogState
    object AddCustomer : HabayebDialogState
    data class AddTransaction(
        val customer: HabayebCustomer,
        val defaultType: String = TransactionType.OWED_BY_THEM.value,
        val editingTx: HabayebTransaction? = null
    ) : HabayebDialogState
    data class EditCustomer(val customer: HabayebCustomer) : HabayebDialogState
    object DeleteConfirm : HabayebDialogState
    object AddCategory : HabayebDialogState
    object BulkAssignCategory : HabayebDialogState
    data class ContextMenu(val customer: CustomerUiState) : HabayebDialogState
    object DeviceActivation : HabayebDialogState
}

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
    onHistorySearchActiveChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val activeThemeColor = MaterialTheme.colorScheme.primary
    val activeSubColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceBackgroundColor = MaterialTheme.colorScheme.background

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = remember(viewModel.settingsState.value.themeMode, systemDark) {
        when (viewModel.settingsState.value.themeMode) {
            1 -> false
            2 -> true
            else -> systemDark
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? Activity)?.window
            window?.let { w ->
                w.statusBarColor = android.graphics.Color.TRANSPARENT
                w.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(w, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currencySymbol = viewModel.settingsState.collectAsStateWithLifecycle().value.currencySymbol
    val isPrivacyModeState = securityViewModel.isPrivacyModeEnabled.collectAsStateWithLifecycle()

    val searchQuery = uiState.searchQuery
    val selectedFilterTab = uiState.selectedFilterTab
    val financialSortMode = uiState.financialSortMode
    val historicalSortMode = uiState.historicalSortMode
    val filteredCustomers by remember { derivedStateOf { uiState.filteredCustomers } }
    val selectedCategory = uiState.selectedCategory
    val customCategories by remember { derivedStateOf { uiState.customCategories } }
    val orderedCategories by remember { derivedStateOf { uiState.orderedCategories } }
    val categoryCounts by remember { derivedStateOf { uiState.categoryCounts } }
    val closedCategoryName = uiState.closedCategoryName
    val activeCustomersCount by remember {
        derivedStateOf {
            uiState.customers.count { it.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) != 0 }
        }
    }
    val pinnedCustomerIds by remember { derivedStateOf { uiState.pinnedCustomerIds } }

    val selectedCustomerIds = remember { mutableStateListOf<String>() }
    var isMultiSelectActive by remember { mutableStateOf(false) }
    var isHistoryTxMultiSelectActive by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCustomerIds.toList()) {
        viewModel.updateSelectedCustomerIds(selectedCustomerIds.toList())
    }

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

    val showActivationRequired by viewModel.showActivationRequired.collectAsStateWithLifecycle()

    LaunchedEffect(showActivationRequired) {
        if (showActivationRequired) {
            activeDialogState = HabayebDialogState.DeviceActivation
            viewModel.resetActivationRequired()
        }
    }

    val listState = rememberLazyListState()
    var highlightedCustomerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(highlightedCustomerId) {
        if (highlightedCustomerId != null) {
            kotlinx.coroutines.delay(350)
            highlightedCustomerId = null
        }
    }

    var pendingTargetAccountId by remember { mutableStateOf<String?>(null) }

    // Scroll to top instantly when tab, category, sorting, or search query changes, avoiding redundant jumps
    LaunchedEffect(selectedFilterTab, selectedCategory, financialSortMode, historicalSortMode, searchQuery) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            listState.scrollToItem(0, 0)
        }
    }

    // Handle scroll-to-account event when adding or modifying a customer / transaction
    LaunchedEffect(Unit) {
        viewModel.uiEventFlow.collect { event ->
            when (event) {
                is com.example.ui.viewmodel.HabayebUiEvent.ScrollToAccount -> {
                    highlightedCustomerId = event.accountId
                    pendingTargetAccountId = event.accountId
                    if (isSearchActive) {
                        viewModel.updateSearchQuery("")
                        onSearchActiveChanged(false)
                    }
                }
                is com.example.ui.viewmodel.HabayebUiEvent.ResetScrollToTop -> {
                    listState.scrollToItem(0, 0)
                }
            }
        }
    }

    // As soon as filteredCustomers contains the target account, scroll to it immediately and accurately
    LaunchedEffect(filteredCustomers, pendingTargetAccountId) {
        val targetId = pendingTargetAccountId
        if (targetId != null) {
            val targetIdx = filteredCustomers.indexOfFirst { it.id == targetId }
            if (targetIdx >= 0) {
                if (targetIdx == 0 || kotlin.math.abs(listState.firstVisibleItemIndex - targetIdx) > 5) {
                    listState.scrollToItem(targetIdx, 0)
                } else {
                    listState.animateScrollToItem(targetIdx, 0)
                }
                pendingTargetAccountId = null
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
        } else if (selectedCategory != null) {
            viewModel.updateSelectedCategoryFilter(null)
        } else if (selectedFilterTab != 0) {
            viewModel.updateSelectedFilterTab(0)
        } else {
            onClose()
        }
    }

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

    val onCustomerLongClickRemembered = remember(isMultiSelectActive, filteredCustomers) {
        { customerId: String ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (isMultiSelectActive) {
                if (!selectedCustomerIds.contains(customerId)) selectedCustomerIds.add(customerId)
            } else {
                val cust = filteredCustomers.find { it.id == customerId }
                if (cust != null) activeDialogState = HabayebDialogState.ContextMenu(cust)
            }
        }
    }

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp), clip = false)
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(activeThemeColor)
                    ) {
                        HabayebHeaderTopBar(
                            isSearchActive = isSearchActive,
                            onSearchActiveChanged = onSearchActiveChanged,
                            searchQuery = searchQuery,
                            onSearchQueryChanged = viewModel::updateSearchQuery,
                            onMenuClick = onMenuClick,
                            haptic = haptic,
                            netDebt = uiState.totalOwedByThem.subtract(uiState.totalOwedToThem),
                            isPrivacyMode = isPrivacyModeState.value,
                            onTogglePrivacy = securityViewModel::togglePrivacyMode,
                            currencySymbol = currencySymbol,
                            onHeaderDoubleClick = onHeaderDoubleClick,
                            isFloatingActive = isFloatingSearchActive,
                            onToggleFloatingClick = { onFloatingSearchActiveChanged(!isFloatingSearchActive) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HabayebFilterTabs(
                        selectedFilterTab = selectedFilterTab,
                        onFilterTabSelected = viewModel::updateSelectedFilterTab,
                        totalOwedByThem = uiState.totalOwedByThem,
                        totalOwedToThem = uiState.totalOwedToThem,
                        currencySymbol = currencySymbol,
                        isPrivacyMode = isPrivacyModeState.value
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    HabayebFilterToolbar(
                        selectedCategory = selectedCategory,
                        customCategories = customCategories,
                        orderedCategories = orderedCategories,
                        categoryCounts = categoryCounts,
                        activeCustomersCount = activeCustomersCount,
                        financialSortMode = financialSortMode,
                        onFinancialSortModeChanged = viewModel::updateFinancialSortMode,
                        historicalSortMode = historicalSortMode,
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
                        closedCategoryName = closedCategoryName,
                        onRenameClosedCategory = viewModel::renameClosedCategory
                    )
                }

                HabayebListSection(
                    listState = listState,
                    filteredCustomers = filteredCustomers,
                    selectedFilterTab = selectedFilterTab,
                    selectedCategory = selectedCategory,
                    selectedCustomerIds = selectedCustomerIds,
                    isPrivacyMode = isPrivacyModeState.value,
                    pinnedCustomerIds = pinnedCustomerIds,
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

            if (!isMultiSelectActive && !isHistoryTxMultiSelectActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .zIndex(12f)
                ) {
                    HabayebFab(
                        targetCustomer = if (activeCustomerForHistory != null) (stableCustomer ?: activeCustomerForHistory) else null,
                        contentPadding = contentPadding,
                        primaryColor = activeThemeColor,
                        containerColor = activeSubColor,
                        haptic = haptic,
                        onAddCustomerClick = { activeDialogState = HabayebDialogState.AddCustomer },
                        onAddTransactionForCustomer = { c ->
                            activeDialogState = HabayebDialogState.AddTransaction(c)
                        }
                    )
                }
            }

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

            val overlaySpring = spring<Float>(dampingRatio = 0.85f, stiffness = 380f)
            val overlayOffsetSpring = spring<IntOffset>(dampingRatio = 0.85f, stiffness = 380f)

            AnimatedVisibility(
                visible = activeCustomerForHistory != null,
                enter = slideInVertically(animationSpec = overlayOffsetSpring, initialOffsetY = { it }) + fadeIn(animationSpec = overlaySpring),
                exit = slideOutVertically(animationSpec = overlayOffsetSpring, targetOffsetY = { it }) + fadeOut(animationSpec = overlaySpring),
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

            HabayebDialogHost(
                activeDialogState = activeDialogState,
                viewModel = viewModel,
                securityViewModel = securityViewModel,
                activeThemeColor = activeThemeColor,
                activeSubColor = activeSubColor,
                selectedCategory = selectedCategory,
                customCategories = customCategories,
                selectedCustomerIds = selectedCustomerIds,
                onMultiSelectActiveChanged = { isMultiSelectActive = it },
                listState = listState,
                coroutineScope = coroutineScope,
                isSearchActive = isSearchActive,
                onSearchActiveChanged = onSearchActiveChanged,
                onHighlightCustomer = { highlightedCustomerId = it },
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

@Composable
private fun HabayebDialogHost(
    activeDialogState: HabayebDialogState,
    viewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    activeThemeColor: Color,
    activeSubColor: Color,
    selectedCategory: String?,
    customCategories: List<CustomCategory>,
    selectedCustomerIds: MutableList<String>,
    onMultiSelectActiveChanged: (Boolean) -> Unit,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    isSearchActive: Boolean,
    onSearchActiveChanged: (Boolean) -> Unit,
    onHighlightCustomer: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onOpenEditCustomer: (HabayebCustomer) -> Unit,
    onOpenDeleteConfirm: () -> Unit
) {
    when (val dialogState = activeDialogState) {
        is HabayebDialogState.None -> {}
        is HabayebDialogState.AddCustomer -> {
            AddCustomerPopup(
                viewModel = viewModel,
                onDismiss = onDismissDialog,
                onCustomerAdded = { newCustomerId ->
                    onHighlightCustomer(newCustomerId)
                    if (isSearchActive) {
                        viewModel.updateSearchQuery("")
                        onSearchActiveChanged(false)
                    }
                    onDismissDialog()
                },
                activeThemeColor = activeThemeColor,
                activeSubColor = activeSubColor
            )
        }
        is HabayebDialogState.AddTransaction -> {
            AddTransactionPopup(
                customer = dialogState.customer,
                viewModel = viewModel,
                initialSelectedType = dialogState.defaultType,
                editingTransaction = dialogState.editingTx,
                onDismiss = onDismissDialog,
                onTransactionSaved = {
                    onHighlightCustomer(dialogState.customer.id)
                    onDismissDialog()
                },
                activeThemeColor = activeThemeColor,
                activeSubColor = activeSubColor
            )
        }
        is HabayebDialogState.EditCustomer -> {
            EditCustomerDialog(
                customer = dialogState.customer,
                viewModel = viewModel,
                activeThemeColor = activeThemeColor,
                onDismiss = onDismissDialog
            )
        }
        is HabayebDialogState.DeleteConfirm -> {
            DeleteConfirmDialog(
                selectedCustomerIds = selectedCustomerIds.toList(),
                viewModel = viewModel,
                onDismiss = onDismissDialog,
                onSuccessBulkDelete = {
                    selectedCustomerIds.clear()
                    onMultiSelectActiveChanged(false)
                    onDismissDialog()
                }
            )
        }
        is HabayebDialogState.AddCategory -> {
            MicroAddCategoryDialog(
                activeThemeColor = activeThemeColor,
                onDismiss = onDismissDialog,
                onSave = { categoryName ->
                    viewModel.saveCustomCategory(categoryName)
                    onDismissDialog()
                }
            )
        }
        is HabayebDialogState.BulkAssignCategory -> {
            HabayebBulkAssignDialog(
                customCategories = customCategories,
                onDismiss = onDismissDialog,
                onAssign = { categoryName ->
                    viewModel.assignCategoryToCustomers(selectedCustomerIds.toList(), categoryName)
                    onMultiSelectActiveChanged(false)
                    selectedCustomerIds.clear()
                    onDismissDialog()
                }
            )
        }
        is HabayebDialogState.ContextMenu -> {
            val pinnedSet by viewModel.pinnedCustomerIds.collectAsStateWithLifecycle()
            val isPinned = pinnedSet.contains(dialogState.customer.id)

            CustomerContextBottomSheet(
                customer = dialogState.customer,
                customCategories = customCategories,
                isPinned = isPinned,
                activeThemeColor = activeThemeColor,
                onDismiss = onDismissDialog,
                onTogglePin = {
                    val targetId = dialogState.customer.id
                    val wasPinned = isPinned
                    viewModel.togglePinCustomer(targetId)
                    if (wasPinned) {
                        coroutineScope.launch {
                            listState.scrollToItem(0, 0)
                        }
                    }
                },
                onAssignCategory = { category ->
                    viewModel.assignCategoryToCustomers(listOf(dialogState.customer.id), category)
                },
                onEnableMultiSelect = {
                    onMultiSelectActiveChanged(true)
                    selectedCustomerIds.add(dialogState.customer.id)
                    onDismissDialog()
                },
                onDelete = {
                    selectedCustomerIds.clear()
                    selectedCustomerIds.add(dialogState.customer.id)
                    onOpenDeleteConfirm()
                },
                onEditClick = {
                    onOpenEditCustomer(dialogState.customer.originalCustomer)
                },
                onUpdateCustomerType = { newType ->
                    viewModel.updateHabayebCustomer(dialogState.customer.originalCustomer.copy(initialType = newType))
                },
                currentActiveCategory = selectedCategory
            )
        }
        is HabayebDialogState.DeviceActivation -> {
            val deviceId by securityViewModel.deviceIdState.collectAsStateWithLifecycle()
            DeviceActivationDialog(
                deviceId = deviceId,
                viewModel = securityViewModel,
                onDismiss = onDismissDialog,
                isAutoTriggered = true
            )
        }
    }
}
