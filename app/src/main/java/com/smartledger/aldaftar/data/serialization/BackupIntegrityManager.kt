package com.smartledger.aldaftar.data.serialization

import org.json.JSONObject
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

object BackupIntegrityManager {
    private const val MAX_BACKUP_BYTES = BackupConstants.MAX_BACKUP_BYTES
    private const val SHA_256 = "SHA-256"

    sealed class IntegrityCheckResult {
        data object Valid : IntegrityCheckResult()
        data class Invalid(val reason: String, val cause: Throwable? = null) : IntegrityCheckResult()
    }

    fun calculateSha256Hash(input: String): String =
    MessageDigest.getInstance(SHA_256)
    .digest(input.toByteArray(StandardCharsets.UTF_8))
    .joinToString("") { "%02x".format(Locale.ROOT, it) }

    fun calculateIntegrityHash(data: BackupPayloadData): String {
        val canonical = StringBuilder(4096)
        append(canonical, "settings.currencySymbol", data.settings.currencySymbol)
        append(canonical, "settings.schoolExpensesEnabled", data.settings.schoolExpensesEnabled)
        append(canonical, "settings.exchangeRatesJson", data.settings.exchangeRatesJson)

        data.commitments
        .sortedWith(
            compareBy<FixedCommitment> { it.orderIndex }
            .thenBy { it.name }
            .thenBy { it.targetAmount.toPlainString() }
            .thenBy { it.currentProgress.toPlainString() }
        )
        .forEachIndexed { index, it ->
            append(canonical, "commitments[$index].name", it.name)
            append(canonical, "commitments[$index].targetAmount", it.targetAmount.toPlainString())
            append(canonical, "commitments[$index].currentProgress", it.currentProgress.toPlainString())
            append(canonical, "commitments[$index].orderIndex", it.orderIndex)
        }

        data.transactions
        .sortedWith(
            compareBy<com.smartledger.aldaftar.data.local.entities.TransactionDb> { it.timestamp }
            .thenBy { it.id }
            .thenBy { it.type }
            .thenBy { it.category }
            .thenBy { it.amount.toPlainString() }
            .thenBy { it.description }
        )
        .forEachIndexed { index, it ->
            append(canonical, "transactions[$index].id", it.id)
            append(canonical, "transactions[$index].timestamp", it.timestamp)
            append(canonical, "transactions[$index].type", it.type)
            append(canonical, "transactions[$index].category", it.category)
            append(canonical, "transactions[$index].amount", it.amount.toPlainString())
            append(canonical, "transactions[$index].description", it.description)
        }

        data.habayebCustomers
        .sortedWith(
            compareBy<com.smartledger.aldaftar.data.local.entities.HabayebCustomer> { it.id }
            .thenBy { it.name }
            .thenBy { it.phone }
            .thenBy { it.notes }
            .thenBy { it.createdAt }
            .thenBy { it.initialType }
        )
        .forEachIndexed { index, it ->
            append(canonical, "customers[$index].id", it.id)
            append(canonical, "customers[$index].name", it.name)
            append(canonical, "customers[$index].phone", it.phone)
            append(canonical, "customers[$index].notes", it.notes)
            append(canonical, "customers[$index].createdAt", it.createdAt)
            append(canonical, "customers[$index].initialType", it.initialType)
            append(canonical, "customers[$index].categoryLink", data.categoryLinks[it.id])
        }

        data.habayebTransactions
        .sortedWith(
            compareBy<com.smartledger.aldaftar.data.local.entities.HabayebTransaction> { it.timestamp }
            .thenBy { it.id }
            .thenBy { it.customerId }
            .thenBy { it.type }
            .thenBy { it.amount.toPlainString() }
            .thenBy { it.currencyCode }
            .thenBy { it.foreignAmount.toPlainString() }
            .thenBy { it.exchangeRate.toPlainString() }
        )
        .forEachIndexed { index, it ->
            append(canonical, "habayebTransactions[$index].id", it.id)
            append(canonical, "habayebTransactions[$index].customerId", it.customerId)
            append(canonical, "habayebTransactions[$index].type", it.type)
            append(canonical, "habayebTransactions[$index].amount", it.amount.toPlainString())
            append(canonical, "habayebTransactions[$index].timestamp", it.timestamp)
            append(canonical, "habayebTransactions[$index].description", it.description)
            append(canonical, "habayebTransactions[$index].linkedMainTxId", it.linkedMainTxId)
            append(canonical, "habayebTransactions[$index].isForeign", it.isForeign)
            append(canonical, "habayebTransactions[$index].currencyCode", it.currencyCode)
            append(canonical, "habayebTransactions[$index].foreignAmount", it.foreignAmount.toPlainString())
            append(canonical, "habayebTransactions[$index].exchangeRate", it.exchangeRate.toPlainString())
            append(canonical, "habayebTransactions[$index].isRateCalculated", it.isRateCalculated)
            append(canonical, "habayebTransactions[$index].equivalentAmount", it.equivalentAmount.toPlainString())
            append(canonical, "habayebTransactions[$index].baseCurrencyCode", it.baseCurrencyCode)
        }

        data.deletedItems
        .sortedWith(
            compareBy<com.smartledger.aldaftar.data.local.entities.DeletedItemEntity> { it.id }
            .thenBy { it.sourceSystem }
            .thenBy { it.originalTableName }
            .thenBy { it.deletedAt }
            .thenBy { it.jsonData }
        )
        .forEachIndexed { index, it ->
            append(canonical, "deletedItems[$index].id", it.id)
            append(canonical, "deletedItems[$index].sourceSystem", it.sourceSystem)
            append(canonical, "deletedItems[$index].originalTableName", it.originalTableName)
            append(canonical, "deletedItems[$index].jsonData", it.jsonData)
            append(canonical, "deletedItems[$index].deletedAt", it.deletedAt)
        }

        data.customCategories
        .sortedWith(
            compareBy<com.smartledger.aldaftar.data.local.entities.CustomCategory> { it.displayOrder }
            .thenBy { it.name }
            .thenBy { it.tabType }
            .thenBy { it.iconEmoji }
            .thenBy { it.isSystemClosed }
        )
        .forEachIndexed { index, it ->
            append(canonical, "customCategories[$index].name", it.name)
            append(canonical, "customCategories[$index].tabType", it.tabType)
            append(canonical, "customCategories[$index].iconEmoji", it.iconEmoji)
            append(canonical, "customCategories[$index].displayOrder", it.displayOrder)
            append(canonical, "customCategories[$index].isSystemClosed", it.isSystemClosed)
        }

        data.categoryLinks.toSortedMap().forEach { (key, value) ->
            append(canonical, "categoryLinks[$key]", value)
        }
        data.pinnedCustomerIdsByCategory.toSortedMap().forEach { (key, ids) ->
            append(canonical, "pinned[$key]", ids.sorted())
        }
        append(canonical, "categoryOrderList", data.categoryOrderList)
        append(canonical, "closedCustomName", data.closedCustomName)

        return calculateSha256Hash(canonical.toString())
    }

