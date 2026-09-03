/**
 * =====================================================================
 * ملف: كائن الوصول لبيانات التصنيفات المخصصة (CustomCategoryDao.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف واجهة الوصول للبيانات (DAO) الخاصة بجدول التصنيفات المخصصة
 * (`custom_categories`) التي ينشئها المستخدم لتنظيم وتصنيف مصاريفه وإيراداته
 * مع أيقونات وألوان وترتيب عرض خاص.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. توفير تدفق حي [Flow] للتصنيفات مرتبة تصاعدياً بحسب `displayOrder` لتحديث قوائم الاختيار في واجهات الإدخال.
 * 2. توفير دالة استعلام مباشر معلقة [suspend] لتجميع بيانات التصنيفات أثناء النسخ الاحتياطي والتقارير.
 * 3. إدراج أو استبدال التصنيفات باستراتيجية `OnConflictStrategy.REPLACE`.
 * 4. تحديث مجموعات التصنيفات لضبط ترتيب الظهور المخصص.
 * 5. حذف تصنيف مخصص محدد أو تفريغ الجدول بالكامل عند عمليات التهيئة والاستعادة.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم عمليات قاعدة البيانات Room والكيان والتدفقات التفاعلية
// ---------------------------------------------------------------------
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CustomCategory
import kotlinx.coroutines.flow.Flow

/**
 * [واجهة الوصول لبيانات التصنيفات المخصصة - CustomCategoryDao]:
 * تترجمها مكتبة Room تلقائياً إلى استعلامات SQL تنفيذية سريعة ومحسنة.
 */
@Dao
interface CustomCategoryDao {

    /**
     * [استعلام جلب كافة التصنيفات كتدفق تفاعلي - getAllCustomCategoriesFlow]:
     * يستعلم عن جميع التصنيفات من جدول `custom_categories` مرتبة حسب حقل `displayOrder`.
     * يُصدر تدفق [Flow] يتفاعل لحظياً مع إضافة أو تعديل أو حذف أي تصنيف لتحديث الواجهات.
     */
    @Query("SELECT * FROM custom_categories ORDER BY displayOrder ASC")
    fun getAllCustomCategoriesFlow(): Flow<List<CustomCategory>>

    /**
     * [استعلام جلب كافة التصنيفات كقائمة مباشرة - getAllCustomCategoriesDirect]:
     * دالة معلقة (suspend) تجلب لقطة سريعة لجميع التصنيفات الحالية كقائمة عادية.
     * تُستخدم في خدمات النسخ الاحتياطي وحسابات التقارير التي لا تحتاج إلى مراقبة مستمرة.
     */
    @Query("SELECT * FROM custom_categories ORDER BY displayOrder ASC")
    suspend fun getAllCustomCategoriesDirect(): List<CustomCategory>

    /**
     * [دالة إدراج أو تحديث تصنيف - insertCategory]:
     * تدرج تصنيفاً جديداً في الجدول، أو تستبدل التصنيف القديم إذا كان يحمل نفس المعرف الأساسي.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)

    /**
     * [دالة تحديث قائمة تصنيفات دفعة واحدة - updateCategories]:
     * تُستخدم عند قيام المستخدم بإعادة ترتيب مواقع التصنيفات بالسحب والإفلات في الواجهة.
     */
    @Update
    suspend fun updateCategories(categories: List<CustomCategory>)

    /**
     * [دالة حذف تصنيف محدد - deleteCategory]:
     * تحذف سجل التصنيف الممرر من جدول قاعدة البيانات.
     */
    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    /**
     * [دالة مسح كافة التصنيفات المخصصة - clearAllCustomCategories]:
     * تفرغ جدول التصنيفات المخصصة بالكامل عند استعادة البيانات أو إعادة ضبط المصنع.
     */
    @Query("DELETE FROM custom_categories")
    suspend fun clearAllCustomCategories()
}

