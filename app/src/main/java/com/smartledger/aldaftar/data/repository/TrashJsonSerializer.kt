package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

object TrashJsonSerializer {

    fun serializeCommitment(fc: FixedCommitment): String {
        return JSONObject().apply {
            put("name", fc.name)
            put("targetAmount", fc.targetAmount)
            put("currentProgress", fc.currentProgress)
            put("orderIndex", fc.orderIndex)
        }.toString()
    }

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

    fun serializeTransaction(tx: TransactionDb): String {
        return serializeTransactionJsonObject(tx).toString()
    }

    fun serializeTransactionBundle(transactions: List<TransactionDb>, title: String): String {
        return JSONObject().apply {
            val txsArray = JSONArray()
            transactions.forEach { tx ->
                txsArray.put(serializeTransactionJsonObject(tx))
            }
            put("transactions", txsArray)
            put("totalTransactions", transactions.size)
            val totalNet = transactions.fold(BigDecimal.ZERO) { acc, tx ->
                if (tx.type == TransactionType.INCOME.value) acc.add(tx.amount) else acc.subtract(tx.amount)
            }
            put("totalNet", totalNet)
            put("name", title)
        }.toString()
    }

    fun serializeHabayebTransaction(tx: HabayebTransaction): String {
        return serializeHabayebTransactionJsonObject(tx).toString()
    }

    private fun serializeTransactionJsonObject(tx: TransactionDb): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("timestamp", tx.timestamp)
            put("type", tx.type)
            put("category", tx.category)
            put("amount", tx.amount)
            put("description", tx.description)
        }
    }

    private fun serializeHabayebTransactionJsonObject(tx: HabayebTransaction): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("customerId", tx.customerId)
            put("type", tx.type)
            put("amount", tx.amount)
            put("timestamp", tx.timestamp)
            put("description", tx.description)
            put("linkedMainTxId", tx.linkedMainTxId ?: JSONObject.NULL)
            put("is_foreign", tx.isForeign)
            put("currency_code", tx.currencyCode)
            put("foreign_amount", tx.foreignAmount)
            put("exchange_rate", tx.exchangeRate)
            put("is_rate_calculated", tx.isRateCalculated)
            put("equivalent_amount", tx.equivalentAmount)
            put("base_currency_code", tx.baseCurrencyCode)
        }
    }
}

