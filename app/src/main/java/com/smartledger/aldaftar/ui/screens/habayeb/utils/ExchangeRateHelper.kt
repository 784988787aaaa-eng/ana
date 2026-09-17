package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.domain.model.CurrencyPair
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

object ExchangeRateHelper {
    
    fun getCurrencyPair(jsonStr: String, baseCurrencySymbol: String, foreignCurrencySymbol: String): CurrencyPair {
        val rate = getRateBigDecimal(jsonStr, baseCurrencySymbol, foreignCurrencySymbol)
        return CurrencyPair(
            baseCurrency = baseCurrencySymbol,
            targetCurrency = foreignCurrencySymbol,
            rate = rate
        )
    }

    fun setCurrencyPair(jsonStr: String, pair: CurrencyPair): String {
        return setRate(jsonStr, pair.baseCurrency, pair.targetCurrency, pair.safeRate)
    }

    fun getRateBigDecimal(jsonStr: String, baseCurrencySymbol: String, foreignCurrencySymbol: String): BigDecimal {
        val baseNorm = CurrencyConfig.getBySymbol(baseCurrencySymbol)?.symbol ?: baseCurrencySymbol
        val foreignNorm = CurrencyConfig.getBySymbol(foreignCurrencySymbol)?.symbol ?: foreignCurrencySymbol
        if (baseNorm == foreignNorm) return BigDecimal.ONE.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            if (root.has(baseNorm) && root.get(baseNorm) is JSONObject) {
                val baseObj = root.getJSONObject(baseNorm)
                if (baseObj.has(foreignNorm)) {
                    val rawVal = baseObj.opt(foreignNorm)
                    val r = when (rawVal) {
                        is String -> rawVal.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
                        else -> BigDecimal.ZERO
                    }
                    if (r.compareTo(BigDecimal.ZERO) > 0) return r.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }
            }
            if (root.has(foreignNorm) && root.get(foreignNorm) is JSONObject) {
                val foreignObj = root.getJSONObject(foreignNorm)
                if (foreignObj.has(baseNorm)) {
                    val rawVal = foreignObj.opt(baseNorm)
                    val invR = when (rawVal) {
                        is String -> rawVal.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
                        else -> BigDecimal.ZERO
                    }
                    if (invR.compareTo(BigDecimal.ZERO) > 0) return BigDecimal.ONE.divide(invR, com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN).setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }
            }
            BigDecimal.ZERO.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        } catch (_: Exception) {
            BigDecimal.ZERO.setScale(com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
        }
    }

    fun getRate(jsonStr: String, baseCurrencySymbol: String, foreignCurrencySymbol: String): BigDecimal {
        return getRateBigDecimal(jsonStr, baseCurrencySymbol, foreignCurrencySymbol)
    }

    fun hasRate(jsonStr: String, baseCurrencySymbol: String, foreignCurrencySymbol: String): Boolean {
        val baseNorm = CurrencyConfig.getBySymbol(baseCurrencySymbol)?.symbol ?: baseCurrencySymbol
        val foreignNorm = CurrencyConfig.getBySymbol(foreignCurrencySymbol)?.symbol ?: foreignCurrencySymbol
        if (baseNorm == foreignNorm) return true
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            if (root.has(baseNorm) && root.get(baseNorm) is JSONObject) {
                val baseObj = root.getJSONObject(baseNorm)
                if (baseObj.has(foreignNorm)) {
                    val rate = getRateBigDecimal(jsonStr, baseCurrencySymbol, foreignCurrencySymbol)
                    if (rate.compareTo(BigDecimal.ZERO) > 0) return true
                }
            }
            if (root.has(foreignNorm) && root.get(foreignNorm) is JSONObject) {
                val foreignObj = root.getJSONObject(foreignNorm)
                if (foreignObj.has(baseNorm)) {
                    val rate = getRateBigDecimal(jsonStr, baseCurrencySymbol, foreignCurrencySymbol)
                    if (rate.compareTo(BigDecimal.ZERO) > 0) return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    fun setRate(jsonStr: String, baseCurrencySymbol: String, foreignCurrencySymbol: String, rate: BigDecimal): String {
        val baseNorm = CurrencyConfig.getBySymbol(baseCurrencySymbol)?.symbol ?: baseCurrencySymbol
        val foreignNorm = CurrencyConfig.getBySymbol(foreignCurrencySymbol)?.symbol ?: foreignCurrencySymbol
        if (baseNorm == foreignNorm) return jsonStr
        if (rate.compareTo(BigDecimal.ZERO) <= 0) return jsonStr

        val updatedJson = try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            
            // Persist exactly the direction supplied by the caller. The reverse
            // direction is a mathematical reciprocal and is never used to choose
            // the multiplication/division operation by currency ordering.
            val baseObj = if (root.has(baseNorm) && root.get(baseNorm) is JSONObject) {
                root.getJSONObject(baseNorm)
            } else {
                JSONObject()
            }
            baseObj.put(foreignNorm, com.smartledger.aldaftar.domain.model.FinancialPolicy.normalizeRate(rate).toPlainString())
            root.put(baseNorm, baseObj)
            // Keep one authoritative directed entry per pair. A reverse entry
            // is deliberately removed so it cannot become stale or conflict.
            root.optJSONObject(foreignNorm)?.remove(baseNorm)

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



