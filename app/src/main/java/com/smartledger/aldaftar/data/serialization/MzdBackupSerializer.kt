package com.smartledger.aldaftar.data.serialization

import android.content.Context
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.navigation.Screen
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.math.BigDecimal

object MzdBackupSerializer {

    suspend fun exportBackupToJson(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context? = null
    ): String = BackupPayloadSerializer.exportBackupToJson(
        settings, commitments, transactions, habayebCustomers, habayebTransactions, deletedItems, context
    )

    suspend fun exportBackupToFile(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context,
        targetFile: File
    ) {
        val jsonStr = BackupPayloadSerializer.exportBackupToJson(
            settings, commitments, transactions, habayebCustomers, habayebTransactions, deletedItems, context
        )
        val parentDir = targetFile.parentFile ?: targetFile.absoluteFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val tempFile = File.createTempFile("tmp_mzd_", ".tmp", parentDir)
        try {
            tempFile.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(jsonStr)
                writer.flush()
            }
            try {
                Files.move(
                    tempFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (_: Exception) {
                Files.move(
                    tempFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }
    }

    fun getBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): BigDecimal =
    BackupPayloadSerializer.getBigDecimal(obj, key, fallback)

    suspend fun importBackupFromJson(
        jsonString: String,
        context: Context? = null
    ): Triple<AppSettings, List<FixedCommitment>, List<TransactionDb>> =
    BackupPayloadSerializer.importBackupFromJson(jsonString, context)

    fun parseCustomCategories(root: JSONObject): List<CustomCategory> {
        val list = mutableListOf<CustomCategory>()
        if (root.has("custom_categories") && !root.isNull("custom_categories")) {
            val catsArr = root.optJSONArray("custom_categories")
            if (catsArr != null) {
                for (i in 0 until catsArr.length()) {
                    val obj = catsArr.getJSONObject(i)
                    list.add(
                        CustomCategory(
                            name = obj.getString("name"),
                            tabType = obj.optString("tab_type", Screen.HABAYEB.name),
                            iconEmoji = obj.optString("icon_emoji", ""),
                            displayOrder = obj.optInt("display_order", i),
                            isSystemClosed = obj.optBoolean("is_system_closed", false)
                        )
                    )
                }
            }
        }
        return list
    }

    fun parseDeletedItems(root: JSONObject): List<DeletedItemEntity> {
        val list = mutableListOf<DeletedItemEntity>()
        if (root.has("deleted_items") && !root.isNull("deleted_items")) {
            val deletedItemsArr = root.optJSONArray("deleted_items")
            if (deletedItemsArr != null) {
                for (i in 0 until deletedItemsArr.length()) {
                    val obj = deletedItemsArr.getJSONObject(i)
                    list.add(
                        DeletedItemEntity(
                            id = obj.getString("id"),
                            sourceSystem = obj.getString("sourceSystem"),
                            originalTableName = obj.getString("originalTableName"),
                            jsonData = obj.getString("jsonData"),
                            deletedAt = obj.getLong("deletedAt")
                        )
                    )
                }
            }
        }
        return list
    }

    data class RestoredHabayebCustomerData(
        val customer: HabayebCustomer,
        val categoryLink: String?
    )

    fun parseHabayebCustomers(root: JSONObject): List<RestoredHabayebCustomerData> {
        val jsonHabayebObj = root.optJSONObject("habayeb_debts")
        ?: root.optJSONObject("habayeb_debts_db")

        val txArr = jsonHabayebObj?.optJSONArray("debt_transactions")
        ?: jsonHabayebObj?.optJSONArray("habayeb_transactions")

        val customerIdToTxTypes = mutableMapOf<String, MutableSet<String>>()
        if (txArr != null) {
            for (i in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(i)
                val cId = obj.optString("customer_id", obj.optString("customerId", "")).trim()
                val tType = obj.optString("type", "").trim()
                if (cId.isNotEmpty() && tType.isNotEmpty()) {
                    customerIdToTxTypes.getOrPut(cId) { mutableSetOf() }.add(tType)
                }
            }
        }

        val custArr = jsonHabayebObj?.optJSONArray("customers")
        ?: jsonHabayebObj?.optJSONArray("habayeb_customers")

        val result = mutableListOf<RestoredHabayebCustomerData>()
        if (custArr != null) {
            for (i in 0 until custArr.length()) {
                val obj = custArr.getJSONObject(i)
                val cId = obj.optString("id", obj.optString("customer_id", "")).trim()

                val explicitInitialType = when {
                    obj.has("initial_type") && !obj.isNull("initial_type") -> obj.optString("initial_type").trim()
                    obj.has("initialType") && !obj.isNull("initialType") -> obj.optString("initialType").trim()
                    else -> ""
                }

                val determinedInitialType = if (explicitInitialType.isNotBlank()) {

                    explicitInitialType
                } else {

                    val txTypesForCust = customerIdToTxTypes[cId]
                    if (txTypesForCust != null && txTypesForCust.isNotEmpty()) {
                        if (txTypesForCust.contains(TransactionType.OWED_TO_THEM.value) || txTypesForCust.contains(TransactionType.PAYMENT_TO_THEM.value)) {
                            TransactionType.OWED_TO_THEM.value
                        } else {
                            TransactionType.OWED_BY_THEM.value
                        }
                    } else {
                        TransactionType.OWED_BY_THEM.value
                    }
                }

                val cust = HabayebCustomer(
                    id = cId,
                    name = obj.getString("name"),
                    phone = obj.optString("phone", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("created_at", obj.optLong("createdAt", System.currentTimeMillis() / 1000)),
                    initialType = determinedInitialType
                )
                val catLink = obj.optString("category_link", null)?.takeIf { it.isNotBlank() }
                result.add(RestoredHabayebCustomerData(cust, catLink))
            }
        }
        return result
    }

    fun parseHabayebTransactions(root: JSONObject, defaultCurrency: String): List<HabayebTransaction> {
        val list = mutableListOf<HabayebTransaction>()
        val jsonHabayebObj = root.optJSONObject("habayeb_debts")
        ?: root.optJSONObject("habayeb_debts_db")

        val txArr = jsonHabayebObj?.optJSONArray("debt_transactions")
        ?: jsonHabayebObj?.optJSONArray("habayeb_transactions")

        if (txArr != null) {
            for (i in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(i)

                val amount = getBigDecimal(obj, "amount")
                val foreignAmount = getBigDecimal(obj, "foreign_amount", getBigDecimal(obj, "foreignAmount", "0").toPlainString())

                val exchangeRate = getBigDecimal(obj, "exchange_rate", getBigDecimal(obj, "exchangeRate", "1").toPlainString())
                val equivalentAmount = getBigDecimal(obj, "equivalent_amount", getBigDecimal(obj, "equivalentAmount", amount.toPlainString()).toPlainString())
                val isForeign = obj.optBoolean("is_foreign", obj.optBoolean("isForeign", false))
                val currencyCode = obj.optString("currency_code", obj.optString("currencyCode", defaultCurrency))
                val baseCurrencyCode = obj.optString("base_currency_code", obj.optString("baseCurrencyCode", defaultCurrency))
                val isRateCalculated = obj.optBoolean("is_rate_calculated", obj.optBoolean("isRateCalculated", false))

                list.add(
                    HabayebTransaction(
                        id = obj.getString("id"),
                        customerId = obj.optString("customer_id", obj.optString("customerId", "")).trim(),
                        type = obj.getString("type"),
                        amount = amount,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        description = obj.optString("description", ""),
                        linkedMainTxId = obj.optString("linked_main_tx_id", obj.optString("linkedMainTxId", null))?.takeIf {
                            it.isNotBlank() && !it.equals("null", ignoreCase = true) && it != "0"
                        },
                        isForeign = isForeign,
                        currencyCode = currencyCode,
                        foreignAmount = foreignAmount,
                        exchangeRate = exchangeRate,
                        isRateCalculated = isRateCalculated,
                        equivalentAmount = equivalentAmount,
                        baseCurrencyCode = baseCurrencyCode
                    )
                )
            }
        }
        return list
    }
}
