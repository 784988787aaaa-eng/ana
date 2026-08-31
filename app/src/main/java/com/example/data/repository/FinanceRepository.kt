/*
 * =====================================================================
 * توثيق معماري وتعليمي — الدفعة 14
 * الملف: app/src/main/java/com/example/data/repository/FinanceRepository.kt
 * =====================================================================
 *
 * قاعدة الثبات: هذا الملف مبني على المصدر الأصلي دون تعديل أي تعليمة
 * تنفيذية. الإضافات التالية تعليقات فقط، والغرض منها تفسير البنية
 * سطراً بسطر باللغة العربية.
 */
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
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ووسائط التخزين والترقيم وصفحات Paging وقاعدة البيانات Room
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb
import com.example.domain.model.TransactionType
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
    private val sourceDar: String by lazy { context.getString(com.example.R.string.source_system_dar) }
    private val sourceHabayeb: String by lazy { context.getString(com.example.R.string.source_system_habayeb) }

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

/*
 * =====================================================================
 * // --- ملاحظات وتوصيات المعمارية البرمجية ---
 * =====================================================================
 * 1. لا يُسمح بتعديل السلوك التنفيذي لهذا الملف ضمن مسار التوثيق الحالي.
 * 2. عند التطوير المستقبلي، تُراجع مسؤوليات الملف مقابل مبادئ الفصل بين
 *    المسؤوليات (SRP) وتقليل الترابط قبل إدخال أي refactor.
 * 3. أي تغيير مقترح يجب أن يُنفذ في دفعة تطوير مستقلة، ثم يخضع لاختبارات
 *    الانحدار وتدقيق عقد البيانات/الواجهات قبل اعتماده.
 */

