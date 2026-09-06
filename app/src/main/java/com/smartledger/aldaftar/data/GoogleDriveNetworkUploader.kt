/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
package com.smartledger.aldaftar.data

// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.smartledger.aldaftar.data.cloud.CloudNetworkEngine
import com.smartledger.aldaftar.data.serialization.BackupPayloadSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
class GoogleDriveNetworkUploader(
    private val context: Context
) {
    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
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

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    sealed class UploadResult {
        object Success : UploadResult()
        object SkippedUnchanged : UploadResult()
        data class AuthError(val statusCode: Int) : UploadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : UploadResult()
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    sealed class DownloadResult {
        data class Success(val content: String) : DownloadResult()
        object FileNotFound : DownloadResult()
        data class InvalidPayload(val message: String) : DownloadResult()
        data class AuthError(val statusCode: Int) : DownloadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : DownloadResult()
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    fun getStoredPayloadHash(): String? = uploaderPrefs.getString(KEY_LAST_UPLOADED_HASH, null)

    fun saveLastUploadedPayloadHash(hash: String) {
        uploaderPrefs.edit().putString(KEY_LAST_UPLOADED_HASH, hash).apply()
        Log.d(TAG, "تم حفظ بصمة النسخة الاحتياطية المرفوعة بنجاح.")
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    fun isPayloadIdentical(jsonContent: String): Boolean {
        val currentHash = BackupPayloadSerializer.calculateSha256Hash(jsonContent)
        val storedHash = getStoredPayloadHash()
        val match = storedHash != null && storedHash == currentHash
        if (match) {
            Log.i(TAG, "فحص البصمة: تطابق تام مع آخر نسخة مرفوعة، سيتم تخطي الرفع غير الضروري.")
        }
        return match
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
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
                val createMetaUrl = DRIVE_FILES_BASE_URL
                val metaJson = JSONObject().apply {
                    put("name", filename)
                    put("parents", org.json.JSONArray().put("appDataFolder"))
                    put("mimeType", MIME_TYPE_OCTET_STREAM)
                }
                val metaBody = metaJson.toString().toRequestBody(MEDIA_TYPE_JSON)

                val createMetaRequest = Request.Builder()
                    .url(createMetaUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .post(metaBody)
                    .build()

                client.newCall(createMetaRequest).execute().use { createMetaResponse ->
                    if (createMetaResponse.isSuccessful) {
                        val rawBody = createMetaResponse.body?.string() ?: ""
                        val createdFile = JSONObject(rawBody)
                        val newFileId = createdFile.getString("id")

                        val uploadMediaUrl = "$DRIVE_UPLOAD_BASE_URL/$newFileId?uploadType=media"
                        val fileBody = backupJsonContent.toRequestBody(MEDIA_TYPE_JSON)

                        val uploadMediaRequest = Request.Builder()
                            .url(uploadMediaUrl)
                            .header(HEADER_AUTHORIZATION, bearer(accessToken))
                            .patch(fileBody)
                            .build()

                        client.newCall(uploadMediaRequest).execute().use { uploadMediaResponse ->
                            if (uploadMediaResponse.isSuccessful) {
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

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
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
                val updateUrl = "$DRIVE_UPLOAD_BASE_URL/$fileId?uploadType=media"
                val mediaBody = backupJsonContent.toRequestBody(MEDIA_TYPE_JSON)

                val updateRequest = Request.Builder()
                    .url(updateUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .patch(mediaBody)
                    .build()

                client.newCall(updateRequest).execute().use { updateResponse ->
                    if (updateResponse.isSuccessful) {
                        // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
                        val metaUrl = "$DRIVE_FILES_BASE_URL/$fileId"
                        val metaJson = JSONObject().apply { put("name", newFileName) }
                        val metaBody = metaJson.toString().toRequestBody(MEDIA_TYPE_JSON)

                        val metaRequest = Request.Builder()
                            .url(metaUrl)
                            .header(HEADER_AUTHORIZATION, bearer(accessToken))
                            .patch(metaBody)
                            .build()

                        client.newCall(metaRequest).execute().use { /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */ }

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

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
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
                val updateRes = updateExistingFile(existingFileId, filename, backupJsonContent, accessToken)
                if (updateRes is UploadResult.Success) {
                    return@withContext updateRes
                }
            }
            createAndUploadNewFile(filename, backupJsonContent, accessToken)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun downloadFileById(
        fileId: String,
        accessToken: String
    ): DownloadResult = withContext(Dispatchers.IO) {
        try {
            cloudEngine.executeWithRetry(operationName = "DownloadFileById", maxRetries = 2) {
                val downloadUrl = "$DRIVE_FILES_BASE_URL/$fileId?alt=media"
                val downloadRequest = Request.Builder()
                    .url(downloadUrl)
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .get()
                    .build()

                client.newCall(downloadRequest).execute().use { downloadResponse ->
                    when {
                        downloadResponse.isSuccessful -> {
                            val content = downloadResponse.body?.string()
                            if (content != null && isValidBackupJson(content)) {
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

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun deleteFileById(
        fileId: String,
        accessToken: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            cloudEngine.executeWithRetry(operationName = "DeleteFileById", maxRetries = 2) {
                val url = "$DRIVE_FILES_BASE_URL/$fileId"
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

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private fun isValidBackupJson(content: String): Boolean {
        if (content.isBlank()) return false
        return try {
            val json = JSONObject(content)
            val sourceObj = if (json.has("mizan_al_dar_db")) json.getJSONObject("mizan_al_dar_db") else json
            sourceObj.has("settings") || sourceObj.has("transactions") || sourceObj.has("commitments") ||
                    sourceObj.has("fixed_commitments") || sourceObj.has("habayeb_debts") || sourceObj.has("habayeb_debts_db")
        } catch (_: Exception) {
            false
        }
    }
}
