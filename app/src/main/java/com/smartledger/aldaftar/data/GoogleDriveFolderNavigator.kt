/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
package com.smartledger.aldaftar.data

// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
class GoogleDriveFolderNavigator(private val client: OkHttpClient) {

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    companion object {
        private const val TAG = "GoogleDriveFolderNavigator"

        private const val DRIVE_FILES_API_URL = "https://www.googleapis.com/drive/v3/files"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ENCODING_UTF8 = "UTF-8"

        private const val SPACE_APP_DATA_FOLDER = "appDataFolder"
        private const val QUERY_LATEST_BACKUP = "name contains 'Mzd_' and name contains '.mzd' and trashed = false"
        private const val QUERY_ALL_MZD_BACKUPS = "name contains '.mzd' and trashed = false"
        private const val ORDER_BY_MODIFIED_TIME_DESC = "modifiedTime desc"
        private const val FIELDS_BACKUPS_LIST = "files(id,name,size,createdTime,modifiedTime)"

        private const val JSON_KEY_FILES = "files"
        private const val JSON_KEY_ID = "id"
        private const val JSON_KEY_NAME = "name"
        private const val JSON_KEY_SIZE = "size"
        private const val JSON_KEY_CREATED_TIME = "createdTime"

        private const val CACHE_EXPIRY_MS = 60_000L // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
    }

    // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
    private var cachedLatestFileId: Pair<Long, String?>? = null

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    sealed class FileSearchResult {
        data class Success(val fileId: String?) : FileSearchResult()
        data class Error(val isAuthError: Boolean, val code: Int) : FileSearchResult()
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    sealed class ListBackupsResult {
        data class Success(val backups: List<CloudBackupFile>) : ListBackupsResult()
        data class Error(val isAuthError: Boolean, val code: Int) : ListBackupsResult()
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private fun buildAuthorizedRequest(url: String, accessToken: String): Request {
        return Request.Builder()
            .url(url)
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$accessToken")
            .get()
            .build()
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private fun isAuthError(code: Int): Boolean = code == 401 || code == 403

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    fun clearCache() {
        cachedLatestFileId = null
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun findLatestBackupFileId(accessToken: String, forceRefresh: Boolean = false): FileSearchResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            val cached = cachedLatestFileId
            if (cached != null && (now - cached.first) < CACHE_EXPIRY_MS) {
                return@withContext FileSearchResult.Success(cached.second)
            }
        }

        try {
            val searchUrl = "$DRIVE_FILES_API_URL?spaces=$SPACE_APP_DATA_FOLDER" +
                    "&orderBy=${URLEncoder.encode(ORDER_BY_MODIFIED_TIME_DESC, ENCODING_UTF8)}" +
                    "&q=${URLEncoder.encode(QUERY_LATEST_BACKUP, ENCODING_UTF8)}"

            val searchRequest = buildAuthorizedRequest(searchUrl, accessToken)

            client.newCall(searchRequest).execute().use { searchResponse ->
                if (searchResponse.isSuccessful) {
                    val rawBody = searchResponse.body?.string() ?: ""
                    val searchResult = JSONObject(rawBody)
                    val files = searchResult.optJSONArray(JSON_KEY_FILES)
                    val existingFileId = if (files != null && files.length() > 0) {
                        files.getJSONObject(0).getString(JSON_KEY_ID)
                    } else {
                        null
                    }
                    cachedLatestFileId = Pair(now, existingFileId)
                    FileSearchResult.Success(existingFileId)
                } else {
                    FileSearchResult.Error(isAuthError(searchResponse.code), searchResponse.code)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء البحث عن أحدث نسخة في Drive: ${e.javaClass.simpleName}")
            FileSearchResult.Error(false, -1)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun listCloudBackups(accessToken: String): ListBackupsResult = withContext(Dispatchers.IO) {
        try {
            val url = "$DRIVE_FILES_API_URL?spaces=$SPACE_APP_DATA_FOLDER" +
                    "&fields=${URLEncoder.encode(FIELDS_BACKUPS_LIST, ENCODING_UTF8)}" +
                    "&q=${URLEncoder.encode(QUERY_ALL_MZD_BACKUPS, ENCODING_UTF8)}" +
                    "&orderBy=${URLEncoder.encode(ORDER_BY_MODIFIED_TIME_DESC, ENCODING_UTF8)}" +
                    "&pageSize=1000"

            val request = buildAuthorizedRequest(url, accessToken)

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: ""
                    val json = JSONObject(rawBody)
                    val filesArray = json.optJSONArray(JSON_KEY_FILES) ?: return@use ListBackupsResult.Success(emptyList())
                    val list = mutableListOf<CloudBackupFile>()
                    for (i in 0 until filesArray.length()) {
                        val obj = filesArray.getJSONObject(i)
                        val id = obj.getString(JSON_KEY_ID)
                        val name = obj.getString(JSON_KEY_NAME)
                        val size = obj.optLong(JSON_KEY_SIZE, 0L)
                        val createdTime = obj.optString(JSON_KEY_CREATED_TIME, "")
                        list.add(CloudBackupFile(id, name, size, createdTime))
                    }
                    ListBackupsResult.Success(list.sortedByDescending { it.name })
                } else {
                    ListBackupsResult.Error(isAuthError(response.code), response.code)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء سرد النسخ السحابية في Drive: ${e.javaClass.simpleName}")
            ListBackupsResult.Error(false, -1)
        }
    }
}
