/**
 * =====================================================================
 * ملف: رافع ومنزل الملفات السحابية عبر الشبكة (GoogleDriveNetworkUploader.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف طبقة النقل الشبكي منخفضة المستوى (Low-level HTTP Network Layer)
 * المسؤولة عن تبادل حزم النسخ الاحتياطي بين التطبيق وخوادم Google Drive REST API.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. رفع النسخ الاحتياطية الجديدة (Create & Upload Media) وتحديث النسخ القائمة (Patch).
 * 2. تنزيل محتوى النسخ المشفرة وفحص سلامة بنيتها التركيبية (JSON Payload Validation).
 * 3. آلية التحقق من عدم التغيير (Zero-Diff Detection) بحساب بصمة SHA-256 للبيانات لمنع استهلاك الباقة بالرفع غير المبرر.
 * 4. إدارة التزامن عبر قفل متبادل (Mutex) لمنع عمليات الرفع المزدوجة المتزامنة.
 * 5. استخدام محرك إعادة المحاولة [CloudNetworkEngine] للتعامل المرن مع انقطاعات الشبكة المؤقتة.
 * 6. حظر تام لتسجيل أي بيانات اعتماد أو نصوص حساسة في السجلات لضمان أمان المستخدم.
 */
package com.example.data

// ---------------------------------------------------------------------
// استيراد حزم الاتصال عبر OkHttp وكوروتين التزامن وتنسيقات الوسائط
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.cloud.CloudNetworkEngine
import com.example.data.serialization.BackupPayloadSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * [فئة رافع بيانات Google Drive - GoogleDriveNetworkUploader]:
 * تنفذ عمليات HTTP (POST, PATCH, GET, DELETE) على ملفات النسخ السحابية.
 */
