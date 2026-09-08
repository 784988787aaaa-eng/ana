package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.data.local.BigDecimalConverter

/** قاعدة بيانات الجيل الجديد للتطبيق. */
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
    version = 1,
    exportSchema = true
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
        const val DATABASE_NAME = "aldaftar_v1.db"

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
         * 5. بناء قاعدة الجيل الجديد مباشرة من الـ Entities الحالية دون مسار ترقية تاريخي.
         * 6. تسجيل استدعاء `onOpen Callback` لتنظيف وتصحيح أي روابط معاملات تالفة عند كل فتح للقاعدة.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addCallback(object : RoomDatabase.Callback() {
                    /**
                     * [استدعاء عند فتح القاعدة - onOpen]:
                     * يتم تنفيذه فور فتح الاتصال بقاعدة البيانات للقيام بفحص الصيانة الذاتية.
                     * يقوم بالبحث عن المعاملات في جدول ديون الحبايب التي تحتوي على معرفات ربط خاطئة أو فارغة أو دائرية
                     * ويقوم بتصفيرها إلى NULL لضمان اتساق البيانات وعدم حدوث انهيارات أثناء الربط المحاسبي.
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
                            e.printStackTrace()
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }
    }
}

