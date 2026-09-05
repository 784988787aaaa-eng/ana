package com.smartledger.aldaftar.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.data.serialization.BackupPayloadValidator
import com.smartledger.aldaftar.data.serialization.BackupValidationResult
import com.smartledger.aldaftar.data.serialization.MzdBackupSerializer
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

data class FinanceRestoreResult(val settings: AppSettings, val isLegacy: Boolean)

data class ValidatedRestoreData(
    val restoredSettings: AppSettings,
    val restoredCommitments: List<FixedCommitment>,
    val restoredTransactions: List<TransactionDb>,
    val customCategories: List<CustomCategory>,
    val deletedItems: List<DeletedItemEntity>,
    val customerData: List<MzdBackupSerializer.RestoredHabayebCustomerData>,
    val habayebTransactions: List<HabayebTransaction>,
    val isLegacy: Boolean
)

class FinanceRestoreService(
    private val database: AppDatabase,
    private val context: Context,
    private val preferenceManager: PreferenceManager = PreferenceManager(context)
) {

    companion object {

        private const val FINANCIAL_SCALE = BackupConstants.FINANCIAL_SCALE

        private const val MAX_RESTORE_BYTES = 64L * 1024L * 1024L

        private val FINANCIAL_ROUNDING = RoundingMode.HALF_EVEN

        private const val PREF_KEY_PINNED_PREFIX = PreferenceManager.PREF_KEY_PINNED_PREFIX
        private const val PREF_CAT_LINK_PREFIX = PreferenceManager.PREF_CAT_LINK_PREFIX
        private const val PREF_CATEGORY_ORDER_LIST_KEY = PreferenceManager.PREF_CATEGORY_ORDER_LIST_KEY
        private const val PREF_CLOSED_CUSTOM_NAME_KEY = PreferenceManager.PREF_CLOSED_CUSTOM_NAME_KEY
    }

    private val settingsDao = database.settingsDao()
    private val commitmentDao = database.commitmentDao()
    private val transactionDao = database.transactionDao()
    private val customCategoryDao = database.customCategoryDao()
    private val trashDao = database.trashDao()
    private val habayebDao = database.habayebDao()

    suspend fun deleteAllData(): Unit = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                transactionDao.clearAllTransactions()
                commitmentDao.clearAllCommitments()
                customCategoryDao.clearAllCustomCategories()
                trashDao.clearAllDeletedItems()
                habayebDao.clearAllCustomers()
                habayebDao.clearAllTransactions()
                settingsDao.insertOrUpdateSettings(AppSettings(isFirstLaunch = false))
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun validateAndParseRestoreData(
        root: JSONObject,
        rawJsonString: String,
        currentLocalSettings: AppSettings
    ): ValidatedRestoreData {

        val data = MzdBackupSerializer.importBackupFromJson(rawJsonString, context)
        val restoredSettingsUnmerged = data.first
        val restoredSettings = restoredSettingsUnmerged.copy(
            themeMode = currentLocalSettings.themeMode,
            doubleCheckExit = currentLocalSettings.doubleCheckExit,
            isPasscodeEnabled = currentLocalSettings.isPasscodeEnabled,
            passcodeHash = currentLocalSettings.passcodeHash,
            recoveryPhraseHash = currentLocalSettings.recoveryPhraseHash,
            recoveryHint = currentLocalSettings.recoveryHint,
            tempPart = currentLocalSettings.tempPart,
            permPart = currentLocalSettings.permPart,
            unifiedDeviceId = currentLocalSettings.unifiedDeviceId,
            isFirstLaunch = currentLocalSettings.isFirstLaunch,
            isAutoBackupEnabled = currentLocalSettings.isAutoBackupEnabled,
            isCloudSyncEnabled = currentLocalSettings.isCloudSyncEnabled
        )

        val restoredCommitments = data.second.map { fc ->
            fc.copy(
                targetAmount = fc.targetAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                currentProgress = fc.currentProgress.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }

        val restoredTransactions = data.third.map { tx ->
            tx.copy(
                amount = tx.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }

        val customCategories = MzdBackupSerializer.parseCustomCategories(root)
        val deletedItems = MzdBackupSerializer.parseDeletedItems(root)
        val restoredCustomerData = MzdBackupSerializer.parseHabayebCustomers(root)
        val habayebTransactions = MzdBackupSerializer.parseHabayebTransactions(root, restoredSettings.currencySymbol)

        val customerIdSet = restoredCustomerData.map { it.customer.id }.toSet().toMutableSet()
        val allCustomerData = restoredCustomerData.toMutableList()
        val mainTxIdSet = restoredTransactions.map { it.id }.toSet()

        val missingCustomerIds = habayebTransactions
        .map { it.customerId.trim() }
        .filter { it.isNotBlank() && !customerIdSet.contains(it) }
        .toSet()

        for (missingId in missingCustomerIds) {
            val fallbackCustomer = HabayebCustomer(
                id = missingId,
                name = "عميل مستعاد ($missingId)",
                phone = "",
                notes = "تم إنشاء ملف العميل تلقائياً عند الاستعادة لتأمين ترابط المعاملات المستعادة",
                createdAt = System.currentTimeMillis() / 1000,
                initialType = TransactionType.OWED_BY_THEM.value
            )
            allCustomerData.add(MzdBackupSerializer.RestoredHabayebCustomerData(fallbackCustomer, null))
            customerIdSet.add(missingId)
        }

        val validatedHabayebTransactions = habayebTransactions.map { tx ->
            val cleanLinkedId = tx.linkedMainTxId?.takeIf {
                it.isNotBlank() && it != tx.id && it != "0" && !it.equals("null", ignoreCase = true) && mainTxIdSet.contains(it)
            }
            val cleanCustomerId = if (customerIdSet.contains(tx.customerId)) tx.customerId else tx.customerId.trim()
            tx.copy(
                customerId = cleanCustomerId,
                linkedMainTxId = cleanLinkedId,
                amount = tx.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                foreignAmount = tx.foreignAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                exchangeRate = tx.exchangeRate.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                equivalentAmount = tx.equivalentAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }

        val isLegacy = root.has(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB) || root.has(BackupConstants.JSON_KEY_HABAYEB_DEBTS_DB)

        return ValidatedRestoreData(
            restoredSettings = restoredSettings,
            restoredCommitments = restoredCommitments,
            restoredTransactions = restoredTransactions,
            customCategories = customCategories,
            deletedItems = deletedItems,
            customerData = allCustomerData,
            habayebTransactions = validatedHabayebTransactions,
            isLegacy = isLegacy
        )
    }

    suspend fun executeMasterRestore(rawJsonString: String): FinanceRestoreResult = withContext(Dispatchers.IO) {
        val rawBytes = rawJsonString.toByteArray(Charsets.UTF_8)
        try {
            if (rawBytes.size > MAX_RESTORE_BYTES) {
                throw IOException("تجاوزت النسخة الاحتياطية الحد الحجمي المسموح")
            }
        } finally {
            rawBytes.fill(0)
        }

        val preValidation = BackupPayloadValidator.validateBackupPayload(rawJsonString, verifyHashStrictly = true)
        if (preValidation is BackupValidationResult.Invalid) {
            throw IOException("فشل تدقيق سلامة النسخة الاحتياطية (${preValidation.errorCode}): ${preValidation.reason}", preValidation.cause)
        }

        val root = JSONObject(rawJsonString)
        val currentLocalSettings = settingsDao.getSettingsDirect() ?: AppSettings()

        val validatedData = validateAndParseRestoreData(root, rawJsonString, currentLocalSettings)

        database.withTransaction {

            transactionDao.clearAllTransactions()
            commitmentDao.clearAllCommitments()
            customCategoryDao.clearAllCustomCategories()
            trashDao.clearAllDeletedItems()
            habayebDao.clearAllCustomers()
            habayebDao.clearAllTransactions()

            settingsDao.insertOrUpdateSettings(validatedData.restoredSettings)
            for (fc in validatedData.restoredCommitments) {
                commitmentDao.insertCommitment(fc)
            }
            for (tx in validatedData.restoredTransactions) {
                transactionDao.insertTransaction(tx)
            }

            for (cat in validatedData.customCategories) {
                customCategoryDao.insertCategory(cat)
            }

            preferenceManager.writeDualPreference { sharedEdit, financeEdit ->
                if (root.has(BackupConstants.JSON_KEY_PINNED_CUSTOMERS) && !root.isNull(BackupConstants.JSON_KEY_PINNED_CUSTOMERS)) {
                    val pinnedObj = root.optJSONObject(BackupConstants.JSON_KEY_PINNED_CUSTOMERS)
                    if (pinnedObj != null) {
                        val keys = pinnedObj.keys()
                        while (keys.hasNext()) {
                            val catKey = keys.next()
                            val arr = pinnedObj.getJSONArray(catKey)
                            val set = mutableSetOf<String>()
                            for (i in 0 until arr.length()) {
                                set.add(arr.getString(i))
                            }
                            val prefKey = "$PREF_KEY_PINNED_PREFIX$catKey"
                            sharedEdit.putStringSet(prefKey, set)
                            financeEdit.putStringSet(prefKey, set)
                        }
                    }
                }

                if (root.has(BackupConstants.JSON_KEY_CATEGORY_ORDER_LIST) && !root.isNull(BackupConstants.JSON_KEY_CATEGORY_ORDER_LIST)) {
                    val catOrder = root.getString(BackupConstants.JSON_KEY_CATEGORY_ORDER_LIST)
                    sharedEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, catOrder)
                    financeEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, catOrder)
                }
                if (root.has(BackupConstants.JSON_KEY_CLOSED_CUSTOM_NAME) && !root.isNull(BackupConstants.JSON_KEY_CLOSED_CUSTOM_NAME)) {
                    val closedCustomName = root.getString(BackupConstants.JSON_KEY_CLOSED_CUSTOM_NAME)
                    sharedEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, closedCustomName)
                    financeEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, closedCustomName)
                }
            }

            for (item in validatedData.deletedItems) {
                trashDao.insertDeletedItem(item)
            }

            for (custData in validatedData.customerData) {
                habayebDao.insertCustomer(custData.customer)
                custData.categoryLink?.let { catLink ->
                    preferenceManager.writeDualPreference { sharedEdit, financeEdit ->
                        sharedEdit.putString("$PREF_CAT_LINK_PREFIX${custData.customer.id}", catLink)
                        financeEdit.putString("$PREF_CAT_LINK_PREFIX${custData.customer.id}", catLink)
                    }
                }
            }

            for (tx in validatedData.habayebTransactions) {
                habayebDao.insertTransaction(tx)
            }
        }

        FinanceRestoreResult(validatedData.restoredSettings, validatedData.isLegacy)
    }
}
