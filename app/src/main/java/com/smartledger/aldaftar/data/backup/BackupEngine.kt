package com.smartledger.aldaftar.data.backup

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import android.util.Base64

class BackupEngine(
    private val context: Context,
    private val database: AppDatabase,
    private val crypto: BackupCrypto = BackupCrypto(context),
    private val paths: BackupPathManager = BackupPathManager()
) {
    companion object {
        private const val FORMAT_VERSION = 1
        private const val APP_ID = "SMARTLEDGER"
        private const val MIME = "application/vnd.smartledger.backup"
    }

    suspend fun createAutomatic(): File = create(paths.automaticFile())
    suspend fun createManual(): File = create(paths.manualFile())

    suspend fun create(target: File): File {
        val payload = snapshot().toString().toByteArray(StandardCharsets.UTF_8)
        val hash = sha256(payload)
        val (salt, iv, encrypted) = crypto.encrypt(payload)
        target.parentFile?.mkdirs()
        val envelope = JSONObject()
            .put("formatVersion", FORMAT_VERSION)
            .put("appId", APP_ID)
            .put("mimeType", MIME)
            .put("createdAt", System.currentTimeMillis())
            .put("schemaVersion", 1)
            .put("appVersion", appVersion())
            .put("payloadSha256", hash)
            .put("encryptionVersion", 2)
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            .put("payload", Base64.encodeToString(encrypted, Base64.NO_WRAP))
        val temp = File(target.parentFile, ".${target.name}.tmp")
        temp.writeText(envelope.toString(), Charsets.UTF_8)
        runCatching {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }.recoverCatching {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }.getOrElse { throw IllegalStateException("تعذر حفظ ملف النسخة") }
        return target
    }

    fun validateEnvelope(bytes: ByteArray) {
        val envelope = JSONObject(String(bytes, StandardCharsets.UTF_8))
        require(envelope.optInt("formatVersion", -1) == FORMAT_VERSION) { "إصدار النسخة غير مدعوم" }
        require(envelope.optString("appId") == APP_ID) { "الملف ليس نسخة SMARTLEDGER" }
        require(envelope.optInt("encryptionVersion", -1) == 2) { "إصدار تشفير النسخة غير مدعوم" }
        require(envelope.optString("salt").isNotBlank() && envelope.optString("iv").isNotBlank() && envelope.optString("payload").isNotBlank()) { "غلاف النسخة ناقص" }
    }

    suspend fun restore(file: File): AppSettings {
        require(file.exists() && file.isFile) { "ملف النسخة غير موجود" }
        return restoreBytes(file.readBytes())
    }

    fun recoveryCode(): String = crypto.localRecoveryCode()

    suspend fun restoreBytes(bytes: ByteArray, recoveryCode: String? = null): AppSettings {
        val envelope = JSONObject(String(bytes, StandardCharsets.UTF_8))
        require(envelope.optInt("formatVersion", -1) == FORMAT_VERSION) { "إصدار النسخة غير مدعوم" }
        require(envelope.optString("appId") == APP_ID) { "الملف ليس نسخة SMARTLEDGER" }
        require(envelope.optInt("encryptionVersion", -1) == 2) { "إصدار تشفير النسخة غير مدعوم" }
        val salt = Base64.decode(envelope.getString("salt"), Base64.DEFAULT)
        val iv = Base64.decode(envelope.getString("iv"), Base64.DEFAULT)
        val cipherText = Base64.decode(envelope.getString("payload"), Base64.DEFAULT)
        val payload = crypto.decrypt(salt, iv, cipherText, recoveryCode)
        require(sha256(payload).equals(envelope.getString("payloadSha256"), ignoreCase = true)) { "سلامة النسخة غير صحيحة" }
        val root = JSONObject(String(payload, StandardCharsets.UTF_8))
        validatePayload(root)
        val restored = rootToDatabase(root)
        return restored
    }

    private suspend fun snapshot(): JSONObject {
        val settings = database.settingsDao().getSettingsDirect() ?: AppSettings(isFirstLaunch = false)
        val commitments = database.commitmentDao().allDirect()
        val transactions = database.transactionDao().allDirect()
        val categories = database.customCategoryDao().getAllCustomCategoriesDirect()
        val trash = database.trashDao().getAllDeletedItemsDirect()
        val customers = database.habayebDao().getAllCustomersDirect()
        val habayebTransactions = database.habayebDao().getAllTransactionsDirect()
        val pins = database.habayebDao().getAllPinsDirect()
        val profile = database.businessProfileDao().get() ?: BusinessProfile()
        val recurring = database.recurringConfigDao().all()
        return JSONObject().apply {
            put("settings", settingsJson(settings))
            put("commitments", JSONArray(commitments.map(::commitmentJson)))
            put("transactions", JSONArray(transactions.map(::transactionJson)))
            put("categories", JSONArray(categories.map(::categoryJson)))
            put("trash", JSONArray(trash.map(::trashJson)))
            put("customers", JSONArray(customers.map(::customerJson)))
            put("habayebTransactions", JSONArray(habayebTransactions.map(::habayebTransactionJson)))
            put("pins", JSONArray(pins.map(::pinJson)))
            put("businessProfile", profileJson(profile))
            put("recurring", JSONArray(recurring.map(::recurringJson)))
        }
    }

    private suspend fun rootToDatabase(root: JSONObject): AppSettings {
        val importedSettings = parseSettings(root.getJSONObject("settings"))
        val currentSettings = database.settingsDao().getSettingsDirect() ?: AppSettings(isFirstLaunch = false)
        val settings = importedSettings.copy(
            id = 1,
            isPasscodeEnabled = currentSettings.isPasscodeEnabled,
            passcodeHash = currentSettings.passcodeHash,
            recoveryPhraseHash = currentSettings.recoveryPhraseHash,
            recoveryHint = currentSettings.recoveryHint
        )
        val commitments = root.array("commitments").map(::parseCommitment)
        val transactions = root.array("transactions").map(::parseTransaction)
        val categories = root.array("categories").map(::parseCategory)
        val trash = root.array("trash").map(::parseTrash)
        val customers = root.array("customers").map(::parseCustomer)
        val habayebTransactions = root.array("habayebTransactions").map(::parseHabayebTransaction)
        val pins = root.array("pins").map(::parsePin)
        val profile = parseProfile(root.getJSONObject("businessProfile"))
        val recurring = root.array("recurring").map(::parseRecurring)
        database.withTransaction {
            database.recurringConfigDao().clear()
            database.habayebDao().clearAllTransactions()
            database.habayebDao().clearAllPins()
            database.habayebDao().clearAllCustomers()
            database.trashDao().clearAllDeletedItems()
            database.transactionDao().clearAllTransactions()
            database.commitmentDao().clearAllCommitments()
            database.customCategoryDao().clearAllCustomCategories()
            database.settingsDao().insertOrUpdateSettings(settings.copy(id = 1))
            database.businessProfileDao().save(profile.copy(id = 1))
            for (item in commitments) database.commitmentDao().insertCommitment(item)
            for (item in categories) database.customCategoryDao().insertCategory(item)
            for (item in transactions) database.transactionDao().insertTransaction(item)
            for (item in customers) database.habayebDao().insertCustomer(item)
            for (item in habayebTransactions) database.habayebDao().insertTransaction(item)
            for (item in pins) database.habayebDao().insertPinnedCustomer(item)
            for (item in recurring) database.recurringConfigDao().save(item)
            for (item in trash) database.trashDao().insertDeletedItem(item)
        }
        return settings
    }

    private fun validatePayload(root: JSONObject) {
        val required = listOf("settings", "commitments", "transactions", "categories", "trash", "customers", "habayebTransactions", "pins", "businessProfile", "recurring")
        required.forEach { require(root.has(it)) { "النسخة ناقصة: $it" } }
    }

    private fun appVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: PackageManager.NameNotFoundException) { "1.0" }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun JSONObject.array(name: String) = getJSONArray(name).let { array -> (0 until array.length()).map { array.getJSONObject(it) } }

    private fun settingsJson(v: AppSettings) = JSONObject().apply {
        put("id", v.id)
        put("currencySymbol", v.currencySymbol)
        put("schoolExpensesEnabled", v.schoolExpensesEnabled)
        put("themeMode", v.themeMode)
        put("doubleCheckExit", v.doubleCheckExit)
        put("isFirstLaunch", v.isFirstLaunch)
        put("onboardingShown", v.onboardingShown)
        put("trashAutoCleanupPeriod", v.trashAutoCleanupPeriod)
        put("exchangeRatesJson", v.exchangeRatesJson)
    }
    private fun commitmentJson(v: FixedCommitment) = JSONObject().apply { put("name", v.name); put("targetAmount", v.targetAmount.toPlainString()); put("currentProgress", v.currentProgress.toPlainString()); put("orderIndex", v.orderIndex) }
    private fun transactionJson(v: TransactionDb) = JSONObject().apply { put("id", v.id); put("timestamp", v.timestamp); put("type", v.type); put("category", v.category); put("amount", v.amount.toPlainString()); put("description", v.description) }
    private fun categoryJson(v: CustomCategory) = JSONObject().apply { put("id", v.id); put("name", v.name); put("tabType", v.tabType); put("iconEmoji", v.iconEmoji); put("displayOrder", v.displayOrder); put("isSystemClosed", v.isSystemClosed) }
    private fun trashJson(v: DeletedItemEntity) = JSONObject().apply { put("id", v.id); put("sourceSystem", v.sourceSystem); put("originalTableName", v.originalTableName); put("jsonData", v.jsonData); put("deletedAt", v.deletedAt) }
    private fun customerJson(v: HabayebCustomer) = JSONObject().apply { put("id", v.id); put("name", v.name); put("phone", v.phone); put("notes", v.notes); put("createdAt", v.createdAt); put("initialType", v.initialType); put("categoryId", v.categoryId) }
    private fun habayebTransactionJson(v: HabayebTransaction) = JSONObject().apply { put("id", v.id); put("customerId", v.customerId); put("type", v.type); put("amount", v.amount.toPlainString()); put("timestamp", v.timestamp); put("description", v.description); put("linkedMainTxId", v.linkedMainTxId); put("isForeign", v.isForeign); put("currencyCode", v.currencyCode); put("foreignAmount", v.foreignAmount.toPlainString()); put("exchangeRate", v.exchangeRate.toPlainString()); put("isRateCalculated", v.isRateCalculated); put("equivalentAmount", v.equivalentAmount.toPlainString()); put("baseCurrencyCode", v.baseCurrencyCode) }
    private fun pinJson(v: PinnedCustomer) = JSONObject().apply { put("scopeCategoryId", v.scopeCategoryId); put("customerId", v.customerId) }
    private fun profileJson(v: BusinessProfile) = JSONObject().apply { put("id", v.id); put("name", v.name); put("description", v.description); put("logoPath", v.logoPath); put("phones", JSONArray(v.phones)) }
    private fun recurringJson(v: RecurringConfigEntity) = JSONObject().apply { put("id", v.id); put("originalTxId", v.originalTxId); put("customerId", v.customerId); put("customerName", v.customerName); put("amount", v.amount.toPlainString()); put("type", v.type); put("description", v.description); put("frequency", v.frequency); put("daysOfWeek", JSONArray(v.daysOfWeek)); put("daysOfMonth", JSONArray(v.daysOfMonth)); put("timeHour", v.timeHour); put("timeMinute", v.timeMinute); put("startDateMillis", v.startDateMillis); put("endDateMillis", v.endDateMillis); put("lastExecutedTimestamp", v.lastExecutedTimestamp); put("isActive", v.isActive); put("isForeign", v.isForeign); put("currencyCode", v.currencyCode); put("foreignAmount", v.foreignAmount.toPlainString()); put("exchangeRate", v.exchangeRate.toPlainString()); put("isRateCalculated", v.isRateCalculated); put("equivalentAmount", v.equivalentAmount.toPlainString()) }

    private fun parseSettings(o: JSONObject) = AppSettings(1, o.optString("currencySymbol", "ر.ي"), o.optBoolean("schoolExpensesEnabled", true), o.optInt("themeMode"), o.optBoolean("doubleCheckExit", true), o.optBoolean("isPasscodeEnabled"), o.optStringOrNull("passcodeHash"), o.optStringOrNull("recoveryPhraseHash"), o.optStringOrNull("recoveryHint"), o.optBoolean("isFirstLaunch", false), o.optBoolean("onboardingShown"), o.optString("trashAutoCleanupPeriod", "NEVER"), o.optString("exchangeRatesJson", "{}"))
    private fun parseCommitment(o: JSONObject) = FixedCommitment(o.getString("name"), o.getString("targetAmount").toBigDecimal(), o.getString("currentProgress").toBigDecimal(), o.getInt("orderIndex"))
    private fun parseTransaction(o: JSONObject) = TransactionDb(o.getString("id"), o.getLong("timestamp"), o.getString("type"), o.getString("category"), o.getString("amount").toBigDecimal(), o.getString("description"))
    private fun parseCategory(o: JSONObject) = CustomCategory(o.getInt("id"), o.getString("name"), o.getString("tabType"), o.getString("iconEmoji"), o.getInt("displayOrder"), o.getBoolean("isSystemClosed"))
    private fun parseTrash(o: JSONObject) = DeletedItemEntity(o.getString("id"), o.getString("sourceSystem"), o.getString("originalTableName"), o.getString("jsonData"), o.getLong("deletedAt"))
    private fun parseCustomer(o: JSONObject) = HabayebCustomer(o.getString("id"), o.getString("name"), o.getString("phone"), o.getString("notes"), o.getLong("createdAt"), o.optString("initialType", "OWED_BY_THEM"), if (o.isNull("categoryId")) null else o.getInt("categoryId"))
    private fun parseHabayebTransaction(o: JSONObject) = HabayebTransaction(o.getString("id"), o.getString("customerId"), o.getString("type"), o.getString("amount").toBigDecimal(), o.getLong("timestamp"), o.getString("description"), o.optStringOrNull("linkedMainTxId"), o.optBoolean("isForeign"), o.optString("currencyCode", "DEFAULT"), o.getString("foreignAmount").toBigDecimal(), o.getString("exchangeRate").toBigDecimal(), o.optBoolean("isRateCalculated"), o.getString("equivalentAmount").toBigDecimal(), o.optString("baseCurrencyCode", "DEFAULT"))
    private fun parsePin(o: JSONObject) = PinnedCustomer(o.getInt("scopeCategoryId"), o.getString("customerId"))
    private fun parseProfile(o: JSONObject) = BusinessProfile(1, o.optString("name"), o.optString("description"), o.optString("logoPath"), o.getJSONArray("phones").let { (0 until it.length()).map(it::getString) })
    private fun parseRecurring(o: JSONObject) = RecurringConfigEntity(o.getString("id"), o.getString("originalTxId"), o.getString("customerId"), o.getString("customerName"), o.getString("amount").toBigDecimal(), o.getString("type"), o.getString("description"), o.getString("frequency"), o.getJSONArray("daysOfWeek").ints(), o.getJSONArray("daysOfMonth").ints(), o.getInt("timeHour"), o.getInt("timeMinute"), o.getLong("startDateMillis"), o.getLong("endDateMillis"), o.getLong("lastExecutedTimestamp"), o.optBoolean("isActive", true), o.optBoolean("isForeign"), o.optString("currencyCode", "DEFAULT"), o.getString("foreignAmount").toBigDecimal(), o.getString("exchangeRate").toBigDecimal(), o.optBoolean("isRateCalculated"), o.getString("equivalentAmount").toBigDecimal())

    private fun JSONArray.ints() = (0 until length()).map { getInt(it) }
    private fun JSONObject.optStringOrNull(name: String): String? = if (isNull(name)) null else optString(name).takeIf(String::isNotBlank)
}
