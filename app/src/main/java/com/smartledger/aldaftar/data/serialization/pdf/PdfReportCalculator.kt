/**
 * =====================================================================
 * ملف: محرك الحسابات المالية لتقارير PDF (PdfReportCalculator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن العقل الحسابي المالي لكافة تقارير PDF المطبوعة في التطبيق.
 * يتولى مسؤولية معالجة المعاملات المالية وترتيبها زمنياً، وتصنيف المبالغ وفق
 * العملة الأساسية للتقرير والعملات الأجنبية، وحساب الأرصدة المتراكمة،
 * وإجماليات المديونيات (لنا) والمدفوعات والمستحقات (علينا)، والصافي الكلي بدقة [BigDecimal].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحساب الدقيق عالي الحساسية (High-Precision BigDecimal Math):
 *    - حماية العمليات المالية من أخطاء الفاصلة العائمة [Floating Point Errors].
 * 2. الترتيب الزمني الصارم للمعاملات:
 *    - الفرز وفق التاريخ والوقت [timestamp] ثم المعرف [id] لضمان اتساق الأرصدة التراكمية.
 * 3. الفصل المحاسبي بين العملة الأساسية والعملات الأجنبية:
 *    - التمييز بين المعاملات المحسوبة بسعر الصرف والمعاملات الأجنبية الصرفة.
 * 4. حساب الملخص الشامل لكافة العملاء [calculateComprehensiveReport]:
 *    - تجميع إجمالي ما لنا على العملاء وما علينا لهم عبر كافة العملات.
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد الكيانات والنماذج وتكوينات العملات وحزم الحسابات الرياضية
// ---------------------------------------------------------------------
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import java.math.BigDecimal

/**
 * [معاملة مالية معالجة ومجهزة للطباعة - ProcessedTransaction]:
 * تحتوي على الكيان الأصلي وبيانات العملة والمبالغ المحسوبة.
 *
 * @property tx المعاملة المالية الأصلية.
 * @property resolvedCurrency رمز العملة النهائي.
 * @property resolvedAmount المبلغ النهائي المعتمد للطباعة.
 * @property isTxForeign هل المعاملة بعملة أجنبية أو محولة بسعر صرف.
 * @property baseCurrencyAmount المبلغ بالعملة الأساسية للتقرير (أو صفر إن كانت أجنبية صرفة).
 * @property pureBaseAmount المبلغ الصافي بالعملة الأساسية دون أسعار صرف.
 */
data class ProcessedTransaction(
    val tx: HabayebTransaction,
    val resolvedCurrency: String,
    val resolvedAmount: BigDecimal,
    val isTxForeign: Boolean,
    val baseCurrencyAmount: BigDecimal,
    val pureBaseAmount: BigDecimal
)

/**
 * [ملخص كشف حساب عميل واحد لـ PDF - SingleCustomerPdfSummary]:
 * يجمع قائمة المعاملات المرتبة وإجماليات المديونيات والمقبوضات وصافي الرصيد.
 *
 * @property sortedProcessedTxs قائمة المعاملات مرتبة زمنياً ومعالجة.
 * @property totalDebts إجمالي المبالغ المدينة (لنا).
 * @property totalPayments إجمالي المقبوضات/المسددات.
 * @property totalDebtsBase إجمالي الديون بالعملة الأساسية الصرفة.
 * @property totalPaymentsBase إجمالي المسددات بالعملة الأساسية الصرفة.
 * @property calculatedNetDebt صافي الرصيد النهائي بالعملة الأساسية.
 * @property uncalculatedForeignSums خريطة أرصدة العملات الأجنبية غير المحولة.
 * @property hasMultipleCurrencies ما إذا كان الحساب يحتوي على أكثر من عملة.
 */
data class SingleCustomerPdfSummary(
    val sortedProcessedTxs: List<ProcessedTransaction>,
    val totalDebts: BigDecimal,
    val totalPayments: BigDecimal,
    val totalDebtsBase: BigDecimal,
    val totalPaymentsBase: BigDecimal,
    val calculatedNetDebt: BigDecimal,
    val uncalculatedForeignSums: Map<String, BigDecimal>,
    val hasMultipleCurrencies: Boolean
)

/**
 * [ملخص تقرير دفتر الحسابات الشامل لـ PDF - ComprehensivePdfSummary]:
 * يجمع إجماليات كافة حسابات العملاء على مستوى المنشأة.
 *
 * @property totalOwedByThem إجمالي ما لنا على جميع العملاء (الديون الخارجية).
 * @property totalOwedToThem إجمالي ما علينا لجميع العملاء (الالتزامات).
 * @property netPrimary صافي الرصيد العام بالعملة الأساسية.
 * @property foreignTotalsMap خريطة إجماليات العملات الأجنبية لكافة الحسابات.
 */
data class ComprehensivePdfSummary(
    val totalOwedByThem: BigDecimal,
    val totalOwedToThem: BigDecimal,
    val netPrimary: BigDecimal,
    val foreignTotalsMap: Map<String, BigDecimal>
)

/**
 * [الكائن الأحادي لمحرك حسابات تقارير PDF - PdfReportCalculator]:
 * يوفر خوارزميات المعالجة المالية والحسابات التراكمية.
 */
object PdfReportCalculator {

