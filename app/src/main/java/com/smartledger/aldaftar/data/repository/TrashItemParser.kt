package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import java.math.BigDecimal
import org.json.JSONObject

/** Persistence-only JSON parsing used by Habayeb restore operations. */
object TrashItemParser {
    private const val DEFAULT_CURRENCY_CODE = "DEFAULT"
    fun parseBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): BigDecimal {
        if (!obj.has(key)) return BigDecimal(fallback)
        val value = obj.optString(key, "")
        if (value.isBlank() || value == "null") return BigDecimal.ZERO
        return value.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    fun parseHabayebCustomer(obj: JSONObject): HabayebCustomer = HabayebCustomer(
        id = obj.getString("id"),
        name = obj.getString("name"),
        phone = obj.optString("phone", ""),
        notes = obj.optString("notes", ""),
        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
        initialType = obj.optString(
            "initialType",
            obj.optString("initial_type", TransactionType.OWED_BY_THEM.value)
        ),
        categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.optInt("categoryId") else null
    )

    fun parseHabayebTransaction(obj: JSONObject): HabayebTransaction = HabayebTransaction(
        id = obj.getString("id"),
        customerId = obj.getString("customerId"),
        type = obj.getString("type"),
        amount = parseBigDecimal(obj, "amount"),
        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
        description = obj.optString("description", ""),
        linkedMainTxId = obj.optString("linkedMainTxId", "").takeIf { it.isNotBlank() && it != "null" },
        isForeign = obj.optBoolean("is_foreign", false),
        currencyCode = obj.optString("currency_code", DEFAULT_CURRENCY_CODE),
        foreignAmount = parseBigDecimal(obj, "foreign_amount"),
        exchangeRate = parseBigDecimal(obj, "exchange_rate", "1"),
        isRateCalculated = obj.optBoolean("is_rate_calculated", false),
        equivalentAmount = parseBigDecimal(obj, "equivalent_amount"),
        baseCurrencyCode = obj.optString("base_currency_code", DEFAULT_CURRENCY_CODE)
    )

    fun parseTransactionDb(obj: JSONObject): TransactionDb = TransactionDb(
        id = obj.getString("id"),
        timestamp = obj.optLong("timestamp", System.currentTimeMillis() / 1000),
        type = obj.getString("type"),
        category = obj.optString("category", ""),
        amount = parseBigDecimal(obj, "amount"),
        description = obj.optString("description", "")
    )
}
