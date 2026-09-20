package com.smartledger.aldaftar.data.cloud

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CloudArchiveStore(context: Context) {
    private val appContext = context.applicationContext
    private val connection = CloudConnectionStore(appContext)

    companion object {
        private const val BACKUP_ROOT_FOLDER_NAME = "الدفتر الذكي برو"
        private const val DRIVE_SCOPE_FILE = "oauth2:https://www.googleapis.com/auth/drive.file"
        private const val BACKUP_MIME = "application/vnd.smartledger.backup"
        private val BACKUP_PATTERN = Regex("""^(?:SNA_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}\.sna|SMN_\d{4}-\d{2}-\d{2}(?:_\d{4})?\.slb)$""", RegexOption.IGNORE_CASE)
        private const val UPLOAD_CHUNK_SIZE = 8 * 1024 * 1024
        private const val MAX_UPLOAD_RETRIES = 4
    }

    fun connected(): Boolean {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext) ?: return false
        return lastAccount.grantedScopes?.any { it.scopeUri == GoogleDriveInternalAuth.SCOPE_DRIVE_FILE.scopeUri } == true
    }

    fun email(): String? {
        val stored = connection.email()
        if (!stored.isNullOrBlank()) return stored
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        return lastAccount?.email?.also { connection.saveEmail(it) }
    }

    fun saveEmail(email: String?) {
        connection.saveEmail(email)
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        connected()
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { GoogleDriveInternalAuth(appContext).client().signOut() }
        connection.clear()
    }

    /** Retrieves direct Google OAuth Access Token from Google Play Services */
    private fun getOAuthAccessToken(): String {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(appContext)
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")

        val accountObj: Account = googleAccount.account
            ?: Account(googleAccount.email ?: email() ?: "user@gmail.com", "com.google")

        return try {
            GoogleAuthUtil.getToken(appContext, accountObj, DRIVE_SCOPE_FILE)
        } catch (e: UserRecoverableAuthException) {
            throw CloudOperationException(401, "auth_recoverable", "يرجى إعادة منح صلاحية Google Drive للحساب.")
        } catch (e: Exception) {
            try {
                GoogleAuthUtil.getToken(appContext, accountObj, DRIVE_SCOPE_FILE)
            } catch (ex: Exception) {
                throw mapNetworkException(ex)
            }
        }
    }

    suspend fun list(search: String = ""): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        if (!connected()) throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        directDriveList(getOAuthAccessToken(), search)
    }

    suspend fun upload(file: File, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        if (!connected()) throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        directDriveUpload(getOAuthAccessToken(), file, name)
    }

    suspend fun download(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        if (!connected()) throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        directDriveDownload(getOAuthAccessToken(), fileId)
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        if (!connected()) throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        directDriveDelete(getOAuthAccessToken(), ids)
    }

    private fun getOrCreateFolderId(token: String, parentId: String, folderName: String): String {
        val cachedId = if (parentId == "root" && folderName == BACKUP_ROOT_FOLDER_NAME) connection.folderId() else null
        if (!cachedId.isNullOrBlank() && verifyFolderExists(cachedId, token)) return cachedId

        val escapedName = folderName.replace("'", "\\'")
        val query = "'$parentId' in parents and name = '$escapedName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val url = "https://www.googleapis.com/drive/v3/files?q=${URLEncoder.encode(query, "UTF-8")}&fields=files(id,name)&pageSize=10"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        try {
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw createExceptionFromResponse(code, text)
            val files = JSONObject(text).optJSONArray("files")
            if (files != null && files.length() > 0) {
                val id = files.getJSONObject(0).getString("id")
                if (parentId == "root" && folderName == BACKUP_ROOT_FOLDER_NAME) connection.saveFolderId(id)
                return id
            }
        } finally {
            conn.disconnect()
        }

        val createConn = (URL("https://www.googleapis.com/drive/v3/files").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        try {
            val body = JSONObject().apply {
                put("name", folderName)
                put("mimeType", "application/vnd.google-apps.folder")
                put("parents", JSONArray().put(parentId))
            }
            createConn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            val code = createConn.responseCode
            val text = (if (code in 200..299) createConn.inputStream else createConn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw createExceptionFromResponse(code, text)
            val id = JSONObject(text).getString("id")
            if (parentId == "root" && folderName == BACKUP_ROOT_FOLDER_NAME) connection.saveFolderId(id)
            return id
        } finally {
            createConn.disconnect()
        }
    }

    private fun verifyFolderExists(folderId: String, token: String): Boolean {
        val url = "https://www.googleapis.com/drive/v3/files/$folderId?fields=id,mimeType,trashed"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return try {
            if (conn.responseCode in 200..299) {
                val json = JSONObject(conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
                json.optString("mimeType") == "application/vnd.google-apps.folder" && !json.optBoolean("trashed", false)
            } else false
        } catch (_: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    private fun monthFolderName(name: String): String {
        val match = Regex("""^(?:SNA_|SMN_)(\d{4})-(\d{2})-""").find(name)
        return if (match != null) "شهر ${match.groupValues[2]}" else "شهر غير محدد"
    }

    private fun directDriveList(token: String, search: String): List<CloudBackupFile> {
        val rootId = getOrCreateFolderId(token, "root", BACKUP_ROOT_FOLDER_NAME)
        val rootQuery = "'$rootId' in parents and trashed=false and mimeType='application/vnd.google-apps.folder'"
        val rootUrl = "https://www.googleapis.com/drive/v3/files?q=${URLEncoder.encode(rootQuery, "UTF-8")}&fields=files(id,name)&pageSize=100"
        val rootConn = (URL(rootUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 20_000
            readTimeout = 30_000
        }
        val folders = try {
            val code = rootConn.responseCode
            val text = (if (code in 200..299) rootConn.inputStream else rootConn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw createExceptionFromResponse(code, text)
            val array = JSONObject(text).optJSONArray("files") ?: JSONArray()
            buildList {
                for (i in 0 until array.length()) add(array.getJSONObject(i))
            }
        } finally {
            rootConn.disconnect()
        }

        val result = mutableListOf<CloudBackupFile>()
        for (folder in folders) {
            val folderId = folder.getString("id")
            val query = "'$folderId' in parents and trashed=false and mimeType != 'application/vnd.google-apps.folder'"
            val url = "https://www.googleapis.com/drive/v3/files?q=${URLEncoder.encode(query, "UTF-8")}&fields=files(id,name,size,modifiedTime,createdTime,mimeType)&orderBy=modifiedTime desc&pageSize=100"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 20_000
                readTimeout = 30_000
            }
            try {
                val code = conn.responseCode
                val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) throw createExceptionFromResponse(code, text)
                val array = JSONObject(text).optJSONArray("files") ?: JSONArray()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val fileName = item.optString("name", "")
                    if (!BACKUP_PATTERN.matches(fileName)) continue
                    if (item.optString("mimeType", BACKUP_MIME) != BACKUP_MIME) continue
                    if (search.isNotBlank() && !fileName.contains(search, ignoreCase = true) && !folder.optString("name").contains(search, ignoreCase = true)) continue
                    val timeMs = parseIsoTime(item.optString("modifiedTime", item.optString("createdTime", "")))
                    result += CloudBackupFile(
                        id = item.getString("id"),
                        name = fileName,
                        size = item.optLong("size", 0L),
                        modifiedTime = timeMs,
                        month = folder.optString("name", monthFolderName(fileName))
                    )
                }
            } finally {
                conn.disconnect()
            }
        }
        return result.sortedByDescending { it.modifiedTime }
    }

    private fun directDriveUpload(token: String, file: File, name: String): CloudBackupFile {
        require(BACKUP_PATTERN.matches(name)) { "invalid_backup_name" }
        val rootId = getOrCreateFolderId(token, "root", BACKUP_ROOT_FOLDER_NAME)
        val monthId = getOrCreateFolderId(token, rootId, monthFolderName(name))
        val escapedName = name.replace("'", "\\'")
        val findQuery = "'$monthId' in parents and trashed=false and name='$escapedName'"
        val findUrl = "https://www.googleapis.com/drive/v3/files?q=${URLEncoder.encode(findQuery, "UTF-8")}&fields=files(id,name,mimeType,size,modifiedTime)&pageSize=10"
        val findConn = (URL(findUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        val existing = try {
            val code = findConn.responseCode
            val text = (if (code in 200..299) findConn.inputStream else findConn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw createExceptionFromResponse(code, text)
            val files = JSONObject(text).optJSONArray("files")
            if (files != null && files.length() > 0) files.getJSONObject(0) else null
        } finally {
            findConn.disconnect()
        }

        val uploadUrl = if (existing != null) {
            initiateResumableUpload(
                token, "PATCH",
                "https://www.googleapis.com/upload/drive/v3/files/${existing.getString("id")}?uploadType=resumable",
                file.length(), name, null
            )
        } else {
            initiateResumableUpload(
                token, "POST",
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable",
                file.length(), name, monthId
            )
        }
        val uploaded = resumableUploadFile(uploadUrl, token, file)
        val timeMs = parseIsoTime(uploaded.optString("modifiedTime", ""))
        return CloudBackupFile(
            id = uploaded.getString("id"),
            name = uploaded.optString("name", name),
            size = uploaded.optLong("size", file.length()),
            modifiedTime = if (timeMs > 0) timeMs else System.currentTimeMillis(),
            month = monthFolderName(name)
        )
    }

    private fun initiateResumableUpload(
        token: String,
        method: String,
        url: String,
        size: Long,
        name: String,
        parentId: String?
    ): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("X-Upload-Content-Type", BACKUP_MIME)
            setRequestProperty("X-Upload-Content-Length", size.toString())
            setRequestProperty("Accept", "application/json")
            connectTimeout = 30_000
            readTimeout = 30_000
        }
        try {
            val metadata = JSONObject().apply {
                put("name", name)
                put("mimeType", BACKUP_MIME)
                if (parentId != null) put("parents", JSONArray().put(parentId))
            }
            conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(metadata.toString()) }
            val code = conn.responseCode
            val sessionUrl = conn.getHeaderField("Location")
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299 || sessionUrl.isNullOrBlank()) throw createExceptionFromResponse(code, body)
            return sessionUrl
        } finally {
            conn.disconnect()
        }
    }

    private fun resumableUploadFile(sessionUrl: String, token: String, file: File): JSONObject {
        val total = file.length()
        var offset = 0L
        var retries = 0
        val buffer = ByteArray(UPLOAD_CHUNK_SIZE)
        RandomAccessFile(file, "r").use { raf ->
            while (offset < total) {
                val toSend = minOf(UPLOAD_CHUNK_SIZE.toLong(), total - offset).toInt()
                raf.seek(offset)
                var read = 0
                while (read < toSend) {
                    val n = raf.read(buffer, read, toSend - read)
                    if (n < 0) throw IOException("backup_file_read_failed")
                    read += n
                }
                val end = offset + toSend - 1
                val conn = (URL(sessionUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", BACKUP_MIME)
                    setRequestProperty("Content-Length", toSend.toString())
                    setRequestProperty("Content-Range", "bytes $offset-$end/$total")
                    connectTimeout = 30_000
                    readTimeout = 120_000
                }
                try {
                    conn.outputStream.use { it.write(buffer, 0, toSend) }
                    val code = conn.responseCode
                    val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    when {
                        code in 200..299 -> return JSONObject(body)
                        code == 308 -> {
                            offset = parseUploadedRange(conn.getHeaderField("Range"), end + 1)
                            retries = 0
                        }
                        code in 429..599 && retries < MAX_UPLOAD_RETRIES -> {
                            retries++
                            Thread.sleep((500L * (1L shl (retries - 1))).coerceAtMost(8_000L))
                            offset = queryResumableOffset(sessionUrl, token, offset)
                        }
                        else -> throw createExceptionFromResponse(code, body)
                    }
                } finally {
                    conn.disconnect()
                }
            }
        }
        throw IOException("backup_upload_incomplete")
    }

    private fun parseUploadedRange(range: String?, fallback: Long): Long {
        val value = range?.substringAfterLast("-", "")?.toLongOrNull()
        return if (value != null) value + 1 else fallback
    }

    private fun queryResumableOffset(sessionUrl: String, token: String, currentOffset: Long): Long {
        val conn = (URL(sessionUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Length", "0")
            setRequestProperty("Content-Range", "bytes */$currentOffset")
            connectTimeout = 20_000
            readTimeout = 30_000
        }
        return try {
            val code = conn.responseCode
            if (code == 308) parseUploadedRange(conn.getHeaderField("Range"), currentOffset) else currentOffset
        } finally {
            conn.disconnect()
        }
    }

    private fun directDriveDownload(token: String, fileId: String): ByteArray {
        require(fileId.isNotBlank()) { "invalid_file" }
        getBackupMetadata(token, fileId)
        val url = "https://www.googleapis.com/drive/v3/files/${URLEncoder.encode(fileId, "UTF-8")}?alt=media"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/octet-stream")
            connectTimeout = 30_000
            readTimeout = 120_000
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val text = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                throw createExceptionFromResponse(code, text)
            }
            return conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun getBackupMetadata(token: String, fileId: String): JSONObject {
        val url = "https://www.googleapis.com/drive/v3/files/${URLEncoder.encode(fileId, "UTF-8")}?fields=id,name,mimeType,parents,trashed"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 20_000
            readTimeout = 30_000
        }
        try {
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw createExceptionFromResponse(code, text)
            val json = JSONObject(text)
            if (json.optBoolean("trashed", false) ||
                !BACKUP_PATTERN.matches(json.optString("name", "")) ||
                json.optString("mimeType") != BACKUP_MIME
            ) {
                throw CloudOperationException(400, "invalid_backup_file", "الملف المحدد ليس نسخة احتياطية صالحة للدفتر الذكي.")
            }
            return json
        } finally {
            conn.disconnect()
        }
    }

    private fun directDriveDelete(token: String, ids: Set<String>): Int {
        var count = 0
        for (fileId in ids.take(100)) {
            if (fileId.isBlank()) continue
            val conn = (URL("https://www.googleapis.com/drive/v3/files/${URLEncoder.encode(fileId, "UTF-8")}")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15_000
                readTimeout = 20_000
            }
            try {
                val code = conn.responseCode
                if (code in 200..299 || code == 404) count++
            } catch (_: Exception) {
            } finally {
                conn.disconnect()
            }
        }
        return count
    }

    private fun parseIsoTime(isoStr: String): Long {
        if (isoStr.isBlank()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(isoStr)?.time ?: parseFallbackIso(isoStr)
        } catch (_: Exception) {
            parseFallbackIso(isoStr)
        }
    }

    private fun parseFallbackIso(isoStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(isoStr)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun createExceptionFromResponse(code: Int, body: String): CloudOperationException {
        val json = runCatching { JSONObject(body) }.getOrNull()
        val errorObj = json?.optJSONObject("error")
        val errorMsg = errorObj?.optString("message") ?: json?.optString("error", "") ?: ""

        val friendlyMessage = when (code) {
            401 -> "انتهت صلاحية جلسة Google Drive. يرجى تسجيل الدخول وإعادة ربط الحساب."
            403 -> "تم رفض الوصول إلى Google Drive. يرجى التأكد من منح صلاحية إدارة ملفات التطبيق."
            404 -> "الملف أو المجلد المطلوب غير موجود في Google Drive."
            429 -> "تم تجاوز حد الطلبات مؤقتًا لـ Google Drive. يرجى الانتظار دقيقة والمحاولة ثانية."
            in 500..599 -> "خدمة Google Drive تواجه ضغطًا مؤقتًا. يرجى المحاولة بعد قليل."
            else -> if (errorMsg.isNotBlank()) "خطأ من Google Drive: $errorMsg" else "تعذر إتمام العملية على Google Drive (رمز: $code)"
        }

        return CloudOperationException(
            statusCode = code,
            errorCode = "drive_http_$code",
            userMessage = friendlyMessage
        )
    }

    private fun mapNetworkException(e: Exception): CloudOperationException {
        if (e is CloudOperationException) return e
        return when (e) {
            is UnknownHostException -> CloudOperationException(0, "network_offline", "تعذر الاتصال بـ Google Drive. تحقق من اتصال الإنترنت وحاول ثانية.", e)
            is SocketTimeoutException -> CloudOperationException(0, "network_timeout", "انتهت مهلة الاتصال بـ Google Drive. تحقق من جودة الإنترنت وحاول ثانية.", e)
            is IOException -> CloudOperationException(0, "network_io", "حدث انقطاع في الشبكة أثناء الاتصال بـ Google Drive.", e)
            else -> CloudOperationException(0, "unexpected_error", e.message ?: "تعذر إتمام الاتصال بـ Google Drive", e)
        }
    }
}
