/**
 * =====================================================================
 * ملف: حاسبة الإجماليات والتصفيات المالية لعملاء الحبايب (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن محرك الحسابات المالية، والتجميع الإحصائي، والفلترة المتقدمة لعملاء
 * قسم "الحبايب". يقوم بحساب صافي الديون بالعملة الافتراضية والعملات الأجنبية، وحساب
 * مجاميع (لنا / لهم) وفلترة الحسابات حسب التصنيف، التبويب، البحث، والتثبيت.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. حساب ملخصات الديون الموحدة (   ):
 *    - الاعتماد على [] كمصدر موحد للحقيقة لضمان تطابق الأرقام مع التقارير والـ .
 * 2. الفرز والفلترة متعددة المعايير (-  & ):
 *    - معالجة تبويبات الديون (لنا، عليهم، الكل)، البحث الموحد بالنصوص المعربة والأرقام، وتجاهل الحسابات المخفية.
 * 3. دعم تثبيت الحسابات في أعلى القائمة (  ):
 *    - فرز وتثبيت الحسابات المميزة في أعلى القائمة مع فرز بقية الحسابات مالياً أو زمنياً.
 * 4. تجميع الإحصائيات والأعداد (  &  ):
 *    - حساب عدد الحسابات النشطة، والحسابات المقفلة، وعدد العملاء في كل تصنيف بشكل فوري.
 */
package com.smartledger.aldaftar.domain.usecase.habayeb

// ---------------------------------------------------------------------
// استيراد حزم الإعدادات والكيانات ومكتبات الحسابات المحاسبية
// ---------------------------------------------------------------------
import android.content.SharedPreferences
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.StringUtils
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.ui.state.CustomersUiState
import java.math.BigDecimal
import java.math.RoundingMode
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants

// =========================================================================
// قسم: الثوابت ونماذج معلمات الفلترة والنتائج المالية
// =========================================================================

/** بادئة مفاتيح ربط العميل بالتصنيف في التفضيلات */
private const val PREFIX_CAT_LINK = HabayebCategoryManager.PREFIX_CAT_LINK
/** المعرف الثابت لتصنيف الحسابات المقفلة */
private const val CATEGORY_CLOSED = FinanceConstants.CATEGORY_CLOSED

/**
 * [معلمات فلترة وتصفية قائمة الحبايب - ]:
 * تغلف كافة خيارات التصفية والبحث والترتيب في كائن واحد متكامل.
 */
data class HabayebFilterParameters(
    val query: String,
    val tab: Int,
    val finSort: Int,
    val histSort: Int,
    val hiddenIds: Set<String>,
    val selectedCat: String?,
    val pinnedIds: Set<String>
)

/** مجموعة المعلمات الأولى المستخدمة لتحسين إعادة الحساب () */
data class HabayebFilterGroup1(
    val query: String,
    val tab: Int,
    val finSort: Int,
    val histSort: Int
)

/** مجموعة المعلمات الثانية للروابط والتصنيفات والتثبيتات */
data class HabayebFilterGroup2(
    val hiddenIds: Set<String>,
    val selectedCat: String?,
    val pinnedIds: Set<String>
)

/**
 * [نتيجة الفلترة والتجميع المالي - ]:
 * تحتوي على القائمة المصفاة للعملاء بالإضافة إلى الإجماليات المالية وإحصائيات التصنيفات.
 */
data class FilteredResult(
    val filteredCustomers: List<CustomerUiState>,
    val totalOwedByThem: BigDecimal,
    val totalOwedToThem: BigDecimal,
    val categoryCounts: Map<String, Int>,
    val activeCustomersCount: Int = 0
)

/**
 * [الكائن الأحادي لمحرك حسابات الحبايب - ]:
 * يوفر دوال حساب الحالة المالية وتطبيق الفلاتر والفرز المالي المتقدم.
 */
object HabayebFinancialCalculator {

