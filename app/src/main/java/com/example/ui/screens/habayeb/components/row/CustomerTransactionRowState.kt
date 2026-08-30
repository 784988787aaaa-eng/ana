package com.example.ui.screens.habayeb.components.row

/*
 * =====================================================================================
 * حالة وحسابات صف المعاملة المالية (Customer Transaction Row State & Calculator)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * ملف مخصص لإدارة وحساب كافة البيانات البصرية والنصية لصف المعاملة المالية في القائمة:
 * 1. حساب ألوان السمة (داكن/فاتح) للديون (أحمر) والائتمان (أخضر) وتنبيهات العملات والبطاقات.
 * 2. نموذج بيانات غير قابل للتغيير (Immutable Data Class) لتخزين البيانات المنسقة مسبقاً وتفادي الحسابات المتكررة (Cached Data).
 * 3. حاسبة الحالة (State Calculator) التي تستخرج العملات، الأوصاف النظيفة، المبالغ المكافئة، أسهم الاتجاه، وتواريخ المعاملات.
 * =====================================================================================
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.HabayebDateFormatter
import com.example.ui.theme.AlertGoldBgDark
import com.example.ui.theme.AlertGoldBorderDark
import com.example.ui.theme.AlertGoldTextDark
import com.example.ui.theme.AlertGoldTextLight
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueBgDark
import com.example.ui.theme.InfoBlueBgLight
import com.example.ui.theme.InfoBlueTextDark
import com.example.ui.theme.InfoBlueTextLight
import com.example.ui.theme.MutedTextDark
import com.example.ui.theme.MutedTextLight
import com.example.ui.theme.SuccessGreenBgDark
import com.example.ui.theme.SuccessGreenBgLight
import com.example.ui.theme.SuccessGreenBorderDark
import com.example.ui.theme.SuccessGreenBorderLight
import com.example.ui.theme.WarningRedBorder
import com.example.ui.theme.WarningRedBorderLight
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import com.example.ui.viewmodel.FinanceConstants
import java.math.BigDecimal
import java.util.Date

// وسم الإشارة إلى عدم وجود عملة مخصصة في الوصف
private const val CURRENCY_NONE_TAG = "NONE"

/*
 * =====================================================================================
 * كائن ألوان الصفوف (RowColors Object)
 * -------------------------------------------------------------------------------------
 * يوفر دوال مساعدة لاسترجاع الألوان المناسبة لحالة السمة (الوضع الليلي والنهاري).
 * =====================================================================================
 */
object RowColors {
    fun creditGreen(isDark: Boolean) = financialCreditColor(isDark)
    fun debtRed(isDark: Boolean) = financialDebtColor(isDark)
    fun mutedGray(isDark: Boolean) = if (isDark) MutedTextDark else MutedTextLight
    
    fun alertGoldBg(isDark: Boolean) = if (isDark) AlertGoldBgDark else com.example.ui.theme.WarningAmberBg
    fun alertGoldBorder(isDark: Boolean) = if (isDark) AlertGoldBorderDark else com.example.ui.theme.WarningAmberBorder
    fun alertGoldText(isDark: Boolean) = if (isDark) AlertGoldTextDark else AlertGoldTextLight
    
    fun infoBlueBg(isDark: Boolean) = if (isDark) InfoBlueBgDark else InfoBlueBgLight
    fun infoBlueBorder(isDark: Boolean) = if (isDark) InfoBlueTextDark else InfoBlue
    fun infoBlueText(isDark: Boolean) = if (isDark) InfoBlueTextDark else InfoBlueTextLight
    
    fun successGreenBg(isDark: Boolean) = if (isDark) SuccessGreenBgDark else SuccessGreenBgLight
    fun successGreenBorder(isDark: Boolean) = if (isDark) SuccessGreenBorderDark else SuccessGreenBorderLight
    fun warningRedBorder(isDark: Boolean) = if (isDark) WarningRedBorder else WarningRedBorderLight
    fun darkGray(isDark: Boolean) = if (isDark) MutedTextDark else MutedTextLight
}

/*
 * =====================================================================================
 * كائن بيانات صف المعاملة المخزنة مسبقاً (TransactionRowCachedData)
 * -------------------------------------------------------------------------------------
 * [الحقول]:
 * - cleanDescription: الوصف المنظف بعد استخراج العملة.
 * - indicatorColor: لون مؤشر المعاملة (أحمر للديون، أخضر للسداد).
 * - txArrow: أيقونة السهم الدالة على اتجاه الحركة المالية (صاعد/هابط).
 * - formattedAmount: المبلغ المنسق نصياً بذكاء.
 * - displayCurrency: رمز العملة المعروضة.
 * - equivalentAmountText: نص المبلغ المكافئ بالعملة الأساسية إن وجد.
 * - dayNameResId: معرف مورد اسم اليوم في الأسبوع.
 * - dateStr: التاريخ بصيغة قصيرة.
 * - timeStr: الوقت بنظام 12 ساعة.
 * - isTxForeign: هل المعاملة بعملة أجنبية مختلفة عن العملة الافتراضية.
 * - isCalculated: هل تم تحويل السعر بحساب سعر الصرف.
 * - typeResId: معرف مورد النص الوصفي لنوع المعاملة.
 * =====================================================================================
 */
