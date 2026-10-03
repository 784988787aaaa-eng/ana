package com.smartledger.aldaftar.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/** سياسة موحدة لدقة القيم المالية والثوابت الحسابية. */
object FinancialPolicy {
    const val scale: Int = 4
    const val rateScale: Int = 12
    val rounding: RoundingMode = RoundingMode.HALF_EVEN

    const val DEFAULT_CURRENCY_CODE: String = "DEFAULT"
    const val FALLBACK_CURRENCY_SYMBOL: String = "ر.ي"
    const val CATEGORY_CLOSED: String = "CLOSED"
    const val TYPE_OWED_TO_THEM: String = "OWED_TO_THEM"
    const val TYPE_OWED_BY_THEM: String = "OWED_BY_THEM"

    fun normalize(value: BigDecimal): BigDecimal = value.setScale(scale, rounding)
    fun normalizeRate(value: BigDecimal): BigDecimal = value.setScale(rateScale, rounding)

    fun numericallyEquals(first: BigDecimal, second: BigDecimal): Boolean =
        first.compareTo(second) == 0

    fun canonicalCurrencySymbol(symbolOrCode: String): String {
        val trimmed = symbolOrCode.trim()
        return when (trimmed.uppercase()) {
            "YER", "ر.ي" -> "ر.ي"
            "SAR", "ر.س" -> "ر.س"
            "USD", "$" -> "$"
            else -> trimmed
        }
    }

    fun validateTransactionSnapshot(
        currencyCode: String,
        baseCurrencyCode: String,
        isRateCalculated: Boolean,
        exchangeRate: BigDecimal,
        amount: BigDecimal,
        foreignAmount: BigDecimal,
        equivalentAmount: BigDecimal
    ) {
        require(amount >= BigDecimal.ZERO) { "مبلغ المعاملة لا يمكن أن يكون سالباً" }
        require(foreignAmount >= BigDecimal.ZERO) { "المبلغ الأجنبي لا يمكن أن يكون سالباً" }
        require(equivalentAmount >= BigDecimal.ZERO) { "المبلغ المعادل لا يمكن أن يكون سالباً" }
        if (isRateCalculated) {
            val canonicalSource = canonicalCurrencySymbol(currencyCode)
            val canonicalTarget = canonicalCurrencySymbol(baseCurrencyCode)
            require(canonicalSource.isNotBlank() && canonicalSource != DEFAULT_CURRENCY_CODE) {
                "عملة المعاملة الأصلية مطلوبة عند الصرف"
            }
            require(canonicalTarget.isNotBlank() && canonicalTarget != DEFAULT_CURRENCY_CODE) {
                "عملة الأساس مطلوبة عند الصرف"
            }
            require(canonicalSource != canonicalTarget) {
                "عملة المصدر لا يمكن أن تطابق عملة الهدف في معاملة مصروفة"
            }
            require(exchangeRate > BigDecimal.ZERO) {
                "سعر الصرف غير موجود أو غير صالح"
            }
            require(equivalentAmount > BigDecimal.ZERO || (amount == BigDecimal.ZERO && foreignAmount == BigDecimal.ZERO)) {
                "المبلغ المعادل مطلوب لمعاملة مصروفة"
            }
        }
    }

    fun convertDirectedAmount(
        amount: BigDecimal,
        sourceCurrency: String,
        targetCurrency: String,
        rate: BigDecimal,
        rateSourceCurrency: String,
        rateTargetCurrency: String
    ): BigDecimal {
        val source = canonicalCurrencySymbol(sourceCurrency)
        val target = canonicalCurrencySymbol(targetCurrency)
        val rateSource = canonicalCurrencySymbol(rateSourceCurrency)
        val rateTarget = canonicalCurrencySymbol(rateTargetCurrency)
        if (source == target) return amount.setScale(scale, rounding)
        require(rate > BigDecimal.ZERO) { "سعر الصرف غير موجود أو غير صالح" }
        val normalizedRate = normalizeRate(rate)
        return when {
            source == rateSource && target == rateTarget ->
                amount.multiply(normalizedRate).setScale(scale, rounding)
            source == rateTarget && target == rateSource ->
                amount.divide(normalizedRate, scale, rounding)
            else -> throw IllegalArgumentException("اتجاه سعر الصرف لا يطابق العملات المطلوبة")
        }
    }
}
