package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.domain.FormatUtils
import com.example.ui.components.circularReveal
import com.example.ui.screens.ledger.components.LedgerBottomDock
import com.example.ui.screens.ledger.components.MainLedgerDialogsManager
import com.example.ui.screens.ledger.components.MainLedgerListSection
import com.example.ui.screens.ledger.components.MainLedgerSelectionBar
import com.example.ui.screens.ledger.components.PinnedMainLedgerHeader
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun MainLedgerView(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    settings: AppSettings,
    onBackIntercept: (Boolean) -> Unit,
    onMenuClick: () -> Unit = {},
    isDrawerOpen: Boolean = false,
    isFloatingSearchActive: Boolean = false,
    onFloatingSearchActiveChanged: (Boolean) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiController = rememberMainLedgerUiController()

    val bottomPadding = contentPadding.calculateBottomPadding()
    val totalCash by viewModel.totalCashState.collectAsStateWithLifecycle()
    val commitments by viewModel.commitmentsState.collectAsStateWithLifecycle()
    val monthlyLedger by viewModel.monthlyLedgerState.collectAsStateWithLifecycle()
    val appSettingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val lazyListState = rememberLazyListState()
    val collapseFractionProvider = remember {
        { if (lazyListState.firstVisibleItemIndex > 0) 1f else (lazyListState.firstVisibleItemScrollOffset.toFloat() / 180f).coerceIn(0f, 1f) }
    }
    val isPinnedVisible = remember {
        derivedStateOf { collapseFractionProvider() > 0f }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive && uiController.activeDialogState !is MainLedgerDialogState.Search) {
            uiController.activeDialogState = MainLedgerDialogState.Search
        } else if (!isSearchActive && uiController.activeDialogState is MainLedgerDialogState.Search) {
            uiController.activeDialogState = MainLedgerDialogState.None
        }
    }

    LaunchedEffect(uiController.activeDialogState) {
        val isSearch = uiController.activeDialogState is MainLedgerDialogState.Search
        if (isSearch != isSearchActive) {
            onSearchActiveChanged(isSearch)
        }
    }

    val deviceId by securityViewModel.deviceIdState.collectAsStateWithLifecycle()
    val showActivationRequired by securityViewModel.showActivationRequired.collectAsStateWithLifecycle()

    LaunchedEffect(showActivationRequired) {
        if (showActivationRequired) {
            uiController.activeDialogState = MainLedgerDialogState.DeviceActivation
            // إعادة ضبط حالة مطالبة التفعيل عبر ViewModel لمنع تكرار فتح الحوار
            securityViewModel.resetActivationRequired()
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResultsState.collectAsStateWithLifecycle()

    BackHandler(enabled = !isDrawerOpen && (uiController.isHabayebActive || uiController.activeDialogState !is MainLedgerDialogState.None || uiController.isSelectionMode || uiController.isDaySelectionMode || uiController.expandedDayKeys.isNotEmpty())) {
        if (uiController.isHabayebActive) {
            uiController.isHabayebActive = false
        } else if (uiController.activeDialogState !is MainLedgerDialogState.None) {
            uiController.activeDialogState = MainLedgerDialogState.None
        } else if (uiController.isSelectionMode || uiController.isDaySelectionMode) {
            uiController.clearSelection()
        } else if (uiController.expandedDayKeys.isNotEmpty()) {
            uiController.expandedDayKeys = emptySet()
        }
    }

    val linkHabayebDebts by habayebViewModel.linkHabayebDebtsState.collectAsStateWithLifecycle()
    val habayebOwedByThemTotal by habayebViewModel.habayebOwedByThemTotalState.collectAsStateWithLifecycle()

    val computedCommitments = remember(commitments, totalCash, linkHabayebDebts, habayebOwedByThemTotal) {
        var remainingCash = if (linkHabayebDebts) totalCash.add(habayebOwedByThemTotal) else totalCash
        commitments.map { fc ->
            val target = fc.targetAmount
            val alreadyPaid = fc.currentProgress
            val needed = (target.subtract(alreadyPaid)).max(BigDecimal.ZERO)
            val allocatedFromCash = if (remainingCash >= needed) {
                remainingCash = remainingCash.subtract(needed)
                needed
            } else if (remainingCash > BigDecimal.ZERO) {
                val temp = remainingCash
                remainingCash = BigDecimal.ZERO
                temp
            } else BigDecimal.ZERO
            Triple(fc, alreadyPaid.add(allocatedFromCash), needed.subtract(allocatedFromCash))
        }
    }

    val isPrivacyMode by securityViewModel.isPrivacyModeEnabled.collectAsStateWithLifecycle()
    val allKeys = remember(monthlyLedger) { monthlyLedger.flatMap { ml -> ml.days.map { "${ml.monthKey}_${it.dayNumber}" } } }
    val selectedDayKeysCountText = when (uiController.selectedDayKeys.size) {
        1 -> stringResource(R.string.ledger_selected_days_count_1)
        2 -> stringResource(R.string.ledger_selected_days_count_2)
        else -> stringResource(R.string.ledger_selected_days_count_more, uiController.selectedDayKeys.size)
    }
    val isSelectAllChecked = uiController.selectedDayKeys.size == allKeys.size && allKeys.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            PinnedMainLedgerHeader(
                isDaySelectionMode = uiController.isDaySelectionMode,
                selectedDayKeys = uiController.selectedDayKeys,
                onCancelDaySelection = { uiController.cancelDaySelection() },
                onSelectAllDays = { uiController.selectAllDays(allKeys) },
                onDeleteSelectedDays = { if (uiController.selectedDayKeys.isNotEmpty()) uiController.activeDialogState = MainLedgerDialogState.DeleteDaysConfirm },
                isSelectAllChecked = isSelectAllChecked,
                selectedDayKeysCountText = selectedDayKeysCountText,
                onMenuClick = onMenuClick,
                onSearchClick = { uiController.activeDialogState = MainLedgerDialogState.Search },
                isFloatingSearchActive = isFloatingSearchActive,
                onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                totalCash = totalCash,
                isPrivacyMode = isPrivacyMode,
                onTogglePrivacyMode = { securityViewModel.togglePrivacyMode() },
                currencySymbol = settings.currencySymbol,
                formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
                commitments = commitments,
                computedCommitments = computedCommitments,
                linkHabayebDebts = linkHabayebDebts,
                onLinkHabayebDebtsChange = { habayebViewModel.toggleLinkHabayebDebts(it) }
            )

            MainLedgerListSection(
                lazyListState = lazyListState,
                bottomPadding = bottomPadding,
                isDaySelectionMode = uiController.isDaySelectionMode,
                selectedDayKeys = uiController.selectedDayKeys,
                currencySymbol = settings.currencySymbol,
                formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
                formatDoubleCurrency = { v, s -> FormatUtils.formatDoubleCurrency(v, s, context) },
                monthlyLedger = monthlyLedger,
                isScreenReady = true,
                collapsedMonths = uiController.collapsedMonths,
                onToggleMonthCollapsed = { mKey -> uiController.toggleMonthCollapsed(mKey) },
                expandedDayKeys = uiController.expandedDayKeys,
                haptic = haptic,
                context = context,
                viewModel = viewModel,
                onEditTransaction = { tx -> uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = tx.type, editingTx = tx) },
                onDayClick = { key -> uiController.handleDayClick(key) },
                onDayLongClick = { key -> uiController.handleDayLongClick(key) },
                isSelectionMode = uiController.isSelectionMode,
                selectedTxIds = uiController.selectedTxIds,
                onTransactionSelectToggle = { txId -> uiController.handleTransactionSelectToggle(txId) },
                modifier = Modifier.weight(1f)
            )
        }

        LedgerBottomDock(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 12.dp),
            isSelectionMode = uiController.isSelectionMode || uiController.isDaySelectionMode,
            selectedTxIdsCount = uiController.selectedTxIds.size,
            onDeleteSelectedClick = {
                viewModel.deleteTransactionsBulk(uiController.selectedTxIds.toList(), context.getString(R.string.ledger_delete_selected_warning, uiController.selectedTxIds.size))
                uiController.clearSelection()
            },
            onShowCommitmentsClick = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
            onAddIncomeClick = { uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = "INCOME", editingTx = null) },
            onAddExpenseClick = { uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = "EXPENSE", editingTx = null) }
        )

        MainLedgerSelectionBar(
            isSelectionActive = (uiController.isSelectionMode && uiController.selectedTxIds.isNotEmpty()) || (uiController.isDaySelectionMode && uiController.selectedDayKeys.isNotEmpty()),
            isDaySelectionMode = uiController.isDaySelectionMode,
            isSelectAllChecked = isSelectAllChecked,
            selectedDayKeysCountText = selectedDayKeysCountText,
            selectedTxCount = uiController.selectedTxIds.size,
            allKeys = allKeys,
            selectedDayKeys = uiController.selectedDayKeys,
            haptic = haptic,
            onClearSelection = { uiController.clearSelection() },
            onDeleteClick = {
                if (uiController.isDaySelectionMode) {
                    if (uiController.selectedDayKeys.isNotEmpty()) uiController.activeDialogState = MainLedgerDialogState.DeleteDaysConfirm
                } else if (uiController.selectedTxIds.isNotEmpty()) {
                    viewModel.deleteTransactionsBulk(uiController.selectedTxIds.toList(), context.getString(R.string.ledger_delete_selected_warning, uiController.selectedTxIds.size))
                    uiController.clearSelection()
                }
            },
            bottomPadding = bottomPadding,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    MainLedgerDialogsManager(
        showTxDialog = uiController.activeDialogState is MainLedgerDialogState.AddTransaction,
        txDialogType = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.type ?: "EXPENSE",
        editingTransaction = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.editingTx,
        currencySymbol = settings.currencySymbol,
        onDismissTxDialog = { uiController.dismissDialog() },
        onSaveTransaction = { id, type, cat, amt, desc ->
            val editingTx = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.editingTx
            if (editingTx != null) {
                viewModel.updateTransaction(editingTx.copy(amount = amt, description = desc, category = cat))
            } else {
                viewModel.addTransaction(type = type, category = cat, amount = amt, description = desc)
            }
            uiController.dismissDialog()
            scope.launch {
                lazyListState.scrollToItem(0)
            }
        },
        showSearch = uiController.activeDialogState is MainLedgerDialogState.Search,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onDismissSearch = { uiController.dismissDialog() },
        showCommitmentsListSheet = uiController.activeDialogState is MainLedgerDialogState.CommitmentsList || uiController.activeDialogState is MainLedgerDialogState.AddCommitment || uiController.activeDialogState is MainLedgerDialogState.ReorderCommitment,
        commitments = commitments,
        computedCommitments = computedCommitments,
        totalCash = totalCash,
        formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
        formatBigDecimalCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
        onDismissCommitmentsList = { uiController.dismissDialog() },
        onAddCommitmentClick = { uiController.activeDialogState = MainLedgerDialogState.AddCommitment(editingCommitment = null) },
        onEditCommitmentClick = { fc -> uiController.activeDialogState = MainLedgerDialogState.AddCommitment(editingCommitment = fc) },
        onDeleteCommitment = { name -> viewModel.deleteCommitment(name) },
        onReorderCommitment = { fc, pos -> viewModel.reorderCommitment(fc, pos) },
        onCommitmentCheckedChange = { fc, checked -> viewModel.saveCommitment(fc.name, fc.targetAmount, if (checked) fc.targetAmount else BigDecimal.ZERO) },
        onSetReorderTarget = { fc -> uiController.activeDialogState = MainLedgerDialogState.ReorderCommitment(fc) },
        showCommitmentDialog = uiController.activeDialogState is MainLedgerDialogState.AddCommitment,
        editingCommitment = (uiController.activeDialogState as? MainLedgerDialogState.AddCommitment)?.editingCommitment,
        onDismissCommitmentDialog = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
        onSaveCommitment = { name, targetAmt, progress ->
            viewModel.saveCommitment(name, targetAmt, progress)
            uiController.activeDialogState = MainLedgerDialogState.CommitmentsList
        },
        reorderCommitmentTarget = (uiController.activeDialogState as? MainLedgerDialogState.ReorderCommitment)?.target,
        onDismissReorderTarget = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
        onApplyReorderTarget = { target, pos ->
            viewModel.reorderCommitment(target, pos)
            uiController.activeDialogState = MainLedgerDialogState.CommitmentsList
        },
        showActivationDialog = uiController.activeDialogState is MainLedgerDialogState.DeviceActivation,
        deviceId = deviceId,
        securityViewModel = securityViewModel,
        onDismissActivationDialog = { uiController.dismissDialog() },
        showDeleteDaysDialog = uiController.activeDialogState is MainLedgerDialogState.DeleteDaysConfirm,
        onDismissDeleteDaysDialog = { uiController.dismissDialog() },
        monthlyLedger = monthlyLedger,
        selectedDayKeys = uiController.selectedDayKeys,
        viewModel = viewModel,
        scope = scope,
        context = context,
        onSuccessDeleteDays = {
            uiController.clearSelection()
            uiController.dismissDialog()
        }
    )

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(uiController.isHabayebActive) {
        if (uiController.isHabayebActive) animProgress.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        else animProgress.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }

    if (animProgress.value > 0f) {
        val revealCenter = Offset(250f, 400f)

        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                alpha = animProgress.value
                scaleX = animProgress.value
                scaleY = animProgress.value
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }.circularReveal(animProgress.value, revealCenter, isRelative = true)
        ) {
            HabayebScreen(
                viewModel = habayebViewModel,
                securityViewModel = securityViewModel,
                onMenuClick = onMenuClick,
                onClose = { scope.launch { uiController.isHabayebActive = false } }
            )
        }
    }
}

