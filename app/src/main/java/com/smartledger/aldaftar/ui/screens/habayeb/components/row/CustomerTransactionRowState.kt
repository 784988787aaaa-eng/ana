package com.smartledger.aldaftar.ui.screens.habayeb.components.row

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.HabayebDateFormatter
import com.smartledger.aldaftar.ui.theme.financialCreditColor
import com.smartledger.aldaftar.ui.theme.financialDebtColor
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import java.math.BigDecimal
import java.util.Date

private const val CURRENCY_NONE_TAG = "NONE"

object RowColors {
    val creditGreen: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.credit

    val debtRed: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.debt

    val mutedGray: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.contentSecondary

    val alertGoldBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldBackground

    val alertGoldBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldBorder

    val alertGoldText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldText

    val infoBlueBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueBackground

    val infoBlueBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueBorder

    val infoBlueText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueText

    val successGreenBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.successGreenBackground

    val successGreenBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.successGreenBorder

    val warningRedBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.debtBorder

    val darkGray: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.contentSecondary

    // Backwards compatibility helpers
    fun creditGreen(isDark: Boolean) = financialCreditColor(isDark)
    fun debtRed(isDark: Boolean) = financialDebtColor(isDark)
    fun mutedGray(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldText(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueText(isDark: Boolean): Color = financialDebtColor(isDark)
    fun successGreenBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun successGreenBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun warningRedBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun darkGray(isDark: Boolean): Color = financialDebtColor(isDark)
}

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

object CustomerTransactionRowStateCalculator {
    fun calculate(
        tx: HabayebTransaction,
        currencySymbol: String,
        initialType: String,
        debtColor: Color,
        creditColor: Color
    ): TransactionRowCachedData {
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

        val txType = TransactionType.fromValue(tx.type)
        val indicatorColor = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.OWED_TO_THEM -> debtColor
            else -> creditColor
        }

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

        val formattedAmount = HabayebMathHelper.formatSmart(displayAmount)

        val equivalentAmountText = if (equivalentAmount != null && equivalentCurrency != null) {
            val formattedEquiv = HabayebMathHelper.formatSmart(equivalentAmount)
            "($formattedEquiv $equivalentCurrency)"
        } else null

        val d = Date(tx.timestamp * 1000L)
        val dateStr = HabayebDateFormatter.formatShortDate(d)
        val timeStr = HabayebDateFormatter.formatTime12h(d)
        val dayNameResId = HabayebDateFormatter.getDayOfWeekResId(tx.timestamp)

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

    fun calculate(
        tx: HabayebTransaction,
        isDark: Boolean,
        currencySymbol: String,
        initialType: String
    ): TransactionRowCachedData = calculate(
        tx = tx,
        currencySymbol = currencySymbol,
        initialType = initialType,
        debtColor = financialDebtColor(isDark),
        creditColor = financialCreditColor(isDark)
    )
}
