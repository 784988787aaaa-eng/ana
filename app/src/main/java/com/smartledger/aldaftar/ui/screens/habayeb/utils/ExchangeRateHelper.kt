package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.domain.model.CurrencyPair
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

object ExchangeRateHelper {
    fun formatApprovedRateForDisplay(
        context: android.content.Context,
        jsonStr: String,
        currencyA: String,
        currencyB: String,
        overrideRate: BigDecimal? = null
    ): String {
        val rateDirect = overrideRate ?: getRateBigDecimal(jsonStr, currencyA, currencyB)
        if (rateDirect.compareTo(BigDecimal.ZERO) <= 0) return ""

        val rateReciprocal = runCatching { BigDecimal.ONE.divide(rateDirect, 12, RoundingMode.HALF_EVEN) }.getOrDefault(BigDecimal.ZERO)

        val (largerCur, smallerCur, displayRate) = if (rateDirect >= BigDecimal.ONE) {
            Triple(currencyA, currencyB, rateDirect)
        } else if (rateReciprocal >= BigDecimal.ONE) {
            Triple(currencyB, currencyA, rateReciprocal)
        } else {
            Triple(currencyA, currencyB, rateDirect)
        }

        val formattedRateStr = com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatSmart(displayRate)
        return context.getString(com.smartledger.aldaftar.R.string.currency_approved_rate_pattern, largerCur, formattedRateStr, smallerCur)
    }

    fun getCurrencyPair(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String): CurrencyPair {
        val rate = getRateBigDecimal(jsonStr, sourceCurrencySymbol, targetCurrencySymbol)
        return CurrencyPair(
            baseCurrency = sourceCurrencySymbol,
            targetCurrency = targetCurrencySymbol,
            rate = rate
        )
    }

    fun setCurrencyPair(jsonStr: String, pair: CurrencyPair): String =
        setRate(jsonStr, pair.baseCurrency, pair.targetCurrency, pair.safeRate)

    /** Returns the direct rate source -> target, or the reciprocal of target -> source. */
    fun getRateBigDecimal(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String): BigDecimal {
        val sourceNorm = CurrencyConfig.getBySymbol(sourceCurrencySymbol)?.symbol ?: sourceCurrencySymbol
        val targetNorm = CurrencyConfig.getBySymbol(targetCurrencySymbol)?.symbol ?: targetCurrencySymbol
        if (sourceNorm == targetNorm) return BigDecimal.ONE.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            if (root.has(sourceNorm) && root.get(sourceNorm) is JSONObject) {
                val sourceObj = root.getJSONObject(sourceNorm)
                if (sourceObj.has(targetNorm)) {
                    val rawVal = sourceObj.opt(targetNorm)
                    val r = if (rawVal is String) rawVal.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO else BigDecimal.ZERO
                    if (r > BigDecimal.ZERO) return r.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }
            }
            if (root.has(targetNorm) && root.get(targetNorm) is JSONObject) {
                val targetObj = root.getJSONObject(targetNorm)
                if (targetObj.has(sourceNorm)) {
                    val rawVal = targetObj.opt(sourceNorm)
                    val reverse = if (rawVal is String) rawVal.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO else BigDecimal.ZERO
                    if (reverse > BigDecimal.ZERO) return BigDecimal.ONE.divide(reverse, com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }
            }
            BigDecimal.ZERO.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        } catch (_: Exception) {
            BigDecimal.ZERO.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        }
    }

    fun getRate(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String): BigDecimal =
        getRateBigDecimal(jsonStr, sourceCurrencySymbol, targetCurrencySymbol)

    fun hasRate(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String): Boolean {
        val sourceNorm = CurrencyConfig.getBySymbol(sourceCurrencySymbol)?.symbol ?: sourceCurrencySymbol
        val targetNorm = CurrencyConfig.getBySymbol(targetCurrencySymbol)?.symbol ?: targetCurrencySymbol
        if (sourceNorm == targetNorm) return true
        return getRateBigDecimal(jsonStr, sourceNorm, targetNorm) > BigDecimal.ZERO
    }

    fun clearRate(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String): String {
        val sourceNorm = CurrencyConfig.getBySymbol(sourceCurrencySymbol)?.symbol ?: sourceCurrencySymbol
        val targetNorm = CurrencyConfig.getBySymbol(targetCurrencySymbol)?.symbol ?: targetCurrencySymbol
        if (sourceNorm == targetNorm) return jsonStr
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            root.optJSONObject(sourceNorm)?.remove(targetNorm)
            root.optJSONObject(targetNorm)?.remove(sourceNorm)
            root.toString()
        } catch (_: Exception) {
            jsonStr
        }
    }

    fun setRate(jsonStr: String, sourceCurrencySymbol: String, targetCurrencySymbol: String, rate: BigDecimal): String {
        val sourceNorm = CurrencyConfig.getBySymbol(sourceCurrencySymbol)?.symbol ?: sourceCurrencySymbol
        val targetNorm = CurrencyConfig.getBySymbol(targetCurrencySymbol)?.symbol ?: targetCurrencySymbol
        if (sourceNorm == targetNorm) return jsonStr
        if (rate.compareTo(BigDecimal.ZERO) <= 0) return jsonStr

        val updatedJson = try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            // The entered rate is authoritative and directional: 1 source = rate target.
            val sourceObj = if (root.has(sourceNorm) && root.get(sourceNorm) is JSONObject) {
                root.getJSONObject(sourceNorm)
            } else {
                JSONObject()
            }
            sourceObj.put(targetNorm, com.smartledger.aldaftar.domain.model.FinancialPolicy.normalizeRate(rate).toPlainString())
            root.put(sourceNorm, sourceObj)
            // One authoritative entry per pair; the reverse is derived mathematically.
            root.optJSONObject(targetNorm)?.remove(sourceNorm)
            root.toString()
        } catch (_: Exception) {
            jsonStr
        }
        return completeMatrix(updatedJson)
    }

    fun completeMatrix(jsonStr: String): String {
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            val symbols = linkedSetOf("ر.ي", "ر.س", "$")
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                symbols.add(key)
                root.optJSONObject(key)?.let { inner ->
                    val innerKeys = inner.keys()
                    while (innerKeys.hasNext()) symbols.add(innerKeys.next())
                }
            }
            // Do not synthesize reverse entries or choose a canonical direction.
            // getRateBigDecimal() derives the reciprocal only when the requested
            // direction is absent.
            for (symbol in symbols) if (!root.has(symbol)) root.put(symbol, JSONObject())
            root.toString()
        } catch (_: Exception) {
            jsonStr
        }
    }

    fun migrateRates(jsonStr: String, oldBase: String, newBase: String): String {
        return jsonStr
    }
}



