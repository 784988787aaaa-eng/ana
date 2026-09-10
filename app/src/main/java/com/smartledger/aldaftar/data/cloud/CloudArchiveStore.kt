package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException
import java.util.UUID

class CloudArchiveStore(context: Context) {
    private val appContext = context.applicationContext
    private val connection = CloudConnectionStore(appContext)
    private val endpoint = appContext.assets.open("license_endpoint.txt").bufferedReader().use { it.readText().trim().trimEnd('/') }.removeSuffix("/license") + "/driveApi"

    companion object {
        private const val TOTAL_POLL_TIMEOUT_MS = 570_000L // 9 minutes and 30 seconds
        private const val POLL_INTERVAL_MS = 2_500L
    }

    fun connected(): Boolean = connection.token() != null

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        if (endpoint.isBlank() || endpoint.startsWith("__")) {
            throw CloudOperationException(
                statusCode = 0,
                errorCode = "not_configured",
                userMessage = "خدمة السحابة غير مهيأة"
            )
        }
        val connectionId = UUID.randomUUID().toString()
        val start = post("$endpoint/connect/start", JSONObject().put("connectionId", connectionId))
        val authUrl = start.optString("authorizationUrl")
        if (authUrl.isBlank()) {
            throw CloudOperationException(
                statusCode = 0,
                errorCode = "invalid_auth_url",
                userMessage = "تعذر بدء تفويض Google Drive"
            )
        }

