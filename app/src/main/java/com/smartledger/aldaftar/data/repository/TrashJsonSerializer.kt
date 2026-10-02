package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import org.json.JSONArray
import org.json.JSONObject

object TrashJsonSerializer {

    fun serializeHabayebBundle(
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        categoryLink: String?,
        pinnedCategories: Set<Int>
    ): String {
        val pinnedCats = JSONArray(); pinnedCategories.forEach(pinnedCats::put)
        return JSONObject().apply {
            put("customer", JSONObject().apply {
                put("id", customer.id); put("name", customer.name); put("phone", customer.phone)
                put("notes", customer.notes); put("createdAt", customer.createdAt)
                put("initialType", customer.initialType)
                put("categoryId", customer.categoryId ?: JSONObject.NULL)
                categoryLink?.let { put("categoryName", it) }
                if (pinnedCats.length() > 0) put("pinnedScopeCategoryIds", pinnedCats)
            })
            put("transactions", JSONArray().apply { transactions.forEach { put(serializeHabayebTransactionJsonObject(it)) } })
            put("totalTransactions", transactions.size); put("name", customer.name)
        }.toString()
    }

    fun serializeHabayebCustomer(customer: HabayebCustomer): String {
        return JSONObject().apply {
            put("id", customer.id)
            put("name", customer.name)
            put("phone", customer.phone)
            put("notes", customer.notes)
            put("createdAt", customer.createdAt)
            put("initialType", customer.initialType)
            put("categoryId", customer.categoryId ?: JSONObject.NULL)
        }.toString()
    }

    fun serializeHabayebTransaction(tx: HabayebTransaction): String {
        return serializeHabayebTransactionJsonObject(tx).toString()
    }

    private fun serializeHabayebTransactionJsonObject(tx: HabayebTransaction): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("customerId", tx.customerId)
            put("type", tx.type)
            put("amount", tx.amount.toPlainString())
            put("timestamp", tx.timestamp)
            put("description", tx.description)
            put("linkedMainTxId", tx.linkedMainTxId ?: JSONObject.NULL)
            put("is_foreign", tx.isForeign)
            put("currency_code", tx.currencyCode)
            put("foreign_amount", tx.foreignAmount.toPlainString())
            put("exchange_rate", tx.exchangeRate.toPlainString())
            put("is_rate_calculated", tx.isRateCalculated)
            put("equivalent_amount", tx.equivalentAmount.toPlainString())
            put("base_currency_code", tx.baseCurrencyCode)
            put("snapshot_version", tx.snapshotVersion)
            put("rate_context", tx.rateContext)
        }
    }

    fun parseBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): java.math.BigDecimal {
        if (!obj.has(key) || obj.isNull(key)) return java.math.BigDecimal(fallback)
        val valueStr = obj.optString(key, "")
        if (valueStr.isNotBlank() && valueStr != "null") {
            try {
                return java.math.BigDecimal(valueStr.trim())
            } catch (_: Exception) {}
        }
        return java.math.BigDecimal(fallback)
    }

    fun parseHabayebCustomer(custData: JSONObject): HabayebCustomer {
        return HabayebCustomer(
            id = custData.getString("id"),
            name = custData.getString("name"),
            phone = custData.optString("phone", ""),
            notes = custData.optString("notes", ""),
            createdAt = custData.optLong("createdAt", System.currentTimeMillis()),
            initialType = custData.optString("initialType", custData.optString("initial_type", com.smartledger.aldaftar.domain.model.TransactionType.OWED_BY_THEM.value)),
            categoryId = if (custData.has("categoryId") && !custData.isNull("categoryId")) custData.optInt("categoryId") else null
        )
    }

    fun parseHabayebTransaction(txObj: JSONObject): HabayebTransaction {
        val linkedId = if (txObj.has("linkedMainTxId") && !txObj.isNull("linkedMainTxId")) {
            txObj.getString("linkedMainTxId")
        } else null
        return HabayebTransaction(
            id = txObj.getString("id"),
            customerId = txObj.getString("customerId"),
            type = txObj.getString("type"),
            amount = parseBigDecimal(txObj, "amount"),
            timestamp = txObj.optLong("timestamp", System.currentTimeMillis()),
            description = txObj.optString("description", ""),
            linkedMainTxId = linkedId,
            isForeign = txObj.optBoolean("is_foreign", false),
            currencyCode = txObj.optString("currency_code", com.smartledger.aldaftar.domain.model.FinancialPolicy.DEFAULT_CURRENCY_CODE),
            foreignAmount = parseBigDecimal(txObj, "foreign_amount"),
            exchangeRate = parseBigDecimal(txObj, "exchange_rate", "0"),
            isRateCalculated = txObj.optBoolean("is_rate_calculated", false),
            equivalentAmount = parseBigDecimal(txObj, "equivalent_amount"),
            baseCurrencyCode = txObj.optString("base_currency_code", com.smartledger.aldaftar.domain.model.FinancialPolicy.DEFAULT_CURRENCY_CODE),
            snapshotVersion = txObj.optInt("snapshot_version", 1),
            rateContext = txObj.optString("rate_context", "HISTORICAL_SNAPSHOT")
        )
    }
}