@Immutable
data class TransactionRowCachedData(
    val cleanDescription: String,
    val indicatorColor: Color,
    val txArrow: ImageVector,
    val formattedAmount: String,
    val displayCurrency: String,
    val equivalentAmountText: String?,
    val dayNameResId: Int,
    val dateStr: String,
    val timeStr: String,
    val isTxForeign: Boolean,
    val isCalculated: Boolean,
    val typeResId: Int
)

/*
 * =====================================================================================
 * حاسبة حالة صف المعاملة (CustomerTransactionRowStateCalculator)
 * -------------------------------------------------------------------------------------
 * تقوم بتحليل كائن المعاملة وحساب كافة القيم والتنسيقات البصرية المطلوبة لعرض الصف بكفاءة عالية.
 * =====================================================================================
 */
object CustomerTransactionRowStateCalculator {
    /*
     * دالة الحساب واستخراج البيانات (calculate)
     * - tx: المعاملة المطلوب حساب حالتها.
     * - isDark: هل الوضع الليلي مفعل.
     * - currencySymbol: رمز العملة الافتراضية للتطبيق.
     * - initialType: النوع الأولي للمعاملة.
     */
    fun calculate(
        tx: HabayebTransaction,
        isDark: Boolean,
        currencySymbol: String,
        initialType: String
    ): TransactionRowCachedData {
        // استخراج العملة والوصف النظيف
        val parsedCurrencyInfo = CurrencyConfig.parseTransactionCurrency(tx.description, CURRENCY_NONE_TAG)
        val txCurrencySymbol = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) {
            tx.currencyCode
        } else if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) {
            parsedCurrencyInfo.first
        } else if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
            tx.baseCurrencyCode
        } else {
            currencySymbol
        }
        val isTxForeign = txCurrencySymbol != currencySymbol
        val cleanDescription = if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) parsedCurrencyInfo.second else tx.description

        // تحديد نوع المعاملة ولون المؤشر
        val txType = TransactionType.fromValue(tx.type)
        val indicatorColor = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.OWED_TO_THEM -> RowColors.debtRed(isDark)
            else -> RowColors.creditGreen(isDark)
        }

        // تحديد اتجاه سهم الحركة المالية
        val txArrow = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> Icons.Default.ArrowUpward
            TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> Icons.Default.ArrowDownward
            else -> Icons.Default.ArrowUpward
        }

        val displayAmount: BigDecimal
        val displayCurrency: String
        val equivalentAmount: BigDecimal?
        val equivalentCurrency: String?

        val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount

        // التحقق من حساب الصرف للعملات الأجنبية
        if (tx.isRateCalculated) {
            val baseCurrency = if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
                tx.baseCurrencyCode
            } else {
                FinanceConstants.FALLBACK_CURRENCY_SYMBOL
            }
            val origCurrency = txCurrencySymbol
            
            displayAmount = sourceAmount
            displayCurrency = origCurrency
            equivalentAmount = tx.equivalentAmount
            equivalentCurrency = baseCurrency
        } else {
            displayAmount = sourceAmount
            displayCurrency = txCurrencySymbol
            equivalentAmount = null
            equivalentCurrency = null
        }

        // تنسيق المبالغ المالية بصيغة ذكية
        val formattedAmount = HabayebMathHelper.formatSmart(displayAmount)

        val equivalentAmountText = if (equivalentAmount != null && equivalentCurrency != null) {
            val formattedEquiv = HabayebMathHelper.formatSmart(equivalentAmount)
            "($formattedEquiv $equivalentCurrency)"
        } else null

        // تنسيق التاريخ والوقت
        val d = Date(tx.timestamp * 1000L)
        val dateStr = HabayebDateFormatter.formatShortDate(d)
        val timeStr = HabayebDateFormatter.formatTime12h(d)
        val dayNameResId = HabayebDateFormatter.getDayOfWeekResId(tx.timestamp)

        // تحديد معرف النص الوصفي لنوع المعاملة
        val typeResId = when (txType) {
            TransactionType.OWED_BY_THEM -> R.string.habayeb_pdf_tx_owed_by
            TransactionType.PAYMENT_BY_THEM -> R.string.habayeb_pdf_tx_payment_by
            TransactionType.OWED_TO_THEM -> R.string.habayeb_pdf_tx_owed_to
            TransactionType.PAYMENT_TO_THEM -> R.string.habayeb_pdf_tx_payment_to
            else -> R.string.habayeb_pdf_tx_generic
        }

        return TransactionRowCachedData(
            cleanDescription = cleanDescription,
            indicatorColor = indicatorColor,
            txArrow = txArrow,
            formattedAmount = formattedAmount,
            displayCurrency = displayCurrency,
            equivalentAmountText = equivalentAmountText,
            dayNameResId = dayNameResId,
            dateStr = dateStr,
            timeStr = timeStr,
            isTxForeign = isTxForeign,
            isCalculated = tx.isRateCalculated,
            typeResId = typeResId
        )
    }
}

