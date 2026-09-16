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
        if (baseNorm == foreignNorm) return BigDecimal.ONE.setScale(4, RoundingMode.HALF_EVEN)
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            if (root.has(baseNorm) && root.get(baseNorm) is JSONObject) {
                val baseObj = root.getJSONObject(baseNorm)
                if (baseObj.has(foreignNorm)) {
                    val rawVal = baseObj.opt(foreignNorm)
                    val r = when (rawVal) {
                        is Number -> BigDecimal(rawVal.toString())
                        is String -> if (rawVal.isNotBlank()) BigDecimal(rawVal.trim()) else BigDecimal.ZERO
                        else -> BigDecimal.ZERO
                    }
                    if (r.compareTo(BigDecimal.ZERO) > 0) return r.setScale(4, RoundingMode.HALF_EVEN)
                }
            }
            if (root.has(foreignNorm) && root.get(foreignNorm) is JSONObject) {
                val foreignObj = root.getJSONObject(foreignNorm)
                if (foreignObj.has(baseNorm)) {
                    val rawVal = foreignObj.opt(baseNorm)
                    val invR = when (rawVal) {
                        is Number -> BigDecimal(rawVal.toString())
                        is String -> if (rawVal.isNotBlank()) BigDecimal(rawVal.trim()) else BigDecimal.ZERO
                        else -> BigDecimal.ZERO
                    }
                    if (invR.compareTo(BigDecimal.ZERO) > 0) return invR.setScale(4, RoundingMode.HALF_EVEN)
                }
            }
            BigDecimal.ONE.setScale(4, RoundingMode.HALF_EVEN)
        } catch (_: Exception) {
            BigDecimal.ONE.setScale(4, RoundingMode.HALF_EVEN)
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
            
            val baseObj = if (root.has(baseNorm) && root.get(baseNorm) is JSONObject) {
                root.getJSONObject(baseNorm)
            } else {
                JSONObject()
            }
            val rateBD = rate.setScale(4, RoundingMode.HALF_EVEN)
            baseObj.put(foreignNorm, rateBD.toPlainString())
            root.put(baseNorm, baseObj)
            
            val foreignObj = if (root.has(foreignNorm) && root.get(foreignNorm) is JSONObject) {
                root.getJSONObject(foreignNorm)
            } else {
                JSONObject()
            }
            foreignObj.put(baseNorm, rateBD.toPlainString())
            root.put(foreignNorm, foreignObj)
            
            root.toString()
        } catch (_: Exception) {
            jsonStr
        }
        return completeMatrix(updatedJson)
    }

    fun completeMatrix(jsonStr: String): String {
        return try {
            val root = JSONObject(if (jsonStr.isBlank()) "{}" else jsonStr)
            val symbolsSet = linkedSetOf("ر.ي", "ر.س", "$")
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                symbolsSet.add(key)
                root.optJSONObject(key)?.let { inner ->
                    val innerKeys = inner.keys()
                    while (innerKeys.hasNext()) symbolsSet.add(innerKeys.next())
                }
            }
            val symbols = symbolsSet.toList()

            fun readRate(src: String, dst: String): BigDecimal? {
                val value = root.optJSONObject(src)?.opt(dst) ?: return null
                val rate = when (value) {
                    is Number -> value.toString().toBigDecimalOrNull()
                    is String -> value.trim().toBigDecimalOrNull()
                    else -> null
                } ?: return null
                return rate.takeIf { it > BigDecimal.ZERO }
            }

            fun canonicalBase(a: String, b: String): String {
                val rankA = CurrencyConfig.getCurrencyRank(a)
                val rankB = CurrencyConfig.getCurrencyRank(b)
                return when {
                    rankA != rankB -> if (rankA < rankB) a else b
                    else -> minOf(a, b)
                }
            }

            // A stored pair is defined as: one unit of the canonical base currency
            // equals `rate` units of the target currency. The reverse direction is
            // therefore the exact reciprocal, never the same rate.
            for (i in symbols.indices) {
                for (j in i + 1 until symbols.size) {
                    val a = symbols[i]
                    val b = symbols[j]
                    val base = canonicalBase(a, b)
                    val target = if (base == a) b else a

                    val direct = readRate(base, target)
                    val reverse = readRate(target, base)
                    val canonicalRate = when {
                        direct != null -> direct
                        reverse != null -> BigDecimal.ONE.divide(reverse, 12, RoundingMode.HALF_EVEN)
                        else -> null
                    } ?: continue

                    if (canonicalRate <= BigDecimal.ZERO) continue
                    val normalized = canonicalRate.setScale(4, RoundingMode.HALF_EVEN)
                    root.optJSONObject(base)?.apply {
                        put(target, normalized.toPlainString())
                    } ?: JSONObject().also {
                        it.put(target, normalized.toPlainString())
                        root.put(base, it)
                    }

                    val inverse = BigDecimal.ONE.divide(normalized, 12, RoundingMode.HALF_EVEN)
                        .setScale(4, RoundingMode.HALF_EVEN)
                    root.optJSONObject(target)?.apply {
                        put(base, inverse.toPlainString())
                    } ?: JSONObject().also {
                        it.put(base, inverse.toPlainString())
                        root.put(target, it)
                    }
                }
            }

            for (symbol in symbols) {
                if (!root.has(symbol)) root.put(symbol, JSONObject())
            }
            root.toString()
        } catch (_: Exception) {
            jsonStr
        }
    }

    fun migrateRates(jsonStr: String, oldBase: String, newBase: String): String {
        return jsonStr
    }
}



