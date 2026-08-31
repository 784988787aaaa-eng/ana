/**
 * =====================================================================
 * ملف: كائن الوصول لبيانات الالتزامات المالية (CommitmentDao.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف واجهة الوصول إلى البيانات (Data Access Object - DAO) الخاصة
 * بجدول الالتزامات المالية والأقساط الشهرية الثابتة (`fixed_commitments`).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. استرجاع قائمة الالتزامات كتدفق تفاعلي حي مستمر [Flow] لتحديث واجهات Compose فوراً عند أي تغيير.
 * 2. الحفاظ على الترتيب المخصص للالتزامات تصاعدياً بحسب حقل `orderIndex`.
 * 3. إضافة أو استبدال الالتزامات باستراتيجية استبدال ذكية `OnConflictStrategy.REPLACE`.
 * 4. تحديث مجموعات الالتزامات دفعة واحدة لتطبيق إعادة الترتيب أو تعديل المبالغ.
 * 5. حذف التزام محدد بالاسم أو مسح الجدول بالكامل عند عمليات استعادة النسخ الاحتياطية.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم مكتبة Room لعمليات قواعد البيانات والكيان وتدفقات الكوروتين
// ---------------------------------------------------------------------
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.FixedCommitment
import kotlinx.coroutines.flow.Flow

/**
 * [واجهة الوصول لبيانات الالتزامات الثابتة - CommitmentDao]:
 * تولد مكتبة Room التنفيذ البرمجي الكامل لهذه الدوال أثناء الترجمة.
 */
@Dao
interface CommitmentDao {

    /**
     * [استعلام جلب كافة الالتزامات كتدفق حي - getAllCommitmentsFlow]:
     * يستعلم عن جميع سجلات الالتزامات من جدول `fixed_commitments` مرتبة تصاعدياً
     * بحسب مؤشر الترتيب `orderIndex`.
     * يُرجع [Flow] يُصدر قائمة جديدة تلقائياً بمجرد إدراج أو تعديل أو حذف أي التزام.
     */
    @Query("SELECT * FROM fixed_commitments ORDER BY orderIndex ASC")
    fun getAllCommitmentsFlow(): Flow<List<FixedCommitment>>

    /**
     * [دالة إدراج أو استبدال التزام - insertCommitment]:
     * تدرج التزاماً مالياً جديداً، وإذا وُجد التزام بنفس المفتاح الأساسي (الاسم)،
     * يتم استبدال السجل القديم بالبيانات الجديدة تلقائياً.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: FixedCommitment)

    /**
     * [دالة تحديث قائمة التزامات - updateCommitments]:
     * تحدث بيانات مجموعة من الالتزامات دفعة واحدة، وتُستخدم بكثرة عند قيام المستخدم
     * بإعادة ترتيب قائمة الالتزامات في واجهة الإعدادات.
     */
    @Update
    suspend fun updateCommitments(commitments: List<FixedCommitment>)

    /**
     * [دالة حذف التزام محدد بالاسم - deleteCommitment]:
     * تحذف الالتزام المالي الذي يطابق الاسم الممرر من جدول الالتزامات.
     */
    @Query("DELETE FROM fixed_commitments WHERE name = :name")
    suspend fun deleteCommitment(name: String)

    /**
     * [دالة مسح كافة الالتزامات - clearAllCommitments]:
     * تفرغ جدول الالتزامات بالكامل، وتُستدعى عند استعادة نسخة احتياطية جديدة
     * أو تصفير بيانات الحساب بالكامل.
     */
    @Query("DELETE FROM fixed_commitments")
    suspend fun clearAllCommitments()
}

// =====================================================================
// --- ملاحظات وتوصيات المعمارية البرمجية ---
// =====================================================================
// 1) Flow مناسب لتغذية Compose بتغيرات الالتزامات بصورة تفاعلية.
// 2) REPLACE في الإدراج يعتمد على هوية المفتاح الأساسي؛ يجب تدقيق أثره عند تغيير
//    المفتاح مستقبلاً حتى لا يتحول التحديث إلى حذف/إعادة إدراج غير مقصود.
// 3) عمليات clearAllCommitments حساسة أثناء الاستعادة، لذا يُفضّل مستقبلاً أن
//    تُنسّق مع Transaction على مستوى Repository لا داخل DAO نفسه.
