package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.finance.FinancialMath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal

/** بوابة الوصول لقيود دفتر اليومية مع الحفاظ على الدقة العشرية دون حسابات الفاصلة العائمة. */
@Dao
interface TransactionDao {

    /** يعيد جميع القيود كتدفق حي مع ترتيب زمني ثابت ومعرف كفاصل تعادل. */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id ASC")
    fun getAllTransactionsFlow(): Flow<List<TransactionDb>>

    /** يجلب صفحة محددة من القيود دون تحميل كامل الجدول إلى الذاكرة. */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getPagedTransactionsDirect(limit: Int, offset: Int): List<TransactionDb>

    /** يجلب مبالغ الدخل كنصوص عشرية يحولها محول قاعدة البيانات إلى قيم عشرية دقيقة قبل الجمع. */
    @Query("SELECT amount FROM transactions WHERE type = 'INCOME'")
    fun getIncomeAmountsFlow(): Flow<List<BigDecimal>>

    /** يجلب مبالغ المصروفات كنصوص عشرية يحولها محول قاعدة البيانات إلى قيم عشرية دقيقة قبل الجمع. */
    @Query("SELECT amount FROM transactions WHERE type = 'EXPENSE'")
    fun getExpenseAmountsFlow(): Flow<List<BigDecimal>>

    /** يجمع مبالغ الدخل والمصروفات في الذاكرة العشرية لتجنب تحويلها إلى أرقام عائمة داخل محرك قاعدة البيانات. */
    fun getTotalCashFlow(): Flow<BigDecimal> = combine(
        getIncomeAmountsFlow(),
        getExpenseAmountsFlow()
    ) { income, expense ->
        val totalIncome = income.fold(BigDecimal.ZERO, FinancialMath::add)
        val totalExpense = expense.fold(BigDecimal.ZERO, FinancialMath::add)
        FinancialMath.subtract(totalIncome, totalExpense)
    }

    /** يجلب مصروفات الفترة كنصوص عشرية دقيقة قبل إجراء الجمع المحاسبي. */
    @Query("SELECT amount FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    suspend fun getExpenseAmountsForPeriod(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<BigDecimal>

    /** يحسب مصروفات الفترة باستخدام الجمع العشري الموحد والتقريب المصرفي. */
    suspend fun getExpensesSumForPeriod(startTimestamp: Long, endTimestamp: Long): BigDecimal =
        getExpenseAmountsForPeriod(startTimestamp, endTimestamp)
            .fold(BigDecimal.ZERO, FinancialMath::add)

    /** يعيد عدد قيود اليومية كتدفق حي لتحديث واجهة المستخدم بعد كل تغيير. */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionsCountFlow(): Flow<Int>

    /** يعيد العدد الحالي لقيود اليومية عند الحاجة إلى قراءة مباشرة. */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionsCountDirect(): Int

    /** يسترجع قيداً واحداً بواسطة معرفه الفريد. */
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionDb?

    /** يدرج القيد أو يستبدل السجل المطابق عند التعارض للحفاظ على سلوك التوافق السابق. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionDb)

    /** يحذف القيد المحدد مع إبقاء التنفيذ غير حاجب لخيط الواجهة. */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionDb)

    /** يحذف قيداً بواسطة معرفه الفريد. */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    /** يفرغ جدول اليومية عند تنفيذ عمليات الاستعادة التي تتطلب إعادة بناء البيانات. */
    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
