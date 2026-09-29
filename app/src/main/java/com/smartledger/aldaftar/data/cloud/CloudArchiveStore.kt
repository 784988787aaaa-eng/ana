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
import java.io.FileInputStream
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
        private const val BACKUP_FOLDER_NAME = "الدفتر الذكي برو"
        private const val DRIVE_SCOPE_FULL = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
        private const val DRIVE_SCOPE_FILE = "oauth2:https://www.googleapis.com/auth/drive.file"
    }

    fun connected(): Boolean {
        if (!connection.token().isNullOrBlank()) return true
        val lastAccount = GoogleSignIn.getLastSignedInAccount(appContext)
        if (lastAccount != null && (!lastAccount.email.isNullOrBlank() || lastAccount.account != null)) return true
        if (!connection.email().isNullOrBlank()) return true
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
    }

    suspend fun googleClientId(): String = withContext(Dispatchers.IO) {
        val configured = com.smartledger.aldaftar.BuildConfig.GOOGLE_CLIENT_ID.trim()
        if (configured.isNotBlank()) return@withContext configured
        val json = runCatching { backendPostJson("/driveApi/config", JSONObject()) }.getOrNull()
        json?.optString("googleClientId")?.trim() ?: ""
    }

    /** Exchanges the one-time Google server auth code if available, otherwise caches email for direct Drive API */
    suspend fun connectWithServerAuthCode(serverAuthCode: String): Boolean = withContext(Dispatchers.IO) {
        if (serverAuthCode.isNotBlank()) {
            runCatching {
                val response = backendPostJson(
                    "/driveApi/connect/google-signin",
                    JSONObject().put("serverAuthCode", serverAuthCode)
                )
                val cloudToken = response.optString("cloudToken").trim()
                if (cloudToken.isNotBlank()) {
                    connection.save(cloudToken)
                    connection.saveFolderId(null)
                }
            }
        }
        GoogleSignIn.getLastSignedInAccount(appContext)?.email?.let(::saveEmail)
        true
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        connected()
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            runCatching { backendPostJson("/driveApi/disconnect", JSONObject().put("cloudToken", cloudToken)) }
        }
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
            GoogleAuthUtil.getToken(appContext, accountObj, DRIVE_SCOPE_FULL)
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
        if (!connected()) {
            throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        }

        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            val backendResult = runCatching { backendList(cloudToken, search) }
            if (backendResult.isSuccess) {
                return@withContext backendResult.getOrThrow()
            }
        }

        // Direct Google Drive API fallback
        val accessToken = getOAuthAccessToken()
        directDriveList(accessToken, search)
    }

    suspend fun upload(file: File, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        if (!connected()) {
            throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        }

        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            val backendResult = runCatching { backendUpload(cloudToken, file, name) }
            if (backendResult.isSuccess) {
                return@withContext backendResult.getOrThrow()
            }
        }

        // Direct Google Drive API fallback
        val accessToken = getOAuthAccessToken()
        directDriveUpload(accessToken, file, name)
    }

    suspend fun download(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        if (!connected()) {
            throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        }

        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            val backendResult = runCatching { backendDownload(cloudToken, fileId) }
            if (backendResult.isSuccess) {
                return@withContext backendResult.getOrThrow()
            }
        }

        // Direct Google Drive API fallback
        val accessToken = getOAuthAccessToken()
        directDriveDownload(accessToken, fileId)
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        if (!connected()) {
            throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        }

        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            val backendResult = runCatching { backendDelete(cloudToken, ids) }
            if (backendResult.isSuccess) {
                return@withContext backendResult.getOrThrow()
            }
        }

        // Direct Google Drive API fallback
        val accessToken = getOAuthAccessToken()
        directDriveDelete(accessToken, ids)
    }

    private fun getOrCreateFolderId(token: String): String {
        val cachedId = connection.folderId()
        if (!cachedId.isNullOrBlank()) {
            if (verifyFolderExists(cachedId, token)) {
                return cachedId
            }
        }

        // Query Drive for existing folder
        val queryStr = URLEncoder.encode("name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false", "UTF-8")
        val fieldsStr = URLEncoder.encode("files(id)", "UTF-8")
        val url = "https://www.googleapis.com/drive/v3/files?q=$queryStr&fields=$fieldsStr"

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 20_000
        }

        try {
            val code = conn.responseCode
            if (code in 200..299) {
                val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val files = JSONObject(text).optJSONArray("files")
                if (files != null && files.length() > 0) {
                    val folderId = files.getJSONObject(0).getString("id")
                    connection.saveFolderId(folderId)
                    return folderId
                }
            }
        } catch (_: Exception) {} finally {
            conn.disconnect()
        }

        // Create folder
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
                put("name", BACKUP_FOLDER_NAME)
                put("mimeType", "application/vnd.google-apps.folder")
            }
            createConn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            val code = createConn.responseCode
            val stream = if (code in 200..299) createConn.inputStream else createConn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw createExceptionFromResponse(code, text)
            }
            val folderId = JSONObject(text).getString("id")
            connection.saveFolderId(folderId)
            return folderId
        } finally {
            createConn.disconnect()
        }
    }

    private fun verifyFolderExists(folderId: String, token: String): Boolean {
        val url = "https://www.googleapis.com/drive/v3/files/$folderId?fields=id,trashed"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return try {
            if (conn.responseCode in 200..299) {
                val json = JSONObject(conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
                !json.optBoolean("trashed", false)
            } else false
        } catch (_: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    private fun directDriveList(token: String, search: String): List<CloudBackupFile> {
        val folderId = getOrCreateFolderId(token)
        val queryStr = URLEncoder.encode("'$folderId' in parents and trashed=false", "UTF-8")
        val fieldsStr = URLEncoder.encode("files(id,name,size,modifiedTime,createdTime)", "UTF-8")
        val url = "https://www.googleapis.com/drive/v3/files?q=$queryStr&fields=$fieldsStr&orderBy=modifiedTime%20desc&pageSize=100"

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 20_000
            readTimeout = 30_000
        }

        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw createExceptionFromResponse(code, text)
            }
            val array = JSONObject(text).optJSONArray("files") ?: JSONArray()
            val list = mutableListOf<CloudBackupFile>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val fileName = item.getString("name")
                if (search.isNotBlank() && !fileName.contains(search, ignoreCase = true)) {
                    continue
                }
                val modIso = item.optString("modifiedTime", item.optString("createdTime", ""))
                val timeMs = parseIsoTime(modIso)
                val monthStr = if (timeMs > 0) SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timeMs)) else ""

                list.add(
                    CloudBackupFile(
                        id = item.getString("id"),
                        name = fileName,
                        size = item.optLong("size", 0L),
                        modifiedTime = if (timeMs > 0) timeMs else System.currentTimeMillis(),
                        month = monthStr
                    )
                )
            }
            return list
        } finally {
            conn.disconnect()
        }
    }

    private fun directDriveUpload(token: String, file: File, name: String): CloudBackupFile {
        val folderId = getOrCreateFolderId(token)
        val boundary = "SmartLedgerBoundary${System.currentTimeMillis()}"
        val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,size,modifiedTime"

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 30_000
            readTimeout = 120_000
        }

        val metadataJson = JSONObject().apply {
            put("name", name)
            put("parents", JSONArray().put(folderId))
        }.toString()

        val CRLF = "\r\n"
        val HYPHENS = "--"

        try {
            conn.outputStream.use { os ->
                val bodyHeader = StringBuilder().apply {
                    append(HYPHENS).append(boundary).append(CRLF)
                    append("Content-Type: application/json; charset=UTF-8").append(CRLF)
                    append(CRLF)
                    append(metadataJson).append(CRLF)
                    append(HYPHENS).append(boundary).append(CRLF)
                    append("Content-Type: application/octet-stream").append(CRLF)
                    append(CRLF)
                }.toString()

                os.write(bodyHeader.toByteArray(Charsets.UTF_8))
                FileInputStream(file).use { fis -> fis.copyTo(os) }
                os.write(CRLF.toByteArray(Charsets.UTF_8))
                os.write("$HYPHENS$boundary$HYPHENS$CRLF".toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (code !in 200..299) {
                throw createExceptionFromResponse(code, text)
            }

            val json = JSONObject(text)
            val timeMs = parseIsoTime(json.optString("modifiedTime", ""))

            return CloudBackupFile(
                id = json.getString("id"),
                name = json.optString("name", name),
                size = json.optLong("size", file.length()),
                modifiedTime = if (timeMs > 0) timeMs else System.currentTimeMillis(),
                month = if (timeMs > 0) SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timeMs)) else ""
            )
        } finally {
            conn.disconnect()
        }
    }

    private fun directDriveDownload(token: String, fileId: String): ByteArray {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
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

    private fun directDriveDelete(token: String, ids: Set<String>): Int {
        var count = 0
        for (fileId in ids) {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15_000
                readTimeout = 20_000
            }
            try {
                val code = conn.responseCode
                if (code in 200..299 || code == 404) {
                    count++
                }
            } catch (_: Exception) {} finally {
                conn.disconnect()
            }
        }
        return count
    }

    private fun backendBaseUrls(): List<String> {
        val lines = runCatching {
            appContext.assets.open("license_endpoint.txt").bufferedReader().use { reader ->
                reader.readLines().map { it.trim().trimEnd('/') }.filter { it.isNotBlank() && !it.startsWith("__") }
            }
        }.getOrNull().orEmpty()
        return lines.ifEmpty { listOf("https://al-daftar-license-api.pages.dev", "https://al-daftar-license-api.mansour-ghawy.workers.dev") }
    }

    private fun backendPostJson(path: String, body: JSONObject): JSONObject {
        val urls = backendBaseUrls()
        var lastException: Exception? = null
        for (baseUrl in urls) {
            try {
                val conn = (URL(baseUrl + "/" + path.trimStart('/')).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 20_000
                    readTimeout = 90_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
                return try {
                    conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
                    val code = conn.responseCode
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                    if (code !in 200..299) {
                        throw CloudOperationException(code, json.optString("error", "cloud_error"),
                            when (json.optString("error")) {
                                "invalid_session" -> "انتهت جلسة Google Drive؛ يرجى إعادة ربط الحساب."
                                "drive_forbidden" -> "ليس لدى Google Drive صلاحية كافية لهذا الحساب."
                                "rate_limited" -> "تم تجاوز حد Google Drive مؤقتاً."
                                else -> "تعذر تنفيذ عملية Google Drive."
                            })
                    }
                    json
                } finally { conn.disconnect() }
            } catch (e: CloudOperationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw mapNetworkException(lastException ?: IOException("تعذر الاتصال بالخادم السحابي"))
    }

    private fun backendList(token: String, search: String): List<CloudBackupFile> {
        val json = backendPostJson("/driveApi/list", JSONObject().put("cloudToken", token).put("search", search))
        val array = json.optJSONArray("items") ?: JSONArray()
        return buildList(array.length()) {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(CloudBackupFile(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    size = item.optLong("size", 0L),
                    modifiedTime = item.optLong("modifiedTime", item.optLong("createdTime", 0L)),
                    month = item.optString("month", "")
                ))
            }
        }
    }

    private fun backendUpload(token: String, file: File, name: String): CloudBackupFile {
        val urls = backendBaseUrls()
        var lastException: Exception? = null
        for (baseUrl in urls) {
            try {
                val conn = (URL("$baseUrl/driveApi/upload").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 30_000
                    readTimeout = 120_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/octet-stream")
                    setRequestProperty("X-SmartLedger-Cloud-Token", token)
                    setRequestProperty("X-SmartLedger-File-Name", URLEncoder.encode(name, "UTF-8"))
                    setFixedLengthStreamingMode(file.length())
                }
                return try {
                    conn.outputStream.use { out -> FileInputStream(file).use { it.copyTo(out) } }
                    val code = conn.responseCode
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                    if (code !in 200..299) {
                        throw CloudOperationException(code, json.optString("error", "cloud_error"), "تعذر رفع النسخة إلى Google Drive.")
                    }
                    val item = json.getJSONObject("item")
                    CloudBackupFile(item.getString("id"), item.getString("name"), item.optLong("size", file.length()), item.optLong("modifiedTime", System.currentTimeMillis()), item.optString("month", ""))
                } finally { conn.disconnect() }
            } catch (e: CloudOperationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw mapNetworkException(lastException ?: IOException("تعذر الاتصال بالخادم السحابي"))
    }

    private fun backendDownload(token: String, fileId: String): ByteArray {
        val urls = backendBaseUrls()
        var lastException: Exception? = null
        for (baseUrl in urls) {
            try {
                val conn = (URL("$baseUrl/driveApi/download").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 20_000
                    readTimeout = 120_000
                    setRequestProperty("Accept", "application/octet-stream")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
                return try {
                    conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(JSONObject().put("cloudToken", token).put("fileId", fileId).toString()) }
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        val text = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                        val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                        throw CloudOperationException(code, json.optString("error", "cloud_error"), "تعذر تنزيل النسخة من Google Drive.")
                    }
                    conn.inputStream.use { it.readBytes() }
                } finally { conn.disconnect() }
            } catch (e: CloudOperationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw mapNetworkException(lastException ?: IOException("تعذر الاتصال بالخادم السحابي"))
    }

    private fun backendDelete(token: String, ids: Set<String>): Int {
        val json = backendPostJson("/driveApi/delete", JSONObject().put("cloudToken", token).put("fileIds", JSONArray(ids.toList())))
        return json.optInt("deleted", 0)
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
