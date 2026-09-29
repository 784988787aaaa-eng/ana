package com.smartledger.aldaftar.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/** سياسة موحدة لدقة القيم المالية. */
object FinancialPolicy {
    const val scale: Int = 4
    const val rateScale: Int = 12
    val rounding: RoundingMode = RoundingMode.HALF_EVEN

    fun normalize(value: BigDecimal): BigDecimal = value.setScale(scale, rounding)
    fun normalizeRate(value: BigDecimal): BigDecimal = value.setScale(rateScale, rounding)

    fun numericallyEquals(first: BigDecimal, second: BigDecimal): Boolean =
        first.compareTo(second) == 0
}