    /**
     * [حساب ومعالجة كشف حساب عميل فردي - calculateSingleCustomerReport]:
     * يرتب المعاملات ويفصل الأرصدة ويحسب إجماليات المديونيات والصافي.
     *
     * @param transactions قائمة معاملات العميل الخام من قاعدة البيانات.
     * @param currencySymbol رمز العملة الأساسية المعتمدة للتقرير.
     * @return كائن [SingleCustomerPdfSummary] متكامل ومجهز للعرض.
     */
    fun calculateSingleCustomerReport(
        transactions: List<HabayebTransaction>,
        currencySymbol: String
    ): SingleCustomerPdfSummary {
        val calcResult = com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator.calculate(
            transactions,
            currencySymbol,
            exchangeRatesJson = null
        )

        val normDefaultSymbol = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol
        val sortedTxs = transactions.sortedWith(compareBy<HabayebTransaction> { it.timestamp }.thenBy { it.id })

        val processedList = ArrayList<ProcessedTransaction>(sortedTxs.size)
        var totalDebtsBase = BigDecimal.ZERO
        var totalPaymentsBase = BigDecimal.ZERO

        for (tx in sortedTxs) {
            val (resolvedCurrency, resolvedAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, currencySymbol)
            val affectsReportPrimaryCurrency = (resolvedCurrency == normDefaultSymbol)

            // Strictly assign baseCurrencyAmount only if this transaction targets the report's primary currency!
            val baseCurrencyAmount = if (affectsReportPrimaryCurrency) resolvedAmount else BigDecimal.ZERO

            val isTxForeign = (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && 
                               tx.currencyCode.isNotBlank() && 
                               tx.currencyCode != normDefaultSymbol) || 
                              tx.isRateCalculated

            val isPureBase = (tx.currencyCode == FinanceConstants.DEFAULT_CURRENCY_CODE || tx.currencyCode.isBlank() || tx.currencyCode == normDefaultSymbol) && !tx.isRateCalculated
            val pureBaseAmount = if (isPureBase) tx.foreignAmount else BigDecimal.ZERO

            val txType = TransactionType.fromValue(tx.type)
            if (affectsReportPrimaryCurrency) {
                if (txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM) {
                    totalDebtsBase = totalDebtsBase.add(pureBaseAmount)
                } else if (txType == TransactionType.PAYMENT_BY_THEM || txType == TransactionType.OWED_TO_THEM) {
                    totalPaymentsBase = totalPaymentsBase.add(pureBaseAmount)
                }
            }

            processedList.add(
                ProcessedTransaction(
                    tx = tx,
                    resolvedCurrency = resolvedCurrency,
                    resolvedAmount = resolvedAmount,
                    isTxForeign = isTxForeign,
                    baseCurrencyAmount = baseCurrencyAmount,
                    pureBaseAmount = pureBaseAmount
                )
            )
        }

        val owedBy = calcResult.owedByThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val payTo = calcResult.paymentToThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val totalDebts = owedBy.add(payTo)

        val payBy = calcResult.paymentByThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val owedTo = calcResult.owedToThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val totalPayments = payBy.add(owedTo)

        val calculatedNetDebt = calcResult.netDebtBigDecimalMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val uncalculatedForeignSums = calcResult.netDebtBigDecimalMap.filterKeys { key ->
            key != normDefaultSymbol && ((calcResult.netDebtBigDecimalMap[key] ?: BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)
        }

        val hasMultipleCurrencies = uncalculatedForeignSums.isNotEmpty() || processedList.any { pt -> pt.isTxForeign }

        return SingleCustomerPdfSummary(
            sortedProcessedTxs = processedList,
            totalDebts = totalDebts,
            totalPayments = totalPayments,
            totalDebtsBase = totalDebtsBase,
            totalPaymentsBase = totalPaymentsBase,
            calculatedNetDebt = calculatedNetDebt,
            uncalculatedForeignSums = uncalculatedForeignSums,
            hasMultipleCurrencies = hasMultipleCurrencies
        )
    }

    /**
     * [حساب الملخص المالي الشامل لكافة العملاء - calculateComprehensiveReport]:
     * يجمع إجماليات المديونيات والالتزامات وصافي الرصيد لكافة الحسابات.
     *
     * @param customers قائمة حالات واجهة المستخدم لكافة العملاء.
     * @return كائن [ComprehensivePdfSummary] يحتوي على الإجماليات الموحدة.
     */
    fun calculateComprehensiveReport(
        customers: List<CustomerUiState>
    ): ComprehensivePdfSummary {
        var totalOwedByThem = BigDecimal.ZERO
        var totalOwedToThem = BigDecimal.ZERO
        val foreignTotalsMap = mutableMapOf<String, BigDecimal>()

        for (c in customers) {
            val bdVal = c.defaultCurrencyTotal
            if (bdVal.compareTo(BigDecimal.ZERO) > 0) {
                totalOwedByThem = totalOwedByThem.add(bdVal)
            } else if (bdVal.compareTo(BigDecimal.ZERO) < 0) {
                totalOwedToThem = totalOwedToThem.add(bdVal.abs())
            }
            for ((curr, valBd) in c.foreignDebts) {
                if (valBd.compareTo(BigDecimal.ZERO) != 0) {
                    foreignTotalsMap[curr] = (foreignTotalsMap[curr] ?: BigDecimal.ZERO).add(valBd)
                }
            }
        }

        val netPrimary = totalOwedByThem.subtract(totalOwedToThem)

        return ComprehensivePdfSummary(
            totalOwedByThem = totalOwedByThem,
            totalOwedToThem = totalOwedToThem,
            netPrimary = netPrimary,
            foreignTotalsMap = foreignTotalsMap
        )
    }
}

