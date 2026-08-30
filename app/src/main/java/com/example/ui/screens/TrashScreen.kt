package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة شاشة سلة المهملات والمحذوفات (Trash & Recycle Bin Screen Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على واجهة سلة المحذوفات الشاملة (TrashScreen):
 * - عرض العناصر المحذوفة مؤقتاً (Soft-Deleted) من قيود المعاملات وحسابات العملاء المجمعة.
 * - التصفية والفرز والبحث النصي في السلة مع نظام التحديد المتعدد.
 * - استعادة العناصر المفردة أو المجمعة، أو الحذف النهائي الدائم مع مهلة الحذف التلقائي.
 * =====================================================================================
 */

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.trash.components.TrashCustomerHistoryOverlay
import com.example.ui.screens.trash.components.TrashDialogsManager
import com.example.ui.screens.trash.components.TrashItemListSection
import com.example.ui.screens.trash.components.TrashTopBarSection
import com.example.ui.screens.trash.components.TrashTransactionDetailBottomSheet
import com.example.ui.screens.trash.components.TrashWrapper
import com.example.ui.screens.trash.utils.TrashItemParser
import com.example.ui.screens.trash.utils.TrashStrings
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.ExperimentalMaterial3Api

/*
 * =====================================================================================
 * تصنيفات فلترة المحذوفات (TrashFilterType)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تعداد (Enum) يحدد نوع العناصر المعروضة في سلة المهملات:
 * - ALL: كافة العناصر المحذوفة (معاملات وعملاء).
 * - TRANSACTIONS: معاملات الديون والقيود المالية فقط.
 * - CUSTOMERS: بطاقات العملاء والحسابات المجمعة المحذوفة فقط.
 * =====================================================================================
 */
enum class TrashFilterType {
    ALL, TRANSACTIONS, CUSTOMERS
}

/*
 * =====================================================================================
 * أنماط ترتيب المحذوفات (TrashSortType)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * خيارات الفرز الزمني والمالي والأبجدي لعناصر السلة:
 * - NEWEST_DELETED: الأحدث حذفاً أولاً.
 * - OLDEST_DELETED: الأقدم حذفاً أولاً.
 * - HIGHEST_AMOUNT: الأعلى قيمة ومبلغاً.
 * - ALPHABETICAL: الترتيب الهجائي حسب الاسم أو الوصف.
 * =====================================================================================
 */
enum class TrashSortType {
    NEWEST_DELETED,
    OLDEST_DELETED,
    HIGHEST_AMOUNT,
    ALPHABETICAL
}

/*
 * =====================================================================================
 * حالات النوافذ المنبثقة التابعة لسلة المهملات (TrashDialogState)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * واجهة مختومة (Sealed Interface) تمثل النوافذ والحوارات النشطة في سلة المحذوفات:
 * - None: لا توجد نوافذ منبثقة مفتوحة.
 * - EmptyConfirm: حوار تأكيد تفريغ السلة بالكامل ومسح جميع المحتويات نهائياً.
 * - CustomerHistoryOverlay: طبقة تراكبية لعرض كشف حساب العميل المحذوف بتفاصيله ومعاملاته.
 * - TransactionDetail: ورقة سفلية لعرض التفاصيل الكاملة لمعاملة محذوفة.
 * =====================================================================================
 */
sealed interface TrashDialogState {
    object None : TrashDialogState
    object EmptyConfirm : TrashDialogState
    data class CustomerHistoryOverlay(val wrapper: TrashWrapper) : TrashDialogState
    data class TransactionDetail(val wrapper: TrashWrapper) : TrashDialogState
}

/*
 * =====================================================================================
 * شاشة سلة المهملات والمحذوفات (TrashScreen)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * الشاشة المركزية لإدارة المحذوفات واسترجاعها:
 * 1. جمع العناصر المحذوفة من قاعدة البيانات مع التحقق الدوري من الحذف التلقائي.
 * 2. شريط علوي تفاعلي يدعم البحث وتحديد عناصر متعددة وتفريغ السلة بالكامل.
 * 3. تحليل وتفسير كيانات المحذوفات وتحويلها إلى كائنات عرض بصرية (TrashWrapper).
 * 4. إدارة نوافذ المعاينة والتأكيد والاسترجاع الفردي أو الجماعي.
 *
 * [المُدخلات]:
 * - viewModel: نموذج بيانات المعاملات العامة وإدارة عمليات الاسترجاع والحذف النهائي.
 * - habayebViewModel: نموذج بيانات حسابات الحبايب لتوفير أسماء العملاء وتفاصيلهم.
 * - onBack: رد نداء الرجوع إلى الشاشة السابقة.
 * - contentPadding: هوامش التباعد من الحواف ونظام التشغيل.
 * =====================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    /*
     * جمع تدفقات الحالة من نماذج البيانات
     */
    val items by viewModel.deletedItemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val customersList by habayebViewModel.habayebCustomersState.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val autoCleanupPeriod by viewModel.autoCleanupPeriod.collectAsStateWithLifecycle()
    val currencySymbol = settings.currencySymbol

    val systemHabayeb = stringResource(id = R.string.source_system_habayeb)

    // تشغيل التنظيف التلقائي للمحذوفات المنتهية الصلاحية عند فتح الشاشة
    LaunchedEffect(Unit) {
        viewModel.cleanLedgerTrashItems()
    }

    // حالات البحث والحوارات النشطة
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeDialogState by remember { mutableStateOf<TrashDialogState>(TrashDialogState.None) }

    // إدارة وضع التحديد المتعدد للعناصر
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<String>() }

    /*
     * تبديل تحديد عنصر محدد
     */
    fun toggleSelection(itemId: String) {
        if (selectedItemIds.contains(itemId)) {
            selectedItemIds.remove(itemId)
            if (selectedItemIds.isEmpty()) isSelectionMode = false
        } else {
            selectedItemIds.add(itemId)
        }
    }

    /*
     * إلغاء التحديد وتفريغ القائمة المحددة
     */
    fun clearSelection() {
        selectedItemIds.clear()
        isSelectionMode = false
    }

    // حالات التصفية والفرز الحالية
    var selectedFilter by remember { mutableStateOf(TrashFilterType.ALL) }
    var selectedSort by remember { mutableStateOf(TrashSortType.NEWEST_DELETED) }

    // قائمة العناصر المعالجة والمجهزة للعرض مع حد التحميل التدريجي
    var processedItems by remember { mutableStateOf(emptyList<TrashWrapper>()) }
    var itemsLimit by remember { mutableStateOf(50) }
    var totalFilteredCount by remember { mutableStateOf(0) }

    /*
     * تجهيز النصوص والموارد اللغوية الخاصة بسلة المهملات
     */
    val unknownText = stringResource(id = R.string.trash_item_unknown)
    val noPhoneText = stringResource(id = R.string.trash_no_phone)
    val noNotesText = stringResource(id = R.string.trash_no_notes)
    val debtTxText = stringResource(id = R.string.trash_type_debt_tx)
    val owedByThemText = stringResource(id = R.string.trash_tx_owed_by_them)
    val paymentByThemText = stringResource(id = R.string.trash_tx_payment_by_them)
    val owedToThemText = stringResource(id = R.string.trash_tx_owed_to_them)
    val paymentToThemText = stringResource(id = R.string.trash_tx_payment_to_them)
    val categoryLabelText = stringResource(id = R.string.trash_label_category, "")
    val customerLabelText = stringResource(id = R.string.trash_associated_customer_label, "%s")
    val equivalentInfoTemplate = stringResource(id = R.string.trash_equivalent_info, "%s", "%s")
    val progressTextTemplate = stringResource(id = R.string.trash_label_progress, "")
    val customerBundleDescTemplate = stringResource(id = R.string.trash_customer_bundle_desc, 0).replace("0", "%d")
    val ledgerBundleDescTemplate = stringResource(id = R.string.trash_ledger_bundle_desc, 0)
    val exchangeRateLabelText = stringResource(id = R.string.trash_exchange_rate_label)

    val trashStrings = remember(
        systemHabayeb, unknownText, noPhoneText, noNotesText, debtTxText,
        owedByThemText, paymentByThemText, owedToThemText, paymentToThemText,
        categoryLabelText, customerLabelText, equivalentInfoTemplate,
        progressTextTemplate, customerBundleDescTemplate, ledgerBundleDescTemplate,
        exchangeRateLabelText
    ) {
        TrashStrings(
            systemHabayeb = systemHabayeb,
            unknownText = unknownText,
            noPhoneText = noPhoneText,
            noNotesText = noNotesText,
            debtTxText = debtTxText,
            owedByThemText = owedByThemText,
            paymentByThemText = paymentByThemText,
            owedToThemText = owedToThemText,
            paymentToThemText = paymentToThemText,
            categoryLabelText = categoryLabelText,
            customerLabelText = customerLabelText,
            equivalentInfoTemplate = equivalentInfoTemplate,
            progressTextTemplate = progressTextTemplate,
            customerBundleDescTemplate = customerBundleDescTemplate,
            ledgerBundleDescTemplate = ledgerBundleDescTemplate,
            exchangeRateLabelText = exchangeRateLabelText
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline

    // إعادة تعيين الحد الأقصى للعرض عند تغيير البحث أو الفلتر
    LaunchedEffect(searchQuery, selectedFilter) {
        itemsLimit = 50
    }

    /*
     * معالجة وتحليل وتصفية عناصر السلة في خيط معالجة خلفي (Background Dispatcher)
     */
    LaunchedEffect(
        items, customersList, currencySymbol, searchQuery, itemsLimit,
        selectedFilter, selectedSort, trashStrings, primaryColor, secondaryColor,
        errorColor, outlineColor
    ) {
        val filtered = withContext(Dispatchers.Default) {
            // تصفية عناصر نظام الحبايب فقط
            val habayebOnly = items.filter {
                it.sourceSystem == systemHabayeb || it.originalTableName.startsWith("habayeb_")
            }

            // التصفية حسب النوع المختار قبل التحليل لتوفير موارد المعالجة
            val typeFiltered = when (selectedFilter) {
                TrashFilterType.ALL -> habayebOnly
                TrashFilterType.TRANSACTIONS -> habayebOnly.filter {
                    it.originalTableName == "habayeb_transactions"
                }
                TrashFilterType.CUSTOMERS -> habayebOnly.filter {
                    it.originalTableName == "habayeb_customers" || it.originalTableName == "habayeb_bundle"
                }
            }

            val parsedList = typeFiltered.map { item ->
                TrashWrapper(
                    entity = item,
                    parsed = TrashItemParser.parse(
                        item = item,
                        customersList = customersList,
                        currencySymbol = currencySymbol,
                        strings = trashStrings,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        errorColor = errorColor,
                        outlineColor = outlineColor
                    )
                )
            }

            var list = parsedList

            if (searchQuery.isNotBlank()) {
                val queryClean = searchQuery.trim().lowercase()
                list = list.filter { wrapper ->
                    wrapper.parsed.searchableText.lowercase().contains(queryClean)
                }
            }

            when (selectedSort) {
                TrashSortType.NEWEST_DELETED -> list.sortedByDescending { it.entity.deletedAt }
                TrashSortType.OLDEST_DELETED -> list.sortedBy { it.entity.deletedAt }
                TrashSortType.HIGHEST_AMOUNT -> list.sortedWith { a, b -> b.parsed.amount.compareTo(a.parsed.amount) }
                TrashSortType.ALPHABETICAL -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.parsed.name })
            }
        }
        totalFilteredCount = filtered.size
        processedItems = filtered.take(itemsLimit)
    }

    /*
     * إدارة زر الرجوع الفيزيائي المتدرج حسب الحالة النشطة
     */
    val handleBackAction = {
        when {
            activeDialogState !is TrashDialogState.None -> {
                activeDialogState = TrashDialogState.None
            }
            isSelectionMode -> {
                clearSelection()
            }
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
            selectedFilter != TrashFilterType.ALL -> {
                selectedFilter = TrashFilterType.ALL
            }
            else -> {
                onBack()
            }
        }
    }

    BackHandler(onBack = handleBackAction)

    /*
     * بناء هيكل الشاشة والعناصر البصرية
     */
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TrashTopBarSection(
                isSearchActive = isSearchActive,
                isSelectionMode = isSelectionMode,
                searchQuery = searchQuery,
                selectedCount = selectedItemIds.size,
                hasItems = items.isNotEmpty(),
                onSearchQueryChange = { searchQuery = it },
                onSearchToggle = { isSearchActive = it },
                onClearSelection = { clearSelection() },
                onBack = handleBackAction,
                onRestoreSelected = {
                    val selectedItems = items.filter { selectedItemIds.contains(it.id) }
                    viewModel.restoreMultipleItems(selectedItems)
                    clearSelection()
                },
                onDeleteSelectedPermanently = {
                    val selectedItems = items.filter { selectedItemIds.contains(it.id) }
                    viewModel.permanentlyDeleteMultipleItems(selectedItems)
                    clearSelection()
                },
                onRequestEmptyTrash = { activeDialogState = TrashDialogState.EmptyConfirm }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
        ) {
            // قائمة بطاقات العناصر المحذوفة مع شريط التحكم بالترتيب وفترة الصلاحية
            TrashItemListSection(
                processedItems = processedItems,
                selectedItemIds = selectedItemIds,
                isSelectionMode = isSelectionMode,
                totalFilteredCount = totalFilteredCount,
                itemsLimit = itemsLimit,
                selectedFilter = selectedFilter,
                selectedSort = selectedSort,
                autoCleanupPeriod = autoCleanupPeriod,
                onFilterSelected = { selectedFilter = it },
                onSortSelected = { selectedSort = it },
                onAutoCleanupPeriodChanged = { viewModel.updateAutoCleanupPeriod(it) },
                onToggleSelection = { itemId ->
                    if (!isSelectionMode) {
                        isSelectionMode = true
                        toggleSelection(itemId)
                    } else {
                        toggleSelection(itemId)
                    }
                },
                onRestoreItem = { item -> viewModel.restoreDeletedItem(item) },
                onPermanentDeleteItem = { item -> viewModel.permanentlyDeleteDeletedItem(item) },
                onOpenCustomerOverlay = { wrapper -> activeDialogState = TrashDialogState.CustomerHistoryOverlay(wrapper) },
                onOpenTransactionDetail = { wrapper -> activeDialogState = TrashDialogState.TransactionDetail(wrapper) },
                onLoadMore = { itemsLimit += 50 }
            )
        }
    }

    /*
     * الطبقة التراكبية لمعاينة تفاصيل حساب العميل المحذوف
     */
    (activeDialogState as? TrashDialogState.CustomerHistoryOverlay)?.let { state ->
        val currentEntity = items.find { it.id == state.wrapper.entity.id }
        if (currentEntity == null) {
            activeDialogState = TrashDialogState.None
        } else {
            val currentParsed = remember(currentEntity, customersList, currencySymbol, trashStrings, primaryColor, secondaryColor, errorColor, outlineColor) {
                TrashItemParser.parse(
                    item = currentEntity,
                    customersList = customersList,
                    currencySymbol = currencySymbol,
                    strings = trashStrings,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    errorColor = errorColor,
                    outlineColor = outlineColor
                )
            }
            TrashCustomerHistoryOverlay(
                item = currentEntity,
                parsedData = currentParsed,
                currencySymbol = currencySymbol,
                onDismiss = { activeDialogState = TrashDialogState.None },
                onRestoreFullAccount = {
                    viewModel.restoreDeletedItem(currentEntity)
                    activeDialogState = TrashDialogState.None
                },
                onDeleteFullAccountPermanently = {
                    viewModel.permanentlyDeleteDeletedItem(currentEntity)
                    activeDialogState = TrashDialogState.None
                },
                onRestoreSingleTx = { txId ->
                    viewModel.restoreSingleTransactionFromBundle(currentEntity.id, txId, currentEntity)
                }
            )
        }
    }

    /*
     * الورقة السفلية لمعاينة تفاصيل المعاملة المحذوفة
     */
    (activeDialogState as? TrashDialogState.TransactionDetail)?.let { state ->
        val currentEntity = items.find { it.id == state.wrapper.entity.id }
        if (currentEntity == null) {
            activeDialogState = TrashDialogState.None
        } else {
            val currentParsed = remember(currentEntity, customersList, currencySymbol, trashStrings, primaryColor, secondaryColor, errorColor, outlineColor) {
                TrashItemParser.parse(
                    item = currentEntity,
                    customersList = customersList,
                    currencySymbol = currencySymbol,
                    strings = trashStrings,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    errorColor = errorColor,
                    outlineColor = outlineColor
                )
            }
            TrashTransactionDetailBottomSheet(
                item = currentEntity,
                parsedData = currentParsed,
                currencySymbol = currencySymbol,
                onDismiss = { activeDialogState = TrashDialogState.None },
                onRestore = {
                    viewModel.restoreDeletedItem(currentEntity)
                    activeDialogState = TrashDialogState.None
                },
                onPermanentDelete = {
                    viewModel.permanentlyDeleteDeletedItem(currentEntity)
                    activeDialogState = TrashDialogState.None
                }
            )
        }
    }

    /*
     * مدير حوار تأكيد تفريغ السلة نهائياً
     */
    TrashDialogsManager(
        showEmptyConfirm = activeDialogState is TrashDialogState.EmptyConfirm,
        onDismissEmptyConfirm = { activeDialogState = TrashDialogState.None },
        onConfirmEmptyTrash = {
            viewModel.emptyTrash()
            activeDialogState = TrashDialogState.None
        }
    )
}