/* --- خريطة الشرح السطري ---
// السطر 1: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 2: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 3: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 4: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 5: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 6: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 7: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 8: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 9: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 10: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 11: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 12: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 13: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 14: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 15: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 16: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 17: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 18: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 19: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 20: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 21: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 22: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 23: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 24: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 25: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 26: يحدد الحزمة المنطقية التي ينتمي إليها الملف، وبالتالي نطاق أسماء أصناف المشروع.
// السطر 27: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 28: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 29: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 30: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 31: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 32: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 33: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 34: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 35: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 36: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 37: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 38: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 39: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 40: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 41: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 42: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 43: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 44: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 45: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 46: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 47: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 48: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 49: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 50: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 51: يستورد نوعاً أو دالة من مكتبة/طبقة أخرى لاستخدامها في هذا الملف.
// السطر 52: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 53: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 54: تعريف نوع/كائن معماري؛ يمثل نقطة تجميع للمسؤولية التي ينفذها الملف.
// السطر 55: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 56: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 57: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 58: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 59: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 60: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 61: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 62: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 63: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 64: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 65: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 66: تعريف نوع/كائن معماري؛ يمثل نقطة تجميع للمسؤولية التي ينفذها الملف.
// السطر 67: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 68: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 69: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 70: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 71: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 72: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 73: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 74: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 75: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 76: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 77: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 78: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 79: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 80: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 81: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 82: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 83: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 84: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 85: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 86: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 87: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 88: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 89: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 90: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 91: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 92: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 93: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 94: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 95: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 96: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 97: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 98: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 99: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 100: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 101: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 102: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 103: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 104: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 105: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 106: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 107: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 108: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 109: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 110: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 111: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 112: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 113: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 114: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 115: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 116: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 117: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 118: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 119: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 120: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 121: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 122: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 123: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 124: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 125: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 126: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 127: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 128: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 129: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 130: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 131: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 132: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 133: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 134: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 135: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 136: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 137: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 138: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 139: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 140: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 141: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 142: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 143: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 144: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 145: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 146: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 147: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 148: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 149: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 150: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 151: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 152: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 153: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 154: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 155: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 156: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 157: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 158: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 159: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 160: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 161: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 162: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 163: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 164: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 165: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 166: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 167: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 168: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 169: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 170: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 171: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 172: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 173: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 174: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 175: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 176: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 177: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 178: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 179: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 180: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 181: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 182: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 183: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 184: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 185: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 186: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 187: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 188: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 189: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 190: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 191: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 192: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 193: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 194: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 195: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 196: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 197: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 198: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 199: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 200: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 201: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 202: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 203: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 204: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 205: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 206: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 207: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 208: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 209: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 210: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 211: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 212: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 213: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 214: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 215: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 216: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 217: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 218: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 219: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 220: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 221: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 222: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 223: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 224: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 225: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 226: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 227: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 228: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 229: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 230: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 231: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 232: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 233: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 234: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 235: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 236: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 237: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 238: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 239: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 240: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 241: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 242: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 243: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 244: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 245: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 246: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 247: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 248: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 249: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 250: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 251: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 252: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 253: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 254: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 255: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 256: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 257: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 258: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 259: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 260: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 261: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 262: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 263: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 264: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 265: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 266: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 267: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 268: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 269: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 270: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 271: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 272: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 273: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 274: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 275: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 276: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 277: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 278: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 279: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 280: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 281: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 282: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 283: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 284: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 285: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 286: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 287: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 288: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 289: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 290: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 291: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 292: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 293: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 294: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 295: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 296: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 297: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 298: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 299: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 300: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 301: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 302: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 303: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 304: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 305: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 306: تحكم في تدفق التنفيذ؛ يحدد أي مسار منطقي سيُنفذ وفق الحالة أو البيانات الحالية.
// السطر 307: تحكم في تدفق التنفيذ؛ يحدد أي مسار منطقي سيُنفذ وفق الحالة أو البيانات الحالية.
// السطر 308: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 309: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 310: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 311: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 312: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 313: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 314: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 315: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 316: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 317: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 318: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 319: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 320: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 321: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 322: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 323: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 324: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 325: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 326: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 327: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 328: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 329: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 330: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 331: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 332: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 333: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 334: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 335: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 336: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 337: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 338: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 339: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 340: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 341: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 342: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 343: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 344: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 345: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 346: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 347: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 348: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 349: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 350: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 351: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 352: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 353: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 354: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 355: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 356: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 357: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 358: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 359: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 360: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 361: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 362: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 363: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 364: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 365: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 366: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 367: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 368: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 369: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 370: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 371: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 372: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 373: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 374: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 375: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 376: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 377: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 378: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 379: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 380: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 381: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 382: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 383: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 384: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 385: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 386: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 387: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 388: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 389: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 390: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 391: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 392: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 393: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 394: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 395: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 396: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 397: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 398: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 399: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 400: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 401: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 402: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 403: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 404: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 405: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 406: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 407: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 408: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 409: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 410: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 411: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 412: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 413: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 414: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 415: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 416: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 417: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 418: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 419: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 420: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 421: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 422: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 423: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 424: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 425: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 426: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 427: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 428: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 429: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 430: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 431: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 432: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 433: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 434: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 435: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 436: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 437: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 438: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 439: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 440: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 441: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 442: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 443: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 444: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 445: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 446: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 447: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 448: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 449: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 450: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 451: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 452: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 453: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 454: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 455: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 456: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 457: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 458: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 459: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 460: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 461: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 462: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 463: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 464: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 465: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 466: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 467: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 468: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 469: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 470: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 471: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 472: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 473: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 474: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 475: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 476: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 477: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 478: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 479: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 480: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 481: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 482: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 483: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 484: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 485: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 486: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 487: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 488: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 489: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 490: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 491: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 492: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 493: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 494: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 495: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 496: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 497: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 498: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 499: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 500: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 501: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 502: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 503: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 504: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 505: إسناد قيمة/نتيجة إلى متغير أو خاصية؛ يجب الحفاظ على التعبير كما هو لأنه جزء من السلوك التنفيذي.
// السطر 506: تعليمة تنفيذية/تعبير برمجي؛ تم الإبقاء عليه حرفياً، ودوره يُفهم ضمن السياق البنيوي المحيط به.
// السطر 507: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 508: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 509: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 510: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 511: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 512: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 513: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 514: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 515: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 516: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 517: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 518: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 519: تعريف خاصية/قيمة حالة أو إعداد؛ نوعها ونطاقها جزء من عقد الملف.
// السطر 520: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 521: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 522: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 523: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 524: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 525: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 526: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 527: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 528: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 529: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 530: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 531: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 532: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 533: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 534: تعليق مصدر أصلي يشرح السياق ولا يغير السلوك التنفيذي.
// السطر 535: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 536: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 537: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 538: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 539: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 540: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 541: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 542: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 543: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 544: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 545: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 546: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 547: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 548: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 549: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 550: سطر فارغ للفصل البصري بين وحدات الشيفرة.
// السطر 551: جزء من KDoc/تعليق توثيقي أصلي؛ لا يُنفذ كتعليمة برمجية.
// السطر 552: تعريف دالة؛ هذا السطر يحدد واجهة الاستدعاء بينما تنفذ الأسطر التابعة السلوك الفعلي.
// السطر 553: استدعاء دالة/منشئ أو تعبير برمجي ينفذ خطوة من خطوات المعالجة.
// السطر 554: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 555: حد بنيوي لكتلة برمجية (بداية/نهاية) يحافظ على نطاق المتغيرات وتسلسل التنفيذ.
// السطر 556: سطر فارغ للفصل البصري بين وحدات الشيفرة.
*/
