/**
 * =====================================================================
 * ملف: قاعدة البيانات الرئيسية للتطبيق (قاعدة البيانات.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف الفئة التجريدية المركزية (الفئة المركزية التجريدية لقاعدة البيانات)
 * المبنية على مكتبة المكتبة المحلية التابعة لنظام أندرويد منظومة أندرويد.
 * إنه القلب النابض لإدارة التخزين المحلي الدائم لكافة بيانات التطبيق المالية والإعدادات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تسجيل كافة الكيانات والجداول (مخطط الكيانات) المكونة لقاعدة البيانات.
 * 2. ربط محولات الأنواع المخصصة [محول الأرقام العشرية] لدعم الدقة المالية دون أخطاء الفاصلة العائمة.
 * 3. توفير نقاط الوصول التجريدية لجميع كائنات الوصول للبيانات (كائنات الوصول للبيانات).
 * 4. تطبيق نمط النسخة الأحادية الآمنة خيطياً (نسخة أحادية آمنة خيطياً) لمنع فتح اتصالات متعددة تستهلك موارد الجهاز.
 * 5. تفعيل نمط التدوين المسبق (التدوين المسبق - التدوين المسبق) لتسريع القراءة والكتابة المتزامنة.
 * 6. ربط سجل الهجرات الكامل (الهجرات 1 31) لضمان ترقية قاعدة بيانات المستخدمين بأمان دون فقدان أي بيانات تاريخية.
 * 7. تنفيذ إجراءات الصيانة التلقائية والتحقق من سلامة واتساق المعاملات المرتبطة عند فتح القاعدة ( استدعاء لاحق).
 */
package com.smartledger.aldaftar.data.local

// ---------------------------------------------------------------------
// استيراد حزم مكتبة المكتبة المحلية ومكونات قاعدة البيانات المحلية والكيانات ومحولات الأنواع
// ---------------------------------------------------------------------
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * [فئة قاعدة البيانات التجريدية - قاعدة البيانات]:
 * 
 * [شرح التعليقات التوضيحية (الوسوم التوضيحية)]:
 * - ``: تعرف الفئة كقاعدة بيانات المكتبة المحلية مع تحديد:
 * 1. ``: قائمة الكيانات (الجداول) المسجلة.
 * 2. ` = 31`: رقم الإصدار الهيكلي الحالي لقاعدة البيانات بعد الهجرات المتتالية.
 * 3. ` = `: لتعطيل تصدير مخطط صيغة البيانات المنظمة أثناء البناء لتقليل الحجم.
 * - ``: تسجيل محول الأنواع للتعامل مع العمليات الحسابية للأرقام العشرية الكبيرة بدقة.
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
    version = 31,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {

 // -----------------------------------------------------------------
 // دوال الوصول التجريدية لكائنات الوصول إلى البيانات (كائنات الوصول للبيانات)
 // توفر المكتبة المحلية التنفيذ الفعلي لهذه الدوال تلقائياً أثناء وقت الترجمة.
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
 * [الكائن المرافق - ]:
 * يتولى إدارة دورة حياة قاعدة البيانات وتوفير مرجع أحادي عبر تطبيق نمط .
 */
    companion object {
 /** الاسم الفعلي لملف قاعدة البيانات المخزن على ذاكرة الجهاز */
        const val DATABASE_NAME = "mizan_al_dar_db"

 /**
 * المتغير المرجعي للنسخة الأحادية:
 * مُعلّم بـ `` لضمان أن أي تعديل عليه يصبح مرئياً فوراً لجميع الخيوط المتزامنة (الخيوط).
 */
        @Volatile
        private var INSTANCE: AppDatabase? = null

 /**
 * [دالة الحصول على قاعدة البيانات - ]:
 * تنشئ قاعدة البيانات أو تعيد النسخة الموجودة مسبقاً بطريقة آمنة خيطياً (القفل المزدوج للتحقق).
 *
 * [خطوات البناء والتهيئة]:
 * 1. فحص النسخة الأحادية؛ إذا كانت موجودة تُعاد فوراً دون قفل لتسريع الأداء.
 * 2. عند عدم وجودها، يتم استخدام كتلة المزامنة `()` لضمان عدم إنشاء نسختين متزامنتين.
 * 3. بناء القاعدة عبر `المكتبة المحلية.` مع تمرير سياق التطبيق العام لمنع تسريب الذاكرة (تسرب الذاكرة).
 * 4. تفعيل نمط `التدوين المسبق` للسماح بالقراءة المتزامنة أثناء عمليات الكتابة.
 * 5. تسجيل مصفوفة الهجرات الكاملة `_` لترقية الجداول القديمة بأمان.
 * 6. تسجيل استدعاء ` استدعاء لاحق` لتنظيف وتصحيح أي روابط معاملات تالفة عند كل فتح للقاعدة.
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
 /**
 * [استدعاء عند فتح القاعدة - ]:
 * يتم تنفيذه فور فتح الاتصال بقاعدة البيانات للقيام بفحص الصيانة الذاتية.
 * يقوم بالبحث عن المعاملات في جدول ديون الحبايب التي تحتوي على معرفات ربط خاطئة أو فارغة أو دائرية
 * ويقوم بتصفيرها إلى لضمان اتساق البيانات وعدم حدوث انهيارات أثناء الربط المحاسبي.
 */
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        try {
                            db.execSQL("""
                                UPDATE habayeb_transactions 
                                SET linkedMainTxId = NULL 
                                WHERE linkedMainTxId IS NOT NULL 
                                  AND (
                                      TRIM(linkedMainTxId) = '' 
                                      OR LOWER(TRIM(linkedMainTxId)) = 'null' 
                                      OR TRIM(linkedMainTxId) = '0' 
                                      OR linkedMainTxId = id
                                  )
                            """)
                        } catch (e: Exception) {
                            android.util.Log.e("SmartLedger", "Operation failed")
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }
    }
}

