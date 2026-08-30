/**
 * =====================================================================
 * ملف: قاعدة البيانات الرئيسية للتطبيق (AppDatabase.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف الفئة التجريدية المركزية (Central Abstract Database Class)
 * المبنية على مكتبة Room Persistence Library التابعة لنظام أندرويد Jetpack.
 * إنه القلب النابض لإدارة التخزين المحلي الدائم لكافة بيانات التطبيق المالية والإعدادات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تسجيل كافة الكيانات والجداول (Entities Schema) المكونة لقاعدة البيانات.
 * 2. ربط محولات الأنواع المخصصة [BigDecimalConverter] لدعم الدقة المالية دون أخطاء الفاصلة العائمة.
 * 3. توفير نقاط الوصول التجريدية لجميع كائنات الوصول للبيانات (DAOs).
 * 4. تطبيق نمط النسخة الأحادية الآمنة خيطياً (Thread-Safe Singleton) لمنع فتح اتصالات متعددة تستهلك موارد الجهاز.
 * 5. تفعيل نمط التدوين المسبق (WAL - Write-Ahead Logging) لتسريع القراءة والكتابة المتزامنة.
 * 6. ربط سجل الهجرات الكامل (Migrations 1 to 31) لضمان ترقية قاعدة بيانات المستخدمين بأمان دون فقدان أي بيانات تاريخية.
 * 7. تنفيذ صيانة روابط المعاملات القديمة مرة واحدة أثناء ترقية قاعدة البيانات، خارج مسار فتح التطبيق.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم مكتبة Room ومكونات SQLite والكيانات ومحولات الأنواع
// ---------------------------------------------------------------------
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb

/**
 * [فئة قاعدة البيانات التجريدية - AppDatabase]:
 * 
 * [شرح التعليقات التوضيحية (Annotations)]:
 * - `@Database`: تعرف الفئة كقاعدة بيانات Room مع تحديد:
 *   1. `entities`: قائمة الكيانات (الجداول) المسجلة.
 *   2. `version = 32`: رقم الإصدار الهيكلي الحالي لقاعدة البيانات بعد الهجرات المتتالية.
 *   3. `exportSchema = false`: لتعطيل تصدير مخطط JSON أثناء البناء لتقليل الحجم.
 * - `@TypeConverters`: تسجيل محول الأنواع للتعامل مع العمليات الحسابية للأرقام العشرية الكبيرة بدقة.
 */
@Database(
    entities = [
        AppSettings::class,
        FixedCommitment::class,
        TransactionDb::class,
        CustomCategory::class,
        DeletedItemEntity::class,
        HabayebCustomer::class,
        HabayebTransaction::class
    ],
    version = 32,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // -----------------------------------------------------------------
    // دوال الوصول التجريدية لكائنات الوصول إلى البيانات (DAOs)
    // توفر Room التنفيذ الفعلي لهذه الدوال تلقائياً أثناء وقت الترجمة.
    // -----------------------------------------------------------------
    /** الوصول لجدول إعدادات التطبيق والعملة والنسخ الاحتياطي */
    abstract fun settingsDao(): SettingsDao
    /** الوصول لجدول الالتزامات والأقساط المالية الدورية */
    abstract fun commitmentDao(): CommitmentDao
    /** الوصول لجدول حركات اليومية والمعاملات المالية الرئيسية */
    abstract fun transactionDao(): TransactionDao
    /** الوصول لجدول التصنيفات المخصصة والأيقونات والألوان */
    abstract fun customCategoryDao(): CustomCategoryDao
    /** الوصول لجدول سلة المهملات والعناصر المحذوفة مؤقتاً */
    abstract fun trashDao(): TrashDao
    /** الوصول لجداول دفتر ديون الحبايب والعملاء وحركاتهم الحسابية */
    abstract fun habayebDao(): HabayebDao

    /**
     * [الكائن المرافق - Companion Object]:
     * يتولى إدارة دورة حياة قاعدة البيانات وتوفير مرجع أحادي عبر تطبيق نمط Singleton.
     */
    companion object {
        /** الاسم الفعلي لملف قاعدة البيانات المخزن على ذاكرة الجهاز */
        const val DATABASE_NAME = "mizan_al_dar_db"

        /**
         * المتغير المرجعي للنسخة الأحادية:
         * مُعلّم بـ `@Volatile` لضمان أن أي تعديل عليه يصبح مرئياً فوراً لجميع الخيوط المتزامنة (Threads).
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * [دالة الحصول على قاعدة البيانات - getDatabase]:
         * تنشئ قاعدة البيانات أو تعيد النسخة الموجودة مسبقاً بطريقة آمنة خيطياً (Double-checked Locking).
         *
         * [خطوات البناء والتهيئة]:
         * 1. فحص النسخة الأحادية؛ إذا كانت موجودة تُعاد فوراً دون قفل لتسريع الأداء.
         * 2. عند عدم وجودها، يتم استخدام كتلة المزامنة `synchronized(this)` لضمان عدم إنشاء نسختين متزامنتين.
         * 3. بناء القاعدة عبر `Room.databaseBuilder` مع تمرير سياق التطبيق العام لمنع تسريب الذاكرة (Memory Leaks).
         * 4. تفعيل نمط `WRITE_AHEAD_LOGGING` للسماح بالقراءة المتزامنة أثناء عمليات الكتابة.
         * 5. تسجيل مصفوفة الهجرات الكاملة `ALL_MIGRATIONS` لترقية الجداول القديمة بأمان.
         * 6. تسجيل الهجرة `MIGRATION_31_32` لتنظيف روابط المعاملات القديمة مرة واحدة أثناء الترقية فقط.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .addCallback(object : RoomDatabase.Callback() {
                })
                .build().also { INSTANCE = it }
            }
        }
    }
}

