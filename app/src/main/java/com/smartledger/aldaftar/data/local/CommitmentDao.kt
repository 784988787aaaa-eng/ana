package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import kotlinx.coroutines.flow.Flow

/**
 * الوصول إلى جدول الالتزامات الثابتة.
 * القراءة المستمرة تُعاد كتدفق، والكتابة معلقة لمنع تنفيذ قاعدة البيانات على الواجهة.
 * استبدال السجل عند تعارض المفتاح يحافظ على سلوك الحفظ التاريخي.
 */
@Dao
interface CommitmentDao {

    /** يعيد الالتزامات مرتبة حسب ترتيب العرض مع تحديث حي عند تغير الجدول. */
    @Query("SELECT * FROM fixed_commitments ORDER BY orderIndex ASC")
    fun getAllCommitmentsFlow(): Flow<List<FixedCommitment>>

    /** يضيف الالتزام أو يستبدل السجل ذي المفتاح نفسه وفق سلوك التخزين المعتمد. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: FixedCommitment)

    /** يحدث مجموعة الالتزامات دفعة واحدة مع إبقاء التنفيذ خارج خيط الواجهة. */
    @Update
    suspend fun updateCommitments(commitments: List<FixedCommitment>)

    /** يحذف الالتزام المطابق لاسمه دون المساس ببقية السجلات. */
    @Query("DELETE FROM fixed_commitments WHERE name = :name")
    suspend fun deleteCommitment(name: String)

    /** يمسح جميع الالتزامات عند إجراءات الاستعادة أو إعادة الضبط المصرح بها. */
    @Query("DELETE FROM fixed_commitments")
    suspend fun clearAllCommitments()
}
