package com.smartledger.aldaftar.domain.notifications

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator
import java.math.BigDecimal

/**
 * TransactionNotificationBuilder
 *
 * Dedicated standalone engine for building short, elegant, intelligent,
 * and clear transaction notifications and text messages for WhatsApp, SMS,
 * and text sharing according to the financial notification specification.
 */
object TransactionNotificationBuilder {

    /**
     * Formats amounts with currency:
     * - USD / $: prefix with $ (e.g. $500, $0, $1,200)
     * - YER / ر.ي: value followed by space and symbol (e.g. 10,000 ر.ي, 0 ر.ي)
     * - SAR / ر.س: value followed by space and symbol (e.g. 1,000 ر.س, 0 ر.س)
     * - Others: value followed by space and symbol
     */
    fun formatCurrencyAmount(amount: BigDecimal, currencySymbol: String): String {
        val formattedNumber = HabayebMathHelper.formatSmart(amount.abs())
        val normCurrency = normalizeCurrencySymbol(currencySymbol)
        return when (normCurrency) {
            "$", "USD" -> "$$formattedNumber"
            "ر.ي", "YER" -> "$formattedNumber ر.ي"
            "ر.س", "SAR" -> "$formattedNumber ر.س"
            else -> if (currencySymbol.isNotBlank()) "$formattedNumber $currencySymbol" else formattedNumber
        }
    }

    fun normalizeCurrencySymbol(symbol: String): String {
        val trimmed = symbol.trim()
        val c = CurrencyConfig.getBySymbol(trimmed) ?: CurrencyConfig.getByCode(trimmed)
        return c?.symbol ?: trimmed
    }

