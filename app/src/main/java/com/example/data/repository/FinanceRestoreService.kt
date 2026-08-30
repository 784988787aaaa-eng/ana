/**
 * =====================================================================
 * ملف: خدمة استعادة البيانات المالية الشاملة (FinanceRestoreService.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * تمثل هذه الخدمة المحرك المسؤول عن استعادة قاعدة البيانات المالية وإعادة بنائها
 * من النسخ الاحتياطية المشفرة أو ملفات الـ JSON، مع تطبيق أعلى معايير الأمان والتكامل المرجعي.
 * 
 * [المسؤوليات المعمارية ونمط الاستعادة على مرحلتين (Two-Phase Restoration)]:
 * 1. مرحلة الفحص والتحقق في الذاكرة (In-Memory Validation Phase):
 *    - قراءة ملف النسخة الاحتياطية وتحليله وتدقيق سلامة جميع الكيانات والمصفوفات قبل لمس قاعدة البيانات.
 *    - الحفاظ الصارم على أمان الجهاز المحلي (عدم استبدال الـ Passcode أو البصمة أو معرف الجهاز الفريد ببيانات النسخة المستعادة).
 *    - تطبيق الدقة المصرفية [BigDecimal] وتوحيد مقياس التقريب (Scale = 4, HALF_EVEN) لجميع المبالغ وأسعار الصرف.
 *    - التحقق من سلامة المفاتيح الأجنبية: اكتشاف المعاملات المعلقة التي لا ينتمي لها عميل وإنشاء بطاقة عميل بديلة تلقائياً لمنع انهيار التكامل المرجعي.
 * 2. مرحلة المعاملة الذرية الشاملة (Atomic Master Transaction Phase):
 *    - تنفيذ عمليتي المسح الشامل وإعادة الإدراج داخل كتلة ذرية واحدة [database.withTransaction].
 *    - التراجع التلقائي الكامل (Rollback) في حال حدوث أي استثناء أثناء الكتابة، مما يمنع تلف البيانات أو ترك قاعدة البيانات في حالة غير متناسقة.
 * 3. حماية الخصوصية:
 *    - حظر تام لطباعة أي مبالغ أو محتوى مالي في سجلات التشخيص (Logs).
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والسجلات ومعاملات Room والكيانات والعمليات الحسابية
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.data.backup.BackupConstants
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb
import com.example.data.serialization.MzdBackupSerializer
import com.example.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * [فئة نتيجة الاستعادة - FinanceRestoreResult]:
 * تغلف إعدادات التطبيق المستعادة ومؤشر ما إذا كانت النسخة الاحتياطية من الإصدارات القديمة (Legacy).
 *
 * @property settings إعدادات التطبيق المحدثة بعد دمج التفضيلات الأمنية المحلية.
 * @property isLegacy مؤشر ما إذا كانت النسخة تتبع هيكل البيانات القديم.
 */
data class FinanceRestoreResult(val settings: AppSettings, val isLegacy: Boolean)

/**
 * [وعاء البيانات المحققة للاستعادة - ValidatedRestoreData]:
 * يحتوي على كافة الكائنات المفحوصة والمطابقة في الذاكرة قبل بدء معاملة الكتابة في قاعدة البيانات.
 *
 * @property restoredSettings إعدادات التطبيق بعد الفحص والدمج.
 * @property restoredCommitments قائمة الالتزامات المالية الثابتة بعد ضبط الدقة الحسابية.
 * @property restoredTransactions قائمة قيود دفتر اليومية العام بعد تطبيع الأرقام.
 * @property customCategories قائمة التصنيفات المخصصة المستعادة.
 * @property deletedItems قائمة عناصر سلة المهملات المستعادة.
 * @property customerData قائمة بطاقات عملاء الحبايب والروابط المرجعية.
 * @property habayebTransactions قائمة معاملات ديون الحبايب المحققة والمربوطة بالعملاء.
 * @property isLegacy هل النسخة من البنية القديمة.
 */
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

