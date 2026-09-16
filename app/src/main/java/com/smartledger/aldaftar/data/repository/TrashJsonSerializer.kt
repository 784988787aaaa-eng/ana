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
        }
    }
}