    /**
     * Builds a WhatsApp (Markdown formatted) notification message.
     */
    fun buildWhatsAppNotification(
        tx: HabayebTransaction,
        customer: HabayebCustomer? = null,
        netDebt: BigDecimal? = null,
        currencySymbol: String = "ر.ي",
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String = buildMessage(
        tx = tx,
        customer = customer,
        netDebt = netDebt,
        currencySymbol = currencySymbol,
        allCustomerTxs = allCustomerTxs,
        isRichText = true
    )

    /**
     * Builds an SMS (plain text formatted) notification message.
     */
    fun buildSmsNotification(
        tx: HabayebTransaction,
        customer: HabayebCustomer? = null,
        netDebt: BigDecimal? = null,
        currencySymbol: String = "ر.ي",
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String = buildMessage(
        tx = tx,
        customer = customer,
        netDebt = netDebt,
        currencySymbol = currencySymbol,
        allCustomerTxs = allCustomerTxs,
        isRichText = false
    )

    /**
     * Internal unified message builder.
     */
    private fun buildMessage(
        tx: HabayebTransaction,
        customer: HabayebCustomer?,
        netDebt: BigDecimal?,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction>,
        isRichText: Boolean
    ): String {
        val txType = TransactionType.fromValue(tx.type)
        val normDefaultCurrency = normalizeCurrencySymbol(currencySymbol)

        // 1. Transaction Type Header
        val (headerEmoji, headerTitle, isPaymentOperation) = when (txType) {
            TransactionType.PAYMENT_TO_THEM -> Triple("🟢", "سداد لكم", true)
            TransactionType.PAYMENT_BY_THEM -> Triple("🟢", "استلام منكم", true)
            TransactionType.OWED_BY_THEM -> Triple("🔴", "دين عليكم", false)
            TransactionType.OWED_TO_THEM -> Triple("🔴", "دين لكم", false)
            TransactionType.INCOME -> Triple("🟢", "دخل", true)
            TransactionType.EXPENSE -> Triple("🔴", "مصروف", false)
        }

        // 2. Transaction Amount and Currency
        val isExchange = tx.isForeign && (tx.isRateCalculated || (tx.exchangeRate > BigDecimal.ZERO && tx.equivalentAmount > BigDecimal.ZERO))
        
        val rawSourceCurrency = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) tx.currencyCode else currencySymbol
        val sourceCurrency = normalizeCurrencySymbol(rawSourceCurrency)

        val rawTargetCurrency = if (tx.baseCurrencyCode != "DEFAULT" && tx.baseCurrencyCode.isNotBlank()) tx.baseCurrencyCode else currencySymbol
        val targetCurrency = normalizeCurrencySymbol(rawTargetCurrency)

        val hasActiveConversion = isExchange && sourceCurrency != targetCurrency && tx.exchangeRate > BigDecimal.ZERO

        val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount

        // 3. Resulting Balance & Side calculation
        val (resultingBalance, resultingCurrency) = determineResultingBalance(
            tx = tx,
            customer = customer,
            providedNetDebt = netDebt,
            defaultCurrencySymbol = normDefaultCurrency,
            allCustomerTxs = allCustomerTxs,
            hasActiveConversion = hasActiveConversion,
            targetCurrency = targetCurrency,
            sourceCurrency = sourceCurrency
        )

        // 4. Build output lines in exact order
        val lines = mutableListOf<String>()

        // Line 1: Header
        if (isRichText) {
            lines.add("$headerEmoji *$headerTitle*")
        } else {
            lines.add("$headerEmoji $headerTitle")
        }

        // Line 2: Original Amount
        lines.add("💰 ${formatCurrencyAmount(sourceAmount, sourceCurrency)}")

        // Lines 3 & 4: Exchange Rate & Converted Equivalent (if applicable)
        if (hasActiveConversion) {
            val sourceUnit = if (sourceCurrency == "$" || sourceCurrency == "USD") "1$" else "1 $sourceCurrency"
            val rateFormatted = HabayebMathHelper.formatActiveRateBadge(tx.exchangeRate)
            val rateText = "$sourceUnit = $rateFormatted $targetCurrency"

            if (isRichText) {
                lines.add("💱 *سعر الصرف:* $rateText")
                lines.add("💰 *ما يعادل:* ${formatCurrencyAmount(tx.equivalentAmount, targetCurrency)}")
            } else {
                lines.add("💱 سعر الصرف: $rateText")
                lines.add("💰 ما يعادل: ${formatCurrencyAmount(tx.equivalentAmount, targetCurrency)}")
            }
        }

        // Line 5: Description / Statement (if present and non-blank)
        val cleanDesc = CurrencyConfig.getCleanDetails(tx.description)
        if (cleanDesc.isNotBlank()) {
            lines.add("📝 $cleanDesc")
        }

        // Line 6: Resulting Balance Line
        val resultLine = buildResultLine(
            resultingBalance = resultingBalance,
            resultingCurrency = resultingCurrency,
            isPaymentOperation = isPaymentOperation,
            isRichText = isRichText
        )
        lines.add(resultLine)

        return lines.joinToString("\n")
    }

    /**
     * Determines the resulting balance and its currency.
     */
    private fun determineResultingBalance(
        tx: HabayebTransaction,
        customer: HabayebCustomer?,
        providedNetDebt: BigDecimal?,
        defaultCurrencySymbol: String,
        allCustomerTxs: List<HabayebTransaction>,
        hasActiveConversion: Boolean,
        targetCurrency: String,
        sourceCurrency: String
    ): Pair<BigDecimal, String> {
        val targetCurr = if (hasActiveConversion) targetCurrency else sourceCurrency

        if (allCustomerTxs.isNotEmpty()) {
            val calcResult = CustomerHistoryCalculator.calculate(allCustomerTxs, defaultCurrencySymbol, null)
            val txRunningBal = calcResult.runningBalances[tx.id]
            if (txRunningBal != null) {
                return Pair(txRunningBal, targetCurr)
            }
            if (providedNetDebt != null) {
                return Pair(providedNetDebt, targetCurr)
            }
            val netInCurr = calcResult.netDebtBigDecimalMap[targetCurr]
            if (netInCurr != null) {
                return Pair(netInCurr, targetCurr)
            }
        }

        if (providedNetDebt != null) {
            return Pair(providedNetDebt, targetCurr)
        }

        val txAmount = if (hasActiveConversion) tx.equivalentAmount else (if (tx.foreignAmount > BigDecimal.ZERO) tx.foreignAmount else tx.amount)
        val txType = TransactionType.fromValue(tx.type)
        val standaloneBalance = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> txAmount
            TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> txAmount.negate()
            else -> txAmount
        }

        return Pair(standaloneBalance, targetCurr)
    }

    /**
     * Builds the final result line with smart "لكم / عليكم" or zero balance.
     */
    private fun buildResultLine(
        resultingBalance: BigDecimal,
        resultingCurrency: String,
        isPaymentOperation: Boolean,
        isRichText: Boolean
    ): String {
        val isZero = resultingBalance.compareTo(BigDecimal.ZERO) == 0
        val isDebtAgainst = resultingBalance.compareTo(BigDecimal.ZERO) > 0 // Net debt against customer
        val formattedAmount = formatCurrencyAmount(resultingBalance.abs(), resultingCurrency)

        val prefixWord = if (isPaymentOperation) "المتبقي" else "الإجمالي"

        return when {
            isZero -> {
                if (isRichText) {
                    "◀ *الرصيد:* $formattedAmount"
                } else {
                    "◀ الرصيد: $formattedAmount"
                }
            }
            isDebtAgainst -> {
                if (isRichText) {
                    "◀ *$prefixWord عليكم:* $formattedAmount"
                } else {
                    "◀ $prefixWord عليكم: $formattedAmount"
                }
            }
            else -> { // isDebtFor (balance is in customer's favor)
                if (isRichText) {
                    "◀ *$prefixWord لكم:* $formattedAmount"
                } else {
                    "◀ $prefixWord لكم: $formattedAmount"
                }
            }
        }
    }
}