/**
 * [خدمة استعادة البيانات المالية - FinanceRestoreService]:
 * مسؤولة عن تفكيك وتحليل ملفات النسخ وإعادة بناء قاعدة البيانات بالكامل بصورة ذرية وآمنة.
 *
 * @param database كائن قاعدة بيانات التطبيق [AppDatabase].
 * @param context سياق التطبيق للوصول لمترجمات السلاسل النصية والموارد.
 * @param preferenceManager مدير التفضيلات المشفرة لاستعادة حالات التثبيت والترتيب.
 */
class FinanceRestoreService(
    private val database: AppDatabase,
    private val context: Context,
    private val preferenceManager: PreferenceManager = PreferenceManager(context)
) {

    /**
     * [الكائن المرافق للثوابت والمقاييس المصرفية]:
     */
    companion object {
        /** وسم السجلات التشخيصية */
        private const val TAG = "FinanceRestoreService"
        /** المقياس العشري المعياري للعمليات المحاسبية */
        private const val FINANCIAL_SCALE = BackupConstants.FINANCIAL_SCALE
        /** نمط التقريب المصرفي المعتمد */
        private val FINANCIAL_ROUNDING = RoundingMode.HALF_EVEN

        /** بادئات ومفاتيح حفظ التفضيلات المشتركة */
        private const val PREF_KEY_PINNED_PREFIX = PreferenceManager.PREF_KEY_PINNED_PREFIX
        private const val PREF_CAT_LINK_PREFIX = PreferenceManager.PREF_CAT_LINK_PREFIX
        private const val PREF_CATEGORY_ORDER_LIST_KEY = PreferenceManager.PREF_CATEGORY_ORDER_LIST_KEY
        private const val PREF_CLOSED_CUSTOM_NAME_KEY = PreferenceManager.PREF_CLOSED_CUSTOM_NAME_KEY
    }

    // -----------------------------------------------------------------
    // مراجع كائنات الوصول للبيانات (DAOs) المستخدمة في الاستعادة
    // -----------------------------------------------------------------
    private val settingsDao = database.settingsDao()
    private val commitmentDao = database.commitmentDao()
    private val transactionDao = database.transactionDao()
    private val customCategoryDao = database.customCategoryDao()
    private val trashDao = database.trashDao()
    private val habayebDao = database.habayebDao()

    /**
     * [تصفية وحذف كافة البيانات المحاسبية - deleteAllData]:
     * تنفذ إفراغاً كاملاً وشاملاً لجميع جداول قاعدة البيانات داخل معاملة ذرية مع إعادة ضبط الإعدادات.
     */
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
            Log.d(TAG, "تم تفريغ كافة الجداول المحاسبية بنجاح داخل معاملة ذرية.")
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تصفية البيانات: ${e.javaClass.simpleName}", e)
            throw e
        }
    }

    /**
     * [التحليل والتحقق من صحة البيانات في الذاكرة - validateAndParseRestoreData]:
     * تفحص بنية الـ JSON وتدمج الإعدادات وتضبط المقاييس المحاسبية وتتحقق من سلامة التكامل المرجعي.
     *
     * @param root كائن JSON الرئيسي للنسخة الاحتياطية.
     * @param rawJsonString النص الكامل للنسخة الاحتياطية.
     * @param currentLocalSettings الإعدادات المحلية الحالية للحفاظ على تفضيلات الأمان للجهاز.
     * @return كائن [ValidatedRestoreData] الجاهز للإدراج في قاعدة البيانات.
     */
    suspend fun validateAndParseRestoreData(
        root: JSONObject,
        rawJsonString: String,
        currentLocalSettings: AppSettings
    ): ValidatedRestoreData {
        // 1. استيراد وتحليل الكيانات وضمان التوافقية
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

        // تطبيق الدقة المالية على الالتزامات الثابتة
        val restoredCommitments = data.second.map { fc ->
            fc.copy(
                targetAmount = fc.targetAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                currentProgress = fc.currentProgress.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }

        // تطبيق الدقة المالية على المعاملات الرئيسية
        val restoredTransactions = data.third.map { tx ->
            tx.copy(
                amount = tx.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }

        val customCategories = MzdBackupSerializer.parseCustomCategories(root)
        val deletedItems = MzdBackupSerializer.parseDeletedItems(root)
        val restoredCustomerData = MzdBackupSerializer.parseHabayebCustomers(root)
        val habayebTransactions = MzdBackupSerializer.parseHabayebTransactions(root, restoredSettings.currencySymbol)

        // 2. التحقق من سلامة العلاقات ومعالجة المعاملات المعلقة بدون عميل
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

        // تنظيف وتدقيق معاملات الديون والعملات الأجنبية
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

    /**
     * [تنفيذ الاستعادة الشاملة للنسخة الاحتياطية - executeMasterRestore]:
     * تنفذ التحليل المسبق ثم تطبق عملية الاستبدال وإعادة البناء الذرية الشاملة داخل قاعدة البيانات.
     *
     * @param rawJsonString النص الكامل لبيانات النسخة الاحتياطية بتنسيق JSON.
     * @return [FinanceRestoreResult] يحتوي على نتيجة وإعدادات الاستعادة.
     */
    suspend fun executeMasterRestore(rawJsonString: String): FinanceRestoreResult = withContext(Dispatchers.IO) {
        val root = JSONObject(rawJsonString)
        val currentLocalSettings = settingsDao.getSettingsDirect() ?: AppSettings()

        // 1. مرحلة التحليل والتحقق في الذاكرة
        val validatedData = validateAndParseRestoreData(root, rawJsonString, currentLocalSettings)

        // 2. المعاملة الذرية الشاملة لقاعدة البيانات (Atomic Master Transaction)
        database.withTransaction {
            // أ. التصفية المتزامنة لكافة الجداول داخل المعاملة
            transactionDao.clearAllTransactions()
            commitmentDao.clearAllCommitments()
            customCategoryDao.clearAllCustomCategories()
            trashDao.clearAllDeletedItems()
            habayebDao.clearAllCustomers()
            habayebDao.clearAllTransactions()

            // ب. إدراج السجلات المستعادة
            settingsDao.insertOrUpdateSettings(validatedData.restoredSettings)
            for (fc in validatedData.restoredCommitments) {
                commitmentDao.insertCommitment(fc)
            }
            for (tx in validatedData.restoredTransactions) {
                transactionDao.insertTransaction(tx)
            }

            // استعادة الفئات المخصصة
            for (cat in validatedData.customCategories) {
                customCategoryDao.insertCategory(cat)
            }

            // استعادة التفضيلات المشتركة وحالات التثبيت
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

            // استعادة سلة المهملات
            for (item in validatedData.deletedItems) {
                trashDao.insertDeletedItem(item)
            }

            // استعادة عملاء الحبايب والروابط
            for (custData in validatedData.customerData) {
                habayebDao.insertCustomer(custData.customer)
                custData.categoryLink?.let { catLink ->
                    preferenceManager.writeDualPreference { sharedEdit, financeEdit ->
                        sharedEdit.putString("$PREF_CAT_LINK_PREFIX${custData.customer.id}", catLink)
                        financeEdit.putString("$PREF_CAT_LINK_PREFIX${custData.customer.id}", catLink)
                    }
                }
            }

            // استعادة معاملات الحبايب المحققة
            for (tx in validatedData.habayebTransactions) {
                habayebDao.insertTransaction(tx)
            }
        }

        Log.d(TAG, "اكتملت الاستعادة الشاملة للبيانات بنجاح. (Legacy: ${validatedData.isLegacy})")
        FinanceRestoreResult(validatedData.restoredSettings, validatedData.isLegacy)
    }
}

