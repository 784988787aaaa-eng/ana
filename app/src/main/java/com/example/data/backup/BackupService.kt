/**
 * =====================================================================
 * ملف: خدمة ومنسق دورة النسخ الاحتياطي (BackupService.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المنسق عالي المستوى (High-Level Backup Coordinator) المسؤول عن
 * استخراج البيانات المالية من قاعدة البيانات، تحويلها لصيغة JSON معيارية،
 * وإدارتها عبر طبقة الملفات في المسار العام المعتمد وتحديث تواريخ النسخ في التفضيلات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. جمع البيانات المتكاملة (Data Aggregation): الإعدادات، المعاملات، الالتزامات، ديون الحبايب، وسلة المهملات.
 * 2. التسلسل والتحويل إلى JSON عبر [BackupPayloadSerializer].
 * 3. الحماية ضد التزامن المزدوج باستخدام قفل [backupMutex] لمنع تداخل عمليات النسخ.
 * 4. إدارة الملفات الفيزيائية في المسار العام المعتمد عبر تفويض المهمة إلى [BackupFileManager].
 * 5. تسجيل وقت آخر عملية نسخ ناجحة بدقة في SharedPreferences.
 * 6. حماية الخصوصية: حظر كامل لتسجيل أي تفاصيل مالية في السجلات.
 */
package com.example.data.backup

// ---------------------------------------------------------------------
// استيراد الكيانات وقواعد البيانات المحلية والمصفوفات والكوروتين
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.serialization.BackupExtraDataProvider
import com.example.data.serialization.BackupPayloadData
import com.example.data.serialization.BackupPayloadSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * [نموذج نتائج عملية النسخ - BackupOperationResult]:
 * يمثل الحالات الناتجة عن محاولة النسخ:
 * - Success: نجاح كتابة الملف والتحقق منه مع إرجاع مرجع الملف والطابع الزمني.
 * - Failure: فشل العملية مع رسالة واضحة للمستخدم والسبب التقني.
 */
sealed class BackupOperationResult {
    data class Success(val file: File, val timestamp: Long) : BackupOperationResult()
    data class Failure(val userMessage: String, val cause: Throwable? = null) : BackupOperationResult()
}

/**
 * [نموذج حالات التنفيذ التفاعلية - BackupExecutionState]:
 * يفيد في مراقبة تقدم العملية في واجهات المستخدم التفاعلية.
 */
sealed class BackupExecutionState {
    object Idle : BackupExecutionState()
    object Running : BackupExecutionState()
    data class Success(val file: File, val timestamp: Long) : BackupExecutionState()
    data class Failed(val reason: String) : BackupExecutionState()
}

/**
 * [فئة خدمة النسخ الاحتياطي - BackupService]:
 * تتولى تجميع البيانات وتسلسلها وحفظها في المسار العام الرسمي المعتمد.
 */
