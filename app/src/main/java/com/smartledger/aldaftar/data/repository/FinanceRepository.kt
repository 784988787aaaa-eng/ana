/**
 * =====================================================================
 * ملف: المستودع المالي المركزي الموحد (FinanceRepository.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا المستودع القلب النابض لطبقة البيانات (Central Data Layer Repository) في التطبيق،
 * حيث يعمل كنقطة وصول مركزية وواجهة موحدة (Unified Facade) لجميع كائنات الوصول للبيانات (DAOs)،
 * ويقوم بتنسيق وحماية العمليات المحاسبية، وتطبيق معايير الدقة المصرفية الصارمة.
 * 
 * [المسؤوليات المعمارية وقواعد الحسابات المالية]:
 * 1. الدقة الحسابية المصرفية الموحدة (Banker's Precision & Rounding):
 *    - ضبط دقة الأرقام المالية على 4 خانات عشرية [FINANCIAL_SCALE] مع التقريب المصرفي [RoundingMode.HALF_EVEN]
 *      لمنع أي تشويه أو تآكل في أجزاء السنت والهللة.
 * 2. الحماية وتأمين الخيوط (Thread Safety):
 *    - حصر كافة عمليات الكتابة والقراءة الثقيلة والاستعلامات على خيوط الإدخال والإخراج [Dispatchers.IO].
 * 3. التحديث التفاعلي للواجهة (Reactive Flow Streams):
 *    - تصدير تدفقات [Flow] لكافة الجداول والكيانات المالية لضمان تحديث واجهات Compose لحظياً بمجرد حدوث أي تعديل.
 * 4. إدارة سلة المهملات والحذف الناعم الآمن (Soft Delete Subsystem):
 *    - تفويض تجميع وتغليف السجلات المحذوفة بصيغة JSON إلى [TrashJsonSerializer] وحفظها في [DeletedItemEntity].
 * 5. تفويض الخدمات المتخصصة (Separation of Concerns):
 *    - تفويض عمليات الاستعادة ومسح البيانات إلى [FinanceRestoreService].
 *    - تفويض التراخيص والفترة التجريبية إلى [TrialManager].
 *    - تفويض التفضيلات المشفرة إلى [PreferenceManager].
 */
package com.smartledger.aldaftar.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ووسائط التخزين والترقيم وصفحات Paging وقاعدة البيانات Room
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/** اسم مستعار لنتيجة الاستعادة لسهولة الاستخدام */
typealias RestoreResult = FinanceRestoreResult

/**
 * [فئة المستودع المالي المركزي - FinanceRepository]:
 * تدير وتنسق كافة العمليات المالية المحاسبية والتدفقات التفاعلية وسلة المهملات.
 *
 * @param database كائن قاعدة البيانات المركزية [AppDatabase].
 * @param context سياق التطبيق للوصول للموارد والترجمات.
 * @param preferenceManager مدير التفضيلات المشفرة والتخزين المزدوج.
 * @param trialManager مدير التراخيص والفترة التجريبية.
 * @param restoreService خدمة إعادة بناء واستعادة قواعد البيانات.
 */
