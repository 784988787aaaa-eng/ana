package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import kotlinx.coroutines.flow.Flow

/**
 * الوصول إلى جدول التصنيفات المخصصة.
 * القراءة المستمرة تُعاد كتدفق، والعمليات الكتابية معلقة لمنع تجميد الواجهة.
 * ترتيب العرض محفوظ في الاستعلام حتى تبقى تجربة المستخدم مستقرة بعد الحفظ.
 */
@Dao
interface CustomCategoryDao {

    /** يعيد التصنيفات مرتبة حسب ترتيب العرض مع مراقبة التغيرات لحظياً. */
    @Query("SELECT * FROM custom_categories ORDER BY displayOrder ASC")
    fun getAllCustomCategoriesFlow(): Flow<List<CustomCategory>>

    /** يعيد لقطة مباشرة من التصنيفات للاستخدام في النسخ والتقارير والمعالجة الخلفية. */
    @Query("SELECT * FROM custom_categories ORDER BY displayOrder ASC")
    suspend fun getAllCustomCategoriesDirect(): List<CustomCategory>

    /** يضيف التصنيف أو يستبدل السجل ذي المفتاح نفسه وفق السلوك التاريخي. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)

    /** يحدث مجموعة التصنيفات دفعة واحدة مع إبقاء التنفيذ خارج خيط الواجهة. */
    @Update
    suspend fun updateCategories(categories: List<CustomCategory>)

    /** يحذف التصنيف المحدد مع تمرير الكيان الكامل لضمان تطابق المفتاح. */
    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    /** يمسح جميع التصنيفات عند الاستعادة أو إعادة الضبط المصرح بها. */
    @Query("DELETE FROM custom_categories")
    suspend fun clearAllCustomCategories()
}
