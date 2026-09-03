/**
 * =====================================================================
 * ملف: مستودع النسخ الاحتياطي والاستعادة الموحد (BackupRepository.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكلاس المستودع المركزي وواجهة الواجهة الموحدة (Facade / Single Gateway)
 * لإدارة كافة عمليات النسخ الاحتياطي، الاستعادة، وإدارة ملفات الأرشيف المالي.
 * 
 * [المسؤوليات المعمارية ونمط التفويض - Delegation Pattern]:
 * 1. نقطة وصول موحدة لطبقة العرض (ViewModels): يخفي التعقيدات الداخلية لخدمات الملفات وقواعد البيانات.
 * 2. تقسيم المسؤوليات (Separation of Concerns):
 *    - إدارة وتخزين الملفات والمجلدات -> [BackupFileManager].
 *    - حزم البيانات والتشفير وتوليد ملفات `.mzd` -> [BackupService].
 *    - فك التشفير والاستعادة الذرية للجداول -> [FinanceRestoreService].
 * 3. المعالجة الآمنة في الخلفية (Thread Safety):
 *    - تنفيذ عمليات قراءة الملفات والاستعادة على خيوط الإدخال والإخراج [Dispatchers.IO].
 * 4. إدارة الأخطاء الصريحة: تمرير الاستثناءات عبر كائنات [Result] دون إخفاء الأسباب الحقيقية للأعطال.
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد وخدمات النسخ الاحتياطي والاستعادة وتدفقات الكوروتين
// ---------------------------------------------------------------------
import android.content.Context
import com.example.data.backup.BackupFileManager
import com.example.data.backup.BackupOperationResult
import com.example.data.backup.BackupService
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * [مستودع النسخ الاحتياطي الموحد - BackupRepository]:
 * يوفر واجهة برمجية منسقة للتعامل مع النسخ الاحتياطي المحلي واستعادة قواعد البيانات.
 *
 * @param context سياق التطبيق للوصول لمسارات التخزين والترجمات.
 * @param database كائن قاعدة البيانات الرئيسية [AppDatabase].
 * @param backupService خدمة حزم وتشفير النسخ الاحتياطي.
 * @param backupFileManager مدير ملفات ومجلدات النسخ الاحتياطي في التخزين المحدود.
 * @param restoreService خدمة تفريغ وإعادة بناء جداول قاعدة البيانات عند الاستعادة.
 */
class BackupRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val backupService: BackupService = BackupService(context, database),
    private val backupFileManager: BackupFileManager = BackupFileManager(context),
    private val restoreService: FinanceRestoreService = FinanceRestoreService(database, context)
) {

    /**
     * [جلب المسار الأساسي لمجلد النسخ الاحتياطي - getBaseBackupDirectory]:
     * يستعلم عن المجلد الرئيسي للنسخ الاحتياطي داخل مساحات المستندات الآمنة للتطبيق.
     */
    fun getBaseBackupDirectory(): File = backupFileManager.getBaseBackupDirectory()

    /**
     * [جلب مسار المجلد الشهري الحالي - getBackupDirectory]:
     * يسترجع مجلد الشهر النشط المخصص للنسخ الاحتياطية الدورية.
     */
    fun getBackupDirectory(): File = backupFileManager.getMonthlyBackupDirectory()

    /**
     * [استرجاع قائمة كافة ملفات النسخ المتوفرة محلياً - getAllLocalBackupFiles]:
     * يفحص مجلدات التخزين ويعيد قائمة بكافة ملفات `.mzd` الجاهزة للاستعادة.
     */
    fun getAllLocalBackupFiles(): List<File> = backupFileManager.getAllBackupFiles()

    /**
     * [إنشاء نسخة احتياطية محلية - createLocalBackup]:
     * يجمع بيانات التطبيق ويشفرها ويحفظها في ملف `.mzd` محلي بالاسم المحدد [customFileName].
     */
    suspend fun createLocalBackup(customFileName: String? = null): BackupOperationResult =
        backupService.performLocalBackup(customFileName)

    /**
     * [إنشاء نسخة احتياطية صامتة - createSilentBackup]:
     * ينفذ عملية نسخ احتياطي تلقائي في الخلفية بدون تفاعل مباشر مع المستخدم (للعمال المجدولين).
     */
    suspend fun createSilentBackup(): BackupOperationResult =
        backupService.performSilentBackup()

    /**
     * [توليد نص بيانات النسخة الاحتياطية - getBackupJson]:
     * يجمع كافة سجلات قاعدة البيانات الحالية ويحولها إلى نص JSON غير مشفر للمزامنة السحابية.
     */
    suspend fun getBackupJson(): String =
        backupService.generateBackupJson()

    /**
     * [الاستعادة الشاملة من نص JSON - restoreFromJson]:
     * يفك حزمة نص الـ JSON ويعيد بناء كافة جداول قاعدة البيانات بصورة ذرية.
     */
    suspend fun restoreFromJson(rawJson: String): FinanceRestoreResult =
        restoreService.executeMasterRestore(rawJson)

    /**
     * [الاستعادة الشاملة من ملف محلي - restoreFromFile]:
     * يقرأ ويفك تشفير ملف النسخة الاحتياطية الممرر [file] ويعيد استعادة البيانات مع معالجة الأخطاء.
     *
     * @param file ملف النسخة الاحتياطية المراد استعادته.
     * @return [Result] مغلف بنتيجة الاستعادة [FinanceRestoreResult] أو الاستثناء الأصلي عند الفشل.
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
     * [حذف كافة البيانات المحاسبية - clearAllData]:
     * يفرغ كافة جداول قاعدة البيانات ويعيد التطبيق إلى حالته الأولية الفارغة.
     */
    suspend fun clearAllData(): Unit =
        restoreService.deleteAllData()
}

