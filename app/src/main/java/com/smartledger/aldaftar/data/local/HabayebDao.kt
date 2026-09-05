/**
 * =====================================================================
 * ملف: كائن الوصول لبيانات دفتر ديون الحبايب (HabayebDao.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف واجهة الوصول إلى البيانات (DAO) المسؤولة عن كافة عمليات القراءة
 * والكتابة لدفتر الديون والمعاملات المالية الشخصية والتجارية "ديون الحبايب"،
 * بما يشمل إدارة ملفات العملاء (`habayeb_customers`) ومعاملاتهم الدائنة والمدينة (`habayeb_transactions`).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. استعلامات العملاء: تدفقات حية [Flow] لبطاقات العملاء، ودوال معلقة مباشرة [suspend] لعمليات النسخ والمزامنة.
 * 2. استعلامات المعاملات: دعم المعاملات المحلية والأجنبية، وحساب عدد السجلات، واستعلامات كشف الحساب الفردي.
 * 3. التصفح والصفحات المحددة (Pagination): دعم مصدر البيانات [PagingSource] للتكامل مع مكتبة Paging 3 لكشوف الحسابات الطويلة.
 * 4. عمليات التكيف وتغيير طبيعة الحساب (Account Adaptation): تعديل اتجاه الحسابات دفعة واحدة باستخدام استعلامات SQL CASE الذكية.
 * 5. العمليات الذرية المركبة [@Transaction]: تنفيذ العمليات المتعددة (مثل إنشاء عميل برصيد افتتاحي أو حذف عميل وحركاته) داخل معاملة قاعدة بيانات ذرية واحدة لمنع تشوه البيانات.
 */
package com.smartledger.aldaftar.data.local

// ---------------------------------------------------------------------
// استيراد حزم مكتبة Room ومكتبة الصفحات Paging والكيانات وتدفقات الكوروتين
// ---------------------------------------------------------------------
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import kotlinx.coroutines.flow.Flow

/**
 * [واجهة الوصول لبيانات ديون الحبايب - HabayebDao]:
 * توفر عمليات الاستعلام والإدراج والتعديل والحذف للعملاء وحركاتهم الحسابية.
 */
@Dao
interface HabayebDao {

    // =================================================================
    // 1. استعلامات ملفات العملاء (Customer Queries)
    // =================================================================

    /**
     * [استعلام جلب كافة العملاء كتدفق حي - getAllCustomersFlow]:
     * يستعلم عن جميع العملاء من جدول `habayeb_customers` مرتبين تنازلياً حسب تاريخ الإنشاء.
     * يُصدر تدفقاً مستمراً لتحديث شاشة قائمة العملاء فوراً عند إضافة أو تعديل أي عميل.
     */
    @Query("SELECT * FROM habayeb_customers ORDER BY createdAt DESC")
    fun getAllCustomersFlow(): Flow<List<HabayebCustomer>>

    /**
     * [استعلام جلب كافة العملاء كقائمة مباشرة - getAllCustomersDirect]:
     * دالة معلقة تجلب لقطة فورية لجميع العملاء، وتُستخدم في تقارير إجمالي الأرصدة والنسخ الاحتياطي.
     */
    @Query("SELECT * FROM habayeb_customers")
    suspend fun getAllCustomersDirect(): List<HabayebCustomer>