class FinanceRepository(
    internal val database: AppDatabase,
    private val context: Context,
    private val preferenceManager: PreferenceManager = PreferenceManager(context),
    private val trialManager: TrialManager = TrialManager(context),
    private val restoreService: FinanceRestoreService = FinanceRestoreService(database, context, preferenceManager)
) {

    /**
     * [الكائن المرافق للثوابت والمقاييس المالية]:
     */
    companion object {
        /** أسماء الجداول وحزم الحذف لسلة المهملات */
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_FIXED_COMMITMENTS = "fixed_commitments"
        private const val TABLE_HABAYEB_CUSTOMERS = "habayeb_customers"
        private const val TABLE_HABAYEB_TRANSACTIONS = "habayeb_transactions"
        private const val BUNDLE_HABAYEB = "habayeb_bundle"
        private const val BUNDLE_DAR = "dar_bundle"

        /** عدد الخانات العشرية المعيارية للعمليات المحاسبية */
        const val FINANCIAL_SCALE = 4
        /** نمط التقريب المصرفي المعتمد في التطبيق */
        val FINANCIAL_ROUNDING: RoundingMode = RoundingMode.HALF_EVEN
    }

    // -----------------------------------------------------------------
    // مراجع كائنات الوصول للبيانات (DAOs) المستخرجة من قاعدة البيانات
    // -----------------------------------------------------------------
    private val settingsDao = database.settingsDao()
    private val commitmentDao = database.commitmentDao()
    private val transactionDao = database.transactionDao()
    private val customCategoryDao = database.customCategoryDao()
    private val trashDao = database.trashDao()
    private val habayebDao = database.habayebDao()

    /** مدير مسارات النسخ الاحتياطي */
    private val backupDirectoryManager = BackupDirectoryManager(context)

    /** أسماء الأنظمة الفرعية للتمييز في سلة المهملات */
    private val sourceDar: String by lazy { context.getString(com.smartledger.aldaftar.R.string.source_system_dar) }
    private val sourceHabayeb: String by lazy { context.getString(com.smartledger.aldaftar.R.string.source_system_habayeb) }

    // -----------------------------------------------------------------
    // دوال التفضيلات والتخزين الأمني
    // -----------------------------------------------------------------

    /** جلب كائن التفضيلات الأمنية المشفرة */
    fun getSecurityPreferences(): SharedPreferences = preferenceManager.getSecurityPreferences()

    /** تنفيذ تعديل مزدوج على التفضيلات العامة والمشفرة */
    fun writeDualPreference(action: (SharedPreferences.Editor, SharedPreferences.Editor) -> Unit) {
        preferenceManager.writeDualPreference(action)
    }

    // -----------------------------------------------------------------
    // التدفقات التفاعلية اللحظية (Reactive Data Streams / Flows)
    // -----------------------------------------------------------------

    /** تدفق إعدادات التطبيق والعملة الرئيسية */
    val settingsFlow: Flow<AppSettings?> = settingsDao.getSettingsFlow()

    /** تدفق قائمة الالتزامات والأقساط الثابتة */
    val commitmentsFlow: Flow<List<FixedCommitment>> = commitmentDao.getAllCommitmentsFlow()

    /** تدفق قيود دفتر اليومية العام */
    val transactionsFlow: Flow<List<TransactionDb>> = transactionDao.getAllTransactionsFlow()

    /** تدفق التصنيفات المخصصة للعمليات */
    val customCategoriesFlow: Flow<List<CustomCategory>> = customCategoryDao.getAllCustomCategoriesFlow()

    /** تدفق عناصر وسجلات سلة المهملات */
    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = trashDao.getAllDeletedItemsFlow()

    /** تدفق قائمة عملاء وحسابات دفتر ديون الحبايب */
    val habayebCustomersFlow: Flow<List<HabayebCustomer>> = habayebDao.getAllCustomersFlow()

    /** تدفق كافة قيود ومعاملات دفتر ديون الحبايب */
    val habayebTransactionsFlow: Flow<List<HabayebTransaction>> = habayebDao.getAllTransactionsFlow()

    /** استرجاع تدفق معاملات عميل معين */
    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<HabayebTransaction>> = 
        habayebDao.getTransactionsForCustomerFlow(customerId)

    /** استرجاع مصدر التقسيم والصفحات (PagingSource) لمعاملات العميل */
    fun getTransactionsPagingSourceForCustomer(customerId: String): PagingSource<Int, HabayebTransaction> =
        habayebDao.getTransactionsPagingSourceForCustomer(customerId)

    /** استرجاع تدفق المعاملات المسجلة بالعملات الأجنبية */
    fun getForeignTransactionsFlow(): Flow<List<HabayebTransaction>> = habayebDao.getForeignTransactionsFlow()

    /** استرجاع تدفق معاملات العميل بعدد أقصى محدد */
    fun getTransactionsForCustomerWithLimitFlow(customerId: String, limit: Int): Flow<List<HabayebTransaction>> = 
        habayebDao.getTransactionsForCustomerWithLimitFlow(customerId, limit)

    /** استرجاع تدفق إجمالي عدد معاملات الحبايب */
    fun getHabayebTransactionsCountFlow(): Flow<Int> = habayebDao.getHabayebTransactionsCountFlow()

    /** استرجاع تدفق صافي السيولة النقدية لدفتر اليومية */
    fun getTotalCashFlow(): Flow<BigDecimal> = transactionDao.getTotalCashFlow()

    /** استرجاع تدفق إجمالي عدد قيود دفتر اليومية */
    fun getTransactionsCountFlow(): Flow<Int> = transactionDao.getTransactionsCountFlow()

    // -----------------------------------------------------------------
    // عمليات الإعدادات والالتزامات الثابتة (Settings & Commitments)
    // -----------------------------------------------------------------

    /** جلب الإعدادات الحالية مباشرة وبشكل معلق */
    suspend fun getSettingsDirect(): AppSettings? = withContext(Dispatchers.IO) {
        settingsDao.getSettingsDirect()
    }

    /** حفظ أو تحديث إعدادات التطبيق */
    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    /** حفظ التزام مالي جديد مع توحيد دقة المبالغ */
    suspend fun saveCommitment(commitment: FixedCommitment) = withContext(Dispatchers.IO) {
        val normalized = commitment.copy(
            targetAmount = commitment.targetAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            currentProgress = commitment.currentProgress.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
        )
        commitmentDao.insertCommitment(normalized)
    }

    /** تحديث قائمة الالتزامات مع تطبيع دقة كافة المبالغ */
    suspend fun updateCommitments(commitments: List<FixedCommitment>) = withContext(Dispatchers.IO) {
        val normalizedList = commitments.map { fc ->
            fc.copy(
                targetAmount = fc.targetAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
                currentProgress = fc.currentProgress.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
            )
        }
        commitmentDao.updateCommitments(normalizedList)
    }

    /** حذف التزام مالي بالاسم */
    suspend fun deleteCommitment(name: String) = withContext(Dispatchers.IO) {
        commitmentDao.deleteCommitment(name)
    }

    /** مسح كافة الالتزامات المالية */
    suspend fun clearCommitments() = withContext(Dispatchers.IO) {
        commitmentDao.clearAllCommitments()
    }

    // -----------------------------------------------------------------
    // عمليات قيود دفتر اليومية العام (Main Transactions Operations)
    // -----------------------------------------------------------------

    /** البحث عن قيد يومية بالمعرف */
    suspend fun getTransactionById(id: String): TransactionDb? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    /** حفظ قيد مالي في اليومية مع تطبيع الدقة العشرية */
    suspend fun saveTransaction(transaction: TransactionDb) = withContext(Dispatchers.IO) {
        val normalized = transaction.copy(
            amount = transaction.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
        )
        transactionDao.insertTransaction(normalized)
    }

    /** حذف قيد يومية ممرر */
    suspend fun deleteTransaction(transaction: TransactionDb) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    /** حذف قيد يومية بالمعرف */
    suspend fun deleteTransactionById(id: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }

    /** مسح كافة قيود دفتر اليومية */
    suspend fun clearTransactions() = withContext(Dispatchers.IO) {
        transactionDao.clearAllTransactions()
    }

    // -----------------------------------------------------------------
    // عمليات التصنيفات المخصصة (Custom Categories Operations)
    // -----------------------------------------------------------------

    /** حفظ تصنيف مخصص جديد */
    suspend fun saveCustomCategory(category: CustomCategory) = withContext(Dispatchers.IO) {
        customCategoryDao.insertCategory(category)
    }

    /** حذف تصنيف مخصص */
    suspend fun deleteCustomCategory(category: CustomCategory) = withContext(Dispatchers.IO) {
        customCategoryDao.deleteCategory(category)
    }

    /** مسح كافة التصنيفات المخصصة */
    suspend fun clearCustomCategories() = withContext(Dispatchers.IO) {
        customCategoryDao.clearAllCustomCategories()
    }

    /** تحديث ترتيب عرض التصنيفات في شريط التصفية */
    suspend fun updateCustomCategoriesOrder(orderedNames: List<String>) = withContext(Dispatchers.IO) {
        val currentCategories = customCategoryDao.getAllCustomCategoriesFlow().first()
        val orderMap = orderedNames.withIndex().associate { it.value to it.index }
        val updatedCategories = currentCategories.map { category ->
            val key = if (category.isSystemClosed) "CLOSED" else category.name
            val index = orderMap[key] ?: 999
            category.copy(displayOrder = index)
        }
        customCategoryDao.updateCategories(updatedCategories)
    }

    /** استرجاع قيود اليومية مقسمة صفحات مباشرة */
    suspend fun getPagedTransactionsDirect(limit: Int, offset: Int): List<TransactionDb> = withContext(Dispatchers.IO) {
        transactionDao.getPagedTransactionsDirect(limit, offset)
    }

    /** حساب إجمالي المصروفات لفترة زمنية محددة */
    suspend fun getExpensesSumForPeriod(startTimestamp: Long, endTimestamp: Long): BigDecimal = withContext(Dispatchers.IO) {
        transactionDao.getExpensesSumForPeriod(startTimestamp, endTimestamp)
    }

    /** جلب العدد الفعلي لقيود اليومية مباشرة */
    suspend fun getTransactionsCountDirect(): Int = withContext(Dispatchers.IO) {
        transactionDao.getTransactionsCountDirect()
    }

    // -----------------------------------------------------------------
    // عمليات دفتر ديون الحبايب والعملاء (Habayeb & Customers Operations)
    // -----------------------------------------------------------------

    /** إدراج عميل جديد في دفتر الحبايب */
    suspend fun insertCustomer(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        habayebDao.insertCustomer(customer)
    }

    /** تحديث بيانات العميل ومطابقة اتجاه المعاملات تلقائياً عند تغيير نوع الحساب المبدئي */
    suspend fun updateCustomer(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val oldCustomer = habayebDao.getCustomerByIdDirect(customer.id)
            habayebDao.updateCustomer(customer)
            if (oldCustomer != null && oldCustomer.initialType != customer.initialType) {
                when (customer.initialType) {
                    TransactionType.OWED_BY_THEM.value -> habayebDao.adaptTransactionsToOwedByThem(customer.id)
                    TransactionType.OWED_TO_THEM.value -> habayebDao.adaptTransactionsToOwedToThem(customer.id)
                }
            }
        }
    }

    /** إدراج عميل جديد مع رصيده الافتتاحي بشكل متزامن وذري */
    suspend fun insertCustomerWithOpeningTransaction(customer: HabayebCustomer, transaction: HabayebTransaction?) = withContext(Dispatchers.IO) {
        val normalizedTx = transaction?.copy(
            amount = transaction.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            foreignAmount = transaction.foreignAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            exchangeRate = transaction.exchangeRate.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            equivalentAmount = transaction.equivalentAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
        )
        habayebDao.insertCustomerWithOpeningTransaction(customer, normalizedTx)
    }

    /** حذف العميل وجميع معاملاته المرتبطة دفعة واحدة */
    suspend fun deleteCustomerAndTransactions(customerId: String) = withContext(Dispatchers.IO) {
        habayebDao.deleteCustomerAndTransactions(customerId)
    }

    /** تعديل اسم العميل */
    suspend fun updateCustomerName(id: String, newName: String) = withContext(Dispatchers.IO) {
        habayebDao.updateCustomerName(id, newName)
    }

    /** إدراج معاملة مالية في كشف حساب العميل مع تطبيع كافة الحقول النقدية */
    suspend fun insertHabayebTransaction(transaction: HabayebTransaction) = withContext(Dispatchers.IO) {
        val normalized = transaction.copy(
            amount = transaction.amount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            foreignAmount = transaction.foreignAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            exchangeRate = transaction.exchangeRate.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING),
            equivalentAmount = transaction.equivalentAmount.setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)
        )
        habayebDao.insertTransaction(normalized)
    }

    /** حذف قيد معاملة عميل */
    suspend fun deleteHabayebTransaction(transaction: HabayebTransaction) = withContext(Dispatchers.IO) {
        habayebDao.deleteTransaction(transaction)
    }

    /** حذف قيد معاملة عميل بالمعرف */
    suspend fun deleteHabayebTransactionById(id: String) = withContext(Dispatchers.IO) {
        habayebDao.deleteTransactionById(id)
    }

    /** البحث عن قيد معاملة عميل بالمعرف */
    suspend fun getHabayebTransactionById(id: String): HabayebTransaction? = withContext(Dispatchers.IO) {
        habayebDao.getTransactionById(id)
    }

    /** جلب بيانات العميل المباشرة بالمعرف */
    suspend fun getCustomerByIdDirect(id: String): HabayebCustomer? = withContext(Dispatchers.IO) {
        habayebDao.getCustomerByIdDirect(id)
    }

    /** استرجاع قائمة كافة العملاء مباشرة */
    suspend fun getAllCustomersDirect(): List<HabayebCustomer> = withContext(Dispatchers.IO) {
        habayebDao.getAllCustomersDirect()
    }

    /** استرجاع قائمة كافة معاملات الحبايب مباشرة */
    suspend fun getAllTransactionsDirect(): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getAllTransactionsDirect()
    }

    /** استرجاع كافة معاملات عميل معين مباشرة */
    suspend fun getTransactionsForCustomerDirect(customerId: String): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getTransactionsForCustomerDirect(customerId)
    }

    /** مسح كافة العملاء */
    suspend fun clearAllCustomers() = withContext(Dispatchers.IO) {
        habayebDao.clearAllCustomers()
    }

    /** مسح كافة معاملات الحبايب */
    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        habayebDao.clearAllTransactions()
    }

    /** استرجاع معاملات العميل مقسمة صفحات مباشرة */
    suspend fun getTransactionsForCustomerPaged(customerId: String, limit: Int, offset: Int): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getTransactionsForCustomerPaged(customerId, limit, offset)
    }

    /** استرجاع العدد الإجمالي لمعاملات الحبايب مباشرة */
    suspend fun getHabayebTransactionsCountDirect(): Int = withContext(Dispatchers.IO) {
        habayebDao.getHabayebTransactionsCountDirect()
    }

    /** حساب العدد الحقيقي الكلي لجميع معاملات التطبيق (يومية + حبايب) لفحص التراخيص */
    suspend fun getRealTotalTransactionsCount(): Int = withContext(Dispatchers.IO) {
        getTransactionsCountDirect() + getHabayebTransactionsCountDirect()
    }

    // -----------------------------------------------------------------
    // عمليات الحذف المؤقت وسلة المهملات (Trash & Soft Delete)
    // -----------------------------------------------------------------

    /** استرجاع كافة عناصر سلة المهملات مباشرة */
    suspend fun getAllDeletedItemsDirect(): List<DeletedItemEntity> = withContext(Dispatchers.IO) {
        trashDao.getAllDeletedItemsDirect()
    }

    /** حفظ عنصر جديد في سلة المهملات */
    suspend fun saveDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.insertDeletedItem(item)
    }

    /** حذف عنصر نهائياً من سلة المهملات */
    suspend fun removeDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.deleteItem(item)
    }

    /** حذف عنصر نهائياً بالمعرف من سلة المهملات */
    suspend fun removeDeletedItemById(id: String) = withContext(Dispatchers.IO) {
        trashDao.deleteItemById(id)
    }

    /** إفراغ سلة المهملات بالكامل */
    suspend fun clearDeletedItems() = withContext(Dispatchers.IO) {
        trashDao.clearAllDeletedItems()
    }

    /** نقل التزام مالي إلى سلة المهملات بعد تحويله إلى JSON */
    suspend fun softDeleteCommitmentToTrash(fc: FixedCommitment) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeCommitment(fc)
        val trashItem = DeletedItemEntity(
            id = "fc_${fc.name}",
            sourceSystem = sourceDar,
            originalTableName = TABLE_FIXED_COMMITMENTS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    /** نقل حزمة عميل كاملة مع كافة معاملاته إلى سلة المهملات ككتلة ذرية واحدة */
    suspend fun softDeleteHabayebBundleToTrash(customer: HabayebCustomer, transactions: List<HabayebTransaction>) = withContext(Dispatchers.IO) {
        val sharedPrefs = getSecurityPreferences()
        val jsonData = TrashJsonSerializer.serializeHabayebBundle(customer, transactions, sharedPrefs)
        val trashItem = DeletedItemEntity(
            id = "bundle_${customer.id}",
            sourceSystem = sourceHabayeb,
            originalTableName = BUNDLE_HABAYEB,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    /** نقل بطاقة عميل إلى سلة المهملات */
    suspend fun softDeleteHabayebCustomerToTrash(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeHabayebCustomer(customer)
        val trashItem = DeletedItemEntity(
            id = "cust_${customer.id}",
            sourceSystem = sourceHabayeb,
            originalTableName = TABLE_HABAYEB_CUSTOMERS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    /** نقل قيد يومية إلى سلة المهملات */
    suspend fun softDeleteTransactionToTrash(tx: TransactionDb) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeTransaction(tx)
        val trashItem = DeletedItemEntity(
            id = tx.id,
            sourceSystem = sourceDar,
            originalTableName = TABLE_TRANSACTIONS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    /** نقل حزمة قيود يومية متعددة إلى سلة المهملات بحزمة مجمعة */
    suspend fun softDeleteTransactionBundleToTrash(transactions: List<TransactionDb>, title: String) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeTransactionBundle(transactions, title)
        val id = "dar_bundle_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val trashItem = DeletedItemEntity(
            id = id,
            sourceSystem = sourceDar,
            originalTableName = BUNDLE_DAR,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    /** نقل معاملة عميل فردية إلى سلة المهملات */
    suspend fun softDeleteHabayebTransactionToTrash(tx: HabayebTransaction) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeHabayebTransaction(tx)
        val trashItem = DeletedItemEntity(
            id = tx.id,
            sourceSystem = sourceHabayeb,
            originalTableName = TABLE_HABAYEB_TRANSACTIONS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }

    // -----------------------------------------------------------------
    // تفويض التراخيص وإدارة مجلدات النسخ الاحتياطي (Licensing & Files)
    // -----------------------------------------------------------------

    /** التحقق من حالة تفعيل التطبيق الدائم */
    fun isAppActivated(): Boolean = trialManager.isAppActivated()

    /** التحقق من انتهاء الفترة التجريبية استناداً لعدد المعاملات الكلي */
    suspend fun isTrialExpiredDirect(): Boolean = withContext(Dispatchers.IO) {
        val totalCount = getRealTotalTransactionsCount()
        trialManager.isTrialExpiredDirect(totalCount)
    }

    /** جلب المسار الأساسي لمجلدات النسخ الاحتياطي */
    fun getBaseBackupDirectory(): File = backupDirectoryManager.getBaseBackupDirectory()

    /** جلب مجلد النسخ الاحتياطي الشهري النشط */
    fun getBackupDirectory(): File = backupDirectoryManager.getBackupDirectory()

    /** البحث العودي عن كافة ملفات النسخ `.mzd` داخل المجلد */
    fun getAllMzdFilesRecursively(rootDir: File): List<File> = backupDirectoryManager.getAllMzdFilesRecursively(rootDir)

    // -----------------------------------------------------------------
    // الاستعادة الشاملة واسترجاع المحذوفات (Master Restore & Undo)
    // -----------------------------------------------------------------

    /** مسح وتفريغ كافة بيانات التطبيق المالية */
    suspend fun deleteAllData(): Unit = withContext(Dispatchers.IO) {
        restoreService.deleteAllData()
    }

    /** تنفيذ الاستعادة الذرية الشاملة لقاعدة البيانات من نص JSON */
    suspend fun executeMasterRestore(rawJsonString: String): FinanceRestoreResult = withContext(Dispatchers.IO) {
        restoreService.executeMasterRestore(rawJsonString)
    }

    /** استرجاع عنصر محذوف من سلة المهملات إلى جدوله الأصلي */
    suspend fun restoreDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.restoreDeletedItem(item)
    }

    /** استرجاع قيد فردي من داخل حزمة قيود محذوفة في سلة المهملات */
    suspend fun restoreSingleTransactionFromBundle(itemId: String, txId: String) = withContext(Dispatchers.IO) {
        trashDao.restoreSingleTransactionFromBundle(itemId, txId)
    }
}