        appContext.startActivity(
            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl))
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )

        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < TOTAL_POLL_TIMEOUT_MS) {
            delay(POLL_INTERVAL_MS)
            val statusResponse = try {
                post("$endpoint/connect/status", JSONObject().put("connectionId", connectionId))
            } catch (ex: CloudOperationException) {
                if (ex.statusCode == 404) {
                    throw CloudOperationException(
                        statusCode = 404,
                        errorCode = "not_found",
                        userMessage = "جلسة ربط Google Drive غير موجودة أو ملغاة، أعد المحاولة."
                    )
                }
                if (ex.statusCode == 410) {
                    throw CloudOperationException(
                        statusCode = 410,
                        errorCode = "expired",
                        userMessage = "انتهت جلسة ربط Google Drive. أعد المحاولة."
                    )
                }
                throw ex
            }

            when (statusResponse.optString("status")) {
                "connected" -> {
                    val token = statusResponse.optString("cloudToken")
                    if (token.isNotBlank()) {
                        connection.save(token)
                        return@withContext true
                    } else {
                        throw CloudOperationException(
                            statusCode = 0,
                            errorCode = "missing_token",
                            userMessage = "تعذر استلام رمز تفويض السحابة"
                        )
                    }
                }
                "failed" -> {
                    val failureCode = statusResponse.optString("failureCode", "oauth_failed")
                    val msg = when (failureCode) {
                        "oauth_access_denied" -> "تم إلغاء أو رفض تفويض Google Drive من قبل المستخدم."
                        "oauth_token_exchange_failed" -> "فشل تبادل رمز تفويض Google Drive مع الخادم."
                        "oauth_invalid_grant" -> "رمز تفويض Google Drive غير صالح أو منتهي."
                        "oauth_redirect_uri_mismatch" -> "خطأ في تهيئة عنوان إعادة التوجيه لـ Google Drive."
                        "oauth_invalid_client" -> "بيانات اعتماد Google Cloud غير متطابقة."
                        "oauth_no_refresh_token" -> "لم تمنح Google رمز وصول دائم، يرجى إعادة الربط واختيار الموافقة الكاملة."
                        else -> "فشل تفويض Google Drive: $failureCode"
                    }
                    throw CloudOperationException(
                        statusCode = 400,
                        errorCode = failureCode,
                        userMessage = msg
                    )
                }
                "pending" -> {
                    // Continue waiting
                }
            }
        }

        throw CloudOperationException(
            statusCode = 408,
            errorCode = "timeout",
            userMessage = "انتهت مهلة ربط Google Drive. أعد المحاولة."
        )
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        connection.token()?.let { runCatching { post("$endpoint/disconnect", JSONObject().put("cloudToken", it)) } }
        connection.clear()
    }

    suspend fun list(search: String = ""): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        val token = requireToken()
        val response = post("$endpoint/list", JSONObject().put("cloudToken", token).put("search", search.trim()))
        val items = response.optJSONArray("items") ?: JSONArray()
        buildList(items.length()) {
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                add(CloudBackupFile(
                    item.getString("id"), item.getString("name"), item.optLong("size"),
                    item.optLong("modifiedTime"), item.optString("month")
                ))
            }
        }
    }

    suspend fun upload(bytes: ByteArray, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        val response = postBytes("$endpoint/upload", bytes, requireToken(), name)
        val item = response.getJSONObject("item")
        CloudBackupFile(item.getString("id"), item.getString("name"), item.optLong("size", bytes.size.toLong()), item.optLong("modifiedTime"), item.optString("month"))
    }

    suspend fun download(id: String): ByteArray = withContext(Dispatchers.IO) {
        val conn: HttpURLConnection
        try {
            conn = (URL("$endpoint/download").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 30_000
                setRequestProperty("Accept", "application/octet-stream")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Cache-Control", "no-store")
            }
        } catch (e: Exception) {
            throw mapNetworkException(e)
        }

        try {
            conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(JSONObject().put("cloudToken", requireToken()).put("fileId", id).toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            if (code !in 200..299) {
                val errText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json = runCatching { JSONObject(errText) }.getOrElse { JSONObject() }
                val errCode = json.optString("error", "drive_error")
                if (errCode == "invalid_session") this@CloudArchiveStore.connection.clear()
                throw CloudOperationException(code, errCode, errorMessage(code, errCode))
            }
            stream?.use { it.readBytes() } ?: throw CloudOperationException(code, "empty_response", "تعذر استلام ملف النسخة السحابية")
        } catch (e: Exception) {
            if (e is CloudOperationException) throw e
            throw mapNetworkException(e)
        } finally {
            conn.disconnect()
        }
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        post("$endpoint/delete", JSONObject().put("cloudToken", requireToken()).put("fileIds", JSONArray(ids.toList()))).optInt("deleted", 0)
    }

    private fun requireToken(): String = connection.token() ?: throw CloudOperationException(
        statusCode = 401,
        errorCode = "not_connected",
        userMessage = "السحابة غير مرتبطة"
    )

    private fun postBytes(url: String, bytes: ByteArray, token: String, name: String): JSONObject {
        val conn: HttpURLConnection
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 60_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/vnd.smartledger.backup")
                setRequestProperty("X-SmartLedger-Cloud-Token", token)
                setRequestProperty("X-SmartLedger-File-Name", java.net.URLEncoder.encode(name, "UTF-8"))
                setRequestProperty("Cache-Control", "no-store")
            }
        } catch (e: Exception) {
            throw mapNetworkException(e)
        }

        return try {
            conn.outputStream.use { it.write(bytes) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (code !in 200..299) {
                val errCode = json.optString("error", "drive_error")
                if (errCode == "invalid_session") this@CloudArchiveStore.connection.clear()
                throw CloudOperationException(code, errCode, errorMessage(code, errCode))
            }
            json
        } catch (e: Exception) {
            if (e is CloudOperationException) throw e
            throw mapNetworkException(e)
        } finally {
            conn.disconnect()
        }
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val conn: HttpURLConnection
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 30_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Cache-Control", "no-store")
            }
        } catch (e: Exception) {
            throw mapNetworkException(e)
        }

        return try {
            conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() } }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (code !in 200..299) {
                val errCode = json.optString("error", "drive_error")
                if (errCode == "invalid_session") this@CloudArchiveStore.connection.clear()
                throw CloudOperationException(code, errCode, errorMessage(code, errCode))
            }
            json
        } catch (e: Exception) {
            if (e is CloudOperationException) throw e
            throw mapNetworkException(e)
        } finally {
            conn.disconnect()
        }
    }

    private fun mapNetworkException(e: Exception): CloudOperationException {
        return when (e) {
            is UnknownHostException -> CloudOperationException(0, "network_offline", "تعذر الاتصال بخدمة السحابة. تحقق من اتصال الإنترنت وحاول مرة أخرى.", e)
            is SocketTimeoutException -> CloudOperationException(0, "network_timeout", "انتهت مهلة الاتصال بخدمة السحابة. تحقق من جودة الإنترنت وحاول ثانية.", e)
            is IOException -> CloudOperationException(0, "network_io", "حدث خطأ أثناء نقل البيانات عبر الشبكة. تحقق من الاتصال وحاول مرة أخرى.", e)
            else -> CloudOperationException(0, "unexpected_network", "تعذر إتمام الاتصال بخدمة السحابة.", e)
        }
    }

    private fun errorMessage(statusCode: Int, error: String): String {
        return when {
            error == "invalid_session" || error == "not_connected" -> "جلسة Google Drive غير صالحة أو منتهية، أعد الربط."
            error == "authorization_failed" -> "تعذر إتمام تفويض Google Drive."
            error == "rate_limited" || statusCode == 429 -> "تم تجاوز محاولات السحابة مؤقتًا. حاول لاحقًا."
            error == "drive_forbidden" || statusCode == 403 -> "تم رفض الوصول إلى Google Drive. تأكد من إعطاء الصلاحيات المطلوبة."
            error == "drive_not_found" || statusCode == 404 -> "العنصر المطلوب غير موجود في Google Drive."
            error == "drive_unavailable" || statusCode in 500..599 -> "خدمة Google Drive غير متاحة حاليًا. حاول لاحقًا."
            statusCode == 410 -> "انتهت جلسة ربط Google Drive. أعد الربط."
            else -> "تعذر تنفيذ عملية Google Drive ($error)."
        }
    }
}