    fun calculateLegacyIntegrityHash(data: BackupPayloadData): String {
        val sb = StringBuilder()
        sb.append("settings:").append(data.settings.currencySymbol).append("|")
        .append(data.settings.schoolExpensesEnabled).append("|")
        .append(data.settings.exchangeRatesJson).append(";")
        sb.append("commitments:")
        data.commitments.sortedWith(compareBy<FixedCommitment> { it.orderIndex }.thenBy { it.name }).forEach {
            sb.append(it.name).append(",").append(it.targetAmount.toPlainString()).append(",")
            .append(it.currentProgress.toPlainString()).append(",").append(it.orderIndex).append("|")
        }
        sb.append(";")
        sb.append("transactions:")
        data.transactions.sortedWith(compareBy<com.smartledger.aldaftar.data.local.entities.TransactionDb> { it.timestamp }.thenBy { it.id }).forEach {
            sb.append(it.id).append(",").append(it.timestamp).append(",").append(it.type).append(",")
            .append(it.category).append(",").append(it.amount.toPlainString()).append(",")
            .append(it.description).append("|")
        }
        sb.append(";")
        sb.append("customers:")
        data.habayebCustomers.sortedBy { it.id }.forEach {
            sb.append(it.id).append(",").append(it.name).append(",").append(it.phone).append(",")
            .append(it.initialType).append("|")
        }
        sb.append(";")
        sb.append("habayebTx:")
        data.habayebTransactions.sortedWith(compareBy<com.smartledger.aldaftar.data.local.entities.HabayebTransaction> { it.timestamp }.thenBy { it.id }).forEach {
            sb.append(it.id).append(",").append(it.customerId).append(",").append(it.type).append(",")
            .append(it.amount.toPlainString()).append(",").append(it.timestamp).append(",")
            .append(it.currencyCode).append(",").append(it.foreignAmount.toPlainString()).append(",")
            .append(it.linkedMainTxId ?: "").append("|")
        }
        sb.append(";")
        sb.append("deletedItems:")
        data.deletedItems.sortedBy { it.id }.forEach {
            sb.append(it.id).append(",").append(it.sourceSystem).append(",")
            .append(it.originalTableName).append(",").append(it.deletedAt).append("|")
        }
        sb.append(";")
        sb.append("customCategories:")
        data.customCategories.sortedWith(compareBy<com.smartledger.aldaftar.data.local.entities.CustomCategory> { it.displayOrder }.thenBy { it.name }).forEach {
            sb.append(it.name).append(",").append(it.tabType).append(",")
            .append(it.displayOrder).append(",").append(it.isSystemClosed).append("|")
        }
        sb.append(";")
        data.categoryLinks.toSortedMap().forEach { (k, v) ->
            sb.append("catLink:").append(k).append("=").append(v).append(";")
        }
        data.pinnedCustomerIdsByCategory.toSortedMap().forEach { (k, set) ->
            sb.append("pinned:").append(k).append("=").append(set.sorted().joinToString(",")).append(";")
        }
        return calculateSha256Hash(sb.toString())
    }