    /**
     * [استعلام جلب عميل بواسطة المعرف - getCustomerByIdDirect]:
     * يسترجع بيانات عميل محدد باستخدام معرفه الفريد [id].
     */
    @Query("SELECT * FROM habayeb_customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: String): HabayebCustomer?

    /**
     * [دالة إدراج أو استبدال عميل - insertCustomer]:
     * تدرج عميلاً جديداً أو تستبدل بياناته إذا كان معرفه مسجلاً مسبقاً.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: HabayebCustomer)

    /**
     * [دالة تحديث بيانات عميل - updateCustomer]:
     * تحدث سجل العميل في جدول قاعدة البيانات.
     */
    @Update
    suspend fun updateCustomer(customer: HabayebCustomer)

    /**
     * [دالة تحديث اسم العميل فقط - updateCustomerName]:
     * تعدل اسم العميل بشكل مباشر وسريع دون الحاجة لتحميل وتحديث كامل السجل.
     */
    @Query("UPDATE habayeb_customers SET name = :newName WHERE id = :id")
    suspend fun updateCustomerName(id: String, newName: String)

    /**
     * [دالة حذف كائن عميل - deleteCustomer]:
     * تحذف سجل العميل الممرر من قاعدة البيانات.
     */
    @Delete
    suspend fun deleteCustomer(customer: HabayebCustomer)

    /**
     * [دالة حذف عميل بواسطة المعرف - deleteCustomerById]:
     * تحذف العميل الذي يحمل المعرف الممرر [id].
     */
    @Query("DELETE FROM habayeb_customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    /**
     * [دالة تفريغ جدول العملاء بالكامل - clearAllCustomers]:
     * تفرغ جدول العملاء، وتُستخدم عند استعادة نسخة احتياطية أو تصفير الحسابات.
     */
    @Query("DELETE FROM habayeb_customers")
    suspend fun clearAllCustomers()


    // =================================================================
    // 2. استعلامات حركات الديون والمعاملات (Transaction Queries)
    // =================================================================

    /**
     * [استعلام جلب كافة معاملات الحبايب كتدفق حي - getAllTransactionsFlow]:
     * يستعلم عن جميع المعاملات في جدول `habayeb_transactions` مرتبة تنازلياً حسب التوقيت.
     */
    @Query("SELECT * FROM habayeb_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<HabayebTransaction>>

    /**
     * [استعلام جلب معاملات عميل محدد كتدفق حي - getTransactionsForCustomerFlow]:
     * يسترجع حركات كشف الحساب الخاصة بعميل معين كتدفق تفاعلي مستمر.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<HabayebTransaction>>

    /**
     * [استعلام جلب معاملات عميل محدد كقائمة مباشرة - getTransactionsForCustomerDirect]:
     * دالة معلقة تجلب كشف حساب العميل دفعة واحدة لتوليد تقارير PDF أو ملفات Excel.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    suspend fun getTransactionsForCustomerDirect(customerId: String): List<HabayebTransaction>

    /**
     * [استعلام جلب كافة المعاملات كقائمة مباشرة - getAllTransactionsDirect]:
     * تجلب جميع سجلات المعاملات للتطبيق لتجميع حزمة النسخ الاحتياطي الشاملة.
     */
    @Query("SELECT * FROM habayeb_transactions")
    suspend fun getAllTransactionsDirect(): List<HabayebTransaction>

    /**
     * [استعلام جلب معاملة محددة بالمعرف - getTransactionById]:
     * يسترجع حركة مالية مفردة للتحقق منها أو تعديل تفاصيلها.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): HabayebTransaction?

    /**
     * [استعلام جلب المعاملات بالعملات الأجنبية كتدفق - getForeignTransactionsFlow]:
     * يستعلم عن كافة الحركات التي تمت بعملات أجنبية (`is_foreign = 1`) لحساب إجماليات العملات والصرافة.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE is_foreign = 1")
    fun getForeignTransactionsFlow(): Flow<List<HabayebTransaction>>

    /**
     * [دالة إدراج أو استبدال معاملة - insertTransaction]:
     * تدرج معاملة جديدة في كشف الحساب مع استبدال أي معاملة سابقة مطابقة للمعرف.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: HabayebTransaction)

    /**
     * [دالة حذف معاملة - deleteTransaction]:
     * تحذف حركة مالية محددة من جدول المعاملات.
     */
    @Delete
    suspend fun deleteTransaction(transaction: HabayebTransaction)

    /**
     * [دالة حذف كافة معاملات عميل محدد - deleteTransactionsByCustomer]:
     * تحذف جميع قيود كشف الحساب المرتبطة بعميل معين عند حذفه أو تصفية حسابه.
     */
    @Query("DELETE FROM habayeb_transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsByCustomer(customerId: String)

    /**
     * [دالة حذف معاملة بواسطة المعرف - deleteTransactionById]:
     * تحذف معاملة مفردة باستخدام معرفها الفريد.
     */
    @Query("DELETE FROM habayeb_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    /**
     * [دالة تفريغ جدول المعاملات بالكامل - clearAllTransactions]:
     * تفرغ جدول حركات الديون بالكامل عند استعادة البيانات.
     */
    @Query("DELETE FROM habayeb_transactions")
    suspend fun clearAllTransactions()

    /**
     * [استعلام حساب إجمالي عدد المعاملات كتدفق - getHabayebTransactionsCountFlow]:
     * يُصدر تدفقاً بعدد المعاملات الكلي لعرض الإحصائيات في شاشات لوحة التحكم.
     */
    @Query("SELECT COUNT(*) FROM habayeb_transactions")
    fun getHabayebTransactionsCountFlow(): Flow<Int>

    /**
     * [استعلام حساب إجمالي عدد المعاملات المباشر - getHabayebTransactionsCountDirect]:
     * يعيد إجمالي عدد المعاملات الحالي كقيمة عددية مباشرة.
     */
    @Query("SELECT COUNT(*) FROM habayeb_transactions")
    suspend fun getHabayebTransactionsCountDirect(): Int


    // =================================================================
    // 3. التصفح والصفحات المحددة (Pagination & Filtered Queries)
    // =================================================================

    /**
     * [مصدر بيانات الصفحات لكشف حساب العميل - getTransactionsPagingSourceForCustomer]:
     * يرجع [PagingSource] للتكامل المباشر مع مكتبة Jetpack Paging 3 لتحميل كشوف الحسابات
     * الكبيرة بمرونة وكفاءة عالية دون استهلاك زائد للذاكرة.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsPagingSourceForCustomer(customerId: String): PagingSource<Int, HabayebTransaction>

    /**
     * [استعلام جلب جزء محدد من المعاملات - getTransactionsForCustomerPaged]:
     * يسترجع دفعة محددة الحجم [limit] مع إزاحة [offset] لتنفيذ التصفح اليدوي.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsForCustomerPaged(customerId: String, limit: Int, offset: Int): List<HabayebTransaction>

    /**
     * [استعلام جلب أحدث المعاملات بحد أقصى كتدفق - getTransactionsForCustomerWithLimitFlow]:
     * يجلب أحدث N معاملة لعميل معين لعرضها في البطاقات المصغرة أو ملخص الحساب.
     */
    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC LIMIT :limit")
    fun getTransactionsForCustomerWithLimitFlow(customerId: String, limit: Int): Flow<List<HabayebTransaction>>


    // =================================================================
    // 4. عمليات التكيف والعمليات الذرية المركبة (@Transaction)
    // =================================================================

    /**
     * [تعديل معاملات العميل لتصبح دائنة له - adaptTransactionsToOwedByThem]:
     * يستبدل أنواع المعاملات الخاصة بعميل محدد باستخدام تعبير SQL CASE لتحويل نوع الدين بسلاسة.
     */
    @Query("""
        UPDATE habayeb_transactions 
        SET type = CASE type 
            WHEN 'OWED_TO_THEM' THEN 'OWED_BY_THEM' 
            WHEN 'PAYMENT_TO_THEM' THEN 'PAYMENT_BY_THEM' 
            ELSE type 
        END 
        WHERE customerId = :customerId AND type IN ('OWED_TO_THEM', 'PAYMENT_TO_THEM')
    """)
    suspend fun adaptTransactionsToOwedByThem(customerId: String)

    /**
     * [تعديل معاملات العميل لتصبح مدينة عليه - adaptTransactionsToOwedToThem]:
     * يعكس أنواع المعاملات الخاصة بالعميل باستخدام تعبير SQL CASE.
     */
    @Query("""
        UPDATE habayeb_transactions 
        SET type = CASE type 
            WHEN 'OWED_BY_THEM' THEN 'OWED_TO_THEM' 
            WHEN 'PAYMENT_BY_THEM' THEN 'PAYMENT_TO_THEM' 
            ELSE type 
        END 
        WHERE customerId = :customerId AND type IN ('OWED_BY_THEM', 'PAYMENT_BY_THEM')
    """)
    suspend fun adaptTransactionsToOwedToThem(customerId: String)

    /**
     * [إدراج عميل مع معاملته الافتتاحية ذرياً - insertCustomerWithOpeningTransaction]:
     * عملية مركبة تدرج ملف العميل والمعاملة الافتتاحية معاً داخل كتلة ذرية `@Transaction`
     * تضمن نجاح العمليتين معاً أو التراجع الكامل عند أي فشل.
     */
    @Transaction
    suspend fun insertCustomerWithOpeningTransaction(customer: HabayebCustomer, transaction: HabayebTransaction?) {
        insertCustomer(customer)
        if (transaction != null) {
            insertTransaction(transaction)
        }
    }

    /**
     * [إدراج مجموعة معاملات دفعة واحدة ذرياً - insertTransactionsBatch]:
     * تدرج قائمة معاملات كاملة داخل معاملة ذرية واحدة لتسريع عملية الاستيراد والحفظ.
     */
    @Transaction
    suspend fun insertTransactionsBatch(transactions: List<HabayebTransaction>) {
        for (tx in transactions) {
            insertTransaction(tx)
        }
    }

    /**
     * [حذف عميل مع كافة معاملاته ذرياً - deleteCustomerAndTransactions]:
     * تضمن مسح جميع معاملات العميل ثم مسح بطاقة العميل نفسه في خطوة ذرية موحدة.
     */
    @Transaction
    suspend fun deleteCustomerAndTransactions(customerId: String) {
        deleteTransactionsByCustomer(customerId)
        deleteCustomerById(customerId)
    }

    /**
     * [حذف مجموعة عملاء ومعاملاتهم دفعة واحدة - deleteCustomersAndTransactionsBatch]:
     * تحذف قائمة من العملاء وحركاتهم الحسابية ذرياً عند الحذف المتعدد في واجهة المستخدم.
     */
    @Transaction
    suspend fun deleteCustomersAndTransactionsBatch(customerIds: List<String>) {
        for (id in customerIds) {
            deleteTransactionsByCustomer(id)
            deleteCustomerById(id)
        }
    }
}