class BackupService(
    private val context: Context,
    private val database: AppDatabase,
    private val fileManager: BackupFileManager = BackupFileManager(context)
) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحدد وسم التسجيل الموحد لعمليات الخدمة.
     */
    companion object {
        private const val TAG = "BackupService"
    }

    private val backupMutex = Mutex()

    /**
     * [دالة تجميع حزمة البيانات - buildBackupPayload]:
     * تستعلم عن كافة الجداول المالية وقوائم الديون والتفضيلات المخصصة وتجمعها في كائن [BackupPayloadData].
     */
    suspend fun buildBackupPayload(): BackupPayloadData = withContext(Dispatchers.IO) {
        val settings = database.settingsDao().getSettingsDirect() ?: AppSettings()
        val commitments = database.commitmentDao().getAllCommitmentsFlow().first()
        val transactions = database.transactionDao().getAllTransactionsFlow().first()
        val customers = database.habayebDao().getAllCustomersDirect()
        val habayebTransactions = database.habayebDao().getAllTransactionsDirect()
        val deletedItems = database.trashDao().getAllDeletedItemsDirect()
        val extraData = BackupExtraDataProvider.fetchExtraBackupData(context, customers)

        BackupPayloadData(
            settings = settings,
            commitments = commitments,
            transactions = transactions,
            habayebCustomers = customers,
            habayebTransactions = habayebTransactions,
            deletedItems = deletedItems,
            customCategories = extraData.customCategories,
            categoryLinks = extraData.categoryLinks,
            pinnedCustomerIdsByCategory = extraData.pinnedMap,
            categoryOrderList = extraData.categoryOrderList,
            closedCustomName = extraData.closedCustomName
        )
    }

    /**
     * [دالة توليد نص JSON - generateBackupJson]:
     * تحول حزمة البيانات المجمعة إلى نص JSON موحد ومطابق لمخطط النظام.
     */
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = buildBackupPayload()
        BackupPayloadSerializer.exportBackupToJson(payload)
    }

    /**
     * [دالة تنفيذ النسخ المحلي الكامل - performLocalBackup]:
     * تنفذ دورة النسخ بالترتيب: تجهيز البيانات -> تحويل JSON -> كتابة ذرية في المسار الشهري العام -> تدقيق الملف -> تسجيل التوقيت.
     */
    suspend fun performLocalBackup(
        customFileName: String? = null,
        targetDir: File? = null
    ): BackupOperationResult = backupMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // 1. مرحلة تجهيز البيانات والتسلسل
                val jsonString = generateBackupJson()
                if (jsonString.isBlank()) {
                    return@withContext BackupOperationResult.Failure(
                        userMessage = "فشل إنشاء حزمة النسخ الاحتياطي: البيانات فارغة",
                        cause = IllegalStateException("Backup JSON payload is empty")
                    )
                }

                // 2. تحديد المجلد واسم الملف في المسار العام المعتمد
                val directory = targetDir ?: fileManager.getMonthlyBackupDirectory()
                val fileName = customFileName ?: fileManager.generateStandardBackupFileName()

                // 3. كتابة الملف بشكل ذري
                val writeResult = fileManager.createBackupFile(directory, fileName, jsonString)
                if (writeResult.isSuccess) {
                    val file = writeResult.getOrThrow()

                    // 4. التحقق النهائي من سلامة الملف
                    val validationResult = fileManager.validateBackupFile(file)
                    if (validationResult.isFailure) {
                        return@withContext BackupOperationResult.Failure(
                            userMessage = "فشل التحقق من سلامة ملف النسخة بعد الإنشاء",
                            cause = validationResult.exceptionOrNull()
                        )
                    }

                    val now = System.currentTimeMillis()

                    // 5. حفظ وتحديث سجل آخر نسخة ناجحة
                    val prefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
                    prefs.edit().putLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, now).apply()

                    Log.d(TAG, "اكتملت دورة النسخ الاحتياطي المحلي بنجاح في المسار العام.")
                    BackupOperationResult.Success(file, now)
                } else {
                    val err = writeResult.exceptionOrNull()
                    Log.e(TAG, "فشل إنشاء ملف النسخة الاحتياطية: ${err?.javaClass?.simpleName}")
                    BackupOperationResult.Failure(
                        userMessage = "فشل إنشاء ملف النسخة الاحتياطية المحلية",
                        cause = err
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "استثناء أثناء دورة النسخ الاحتياطي: ${e.javaClass.simpleName}")
                BackupOperationResult.Failure(
                    userMessage = "حدث خطأ أثناء النسخ الاحتياطي: ${e.localizedMessage ?: ""}",
                    cause = e
                )
            }
        }
    }

    /**
     * [دالة النسخ الاحتياطي الصامت - performSilentBackup]:
     * تنفذ نسخة تلقائية باسم مخصص في المجلد الشهري العام دون إزعاج المستخدم.
     */
    suspend fun performSilentBackup(): BackupOperationResult = withContext(Dispatchers.IO) {
        performLocalBackup(
            customFileName = BackupConstants.BACKUP_SILENT_FILE_NAME,
            targetDir = fileManager.getMonthlyBackupDirectory()
        )
    }
}