    fun verifyIntegrity(data: BackupPayloadData, expectedHash: String): Boolean {
        if (!expectedHash.matches(Regex("[0-9a-fA-F]{64}"))) return false
        val calculated = calculateIntegrityHash(data).toByteArray(StandardCharsets.UTF_8)
        val expected = expectedHash.lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(calculated, expected)
    }

    fun verifyLegacyIntegrity(data: BackupPayloadData, expectedHash: String): Boolean {
        if (!expectedHash.matches(Regex("[0-9a-fA-F]{64}"))) return false
        val calculated = calculateLegacyIntegrityHash(data).toByteArray(StandardCharsets.UTF_8)
        val expected = expectedHash.lowercase(Locale.ROOT).toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(calculated, expected)
    }

    fun validateBackupFileIntegrity(file: File): IntegrityCheckResult {
        if (!file.exists()) return IntegrityCheckResult.Invalid("ملف النسخة غير موجود: ${file.name}")
        if (!file.isFile) return IntegrityCheckResult.Invalid("المسار المحدد ليس ملفاً: ${file.name}")
        if (file.length() <= 0L) return IntegrityCheckResult.Invalid("ملف النسخة الاحتياطية فارغ")
        if (file.length() > MAX_BACKUP_BYTES) {
            return IntegrityCheckResult.Invalid("حجم ملف النسخة الاحتياطية يتجاوز الحد المسموح")
        }

        val content = try {
            file.readText(StandardCharsets.UTF_8)
        } catch (e: IOException) {
            return IntegrityCheckResult.Invalid("تعذر قراءة ملف النسخة الاحتياطية", e)
        }

        if (content.isBlank()) return IntegrityCheckResult.Invalid("محتوى النسخة الاحتياطية فارغ")

        val root = try {
            JSONObject(content)
        } catch (e: Exception) {
            return IntegrityCheckResult.Invalid("صيغة النسخة الاحتياطية ليست JSON صالحاً", e)
        }

        val source = if (root.has(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB)) {
            root.optJSONObject(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB)
            ?: return IntegrityCheckResult.Invalid("حاوية النسخة القديمة غير صالحة")
        } else {
            root
        }

        val hasPayload = source.has(BackupConstants.JSON_KEY_SETTINGS) ||
            source.has(BackupConstants.JSON_KEY_FIXED_COMMITMENTS) ||
            source.has(BackupConstants.JSON_KEY_COMMITMENTS) ||
            source.has(BackupConstants.JSON_KEY_TRANSACTIONS) ||
            source.has(BackupConstants.JSON_KEY_HABAYEB_CUSTOMERS) ||
            source.has(BackupConstants.JSON_KEY_HABAYEB_TRANSACTIONS) ||
            source.has(BackupConstants.JSON_KEY_HABAYEB_DEBTS_DB)

        if (!hasPayload) {
            return IntegrityCheckResult.Invalid("بنية النسخة الاحتياطية غير معروفة")
        }
        return IntegrityCheckResult.Valid
    }

    private fun append(builder: StringBuilder, key: String, value: Any?) {
        val encoded = when (value) {
            null -> "<null>"
            is BigDecimal -> value.toPlainString()
            is Iterable<*> -> value.joinToString(",", prefix = "[", postfix = "]") { it.toString() }
            else -> value.toString()
        }
        builder.append(key.length)
            .append(':')
            .append(key)
            .append(encoded.length)
            .append(':')
            .append(encoded)
            .append(';')
    }
}
