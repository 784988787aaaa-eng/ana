package com.smartledger.aldaftar.ui.helper

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object HabayebMathHelper {
    private val numberFormatThreadLocal = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(Locale.US)
    }

    fun toBigDecimal(value: String): BigDecimal {
        return try {
            val clean = value.trim()
            if (clean.isBlank() || clean.equals("null", ignoreCase = true)) BigDecimal.ZERO else BigDecimal(clean)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = a.add(b)
    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = a.subtract(b)
    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = a.multiply(b)
    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return a.divide(b, 10, RoundingMode.HALF_EVEN)
    }

    fun formatSmart(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val rounded = value.setScale(2, RoundingMode.HALF_EVEN)
        val stripped = rounded.stripTrailingZeros()
        val formatter = numberFormatThreadLocal.get()
        if (stripped.scale() <= 0) {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 2
        }
        return formatter.format(stripped)
    }

    fun formatRate(value: BigDecimal): String {
        return try {
            if (value.compareTo(BigDecimal.ZERO) <= 0) return "0"
            if (value >= BigDecimal.ONE) {
                value.setScale(4, RoundingMode.HALF_EVEN)
                    .stripTrailingZeros()
                    .toPlainString()
            } else {
                value.setScale(8, RoundingMode.HALF_EVEN)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        } catch (e: Exception) {
            value.toString()
        }
    }

    /**
     * Formats rate specifically for badges in transaction rows and cards.
     * If rate is < 1 (reciprocal fractional quote like 0.00714286 or 0.00181818),
     * it computes the reciprocal (e.g. 140 or 550) so the badge remains compact,
     * beautiful, single-line, and matches the market quote.
     */
    fun formatActiveRateBadge(value: BigDecimal): String {
        return try {
            if (value.compareTo(BigDecimal.ZERO) <= 0) return "0"
            val effective = if (value < BigDecimal.ONE) {
                runCatching {
                    BigDecimal.ONE.divide(value, 8, RoundingMode.HALF_EVEN)
                }.getOrDefault(value)
            } else {
                value
            }
            val stripped = effective.setScale(4, RoundingMode.HALF_EVEN).stripTrailingZeros()
            if (stripped.scale() <= 0) {
                stripped.toBigInteger().toString()
            } else {
                // Max 2 decimal digits for fractions like 3.75
                stripped.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
            }
        } catch (e: Exception) {
            formatRate(value)
        }
    }
}


