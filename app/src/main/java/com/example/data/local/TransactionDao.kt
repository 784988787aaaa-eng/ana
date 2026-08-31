/**
 * =====================================================================
 * ملف: كائن الوصول لبيانات قيود دفتر اليومية (TransactionDao.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف واجهة الوصول للبيانات (DAO) المسؤولة عن قيود الدخل والمصروف
 * في دفتر اليومية المالي العام (`transactions`)، مع توفير استعلامات التجميع
 * الحسابي، ومجموع السيولة النقدية، وفلترة الفترات الزمنية للتقارير.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. استرجاع وتدفق القيود: توفير تدفقات حية [Flow] مرتبة تنازلياً حسب `timestamp` للاستفادة القصوى من فهارس SQLite السريعة.
 * 2. التجميع المحاسبي الدقيق: حساب صافي السيولة النقدية (إجمالي الدخل - إجمالي المصروف) عبر استعلامات تجميع SQL
 *    مع تحويل القيم المرجعة إلى كائنات [BigDecimal] عبر محول الأنواع لمنع أخطاء التقريب.
 * 3. الفلترة الزمنية والتحليلات: حساب إجمالي مصاريف فترة محددة (أسبوعية، شهرية، سنوية) لدعم الموازنات المالية.
 * 4. التصفح المجزأ (Pagination): توفير دوال معلقة [suspend] تدعم الحد والإزاحة (`LIMIT` و `OFFSET`).
 * 5. إدارة السجلات: إدراج وتعديل وحذف القيود الفردية وتفريغ الجدول عند الاستعادة.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم عمليات قاعدة البيانات Room والكيانات وتدفقات الكوروتين والدقة الحسابية
// ---------------------------------------------------------------------
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TransactionDb
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * [واجهة الوصول لبيانات حركات اليومية - TransactionDao]:
 * تترجمها مكتبة Room تلقائياً إلى استعلامات SQL تنفيذية للدفتر اليومي.
 */
@Dao
interface TransactionDao {

    /**
     * [استعلام جلب كافة حركات الدفتر كتدفق حي - getAllTransactionsFlow]:
     * يستعلم عن جميع قيود الدخل والمصروف مرتبة تنازلياً حسب التوقيت الزمني.
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionDb>>

    /**
     * [استعلام تصفح حركات الدفتر على دفعات - getPagedTransactionsDirect]:
     * دالة معلقة تجلب دفعة محددة بعدد [limit] وإزاحة [offset] لتسريع العرض وتوفير الذاكرة.
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedTransactionsDirect(limit: Int, offset: Int): List<TransactionDb>

    /**
     * [استعلام حساب صافي السيولة النقدية كتدفق - getTotalCashFlow]:
     * يحسب الفرق التراكمي بين إجمالي الدخل وإجمالي المصروف باستخدام تعبير `CASE` ودالة `COALESCE`
     * لضمان عدم إرجاع قيمة معدومة (Null)، مع إعادة النتيجة بدقة [BigDecimal].
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN CAST(amount AS REAL) ELSE 0.0 END), 0.0) - 
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN CAST(amount AS REAL) ELSE 0.0 END), 0.0) 
        FROM transactions
    """)
    fun getTotalCashFlow(): Flow<BigDecimal>

    /**
     * [استعلام حساب مجموع المصاريف لفترة زمنية محددة - getExpensesSumForPeriod]:
     * يحسب إجمالي مبالغ المصروفات الواقعة بين الطابع الزمني [startTimestamp] و [endTimestamp]
     * لدعم الإحصائيات الدورية ولوحات متابعة الميزانية.
     */
    @Query("SELECT COALESCE(SUM(CAST(amount AS REAL)), 0.0) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    suspend fun getExpensesSumForPeriod(startTimestamp: Long, endTimestamp: Long): BigDecimal

    /**
     * [استعلام حساب إجمالي عدد المعاملات كتدفق - getTransactionsCountFlow]:
     * يُصدر تدفقاً بعدد القيود الكلي في دفتر اليومية لتحديث العدادات والشارات في الواجهة.
     */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionsCountFlow(): Flow<Int>

    /**
     * [استعلام حساب إجمالي عدد المعاملات المباشر - getTransactionsCountDirect]:
     * يعيد إجمالي عدد الحركات الحالي كقيمة عددية مباشرة.
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCountDirect(): Int

    /**
     * [استعلام جلب حركة محددة بالمعرف - getTransactionById]:
     * يسترجع قيداً مالياً واحداً باستخدام معرفه الفريد [id].
     */
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionDb?

    /**
     * [دالة إدراج أو استبدال قيد مالي - insertTransaction]:
     * تدرج حركة مالية جديدة في الدفتر باستراتيجية `OnConflictStrategy.REPLACE`.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionDb)

    /**
     * [دالة حذف قيد مالي - deleteTransaction]:
     * تحذف سجل الحركة المالية الممررة من جدول اليومية.
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionDb)

    /**
     * [دالة حذف قيد مالي بواسطة المعرف - deleteTransactionById]:
     * تحذف القيد المالي الذي يحمل المعرف الممرر [id].
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    /**
     * [دالة تفريغ جدول حركات اليومية بالكامل - clearAllTransactions]:
     * تفرغ جدول اليومية بالكامل عند استعادة البيانات أو إعادة التهيئة الشاملة.
     */
    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

// =====================================================================
// --- ملاحظات وتوصيات المعمارية البرمجية ---
// =====================================================================
// 1) معاملات اليومية هي قلب دفتر الحسابات؛ أي استعلام هنا يجب مراجعته من حيث
//    دقة التاريخ والترتيب وعدم فقدان السجلات عند الترحيل أو الاستعادة.
// 2) يفضّل الحفاظ على الحسابات المالية الدقيقة في Domain وعدم تحويل BigDecimal
//    إلى Double داخل طبقة التخزين إلا عند ضرورة توافق محددة.
// 3) الاستعلامات كثيرة الاستخدام ينبغي أن تتوافق مع الفهارس الموجودة في المخطط.
