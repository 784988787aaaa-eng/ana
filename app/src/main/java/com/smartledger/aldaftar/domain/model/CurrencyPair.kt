package com.smartledger.aldaftar.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class CurrencyPair(
    val baseCurrency: String, // rate source currency
    val targetCurrency: String, // rate target currency
    val rate: BigDecimal = BigDecimal.ZERO
) {
    val isValid: Boolean
        get() = rate.compareTo(BigDecimal.ZERO) > 0

    val safeRate: BigDecimal
        get() = rate.takeIf { it > BigDecimal.ZERO }
            ?.setScale(FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
            ?: throw IllegalArgumentException("سعر الصرف غير موجود أو غير صالح")

    val isSelfPair: Boolean
        get() = baseCurrency.trim().equals(targetCurrency.trim(), ignoreCase = true)
}

