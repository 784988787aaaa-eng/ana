package com.smartledger.aldaftar.ui.helper

import com.smartledger.aldaftar.domain.model.FinancialPolicy
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
        val normalized = FinancialPolicy.normalize(value)
        if (normalized.compareTo(BigDecimal.ZERO) == 0) return "0"
        val stripped = normalized.stripTrailingZeros()
        val formatter = numberFormatThreadLocal.get() ?: NumberFormat.getNumberInstance(Locale.US)
        if (stripped.scale() <= 0) {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = FinancialPolicy.scale
        }
        return formatter.format(stripped)
    }

    fun formatRate(value: BigDecimal): String {
        return try {
            if (value.compareTo(BigDecimal.ZERO) <= 0) return "0"
            val normalized = FinancialPolicy.normalizeRate(value).stripTrailingZeros()
            if (normalized.scale() <= 0) normalized.toBigInteger().toString() else normalized.toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }

    fun normalizeAmountDigits(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        for (i in 0 until input.length) {
            when (val ch = input[i]) {
                ',', '٬', ' ' -> {} // Thousands separator & spaces removed on paste/input
                '٫' -> sb.append('.')
                in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun sanitizeAmountInput(raw: String, maxScale: Int = FinancialPolicy.scale): String? {
        if (raw.isEmpty()) return ""
        val normalized = normalizeAmountDigits(raw)
        if (normalized.isEmpty()) return ""
        val dotCount = normalized.count { it == '.' }
        val isValidChars = normalized.all { it.isDigit() || it == '.' }
        val dotIdx = normalized.indexOf('.')
        val validDecimals = dotIdx == -1 || (normalized.length - dotIdx - 1 <= maxScale)
        if (!isValidChars || dotCount > 1 || !validDecimals) return null

        return if (normalized.startsWith("0") && normalized.length > 1 && normalized[1] != '.') {
            normalized.trimStart('0').ifEmpty { "0" }
        } else if (normalized.startsWith(".")) {
            "0$normalized"
        } else {
            normalized
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


