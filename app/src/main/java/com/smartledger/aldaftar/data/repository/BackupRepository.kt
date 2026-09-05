/**
 * =====================================================================
 * ملف: مستودع النسخ الاحتياطي والاستعادة الموحد (مستودع النسخ.المكوّن)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكلاس المستودع المركزي وواجهة الواجهة الموحدة (واجهة موحدة / بوابة موحدة)
 * لإدارة كافة عمليات النسخ الاحتياطي، الاستعادة، وإدارة ملفات الأرشيف المالي.
 * 
 * [المسؤوليات المعمارية ونمط التفويض - نمط التفويض]:
 * 1. نقطة وصول موحدة لطبقة العرض (نماذج العرض): يخفي التعقيدات الداخلية لخدمات الملفات وقواعد البيانات.
 * 2. تقسيم المسؤوليات (فصل المسؤوليات):
 *    - إدارة وتخزين الملفات والمجلدات -> [مدير الملفات].
 *    - حزم البيانات والتشفير وتوليد ملفات  -> [خدمة النسخ].
 *    - فك التشفير والاستعادة الذرية للجداول -> [خدمة استعادة البيانات].
 * 3. المعالجة الآمنة في الخلفية (سلامة التزامن):
 *    - تنفيذ عمليات قراءة الملفات والاستعادة على خيوط الإدخال والإخراج [مسار الإدخال والإخراج].
 * 4. إدارة الأخطاء الصريحة: تمرير الاستثناءات عبر كائنات [النتيجة] دون إخفاء الأسباب الحقيقية للأعطال.
 */
package com.smartledger.aldaftar.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد وخدمات النسخ الاحتياطي والاستعادة وتدفقات الكوروتين
// ---------------------------------------------------------------------
import android.content.Context
import com.smartledger.aldaftar.data.backup.BackupFileManager
import com.smartledger.aldaftar.data.backup.BackupOperationResult
import com.smartledger.aldaftar.data.backup.BackupService
import com.smartledger.aldaftar.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * [مستودع النسخ الاحتياطي الموحد - مستودع النسخ]:
 * يوفر واجهة برمجية منسقة للتعامل مع النسخ الاحتياطي المحلي واستعادة قواعد البيانات.
 *
 * @معامل السياق سياق التطبيق للوصول لمسارات التخزين والترجمات.
 * @معامل المكوّن كائن قاعدة البيانات الرئيسية [المكوّن].
 * @معامل المكوّن خدمة حزم وتشفير النسخ الاحتياطي.
 * @معامل المكوّن مدير ملفات ومجلدات النسخ الاحتياطي في التخزين المحدود.
 * @معامل المكوّن خدمة تفريغ وإعادة بناء جداول قاعدة البيانات عند الاستعادة.
 */
class BackupRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val backupService: BackupService = BackupService(context, database),
    private val backupFileManager: BackupFileManager = BackupFileManager(context),
    private val restoreService: FinanceRestoreService = FinanceRestoreService(database, context)
) {

    /**
     * [جلب المسار الأساسي لمجلد النسخ الاحتياطي - جلب المجلد الأساسي]:
     * يستعلم عن المجلد الرئيسي للنسخ الاحتياطي داخل مساحات المستندات الآمنة للتطبيق.
     */
    fun getBaseBackupDirectory(): File = backupFileManager.getBaseBackupDirectory()

    /**
     * [جلب مسار المجلد الشهري الحالي - جلب مجلد النسخ]:
     * يسترجع مجلد الشهر النشط المخصص للنسخ الاحتياطية الدورية.
     */
    fun getBackupDirectory(): File = backupFileManager.getMonthlyBackupDirectory()

    /**
     * [استرجاع قائمة كافة ملفات النسخ المتوفرة محلياً - المكوّن]:
     * يفحص مجلدات التخزين ويعيد قائمة بكافة ملفات  الجاهزة للاستعادة.
     */
    fun getAllLocalBackupFiles(): List<File> = backupFileManager.getAllBackupFiles()

    /**
     * [إنشاء نسخة احتياطية محلية - المكوّن]:
     * يجمع بيانات التطبيق ويشفرها ويحفظها في ملف  محلي بالاسم المحدد [المكوّن].
     */
    suspend fun createLocalBackup(customFileName: String? = null): BackupOperationResult =
        backupService.performLocalBackup(customFileName)

    /**
     * [إنشاء نسخة احتياطية صامتة - المكوّن]:
     * ينفذ عملية نسخ احتياطي تلقائي في الخلفية بدون تفاعل مباشر مع المستخدم (للعمال المجدولين).
     */
    suspend fun createSilentBackup(): BackupOperationResult =
        backupService.performSilentBackup()

    /**
     * [توليد نص بيانات النسخة الاحتياطية - المكوّن]:
     * يجمع كافة سجلات قاعدة البيانات الحالية ويحولها إلى نص بيانات منظمة غير مشفر للمزامنة السحابية.
     */
    suspend fun getBackupJson(): String =
        backupService.generateBackupJson()

    /**
     * [الاستعادة الشاملة من نص بيانات منظمة - المكوّن]:
     * يفك حزمة نص الـ بيانات منظمة ويعيد بناء كافة جداول قاعدة البيانات بصورة ذرية.
     */
    suspend fun restoreFromJson(rawJson: String): FinanceRestoreResult =
        restoreService.executeMasterRestore(rawJson)

    /**
     * [الاستعادة الشاملة من ملف محلي - المكوّن]:
     * يقرأ ويفك تشفير ملف النسخة الاحتياطية الممرر [المكوّن] ويعيد استعادة البيانات مع معالجة الأخطاء.
     *
     * @معامل المكوّن ملف النسخة الاحتياطية المراد استعادته.
     * @القيمة المعادة [النتيجة] مغلف بنتيجة الاستعادة [المكوّنالنتيجة] أو الاستثناء الأصلي عند الفشل.
     */
    suspend fun restoreFromFile(file: File): Result<FinanceRestoreResult> = withContext(Dispatchers.IO) {
        val readResult = backupFileManager.readBackupFile(file)
        if (readResult.isSuccess) {
            try {
                val content = readResult.getOrThrow()
                val restoreResult = restoreService.executeMasterRestore(content)
                Result.success(restoreResult)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        } else {
            val originalException = readResult.exceptionOrNull() ?: IOException("فشل قراءة ملف النسخة الاحتياطية: ${file.name}")
            Result.failure(originalException)
        }
    }

    /**
     * [حذف كافة البيانات المحاسبية - المكوّن]:
     * يفرغ كافة جداول قاعدة البيانات ويعيد التطبيق إلى حالته الأولية الفارغة.
     */
    suspend fun clearAllData(): Unit =
        restoreService.deleteAllData()
}

