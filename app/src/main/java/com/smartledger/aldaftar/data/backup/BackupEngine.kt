package com.smartledger.aldaftar.data.backup

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import java.math.BigDecimal

class BackupEngine(
    private val context: Context,
    private val database: AppDatabase,
    private val crypto: BackupCrypto = BackupCrypto(context),
    private val paths: BackupPathManager = BackupPathManager(context)
) {
    companion object {
        private const val FORMAT_VERSION = 1
        private const val APP_ID = "SMARTLEDGER"
        private const val MIME = "application/vnd.smartledger.backup"
        private const val CURRENT_SCHEMA_VERSION = 1
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
            .put("schemaVersion", CURRENT_SCHEMA_VERSION)
            .put("appVersion", appVersion())
            .put("payloadSha256", hash)
            .put("encryptionVersion", 2)
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            .put("payload", Base64.encodeToString(encrypted, Base64.NO_WRAP))
        val temp = File(target.parentFile, ".${target.name}.tmp")
        temp.writeText(envelope.toString(), Charsets.UTF_8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                Files.move(
                    temp.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            }.recoverCatching {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse { throw IllegalStateException("تعذر حفظ ملف النسخة", it) }
        } else {
            if (target.exists() && !target.delete()) {
                throw IllegalStateException("تعذر استبدال ملف النسخة")
            }
            if (!temp.renameTo(target)) {
                throw IllegalStateException("تعذر حفظ ملف النسخة")
            }
        }
        return target
    }

    fun validateEnvelope(bytes: ByteArray) {
        val envelope = JSONObject(String(bytes, StandardCharsets.UTF_8))
        require(envelope.optInt("formatVersion", -1) == FORMAT_VERSION) { "إصدار النسخة غير مدعوم" }
        require(envelope.optString("appId") == APP_ID) { "الملف ليس نسخة SMARTLEDGER" }
        require(envelope.optInt("encryptionVersion", -1) == 2) { "إصدار تشفير النسخة غير مدعوم" }
        require(envelope.optString("mimeType") == MIME) { "نوع ملف النسخة غير مدعوم" }
        require(envelope.optInt("schemaVersion", -1) == CURRENT_SCHEMA_VERSION) { "مخطط النسخة غير مدعوم" }
        require(envelope.optString("payloadSha256").matches(Regex("[0-9a-fA-F]{64}"))) { "بصمة النسخة ناقصة أو غير صالحة" }
        require(envelope.optString("salt").isNotBlank() && envelope.optString("iv").isNotBlank() && envelope.optString("payload").isNotBlank()) { "غلاف النسخة ناقص" }
    }

    suspend fun restore(file: File): AppSettings {
        require(file.exists() && file.isFile) { "ملف النسخة غير موجود" }
        return restoreBytes(file.readBytes())
    }

    fun recoveryCode(): String = crypto.localRecoveryCode()

    suspend fun restoreBytes(bytes: ByteArray, recoveryCode: String? = null): AppSettings {
        validateEnvelope(bytes)
        val envelope = JSONObject(String(bytes, StandardCharsets.UTF_8))
        val salt = Base64.decode(envelope.getString("salt"), Base64.DEFAULT)
        val iv = Base64.decode(envelope.getString("iv"), Base64.DEFAULT)
        val cipherText = Base64.decode(envelope.getString("payload"), Base64.DEFAULT)
        val payload = crypto.decrypt(salt, iv, cipherText, recoveryCode)
        require(sha256(payload).equals(envelope.getString("payloadSha256"), ignoreCase = true)) { "سلامة النسخة غير صحيحة" }
        val root = JSONObject(String(payload, StandardCharsets.UTF_8))
        validatePayload(root)
        // Protect the currently installed state before any destructive restore.
        // The backup source is already in memory, so creating the safety copy
        // cannot overwrite the source file being restored.
        createAutomatic()
        val restored = rootToDatabase(root)
        return restored
    }

    private suspend fun snapshot(): JSONObject = database.withTransaction {
        val settings = database.settingsDao().getSettingsDirect() ?: AppSettings(isFirstLaunch = false)
        val categories = database.customCategoryDao().getAllCustomCategoriesDirect()
        val trash = database.trashDao().getAllDeletedItemsDirect()
        val customers = database.habayebDao().getAllCustomersDirect()
        val habayebTransactions = database.habayebDao().getAllTransactionsDirect()
        val pins = database.habayebDao().getAllPinsDirect()
        val profile = database.businessProfileDao().get() ?: BusinessProfile()
        val recurring = database.recurringConfigDao().all()
        JSONObject().apply {
            put("settings", settingsJson(settings))
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
        // Onboarding is an installation-local lifecycle marker, not user data.
        // Never let a backup or app update reset it and show the welcome dialog
        // again on an already-installed application.
        val settings = importedSettings.copy(
            id = 1,
            isFirstLaunch = currentSettings.isFirstLaunch,
            onboardingShown = currentSettings.onboardingShown,
            isPasscodeEnabled = currentSettings.isPasscodeEnabled,
            passcodeHash = currentSettings.passcodeHash,
            recoveryPhraseHash = currentSettings.recoveryPhraseHash,
            recoveryHint = currentSettings.recoveryHint
        )
        val categories = root.array("categories").map(::parseCategory)
        val trash = root.array("trash").map(::parseTrash)
        val customers = root.array("customers").map(::parseCustomer)
        val habayebTransactions = root.array("habayebTransactions").map(::parseHabayebTransaction)
        val transactionById = habayebTransactions.associateBy { it.id }
        val pins = root.array("pins").map(::parsePin)
        val profile = parseProfile(root.getJSONObject("businessProfile"))
        val recurring = root.array("recurring").map(::parseRecurring)
        database.withTransaction {
            database.recurringConfigDao().clear()
            database.habayebDao().clearAllTransactions()
            database.habayebDao().clearAllPins()
            database.habayebDao().clearAllCustomers()
            database.trashDao().clearAllDeletedItems()
            database.customCategoryDao().clearAllCustomCategories()
            database.settingsDao().insertOrUpdateSettings(settings.copy(id = 1))
            database.businessProfileDao().save(profile.copy(id = 1))
            for (item in categories) database.customCategoryDao().insertCategory(item)
            for (item in customers) database.habayebDao().insertCustomer(item)
            for (item in habayebTransactions) database.habayebDao().insertTransaction(item)
            for (item in pins) database.habayebDao().insertPinnedCustomer(item)
            for (item in recurring) database.recurringConfigDao().save(item)
            for (item in trash) database.trashDao().insertDeletedItem(item)
        }
        return settings
    }

    private fun validatePayload(root: JSONObject) {
        val required = listOf("settings", "categories", "trash", "customers", "habayebTransactions", "pins", "businessProfile", "recurring")
        required.forEach { require(root.has(it)) { "النسخة ناقصة: $it" } }
        require(root.get("settings") is JSONObject) { "بيانات الإعدادات غير صالحة" }
        require(root.get("businessProfile") is JSONObject) { "بيانات الملف التجاري غير صالحة" }

        val categories = root.getJSONArray("categories")
        val customers = root.getJSONArray("customers")
        val transactions = root.getJSONArray("habayebTransactions")
        val pins = root.getJSONArray("pins")
        val recurring = root.getJSONArray("recurring")

        val categoryIds = mutableSetOf<Int>()
        for (i in 0 until categories.length()) {
            val category = categories.getJSONObject(i)
            val id = category.optInt("id", Int.MIN_VALUE)
            require(id != Int.MIN_VALUE) { "التصنيف ناقص: id" }
            require(categoryIds.add(id)) { "النسخة تحتوي تصنيفاً مكرراً: $id" }
        }

        val customerIds = mutableSetOf<String>()
        for (i in 0 until customers.length()) {
            val customer = customers.getJSONObject(i)
            val id = customer.optString("id")
            require(id.isNotBlank()) { "العميل ناقص: id" }
            require(customerIds.add(id)) { "النسخة تحتوي عميلاً مكرراً: $id" }
            val categoryId = if (customer.has("categoryId") && !customer.isNull("categoryId")) customer.getInt("categoryId") else null
            require(categoryId == null || categoryId in categoryIds) { "العميل $id يشير إلى تصنيف غير موجود: $categoryId" }
        }

        val transactionIds = mutableSetOf<String>()
        for (i in 0 until transactions.length()) {
            val tx = transactions.getJSONObject(i)
            val id = tx.optString("id")
            require(id.isNotBlank()) { "المعاملة المالية ناقصة: id" }
            require(transactionIds.add(id)) { "النسخة تحتوي معاملة مكررة: $id" }
            require(tx.optString("customerId").isNotBlank()) { "المعاملة $id ناقص فيها customerId" }
            require(tx.optString("customerId") in customerIds) { "المعاملة $id تشير إلى عميل غير موجود" }
            listOf("type", "amount", "timestamp", "description",
                "isForeign", "currencyCode", "foreignAmount", "exchangeRate",
                "isRateCalculated", "equivalentAmount", "baseCurrencyCode").forEach {
                require(tx.has(it)) { "المعاملة المالية ناقصة: $it" }
            }
        }

        val pinKeys = mutableSetOf<String>()
        for (i in 0 until pins.length()) {
            val pin = pins.getJSONObject(i)
            val scope = pin.optInt("scopeCategoryId", Int.MIN_VALUE)
            val customerId = pin.optString("customerId")
            require(scope != Int.MIN_VALUE && customerId.isNotBlank()) { "بيانات التثبيت ناقصة" }
            require(customerId in customerIds) { "التثبيت يشير إلى عميل غير موجود: $customerId" }
            require(scope == 0 || scope in categoryIds) { "التثبيت يشير إلى تصنيف غير موجود: $scope" }
            require(pinKeys.add("$scope::$customerId")) { "النسخة تحتوي تثبيتاً مكرراً: $scope/$customerId" }
        }

        val recurringIds = mutableSetOf<String>()
        for (i in 0 until recurring.length()) {
            val r = recurring.getJSONObject(i)
            val id = r.optString("id")
            val originalTxId = r.optString("originalTxId")
            val customerId = r.optString("customerId")
            require(id.isNotBlank()) { "قالب التكرار ناقص: id" }
            require(recurringIds.add(id)) { "النسخة تحتوي قالب تكرار مكرراً: $id" }
            require(customerId.isNotBlank() && customerId in customerIds) { "قالب التكرار $id يشير إلى عميل غير موجود" }
            require(originalTxId.isNotBlank() && originalTxId in transactionIds) { "قالب التكرار $id يشير إلى معاملة أصلية غير موجودة" }
            listOf("amount", "type", "frequency", "startDateMillis", "endDateMillis", "isForeign", "currencyCode",
                "foreignAmount", "exchangeRate", "isRateCalculated", "equivalentAmount").forEach {
                require(r.has(it)) { "قالب التكرار ناقص: $it" }
            }
            require(r.optString("customerId") == transactions.getJSONObject((0 until transactions.length()).firstOrNull { transactions.getJSONObject(it).optString("id") == originalTxId } ?: -1).optString("customerId")) {
                "قالب التكرار $id لا يطابق عميل المعاملة الأصلية"
            }
            require(r.has("baseCurrencyCode")) { "قالب التكرار ناقص: baseCurrencyCode" }
            if (r.optBoolean("isRateCalculated", false)) {
                require(r.optString("currencyCode").isNotBlank() && r.optString("currencyCode") != "DEFAULT") { "قالب التكرار المصروف بلا عملة أصلية" }
                require(r.optString("baseCurrencyCode").isNotBlank() && r.optString("baseCurrencyCode") != "DEFAULT") { "قالب التكرار المصروف بلا عملة أساس" }
                require(r.optString("exchangeRate").toBigDecimalOrNull()?.signum() == 1) { "سعر قالب التكرار غير صالح" }
            }
        }
    }

    private fun appVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (_: PackageManager.NameNotFoundException) { "1.0.0" }

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
    private fun categoryJson(v: CustomCategory) = JSONObject().apply { put("id", v.id); put("name", v.name); put("tabType", v.tabType); put("iconEmoji", v.iconEmoji); put("displayOrder", v.displayOrder); put("isSystemClosed", v.isSystemClosed) }
    private fun trashJson(v: DeletedItemEntity) = JSONObject().apply { put("id", v.id); put("sourceSystem", v.sourceSystem); put("originalTableName", v.originalTableName); put("jsonData", v.jsonData); put("deletedAt", v.deletedAt); put("searchableText", v.searchableText); put("amount", v.amount.toPlainString()); put("displayName", v.displayName) }
    private fun customerJson(v: HabayebCustomer) = JSONObject().apply { put("id", v.id); put("name", v.name); put("phone", v.phone); put("notes", v.notes); put("createdAt", v.createdAt); put("initialType", v.initialType); put("categoryId", v.categoryId) }
    private fun habayebTransactionJson(v: HabayebTransaction) = JSONObject().apply { put("id", v.id); put("customerId", v.customerId); put("type", v.type); put("amount", v.amount.toPlainString()); put("timestamp", v.timestamp); put("description", v.description); put("linkedMainTxId", v.linkedMainTxId); put("isForeign", v.isForeign); put("currencyCode", v.currencyCode); put("foreignAmount", v.foreignAmount.toPlainString()); put("exchangeRate", v.exchangeRate.toPlainString()); put("isRateCalculated", v.isRateCalculated); put("equivalentAmount", v.equivalentAmount.toPlainString()); put("baseCurrencyCode", v.baseCurrencyCode); put("snapshotVersion", v.snapshotVersion); put("rateContext", v.rateContext) }
    private fun pinJson(v: PinnedCustomer) = JSONObject().apply { put("scopeCategoryId", v.scopeCategoryId); put("customerId", v.customerId) }
    private fun profileJson(v: BusinessProfile) = JSONObject().apply { put("id", v.id); put("name", v.name); put("description", v.description); put("logoPath", v.logoPath); put("phones", JSONArray(v.phones)) }
    private fun recurringJson(v: RecurringConfigEntity) = JSONObject().apply { put("id", v.id); put("originalTxId", v.originalTxId); put("customerId", v.customerId); put("customerName", v.customerName); put("amount", v.amount.toPlainString()); put("type", v.type); put("description", v.description); put("frequency", v.frequency); put("daysOfWeek", JSONArray(v.daysOfWeek)); put("daysOfMonth", JSONArray(v.daysOfMonth)); put("timeHour", v.timeHour); put("timeMinute", v.timeMinute); put("startDateMillis", v.startDateMillis); put("endDateMillis", v.endDateMillis); put("lastExecutedTimestamp", v.lastExecutedTimestamp); put("isActive", v.isActive); put("isForeign", v.isForeign); put("currencyCode", v.currencyCode); put("foreignAmount", v.foreignAmount.toPlainString()); put("exchangeRate", v.exchangeRate.toPlainString()); put("isRateCalculated", v.isRateCalculated); put("equivalentAmount", v.equivalentAmount.toPlainString()); put("baseCurrencyCode", v.baseCurrencyCode); put("snapshotVersion", v.snapshotVersion); put("rateContext", v.rateContext) }

    private fun parseSettings(o: JSONObject) = AppSettings(1, o.optString("currencySymbol", "ر.ي"), o.optBoolean("schoolExpensesEnabled", true), o.optInt("themeMode"), o.optBoolean("doubleCheckExit", true), o.optBoolean("isPasscodeEnabled"), o.optStringOrNull("passcodeHash"), o.optStringOrNull("recoveryPhraseHash"), o.optStringOrNull("recoveryHint"), o.optBoolean("isFirstLaunch", false), o.optBoolean("onboardingShown"), o.optString("trashAutoCleanupPeriod", "NEVER"), o.optString("exchangeRatesJson", "{}"))
    private fun parseCategory(o: JSONObject) = CustomCategory(o.getInt("id"), o.getString("name"), o.getString("tabType"), o.getString("iconEmoji"), o.optInt("displayOrder", 0), o.optBoolean("isSystemClosed", false))
    private fun parseTrash(o: JSONObject) = DeletedItemEntity(o.getString("id"), o.getString("sourceSystem"), o.getString("originalTableName"), o.getString("jsonData"), o.getLong("deletedAt"), o.optString("searchableText"), o.optString("amount", "0").toBigDecimal(), o.optString("displayName"))
    private fun parseCustomer(o: JSONObject) = HabayebCustomer(o.getString("id"), o.getString("name"), o.getString("phone"), o.getString("notes"), o.getLong("createdAt"), o.optString("initialType", "OWED_BY_THEM"), if (o.has("categoryId") && !o.isNull("categoryId")) o.getInt("categoryId") else null)
    private fun parseHabayebTransaction(o: JSONObject) = HabayebTransaction(o.getString("id"), o.getString("customerId"), o.getString("type"), o.getString("amount").toBigDecimal(), o.getLong("timestamp"), o.getString("description"), o.optStringOrNull("linkedMainTxId"), o.optBoolean("isForeign", false), o.optString("currencyCode", "DEFAULT"), o.optString("foreignAmount", "0").toBigDecimal(), o.optString("exchangeRate", "0").toBigDecimal(), o.optBoolean("isRateCalculated", false), o.optString("equivalentAmount", "0").toBigDecimal(), o.optString("baseCurrencyCode", "DEFAULT"), o.optInt("snapshotVersion", 1), o.optString("rateContext", "HISTORICAL_SNAPSHOT"))
    private fun parsePin(o: JSONObject) = PinnedCustomer(o.getInt("scopeCategoryId"), o.getString("customerId"))
    private fun parseProfile(o: JSONObject) = BusinessProfile(1, o.optString("name"), o.optString("description"), o.optString("logoPath"), o.optJSONArray("phones")?.let { arr -> (0 until arr.length()).map { arr.getString(it) } } ?: emptyList())
    private fun parseRecurring(o: JSONObject) = RecurringConfigEntity(o.getString("id"), o.getString("originalTxId"), o.getString("customerId"), o.optString("customerName", ""), o.getString("amount").toBigDecimal(), o.optString("type", ""), o.optString("description", ""), o.optString("frequency", ""), o.intList("daysOfWeek"), o.intList("daysOfMonth"), o.optInt("timeHour", 0), o.optInt("timeMinute", 0), o.optLong("startDateMillis", 0L), o.optLong("endDateMillis", 0L), o.optLong("lastExecutedTimestamp", 0L), o.optBoolean("isActive", true), o.optBoolean("isForeign", false), o.optString("currencyCode", "DEFAULT"), o.optString("foreignAmount", "0").toBigDecimal(), o.optString("exchangeRate", "0").toBigDecimal(), o.optBoolean("isRateCalculated", false), o.optString("equivalentAmount", "0").toBigDecimal(), o.optString("baseCurrencyCode", "DEFAULT"), o.optInt("snapshotVersion", 1), o.optString("rateContext", "FIXED_SNAPSHOT"))
}