class GoogleDriveNetworkUploader(
    private val context: Context
) {
    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على ثوابت روابط الرفع وبيانات الوسائط ومفاتيح التفضيلات لبصمة النسخة.
     */
    companion object {
        private const val TAG = "GoogleDriveNetworkUploader"

        private const val PREFS_NAME = "google_drive_uploader_prefs"
        private const val KEY_LAST_UPLOADED_HASH = "last_uploaded_payload_hash"

        private const val DRIVE_FILES_BASE_URL = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_BASE_URL = "https://www.googleapis.com/upload/drive/v3/files"
        private const val MIME_TYPE_OCTET_STREAM = "application/octet-stream"

        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        private const val HEADER_AUTHORIZATION = "Authorization"
        private fun bearer(accessToken: String) = "Bearer $accessToken"
    }

    private val cloudEngine = CloudNetworkEngine.getInstance(context)
    private val client = cloudEngine.client
    private val uploadMutex = Mutex()

    private val uploaderPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * [نموذج نتائج الرفع الموحد - UploadResult]:
     * يمثل الحالات المختلفة لعملية الرفع:
     * - Success: نجاح الرفع والتأكيد السحابي.
     * - SkippedUnchanged: تخطي الرفع لأن البيانات متطابقة تماماً مع السحابة (Zero-Diff).
     * - AuthError: خطأ تفويض يتطلب تجديد رمز الوصول (401/403).
     * - Failure: فشل العملية مع توضيح إمكانية إعادة المحاولة.
     */
    sealed class UploadResult {
        object Success : UploadResult()
        object SkippedUnchanged : UploadResult()
        data class AuthError(val statusCode: Int) : UploadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : UploadResult()
    }

    /**
     * [نموذج نتائج التنزيل الموحد - DownloadResult]:
     * يمثل حالات استرجاع النسخة من السحابة:
     * - Success: نجاح التنزيل وصحة بنية البيانات.
     * - FileNotFound: الملف المطلوب غير موجود في السحابة.
     * - InvalidPayload: الملف منزل ولكنه تالف أو لا يطابق هيكل النسخ المتوقع.
     * - AuthError: خطأ صلاحيات.
     * - Failure: خطأ شبكي أو استثناء غير متوقع.
     */
    sealed class DownloadResult {
        data class Success(val content: String) : DownloadResult()
        object FileNotFound : DownloadResult()
        data class InvalidPayload(val message: String) : DownloadResult()
        data class AuthError(val statusCode: Int) : DownloadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : DownloadResult()
    }

    /**
     * [دوال إدارة البصمة الرقمية للنسخة - Payload Hash]:
     * تخزن وتسترجع كود SHA-256 للمحتوى لتفادي تكرار رفع نفس البيانات دون أي تغيير.
     */
    fun getStoredPayloadHash(): String? = uploaderPrefs.getString(KEY_LAST_UPLOADED_HASH, null)

    fun saveLastUploadedPayloadHash(hash: String) {
        uploaderPrefs.edit().putString(KEY_LAST_UPLOADED_HASH, hash).apply()
        Log.d(TAG, "تم حفظ بصمة النسخة الاحتياطية المرفوعة بنجاح.")
    }

    /**
     * [دالة فحص تطابق المحتوى]:
     * تقارن بصمة البيانات الحالية مع البصمة المسجلة لآخر رفع ناجح.
     */
    fun isPayloadIdentical(jsonContent: String): Boolean {
        // [توثيق المتغير/الخاصية: currentHash]: قيمة بصمة بيانات تُستخدم للتحقق من التطابق أو سلامة المحتوى.
        val currentHash = BackupPayloadSerializer.calculateSha256Hash(jsonContent)
        // [توثيق المتغير/الخاصية: storedHash]: قيمة بصمة بيانات تُستخدم للتحقق من التطابق أو سلامة المحتوى.
        val storedHash = getStoredPayloadHash()
        // [توثيق المتغير/الخاصية: match]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val match = storedHash != null && storedHash == currentHash
        if (match) {
            Log.i(TAG, "فحص البصمة: تطابق تام مع آخر نسخة مرفوعة، سيتم تخطي الرفع غير الضروري.")
        }
        return match
    }

    /**
     * [دالة إنشاء ورفع ملف جديد - createAndUploadNewFile]:
     * تنشئ البيانات الوصفية للملف (Metadata) في مجلد appDataFolder ثم ترفع المحتوى الفعلي (Media).
     */
    suspend fun createAndUploadNewFile(
        filename: String,
        backupJsonContent: String,
        accessToken: String
    ): UploadResult = withContext(Dispatchers.IO) {
        if (backupJsonContent.isBlank()) {
            return@withContext UploadResult.Failure("محتوى النسخة فارغ", isRetryable = false)
        }
        try {
            cloudEngine.executeWithRetry(operationName = "CreateAndUploadFile", maxRetries = 2) {
                // [توثيق المتغير/الخاصية: createMetaUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                val createMetaUrl = DRIVE_FILES_BASE_URL
                // [توثيق المتغير/الخاصية: metaJson]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val metaJson = JSONObject().apply {
                    put("name", filename)
                    put("parents", org.json.JSONArray().put("appDataFolder"))
                    put("mimeType", MIME_TYPE_OCTET_STREAM)
                }
                // [توثيق المتغير/الخاصية: metaBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val metaBody = metaJson.toString().toRequestBody(MEDIA_TYPE_JSON)

                // [توثيق المتغير/الخاصية: createMetaRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val createMetaRequest = Request.Builder()
                    .url(createMetaUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .post(metaBody)
                    .build()

                client.newCall(createMetaRequest).execute().use { createMetaResponse ->
                    if (createMetaResponse.isSuccessful) {
                        // [توثيق المتغير/الخاصية: rawBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val rawBody = createMetaResponse.body?.string() ?: ""
                        // [توثيق المتغير/الخاصية: createdFile]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val createdFile = JSONObject(rawBody)
                        // [توثيق المتغير/الخاصية: newFileId]: معرّف مرجعي يميز العنصر أو المهمة المرتبطة به.
                        val newFileId = createdFile.getString("id")

                        // [توثيق المتغير/الخاصية: uploadMediaUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                        val uploadMediaUrl = "$DRIVE_UPLOAD_BASE_URL/$newFileId?uploadType=media"
                        // [توثيق المتغير/الخاصية: fileBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val fileBody = backupJsonContent.toRequestBody(MEDIA_TYPE_JSON)

                        // [توثيق المتغير/الخاصية: uploadMediaRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val uploadMediaRequest = Request.Builder()
                            .url(uploadMediaUrl)
                            .header(HEADER_AUTHORIZATION, bearer(accessToken))
                            .patch(fileBody)
                            .build()

                        client.newCall(uploadMediaRequest).execute().use { uploadMediaResponse ->
                            if (uploadMediaResponse.isSuccessful) {
                                // [توثيق المتغير/الخاصية: currentHash]: قيمة بصمة بيانات تُستخدم للتحقق من التطابق أو سلامة المحتوى.
                                val currentHash = BackupPayloadSerializer.calculateSha256Hash(backupJsonContent)
                                saveLastUploadedPayloadHash(currentHash)
                                UploadResult.Success
                            } else {
                                if (uploadMediaResponse.code == 401 || uploadMediaResponse.code == 403) {
                                    UploadResult.AuthError(uploadMediaResponse.code)
                                } else {
                                    UploadResult.Failure("Upload media failed: ${uploadMediaResponse.code}", isRetryable = uploadMediaResponse.code >= 500)
                                }
                            }
                        }
                    } else {
                        if (createMetaResponse.code == 401 || createMetaResponse.code == 403) {
                            UploadResult.AuthError(createMetaResponse.code)
                        } else {
                            UploadResult.Failure("Create metadata failed: ${createMetaResponse.code}", isRetryable = createMetaResponse.code >= 500)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "فشل شبكي أثناء رفع ملف جديد: ${e.javaClass.simpleName}")
            UploadResult.Failure(e.localizedMessage ?: "Network error", isRetryable = true)
        } catch (e: Exception) {
            Log.e(TAG, "استثناء أثناء رفع ملف جديد: ${e.javaClass.simpleName}")
            UploadResult.Failure(e.localizedMessage ?: "Unexpected upload error", isRetryable = false)
        }
    }

    /**
     * [دالة تحديث محتوى ملف موجود - updateExistingFile]:
     * تستبدل محتوى ملف موجود بالفعل في Google Drive دون الحاجة لتغيير معرف الملف.
     */
    suspend fun updateExistingFile(
        fileId: String,
        newFileName: String,
        backupJsonContent: String,
        accessToken: String
    ): UploadResult = withContext(Dispatchers.IO) {
        if (backupJsonContent.isBlank()) {
            return@withContext UploadResult.Failure("محتوى النسخة فارغ", isRetryable = false)
        }
        try {
            cloudEngine.executeWithRetry(operationName = "UpdateExistingFile", maxRetries = 2) {
                // [توثيق المتغير/الخاصية: updateUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                val updateUrl = "$DRIVE_UPLOAD_BASE_URL/$fileId?uploadType=media"
                // [توثيق المتغير/الخاصية: mediaBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val mediaBody = backupJsonContent.toRequestBody(MEDIA_TYPE_JSON)

                // [توثيق المتغير/الخاصية: updateRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val updateRequest = Request.Builder()
                    .url(updateUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .patch(mediaBody)
                    .build()

                client.newCall(updateRequest).execute().use { updateResponse ->
                    if (updateResponse.isSuccessful) {
                        // تحديث اسم الملف إذا لزم الأمر
                        // [توثيق المتغير/الخاصية: metaUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                        val metaUrl = "$DRIVE_FILES_BASE_URL/$fileId"
                        // [توثيق المتغير/الخاصية: metaJson]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val metaJson = JSONObject().apply { put("name", newFileName) }
                        // [توثيق المتغير/الخاصية: metaBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val metaBody = metaJson.toString().toRequestBody(MEDIA_TYPE_JSON)

                        // [توثيق المتغير/الخاصية: metaRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val metaRequest = Request.Builder()
                            .url(metaUrl)
                            .header(HEADER_AUTHORIZATION, bearer(accessToken))
                            .patch(metaBody)
                            .build()

                        client.newCall(metaRequest).execute().use { /* ignore meta update response */ }

                        // [توثيق المتغير/الخاصية: currentHash]: قيمة بصمة بيانات تُستخدم للتحقق من التطابق أو سلامة المحتوى.
                        val currentHash = BackupPayloadSerializer.calculateSha256Hash(backupJsonContent)
                        saveLastUploadedPayloadHash(currentHash)
                        UploadResult.Success
                    } else {
                        if (updateResponse.code == 401 || updateResponse.code == 403) {
                            UploadResult.AuthError(updateResponse.code)
                        } else {
                            UploadResult.Failure("Update file failed: ${updateResponse.code}", isRetryable = updateResponse.code >= 500)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "فشل شبكي أثناء تحديث ملف موجود: ${e.javaClass.simpleName}")
            UploadResult.Failure(e.localizedMessage ?: "Network error", isRetryable = true)
        } catch (e: Exception) {
            Log.e(TAG, "استثناء أثناء تحديث ملف موجود: ${e.javaClass.simpleName}")
            UploadResult.Failure(e.localizedMessage ?: "Unexpected update error", isRetryable = false)
        }
    }

    /**
     * [دالة الرفع الآمن المنسق - uploadBackupSafe]:
     * تنفذ تدقيق عدم التغيير وتستخدم قفل Mutex لمنع أي رفع متزامن مزدوج.
     */
    suspend fun uploadBackupSafe(
        filename: String,
        backupJsonContent: String,
        accessToken: String,
        existingFileId: String? = null
    ): UploadResult = uploadMutex.withLock {
        withContext(Dispatchers.IO) {
            if (isPayloadIdentical(backupJsonContent)) {
                return@withContext UploadResult.SkippedUnchanged
            }

            if (!existingFileId.isNullOrEmpty()) {
                // [توثيق المتغير/الخاصية: updateRes]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val updateRes = updateExistingFile(existingFileId, filename, backupJsonContent, accessToken)
                if (updateRes is UploadResult.Success) {
                    return@withContext updateRes
                }
            }
            createAndUploadNewFile(filename, backupJsonContent, accessToken)
        }
    }

    /**
     * [دالة تنزيل الملف بالمعرف - downloadFileById]:
     * تجلب المحتوى الخام لملف محدد من Google Drive وتتأكد من صحة هيكل JSON للنسخة.
     */
    suspend fun downloadFileById(
        fileId: String,
        accessToken: String
    ): DownloadResult = withContext(Dispatchers.IO) {
        try {
            cloudEngine.executeWithRetry(operationName = "DownloadFileById", maxRetries = 2) {
                // [توثيق المتغير/الخاصية: downloadUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                val downloadUrl = "$DRIVE_FILES_BASE_URL/$fileId?alt=media"
                // [توثيق المتغير/الخاصية: downloadRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                val downloadRequest = Request.Builder()
                    .url(downloadUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .get()
                    .build()

                client.newCall(downloadRequest).execute().use { downloadResponse ->
                    when {
                        downloadResponse.isSuccessful -> {
                            // [توثيق المتغير/الخاصية: content]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                            val content = downloadResponse.body?.string()
                            if (content != null && isValidBackupJson(content)) {
                                // [توثيق المتغير/الخاصية: downloadedHash]: قيمة بصمة بيانات تُستخدم للتحقق من التطابق أو سلامة المحتوى.
                                val downloadedHash = BackupPayloadSerializer.calculateSha256Hash(content)
                                saveLastUploadedPayloadHash(downloadedHash)
                                DownloadResult.Success(content)
                            } else {
                                Log.e(TAG, "الملف المنزل غير صالح أو لا يحتوي على بنية البيانات المتوقعة")
                                DownloadResult.InvalidPayload("Invalid backup payload structure")
                            }
                        }
                        downloadResponse.code == 404 -> {
                            DownloadResult.FileNotFound
                        }
                        downloadResponse.code == 401 || downloadResponse.code == 403 -> {
                            DownloadResult.AuthError(downloadResponse.code)
                        }
                        else -> {
                            DownloadResult.Failure("Download failed: ${downloadResponse.code}", isRetryable = downloadResponse.code >= 500)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "فشل شبكي أثناء تنزيل الملف: ${e.javaClass.simpleName}")
            DownloadResult.Failure(e.localizedMessage ?: "Network error", isRetryable = true)
        } catch (e: Exception) {
            Log.e(TAG, "استثناء أثناء تنزيل الملف: ${e.javaClass.simpleName}")
            DownloadResult.Failure(e.localizedMessage ?: "Unexpected download error", isRetryable = false)
        }
    }

    /**
     * [دالة حذف ملف من السحابة - deleteFileById]:
     * ترسل طلب DELETE إلى Google Drive لإزالة ملف محدد من مساحة appDataFolder.
     */
    suspend fun deleteFileById(
        fileId: String,
        accessToken: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            cloudEngine.executeWithRetry(operationName = "DeleteFileById", maxRetries = 2) {
                // [توثيق المتغير/الخاصية: url]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
                val url = "$DRIVE_FILES_BASE_URL/$fileId"
                // [توثيق المتغير/الخاصية: request]: طلب HTTP أو طلب عمل مبني للتنفيذ اللاحق.
                val request = Request.Builder()
                    .url(url)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .delete()
                    .build()

                client.newCall(request).execute().use { response ->
                    response.isSuccessful || response.code == 404
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل حذف الملف من السحابة: ${e.javaClass.simpleName}")
            false
        }
    }

    /**
     * [دالة التحقق من صحة بنية JSON للنسخة - isValidBackupJson]:
     * تفحص احتواء الـ JSON على الجداول والكيانات المالية الأساسية لتطبيق الميزان.
     */
    private fun isValidBackupJson(content: String): Boolean {
        if (content.isBlank()) return false
        return try {
            // [توثيق المتغير/الخاصية: json]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val json = JSONObject(content)
            // [توثيق المتغير/الخاصية: sourceObj]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val sourceObj = if (json.has("mizan_al_dar_db")) json.getJSONObject("mizan_al_dar_db") else json
            sourceObj.has("settings") || sourceObj.has("transactions") || sourceObj.has("commitments") ||
                    sourceObj.has("fixed_commitments") || sourceObj.has("habayeb_debts") || sourceObj.has("habayeb_debts_db")
        } catch (_: Exception) {
            false
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// - يُستحسن إبقاء مقارنة hash قبل الرفع لتقليل النقل غير الضروري، مع توثيق مصدر الـhash في عقد النسخة الاحتياطية.
// - يجب التفريق بين فشل المصادقة، عدم وجود الملف، وفشل الشبكة عند استرجاع/حذف الملفات.
// - يفضل مستقبلاً إضافة اختبارات idempotency لمسارات create/update.
// - هذه الملاحظات توصيات مستقبلية فقط ولا تغيّر التنفيذ الحالي أو عقده البرمجي.
