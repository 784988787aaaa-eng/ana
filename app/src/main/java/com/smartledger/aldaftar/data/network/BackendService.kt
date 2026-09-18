package com.smartledger.aldaftar.data.network

import android.content.Context
import com.smartledger.aldaftar.data.cloud.CloudOperationException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BackendService(private val context: Context) {

    private fun backendBaseUrls(): List<String> {
        val lines = runCatching {
            context.assets.open("license_endpoint.txt").bufferedReader().use { reader ->
                reader.readLines().map { it.trim().trimEnd('/') }.filter { it.isNotBlank() && !it.startsWith("__") }
            }
        }.getOrNull().orEmpty()
        return lines.ifEmpty {
            listOf(
                "https://al-daftar-license-api.pages.dev",
                "https://al-daftar-license-api.mansour-ghawy.workers.dev"
            )
        }
    }

    fun postJson(path: String, body: JSONObject, timeoutMs: Int = 20_000): JSONObject {
        val urls = backendBaseUrls()
        var lastException: Exception? = null

        for (baseUrl in urls) {
            try {
                val fullUrl = baseUrl + "/" + path.trimStart('/')
                val conn = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs + 10_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Cache-Control", "no-store")
                }
                return try {
                    conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
                    val code = conn.responseCode
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                    if (code !in 200..299) {
                        val errCode = json.optString("error", "backend_error")
                        val userMsg = when (errCode) {
                            "invalid_session" -> "انتهت جلسة Google Drive؛ يرجى إعادة ربط الحساب."
                            "drive_forbidden" -> "ليس لدى Google Drive صلاحية كافية لهذا الحساب."
                            "rate_limited" -> "تم تجاوز حد الطلبات مؤقتاً."
                            "revoked" -> "الترخيص ملغى"
                            "activation_invalid" -> "رمز التفعيل غير صحيح"
                            "trial_expired" -> "انتهت الفترة التجريبية لهذا الترخيص"
                            "account_disabled" -> "تم إيقاف هذا الحساب من قبل الإدارة"
                            else -> json.optString("message", "تعذر تنفيذ العملية عبر الخادم.")
                        }
                        throw CloudOperationException(code, errCode, userMsg)
                    }
                    json
                } finally {
                    conn.disconnect()
                }
            } catch (e: CloudOperationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw CloudOperationException(0, "network_error", "تعذر الاتصال بالخادم السحابي", lastException ?: IOException("Network error"))
    }
}
