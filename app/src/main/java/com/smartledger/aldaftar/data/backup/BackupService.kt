/**
 * =====================================================================
 * ملف: خدمة ومنسق دورة النسخ الاحتياطي (خدمة النسخ.المكوّن)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المنسق عالي المستوى المسؤول عن
 * استخراج البيانات المالية من قاعدة البيانات، تحويلها لصيغة بيانات منظمة معيارية،
 * وإدارتها عبر طبقة الملفات في المسار العام المعتمد وتحديث تواريخ النسخ في التفضيلات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. جمع البيانات المتكاملة: الإعدادات، المعاملات، الالتزامات، ديون الحبايب، وسلة المهملات.
 * 2. التسلسل والتحويل إلى بيانات منظمة عبر [مسلسل حزمة النسخ].
 * 3. الحماية ضد التزامن المزدوج باستخدام قفل [قفل النسخ] لمنع تداخل عمليات النسخ.
 * 4. إدارة الملفات الفيزيائية في المسار العام المعتمد عبر تفويض المهمة إلى [مدير الملفات].
 * 5. تسجيل وقت آخر عملية نسخ ناجحة بدقة في التفضيلات المشتركة.
 * 6. حماية الخصوصية: حظر كامل لتسجيل أي تفاصيل مالية في السجلات.
 */
package com.smartledger.aldaftar.data.backup

// ---------------------------------------------------------------------
// استيراد الكيانات وقواعد البيانات المحلية والمصفوفات والكوروتين
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.serialization.BackupExtraDataProvider
import com.smartledger.aldaftar.data.serialization.BackupPayloadData
import com.smartledger.aldaftar.data.serialization.BackupPayloadSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * [نموذج نتائج عملية النسخ - نتيجة عملية النسخ]:
 * يمثل الحالات الناتجة عن محاولة النسخ:
 * - نجاح: نجاح كتابة الملف والتحقق منه مع إرجاع مرجع الملف والطابع الزمني.
 * - فشل: فشل العملية مع رسالة واضحة للمستخدم والسبب التقني.
 */
sealed class BackupOperationResult {
    data class Success(val file: File, val timestamp: Long) : BackupOperationResult()
    data class Failure(val userMessage: String, val cause: Throwable? = null) : BackupOperationResult()
}

/**
 * [نموذج حالات التنفيذ التفاعلية - حالة تنفيذ النسخ]:
 * يفيد في مراقبة تقدم العملية في واجهات المستخدم التفاعلية.
 */
sealed class BackupExecutionState {
    object Idle : BackupExecutionState()
    object Running : BackupExecutionState()
    data class Success(val file: File, val timestamp: Long) : BackupExecutionState()
    data class Failed(val reason: String) : BackupExecutionState()
}

/**
 * [فئة خدمة النسخ الاحتياطي - خدمة النسخ]:
 * تتولى تجميع البيانات وتسلسلها وحفظها في المسار العام الرسمي المعتمد.
 */
class BackupService(
    private val context: Context,
    private val database: AppDatabase,
    private val fileManager: BackupFileManager = BackupFileManager(context)
) {

    /**
     * [الكائن المرافق - الكائن المرافق]:
     * يحدد وسم التسجيل الموحد لعمليات الخدمة.
     */
    companion object {
        private const val TAG = "BackupService"
    }

    private val backupMutex = Mutex()

    /**
     * [دالة تجميع حزمة البيانات - المكوّن]:
     * تستعلم عن كافة الجداول المالية وقوائم الديون والتفضيلات المخصصة وتجمعها في كائن [المكوّن].
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
     * [دالة توليد نص بيانات منظمة - المكوّن]:
     * تحول حزمة البيانات المجمعة إلى نص بيانات منظمة موحد ومطابق لمخطط النظام.
     */
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = buildBackupPayload()
        BackupPayloadSerializer.exportBackupToJson(payload)
    }

    /**
     * [دالة تنفيذ النسخ المحلي الكامل - المكوّن]:
     * تنفذ دورة النسخ بالترتيب: تجهيز البيانات -> تحويل بيانات منظمة -> كتابة ذرية في المسار الشهري العام -> تدقيق الملف -> تسجيل التوقيت.
     */
    suspend fun performLocalBackup(
        customFileName: String? = null,
        targetDir: File? = null
    ): BackupOperationResult = backupMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // المرحلة الأولى: تجهيز البيانات وتحويلها إلى الصيغة المعيارية.
                val jsonString = generateBackupJson()
                if (jsonString.isBlank()) {
                    return@withContext BackupOperationResult.Failure(
                        userMessage = "فشل إنشاء حزمة النسخ الاحتياطي: البيانات فارغة",
                        cause = IllegalStateException("حزمة النسخ الاحتياطية فارغة")
                    )
                }

                // المرحلة الثانية: تحديد المجلد واسم الملف داخل مساحة النسخ المعتمدة.
                val directory = targetDir ?: fileManager.getMonthlyBackupDirectory()
                val fileName = customFileName ?: fileManager.generateStandardBackupFileName()

                // المرحلة الثالثة: كتابة الملف بطريقة ذرية تمنع بقاء نسخة ناقصة.
                val writeResult = fileManager.createBackupFile(directory, fileName, jsonString)
                if (writeResult.isSuccess) {
                    val file = writeResult.getOrThrow()

                    // المرحلة الرابعة: التحقق النهائي من سلامة الملف قبل إعلان النجاح.
                    val validationResult = fileManager.validateBackupFile(file)
                    if (validationResult.isFailure) {
                        return@withContext BackupOperationResult.Failure(
                            userMessage = "فشل التحقق من سلامة ملف النسخة بعد الإنشاء",
                            cause = validationResult.exceptionOrNull()
                        )
                    }

                    val now = System.currentTimeMillis()

                    // المرحلة الخامسة: حفظ وقت آخر نسخة ناجحة في التفضيلات المحلية.
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
     * [دالة النسخ الاحتياطي الصامت - المكوّن]:
     * تنفذ نسخة تلقائية باسم مخصص في المجلد الشهري العام دون إزعاج المستخدم.
     */
    suspend fun performSilentBackup(): BackupOperationResult = withContext(Dispatchers.IO) {
        performLocalBackup(
            customFileName = BackupConstants.BACKUP_SILENT_FILE_NAME,
            targetDir = fileManager.getMonthlyBackupDirectory()
        )
    }
}
