package com.smartledger.aldaftar.data.local

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import java.math.BigDecimal

/**
 * Exact-decimal aggregation for customer balances.
 *
 * Monetary values are persisted as decimal text via BigDecimalConverter. SQLite
 * SUM() coerces that representation to floating-point numeric values, so the
 * balance authority must aggregate the persisted BigDecimal values in Kotlin.
 */
internal fun aggregateCustomerCurrencyBalances(
    transactions: List<HabayebTransaction>,
    defaultCurrencySymbol: String
): List<CustomerCurrencyBalance> {
    data class Accumulator(
        val customerId: String,
        val currencyCode: String,
        var netAmount: BigDecimal = BigDecimal.ZERO,
        var netEquivalentAmount: BigDecimal = BigDecimal.ZERO,
        var lastTimestamp: Long = Long.MIN_VALUE,
        var txCount: Int = 0
    )

    fun effectiveCurrency(tx: HabayebTransaction): String {
        val raw = if (tx.isRateCalculated) tx.baseCurrencyCode else tx.currencyCode
        return raw.takeUnless { it.isBlank() || it == "DEFAULT" } ?: defaultCurrencySymbol
    }

    fun displayAmount(tx: HabayebTransaction): BigDecimal {
        if (tx.isRateCalculated) return tx.equivalentAmount
        if (tx.currencyCode.isBlank() || tx.currencyCode == "DEFAULT") return tx.amount
        return if (tx.isForeign && tx.foreignAmount > BigDecimal.ZERO) {
            tx.foreignAmount
        } else {
            tx.amount
        }
    }

    fun direction(type: String): Int = when (type) {
        "OWED_BY_THEM", "PAYMENT_TO_THEM" -> 1
        "OWED_TO_THEM", "PAYMENT_BY_THEM" -> -1
        else -> 0
    }

    val grouped = linkedMapOf<Pair<String, String>, Accumulator>()
    for (tx in transactions) {
        val key = tx.customerId to effectiveCurrency(tx)
        val accumulator = grouped.getOrPut(key) {
            Accumulator(customerId = tx.customerId, currencyCode = key.second)
        }
        val sign = direction(tx.type)
        if (sign != 0) {
            val signedAmount = displayAmount(tx).multiply(BigDecimal.valueOf(sign.toLong()))
            val signedEquivalent = tx.equivalentAmount.multiply(BigDecimal.valueOf(sign.toLong()))
            accumulator.netAmount = accumulator.netAmount.add(signedAmount)
            accumulator.netEquivalentAmount = accumulator.netEquivalentAmount.add(signedEquivalent)
        }
        accumulator.lastTimestamp = maxOf(accumulator.lastTimestamp, tx.timestamp)
        accumulator.txCount++
    }

    return grouped.values.map {
        CustomerCurrencyBalance(
            customerId = it.customerId,
            currencyCode = it.currencyCode,
            netAmount = it.netAmount,
            netEquivalentAmount = it.netEquivalentAmount,
            lastTimestamp = if (it.lastTimestamp == Long.MIN_VALUE) 0L else it.lastTimestamp,
            txCount = it.txCount
        )
    }
}
