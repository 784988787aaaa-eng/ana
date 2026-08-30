package com.example.ui.screens.habayeb.utils

/*
 * =====================================================================================
 * مُساعد فلترة وبحث معاملات العميل (Customer History Filter Helper)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * توفير دالة مساعدة معتمدة على Jetpack Compose لفلترة وبحث قائمة معاملات العميل بكفاءة عالية:
 * 1. تنفيذ عمليات الفلترة في خيط خلفي (Dispatchers.Default) عبر produceState لتفادي تجميد الواجهة.
 * 2. دعم البحث الذكي مع تطبيع الحروف العربية (إزالة الهمزات والتشكيل) والبحث في المبالغ والملاحظات والنوع.
 * 3. دعم الفلترة الزمنية (اليوم الحالي، الشهر الحالي، ونطاق تواريخ مخصص بين تاريخين).
 * 4. دعم الفلترة حسب نوع الحركة (ديون فقط / مدفوعات وسداد فقط).
 * 5. دعم الفلترة حسب العملة المحددة.
 * 6. تحسين الأداء عبر تجاوز المعالجة وإرجاع الترتيب المباشر عند عدم وجود فلاتر نشطة.
 * =====================================================================================
 */

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.example.R
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.StringUtils
import com.example.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * =====================================================================================
 * دالة تذكر المعاملات المفلترة (rememberFilteredCustomerTransactions)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - context: سياق التطبيق للوصول إلى الموارد النصية.
 * - allCustomerTxs: القائمة الكاملة لمعاملات العميل.
 * - txSearchQuery: نص استعلام البحث.
 * - dateFilterMode: وضع فلترة التاريخ (0: الكل، 1: اليوم، 2: الشهر، 3: مخصص).
 * - customStartDate: تاريخ البداية المخصص بالمللي ثانية.
 * - customEndDate: تاريخ النهاية المخصص بالمللي ثانية.
 * - typeFilterMode: وضع فلترة النوع (0: الكل، 1: ديون، 2: مدفوعات).
 * - selectedCurrencyFilter: رمز العملة المحددة للفلترة إن وجد.
 * - currencySymbol: رمز العملة الافتراضية.
 * - exchangeRatesJson: بيانات أسعار الصرف بصيغة JSON.
 * =====================================================================================
 */
@Composable
fun rememberFilteredCustomerTransactions(
    context: Context,
    allCustomerTxs: List<HabayebTransaction>,
    txSearchQuery: String,
    dateFilterMode: Int,
    customStartDate: Long?,
    customEndDate: Long?,
    typeFilterMode: Int,
    selectedCurrencyFilter: String?,
    currencySymbol: String,
    exchangeRatesJson: String?
): State<List<HabayebTransaction>> {
    // حساب الحدود الزمنية لليوم والشهر لتفادي إعادة الحساب المتكرر
    val dateBoundaries = remember(dateFilterMode) {
        if (dateFilterMode == 0) return@remember longArrayOf(0, 0, 0, 0)
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis / 1000
        val todayEnd = todayStart + 86400

        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val monthStart = cal.timeInMillis / 1000
        cal.add(java.util.Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis / 1000

        longArrayOf(todayStart, todayEnd, monthStart, monthEnd)
    }

    val isSearchBlank = txSearchQuery.isBlank()
    val hasNoFilters = isSearchBlank && dateFilterMode == 0 && typeFilterMode == 0 && selectedCurrencyFilter == null

    // القائمة المرتبة تنازلياً حسب التاريخ افتراضياً
    val sortedDirect = remember(allCustomerTxs) {
        if (allCustomerTxs.isEmpty()) emptyList() else allCustomerTxs.sortedByDescending { it.timestamp }
    }

    // إذا لم تكن هناك أي فلاتر مطبقة، نعيد القائمة المرتبة مباشرة
    if (hasNoFilters) {
        return androidx.compose.runtime.rememberUpdatedState(sortedDirect)
    }

    // تشغيل الفلترة في الخلفية عبر produceState
    return produceState<List<HabayebTransaction>>(
        initialValue = sortedDirect,
        allCustomerTxs, txSearchQuery, dateFilterMode, customStartDate, customEndDate, typeFilterMode, selectedCurrencyFilter, currencySymbol, exchangeRatesJson, dateBoundaries
    ) {
        withContext(Dispatchers.Default) {
            val todayStart = dateBoundaries[0]
            val todayEnd = dateBoundaries[1]
            val monthStart = dateBoundaries[2]
            val monthEnd = dateBoundaries[3]

            val searchDebtStr = context.getString(R.string.customer_history_search_debt)
            val searchPaymentStr = context.getString(R.string.customer_history_search_payment)
            val normalizedQuery = if (!isSearchBlank) StringUtils.normalizeArabic(txSearchQuery) else ""
            val normalizedDebtStr = if (!isSearchBlank) StringUtils.normalizeArabic(searchDebtStr) else ""
            val normalizedPaymentStr = if (!isSearchBlank) StringUtils.normalizeArabic(searchPaymentStr) else ""

            val safeRatesJson = exchangeRatesJson ?: ""

            val baseFiltered = allCustomerTxs.filter { tx ->
                // مطابقة البحث النصي
                val matchesSearch = if (isSearchBlank) {
                    true
                } else {
                    val normalizedDesc = StringUtils.normalizeArabic(tx.description)
                    val typeText = if (tx.type == TransactionType.OWED_BY_THEM.value) normalizedDebtStr else normalizedPaymentStr

                    normalizedDesc.contains(normalizedQuery, ignoreCase = true) ||
                    tx.amount.toString().contains(txSearchQuery) ||
                    tx.foreignAmount.toString().contains(txSearchQuery) ||
                    typeText.contains(normalizedQuery, ignoreCase = true)
                }

                // مطابقة فلتر التاريخ
                val matchesDate = when (dateFilterMode) {
                    1 -> tx.timestamp in todayStart..todayEnd
                    2 -> tx.timestamp in monthStart..monthEnd
                    3 -> {
                        val startSec = (customStartDate ?: 0L) / 1000
                        val endSec = if (customEndDate != null) (customEndDate / 1000) + 86400 else Long.MAX_VALUE
                        tx.timestamp in startSec..endSec
                    }
                    else -> true
                }

                // مطابقة فلتر نوع المعاملة
                val matchesType = when (typeFilterMode) {
                    1 -> tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.OWED_TO_THEM.value
                    2 -> tx.type == TransactionType.PAYMENT_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
                    else -> true
                }

                // مطابقة فلتر العملة
                val matchesCurrency = if (selectedCurrencyFilter != null) {
                    val (txCurrency, _) = CurrencyConfig.getTransactionCurrencyAndAmount(tx, currencySymbol, safeRatesJson)
                    txCurrency == selectedCurrencyFilter
                } else {
                    true
                }

                matchesSearch && matchesDate && matchesType && matchesCurrency
            }
            value = baseFiltered.sortedByDescending { it.timestamp }
        }
    }
}