    /**
     * [حساب حالة واجهة المستخدم الشاملة للعملاء - ]:
     * يجمع الحركات لكل عميل، ويحسب صافي الدين بالعملة المحلية والأجنبية، ومجاميع الديون الإجمالية.
     *
     * @  قائمة عملاء الحبايب من قاعدة البيانات.
     * @  كافة الحركات المالية المسجلة.
     * @  إعدادات التطبيق متضمنة أسعار الصرف والعملة الافتراضية.
     * @ كائن [] المتكامل للواجهة.
     */
    fun calculateCustomersUiState(
        customers: List<HabayebCustomer>,
        allTransactions: List<HabayebTransaction>,
        settings: AppSettings
    ): CustomersUiState {
        val defaultCurrency = settings.currencySymbol
        val normDefaultCurrency = com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(defaultCurrency)?.symbol ?: defaultCurrency
        val transactionsByCustomer = allTransactions.groupBy { it.customerId }

        var globalTotalOwedByThem = BigDecimal.ZERO
        var globalTotalOwedToThem = BigDecimal.ZERO

        val customerStates = ArrayList<CustomerUiState>(customers.size)
        for (customer in customers) {
            val custTxs = transactionsByCustomer[customer.id] ?: emptyList()
            val summary = CustomerHistoryCalculator.calculateSummary(
                custTxs,
                defaultCurrency,
                settings.exchangeRatesJson,
                customer.createdAt
            )

            val defaultCurrencyTotal = summary.netDebtBigDecimalMap[normDefaultCurrency] ?: BigDecimal.ZERO
            val defaultCurrencyTotalAbs = defaultCurrencyTotal.abs()
            val activeForeignDebts = if (summary.netDebtBigDecimalMap.size > 1) {
                summary.netDebtBigDecimalMap
                    .filterKeys { it != normDefaultCurrency }
                    .filterValues { bd -> bd.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0 }
            } else {
                emptyMap()
            }

            val displayCurrency = summary.primaryDisplayCurrency
            val displayNetDebt = summary.netDebt
            val lastTxTime = summary.lastTimestamp
            val normalizedName = StringUtils.normalizeArabic(customer.name)

            val state = CustomerUiState(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                notes = customer.notes,
                createdAt = customer.createdAt,
                totalTransactions = custTxs.size,
                netDebt = defaultCurrencyTotal,
                displayNetDebt = displayNetDebt,
                displayCurrencySymbol = displayCurrency,
                lastTransactionTimestamp = lastTxTime,
                originalCustomer = customer,
                foreignDebts = activeForeignDebts,
                defaultCurrencyTotal = defaultCurrencyTotal,
                normalizedName = normalizedName,
                defaultCurrencyTotalAbs = defaultCurrencyTotalAbs
            )
            customerStates.add(state)

            if (!state.isClosed) {
                val cmp = defaultCurrencyTotal.compareTo(BigDecimal.ZERO)
                if (cmp > 0) {
                    globalTotalOwedByThem = globalTotalOwedByThem.add(defaultCurrencyTotal)
                } else if (cmp < 0) {
                    globalTotalOwedToThem = globalTotalOwedToThem.add(defaultCurrencyTotalAbs)
                }
            }
        }

        return CustomersUiState(
            customers = customerStates,
            totalOwedByThem = globalTotalOwedByThem.setScale(4, RoundingMode.HALF_EVEN),
            totalOwedToThem = globalTotalOwedToThem.setScale(4, RoundingMode.HALF_EVEN),
            isLoading = false
        )
    }

