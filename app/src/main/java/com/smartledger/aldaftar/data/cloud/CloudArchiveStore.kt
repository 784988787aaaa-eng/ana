package com.smartledger.aldaftar.data.cloud

import android.content.Context
import android.os.Build
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
        private const val BACKUP_FOLDER_NAME = "الدفتر الذكي برو"
    }

    fun connected(): Boolean = !connection.token().isNullOrBlank()

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
        val json = backendPostJson("/driveApi/config", JSONObject())
        json.optString("googleClientId").trim().also {
            require(it.isNotBlank()) { "معرّف Google OAuth غير مهيأ على الخادم" }
        }
    }

    /** Exchanges the one-time Google server auth code on the trusted backend.
     * The Google refresh token never enters the Android process; the backend
     * stores it encrypted and returns only a short-lived cloud session token.
     */
    suspend fun connectWithServerAuthCode(serverAuthCode: String): Boolean = withContext(Dispatchers.IO) {
        require(serverAuthCode.isNotBlank()) { "رمز مصادقة Google فارغ" }
        val response = backendPostJson(
            "/driveApi/connect/google-signin",
            JSONObject().put("serverAuthCode", serverAuthCode)
        )
        val cloudToken = response.optString("cloudToken").trim()
        require(cloudToken.isNotBlank()) { "لم يُرجع خادم Google Drive جلسة صالحة" }
        connection.save(cloudToken)
        connection.saveFolderId(null)
        GoogleSignIn.getLastSignedInAccount(appContext)?.email?.let(::saveEmail)
        true
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        !connection.token().isNullOrBlank()
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        val cloudToken = connection.token()
        if (!cloudToken.isNullOrBlank()) {
            runCatching { backendPostJson("/driveApi/disconnect", JSONObject().put("cloudToken", cloudToken)) }
        }
        runCatching { GoogleDriveInternalAuth(appContext).client().signOut() }
        connection.clear()
    }

    suspend fun list(search: String = ""): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        val cloudToken = connection.token()?.takeIf { it.isNotBlank() }
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        backendList(cloudToken, search)
    }

    suspend fun upload(file: java.io.File, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        val cloudToken = connection.token()?.takeIf { it.isNotBlank() }
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        backendUpload(cloudToken, file, name)
    }

    suspend fun download(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        val cloudToken = connection.token()?.takeIf { it.isNotBlank() }
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        backendDownload(cloudToken, fileId)
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        val cloudToken = connection.token()?.takeIf { it.isNotBlank() }
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        backendDelete(cloudToken, ids)
    }

    private val backendService = com.smartledger.aldaftar.data.network.BackendService(appContext)

    private fun backendBaseUrls(): List<String> = runCatching {
        appContext.assets.open("license_endpoint.txt").bufferedReader().use { reader ->
            reader.readLines().map { it.trim().trimEnd('/') }.filter { it.isNotBlank() && !it.startsWith("__") }
        }
    }.getOrNull()?.takeIf { it.isNotEmpty() } ?: listOf("https://al-daftar-license-api.pages.dev", "https://al-daftar-license-api.mansour-ghawy.workers.dev")

    private fun backendPostJson(path: String, body: JSONObject): JSONObject {
        return backendService.postJson(path, body)
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

    private fun backendUpload(token: String, file: java.io.File, name: String): CloudBackupFile {
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
                    conn.outputStream.use { out -> java.io.FileInputStream(file).use { it.copyTo(out) } }
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
