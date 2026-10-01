package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import org.json.JSONArray
import org.json.JSONObject

object TrashJsonSerializer {

    fun serializeHabayebBundle(
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        categoryLink: String?,
        pinnedCategories: Set<Int>,
        recurringConfigs: List<RecurringConfigEntity> = emptyList()
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
            put("recurringConfigs", JSONArray().apply { recurringConfigs.forEach { put(serializeRecurringConfigJsonObject(it)) } })
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

    fun serializeHabayebTransaction(tx: HabayebTransaction, recurringConfig: RecurringConfigEntity? = null): String {
        return JSONObject(serializeHabayebTransactionJsonObject(tx).toString()).apply {
            recurringConfig?.let { put("recurringConfig", serializeRecurringConfigJsonObject(it)) }
        }.toString()
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

    private fun serializeRecurringConfigJsonObject(config: RecurringConfigEntity): JSONObject {
        return JSONObject().apply {
            put("id", config.id)
            put("originalTxId", config.originalTxId)
            put("customerId", config.customerId)
            put("customerName", config.customerName)
            put("amount", config.amount.toPlainString())
            put("type", config.type)
            put("description", config.description)
            put("frequency", config.frequency)
            put("daysOfWeek", JSONArray().apply { config.daysOfWeek.forEach(::put) })
            put("daysOfMonth", JSONArray().apply { config.daysOfMonth.forEach(::put) })
            put("timeHour", config.timeHour)
            put("timeMinute", config.timeMinute)
            put("startDateMillis", config.startDateMillis)
            put("endDateMillis", config.endDateMillis)
            put("lastExecutedTimestamp", config.lastExecutedTimestamp)
            put("isActive", config.isActive)
            put("isForeign", config.isForeign)
            put("currencyCode", config.currencyCode)
            put("foreignAmount", config.foreignAmount.toPlainString())
            put("exchangeRate", config.exchangeRate.toPlainString())
            put("isRateCalculated", config.isRateCalculated)
            put("equivalentAmount", config.equivalentAmount.toPlainString())
            put("baseCurrencyCode", config.baseCurrencyCode)
            put("snapshotVersion", config.snapshotVersion)
            put("rateContext", config.rateContext)
        }
    }
}