    /**
     * [استخراج خارطة التصنيفات من التفضيلات - ]:
     */
    fun extractCategoryMap(sharedPrefs: SharedPreferences): Map<String, String> {
        val map = mutableMapOf<String, String>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith(PREFIX_CAT_LINK) && value is String) {
                map[key.removePrefix(PREFIX_CAT_LINK)] = value
            }
        }
        return map
    }

    /**
     * [حساب النتيجة المصفاة باستخدام التفضيلات]:
     */
    fun calculateFilteredResult(
        uiState: CustomersUiState,
        params: HabayebFilterParameters,
        sharedPrefs: SharedPreferences
    ): FilteredResult {
        return calculateFilteredResult(uiState, params, extractCategoryMap(sharedPrefs))
    }

    /**
     * [تطبيق الفلترة والفرز والإحصاءات المتقدمة - ]:
     * يمر على العملاء لتطبيق فلاتر البحث، التبويب، والتصنيف، وحساب المجاميع وإحصائيات العدادات.
     */
    fun calculateFilteredResult(
        uiState: CustomersUiState,
        params: HabayebFilterParameters,
        categoryMap: Map<String, String>
    ): FilteredResult {
        val normalizedQuery = if (params.query.isNotEmpty()) StringUtils.normalizeArabic(params.query) else ""
        val counts = mutableMapOf<String, Int>()
        var closedCount = 0
        var activeCustomersCount = 0

        var owedByTotal = BigDecimal.ZERO
        var owedToTotal = BigDecimal.ZERO

        val baseFilteredList = ArrayList<CustomerUiState>(uiState.customers.size)
        val selectedCat = params.selectedCat
        val isQueryEmpty = normalizedQuery.isEmpty()

        for (customerUi in uiState.customers) {
            val isClosed = customerUi.isClosed
            val linkedCat = categoryMap[customerUi.id]

            if (isClosed) {
                closedCount++
            } else {
                activeCustomersCount++
                if (linkedCat != null) {
                    counts[linkedCat] = (counts[linkedCat] ?: 0) + 1
                }
            }

            val matchesSelectedCatForTotals = when (selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == selectedCat
            }

            if (matchesSelectedCatForTotals) {
                val bdVal = customerUi.defaultCurrencyTotal
                val cmp = bdVal.compareTo(BigDecimal.ZERO)
                if (cmp > 0) {
                    owedByTotal = owedByTotal.add(bdVal)
                } else if (cmp < 0) {
                    owedToTotal = owedToTotal.add(customerUi.defaultCurrencyTotalAbs)
                }
            }

            if (params.hiddenIds.contains(customerUi.id)) continue

            val matchesTab = when (params.tab) {
                1 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) > 0
                2 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) < 0
                else -> true
            }
            if (!matchesTab) continue

            val matchesCategory = when (selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == selectedCat
            }
            if (!matchesCategory) continue

            val matchesSearch = isQueryEmpty ||
                    customerUi.normalizedName.contains(normalizedQuery, ignoreCase = true) ||
                    customerUi.phone.contains(params.query, ignoreCase = true)
            if (!matchesSearch) continue

            baseFilteredList.add(customerUi)
        }

        counts[CATEGORY_CLOSED] = closedCount

        val finalFilteredList = if (params.pinnedIds.isEmpty()) {
            when {
                params.finSort == 1 -> baseFilteredList.sortedByDescending { it.defaultCurrencyTotalAbs }
                params.finSort == 2 -> baseFilteredList.sortedBy { it.defaultCurrencyTotalAbs }
                params.histSort == 2 -> baseFilteredList.sortedBy { it.lastTransactionTimestamp }
                else -> baseFilteredList.sortedByDescending { it.lastTransactionTimestamp }
            }
        } else {
            val (pinnedList, unpinnedList) = baseFilteredList.partition { params.pinnedIds.contains(it.id) }
            val sortedUnpinned = when {
                params.finSort == 1 -> unpinnedList.sortedByDescending { it.defaultCurrencyTotalAbs }
                params.finSort == 2 -> unpinnedList.sortedBy { it.defaultCurrencyTotalAbs }
                params.histSort == 2 -> unpinnedList.sortedBy { it.lastTransactionTimestamp }
                else -> unpinnedList.sortedByDescending { it.lastTransactionTimestamp }
            }
            pinnedList.sortedByDescending { it.lastTransactionTimestamp } + sortedUnpinned
        }

        return FilteredResult(
            filteredCustomers = finalFilteredList,
            totalOwedByThem = owedByTotal,
            totalOwedToThem = owedToTotal,
            categoryCounts = counts,
            activeCustomersCount = activeCustomersCount
        )
    }
}
