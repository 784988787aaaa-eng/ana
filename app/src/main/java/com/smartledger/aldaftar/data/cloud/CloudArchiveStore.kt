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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CloudArchiveStore(context: Context) {
    private val appContext = context.applicationContext
    private val connection = CloudConnectionStore(appContext)

    companion object {
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
        private const val SCOPES = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
        private const val BACKUP_FOLDER_NAME = "الدفتر الذكي - النسخ الاحتياطي"
        private const val BOUNDARY = "===SMART_LEDGER_BACKUP_BOUNDARY==="
    }

    fun connected(): Boolean {
        val storedEmail = connection.email()
        if (!storedEmail.isNullOrBlank()) return true
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (lastAccount?.email != null) {
            connection.saveEmail(lastAccount.email)
            return true
        }
        return false
    }

    fun email(): String? {
        val stored = connection.email()
        if (!stored.isNullOrBlank()) return stored
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        return lastAccount?.email?.also { connection.saveEmail(it) }
    }

    fun saveEmail(email: String?) {
        connection.saveEmail(email)
        if (email != null) {
            connection.save("connected")
        }
    }

    suspend fun googleClientId(): String = withContext(Dispatchers.IO) {
        // Return default or empty if not configured
        ""
    }

    suspend fun connectWithServerAuthCode(serverAuthCode: String): Boolean = withContext(Dispatchers.IO) {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (lastAccount?.email != null) {
            saveEmail(lastAccount.email)
            return@withContext true
        }
        true
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (lastAccount?.email != null) {
            saveEmail(lastAccount.email)
            return@withContext true
        }
        false
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching {
            GoogleDriveInternalAuth(appContext).client().signOut()
        }
        connection.clear()
    }

    suspend fun list(search: String = ""): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        executeWithTokenRetry { token ->
            val folderId = getOrCreateBackupFolder(token)
            val queryParts = mutableListOf<String>()
            queryParts.add("trashed = false")

            if (folderId != null) {
                queryParts.add("('$folderId' in parents or name contains '.mzd' or name contains 'smartledger' or name contains 'backup' or mimeType = 'application/vnd.smartledger.backup')")
            } else {
                queryParts.add("(name contains '.mzd' or name contains 'smartledger' or name contains 'backup' or mimeType = 'application/vnd.smartledger.backup')")
            }

            if (search.isNotBlank()) {
                val cleanSearch = search.trim().replace("'", "\\'")
                queryParts.add("name contains '$cleanSearch'")
            }

            val fullQuery = queryParts.joinToString(" and ")
            val urlStr = "$DRIVE_API_BASE/files?q=${URLEncoder.encode(fullQuery, "UTF-8")}&fields=files(id,name,size,modifiedTime,createdTime,mimeType)&orderBy=modifiedTime+desc&pageSize=100&spaces=drive"

            val json = getJson(urlStr, token)
            val filesArray = json.optJSONArray("files") ?: JSONArray()
            val result = mutableListOf<CloudBackupFile>()

            for (i in 0 until filesArray.length()) {
                val fileObj = filesArray.getJSONObject(i)
                val mime = fileObj.optString("mimeType", "")
                if (mime == "application/vnd.google-apps.folder") continue

                val id = fileObj.getString("id")
                val name = fileObj.optString("name", "smartledger_backup.mzd")
                val size = fileObj.optLong("size", 0L)
                val modifiedTimeStr = fileObj.optString("modifiedTime", fileObj.optString("createdTime", ""))
                val modifiedTime = parseIsoTime(modifiedTimeStr)
                val month = SimpleDateFormat("yyyy-MM", Locale.US).format(modifiedTime)

                result.add(CloudBackupFile(id = id, name = name, size = size, modifiedTime = modifiedTime, month = month))
            }
            result
        }
    }

    suspend fun upload(bytes: ByteArray, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        executeWithTokenRetry { token ->
            val folderId = getOrCreateBackupFolder(token)
            val uploadUrlStr = "$DRIVE_UPLOAD_BASE/files?uploadType=multipart&fields=id,name,size,modifiedTime,createdTime"

            val metadataJson = JSONObject().apply {
                put("name", name)
                put("description", "SmartLedger Encrypted Backup")
                put("mimeType", "application/octet-stream")
                if (folderId != null) {
                    put("parents", JSONArray().put(folderId))
                }
            }

            val bodyStream = ByteArrayOutputStream()
            val header1 = "--$BOUNDARY\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${metadataJson}\r\n"
            val header2 = "--$BOUNDARY\r\nContent-Type: application/octet-stream\r\n\r\n"
            val footer = "\r\n--$BOUNDARY--\r\n"

            bodyStream.write(header1.toByteArray(Charsets.UTF_8))
            bodyStream.write(header2.toByteArray(Charsets.UTF_8))
            bodyStream.write(bytes)
            bodyStream.write(footer.toByteArray(Charsets.UTF_8))

            val payloadBytes = bodyStream.toByteArray()

            val conn = (URL(uploadUrlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 90_000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "multipart/related; boundary=$BOUNDARY")
                setRequestProperty("Content-Length", payloadBytes.size.toString())
                setRequestProperty("Accept", "application/json")
            }

            try {
                conn.outputStream.use { it.write(payloadBytes) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

                if (code !in 200..299) {
                    throw createExceptionFromResponse(code, responseText)
                }

                val item = JSONObject(responseText)
                val id = item.getString("id")
                val fileName = item.optString("name", name)
                val size = item.optLong("size", bytes.size.toLong())
                val modifiedTimeStr = item.optString("modifiedTime", item.optString("createdTime", ""))
                val modifiedTime = parseIsoTime(modifiedTimeStr)
                val month = SimpleDateFormat("yyyy-MM", Locale.US).format(modifiedTime)

                CloudBackupFile(id = id, name = fileName, size = size, modifiedTime = modifiedTime, month = month)
            } finally {
                conn.disconnect()
            }
        }
    }

    suspend fun download(id: String): ByteArray = withContext(Dispatchers.IO) {
        executeWithTokenRetry { token ->
            val urlStr = "$DRIVE_API_BASE/files/$id?alt=media"
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 25_000
                readTimeout = 90_000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/octet-stream")
            }

            try {
                val code = conn.responseCode
                if (code !in 200..299) {
                    val errText = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    throw createExceptionFromResponse(code, errText)
                }
                conn.inputStream.use { it.readBytes() }
            } finally {
                conn.disconnect()
            }
        }
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        executeWithTokenRetry { token ->
            var count = 0
            for (id in ids) {
                val urlStr = "$DRIVE_API_BASE/files/$id"
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    setRequestProperty("Authorization", "Bearer $token")
                }
                try {
                    val code = conn.responseCode
                    if (code in 200..299 || code == 404) {
                        count++
                    }
                } finally {
                    conn.disconnect()
                }
            }
            count
        }
    }

    private suspend fun <T> executeWithTokenRetry(block: suspend (String) -> T): T {
        var token = getAccessToken()
        return try {
            block(token)
        } catch (e: CloudOperationException) {
            if (e.statusCode == 401) {
                // Clear old token and retry once with a fresh token
                invalidateToken(token)
                token = getAccessToken()
                block(token)
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw mapNetworkException(e)
        }
    }

    private fun getAccessToken(): String {
        val email = email() ?: throw CloudOperationException(
            statusCode = 401,
            errorCode = "not_connected",
            userMessage = "يرجى ربط حساب Google Drive أولاً."
        )

        val account = Account(email, "com.google")
        return try {
            GoogleAuthUtil.getToken(appContext, account, SCOPES)
        } catch (e: UserRecoverableAuthException) {
            throw CloudOperationException(
                statusCode = 401,
                errorCode = "auth_required",
                userMessage = "يلزم تأكيد تفويض حساب Google Drive. يرجى الضغط على زر ربط الحساب.",
                cause = e
            )
        } catch (e: Exception) {
            // Try with last signed-in account if available
            val last = GoogleSignIn.getLastSignedInAccount(appContext)
            if (last?.account != null) {
                try {
                    return GoogleAuthUtil.getToken(appContext, last.account!!, SCOPES)
                } catch (ex: Exception) {
                    // Fallthrough to exception mapping
                }
            }
            throw mapNetworkException(e)
        }
    }

    private fun invalidateToken(token: String) {
        runCatching {
            GoogleAuthUtil.clearToken(appContext, token)
        }
    }

    private fun getOrCreateBackupFolder(token: String): String? {
        val cachedFolderId = connection.folderId()
        if (!cachedFolderId.isNullOrBlank()) {
            return cachedFolderId
        }

        return try {
            val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$BACKUP_FOLDER_NAME' and trashed = false"
            val searchUrl = "$DRIVE_API_BASE/files?q=${URLEncoder.encode(query, "UTF-8")}&fields=files(id,name)&spaces=drive"
            val searchJson = getJson(searchUrl, token)
            val files = searchJson.optJSONArray("files")

            if (files != null && files.length() > 0) {
                val id = files.getJSONObject(0).getString("id")
                connection.saveFolderId(id)
                return id
            }

            // Create new folder
            val createConn = (URL("$DRIVE_API_BASE/files").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            val meta = JSONObject().apply {
                put("name", BACKUP_FOLDER_NAME)
                put("mimeType", "application/vnd.google-apps.folder")
                put("description", "مجلد النسخ الاحتياطية لتطبيق الدفتر الذكي")
            }

            createConn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(meta.toString()) }
            val code = createConn.responseCode
            if (code in 200..299) {
                val res = createConn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val newId = JSONObject(res).optString("id")
                if (newId.isNotBlank()) {
                    connection.saveFolderId(newId)
                    return newId
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun getJson(urlStr: String, token: String): JSONObject {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 40_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
        }

        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw createExceptionFromResponse(code, text)
            }
            return JSONObject(text)
        } finally {
            conn.disconnect()
        }
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
            401 -> "انتهت صلاحية جلسة Google Drive. يرجى إعادة ربط الحساب."
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
