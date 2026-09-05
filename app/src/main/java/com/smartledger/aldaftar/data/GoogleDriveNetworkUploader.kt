package com.smartledger.aldaftar.data

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

class GoogleDriveNetworkUploader(
    private val context: Context
) {
    companion object {
        private const val TAG = "GoogleDriveNetworkUploader"

        private const val PREFS_NAME = "google_drive_uploader_prefs"
        private const val KEY_LAST_UPLOADED_HASH = "last_uploaded_payload_hash"

        private const val DRIVE_FILES_BASE_URL = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_BASE_URL = "https://www.googleapis.com/upload/drive/v3/files"
        private const val MIME_TYPE_JSON_VALUE = "application/json"

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

    sealed class UploadResult {
        object Success : UploadResult()
        object SkippedUnchanged : UploadResult()
        object FileNotFound : UploadResult()
        data class AuthError(val statusCode: Int) : UploadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : UploadResult()
    }

    sealed class DownloadResult {
        data class Success(val content: String) : DownloadResult()
        object FileNotFound : DownloadResult()
        data class InvalidPayload(val message: String) : DownloadResult()
        data class AuthError(val statusCode: Int) : DownloadResult()
        data class Failure(val message: String, val isRetryable: Boolean) : DownloadResult()
    }

    fun getStoredPayloadHash(): String? = uploaderPrefs.getString(KEY_LAST_UPLOADED_HASH, null)

    fun saveLastUploadedPayloadHash(hash: String) {
        uploaderPrefs.edit().putString(KEY_LAST_UPLOADED_HASH, hash).apply()
        Log.d(TAG, "تم حفظ بصمة النسخة الاحتياطية المرفوعة بنجاح.")
    }

    fun isPayloadIdentical(jsonContent: String): Boolean {
        val currentHash = BackupPayloadSerializer.calculateSha256Hash(jsonContent)
        val storedHash = getStoredPayloadHash()
        val match = storedHash != null && storedHash == currentHash
        if (match) {
            Log.i(TAG, "فحص البصمة: تطابق تام مع آخر نسخة مرفوعة، سيتم تخطي الرفع غير الضروري.")
        }
        return match
    }

    suspend fun createAndUploadNewFile(
        filename: String,
        backupJsonContent: String,
        accessToken: String
    ): UploadResult = withContext(Dispatchers.IO) {
        if (backupJsonContent.isBlank()) {
            return@withContext UploadResult.Failure("محتوى النسخة فارغ", isRetryable = false)
        }
        try {
            val createRequest = Request.Builder()
                .url(DRIVE_FILES_BASE_URL)
                .header(HEADER_AUTHORIZATION, bearer(accessToken))
                .post(
                    JSONObject()
                        .put("name", filename)
                        .put("parents", org.json.JSONArray().put("appDataFolder"))
                        .put("mimeType", MIME_TYPE_JSON_VALUE)
                        .toString()
                        .toRequestBody(MEDIA_TYPE_JSON)
                )
                .build()

            val fileId = client.newCall(createRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext responseCodeResult(response, "Create metadata")
                }
                JSONObject(response.body?.string().orEmpty()).optString("id").takeIf { it.isNotBlank() }
                    ?: return@withContext UploadResult.Failure("Create metadata returned no file id", false)
            }

            val uploadResult = try {
                cloudEngine.executeWithRetry(operationName = "UploadBackupMedia", maxRetries = 3) {
                    val request = Request.Builder()
                        .url("$DRIVE_UPLOAD_BASE_URL/$fileId?uploadType=media")
                        .header(HEADER_AUTHORIZATION, bearer(accessToken))
                        .patch(backupJsonContent.toRequestBody(MEDIA_TYPE_JSON))
                        .build()
                    client.newCall(request).execute().use { response ->
                        responseCodeResult(response, "Upload media")
                    }
                }
            } catch (_: IOException) {
                UploadResult.Failure("Network error", true)
            }

            if (uploadResult !is UploadResult.Success) {
                deleteFileById(fileId, accessToken)
                return@withContext uploadResult
            }

            saveLastUploadedPayloadHash(BackupPayloadSerializer.calculateSha256Hash(backupJsonContent))
            UploadResult.Success
        } catch (e: IOException) {
            Log.e(TAG, "فشل شبكي أثناء إنشاء النسخة: ${e.javaClass.simpleName}")
            UploadResult.Failure("Network error", true)
        } catch (e: Exception) {
            Log.e(TAG, "فشل إنشاء النسخة: ${e.javaClass.simpleName}")
            UploadResult.Failure("Unexpected upload error", false)
        }
    }

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
            val mediaResult = cloudEngine.executeWithRetry(operationName = "UpdateBackupMedia", maxRetries = 3) {
                val request = Request.Builder()
                    .url("$DRIVE_UPLOAD_BASE_URL/$fileId?uploadType=media")
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .patch(backupJsonContent.toRequestBody(MEDIA_TYPE_JSON))
                    .build()
                client.newCall(request).execute().use { response ->
                    responseCodeResult(response, "Update file")
                }
            }
            if (mediaResult !is UploadResult.Success) return@withContext mediaResult

            val metadataResult = cloudEngine.executeWithRetry(operationName = "UpdateBackupMetadata", maxRetries = 3) {
                val body = JSONObject().put("name", newFileName).toString().toRequestBody(MEDIA_TYPE_JSON)
                val request = Request.Builder()
                    .url("$DRIVE_FILES_BASE_URL/$fileId")
                    .header(HEADER_AUTHORIZATION, bearer(accessToken))
                    .patch(body)
                    .build()
                client.newCall(request).execute().use { response ->
                    responseCodeResult(response, "Update metadata")
                }
            }
            if (metadataResult !is UploadResult.Success) return@withContext metadataResult

            saveLastUploadedPayloadHash(BackupPayloadSerializer.calculateSha256Hash(backupJsonContent))
            UploadResult.Success
        } catch (e: IOException) {
            Log.e(TAG, "فشل شبكي أثناء تحديث النسخة: ${e.javaClass.simpleName}")
            UploadResult.Failure("Network error", isRetryable = true)
        } catch (e: Exception) {
            Log.e(TAG, "فشل تحديث النسخة: ${e.javaClass.simpleName}")
            UploadResult.Failure("Unexpected update error", isRetryable = false)
        }
    }

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
                when (val result = updateExistingFile(existingFileId, filename, backupJsonContent, accessToken)) {
                    UploadResult.FileNotFound -> return@withContext createAndUploadNewFile(filename, backupJsonContent, accessToken)
                    else -> return@withContext result
                }
            }
            createAndUploadNewFile(filename, backupJsonContent, accessToken)
        }
    }

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

        private fun responseCodeResult(response: okhttp3.Response, operation: String): UploadResult {
        return when {
            response.isSuccessful -> UploadResult.Success
            response.code == 404 -> UploadResult.FileNotFound
            response.code == 401 || response.code == 403 -> UploadResult.AuthError(response.code)
            else -> UploadResult.Failure(
                "$operation failed: ${response.code}",
                isRetryable = response.code == 429 || response.code >= 500
            )
        }
    }

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
