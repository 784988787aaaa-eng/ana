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
import java.util.UUID

class CloudArchiveStore(context: Context) {
    private val appContext = context.applicationContext
    private val connection = CloudConnectionStore(appContext)
    private val endpoint = appContext.assets.open("license_endpoint.txt").bufferedReader().use { it.readText().trim().trimEnd('/') }.removeSuffix("/license") + "/driveApi"

    fun connected(): Boolean = connection.token() != null

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        require(endpoint.isNotBlank() && !endpoint.startsWith("__")) { "خدمة السحابة غير مهيأة" }
        val connectionId = UUID.randomUUID().toString()
        val start = post("$endpoint/connect/start", JSONObject().put("connectionId", connectionId))
        val authUrl = start.getString("authorizationUrl")
        appContext.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(authUrl)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        repeat(60) {
            delay(2_000)
            val status = post("$endpoint/connect/status", JSONObject().put("connectionId", connectionId))
            when (status.optString("status")) {
                "connected" -> {
                    connection.save(status.getString("cloudToken"))
                    return@withContext true
                }
                "failed" -> return@withContext false
            }
        }
        false
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
        val connection = (URL("$endpoint/download").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Cache-Control", "no-store")
        }
        try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(JSONObject().put("cloudToken", requireToken()).put("fileId", id).toString()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            if (connection.responseCode !in 200..299) throw IllegalStateException("تعذر تنزيل النسخة السحابية")
            stream?.use { it.readBytes() } ?: error("تعذر تنزيل النسخة السحابية")
        } finally { connection.disconnect() }
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        post("$endpoint/delete", JSONObject().put("cloudToken", requireToken()).put("fileIds", JSONArray(ids.toList()))).optInt("deleted", 0)
    }

    private fun requireToken(): String = connection.token() ?: error("السحابة غير مرتبطة")

    private fun postBytes(url: String, bytes: ByteArray, token: String, name: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
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
        return try {
            connection.outputStream.use { it.write(bytes) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (connection.responseCode !in 200..299) {
                if (json.optString("error") == "invalid_session") this@CloudArchiveStore.connection.clear()
                throw IllegalStateException("تعذر رفع النسخة السحابية")
            }
            json
        } finally { connection.disconnect() }
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Cache-Control", "no-store")
        }
        return try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() } }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (connection.responseCode !in 200..299) {
                if (json.optString("error") == "invalid_session") this@CloudArchiveStore.connection.clear()
                throw IllegalStateException(errorMessage(json.optString("error")))
            }
            json
        } finally { connection.disconnect() }
    }

    private fun errorMessage(error: String): String = when (error) {
        "not_connected", "invalid_session" -> "جلسة Google Drive غير صالحة، أعد الربط"
        "authorization_failed" -> "تعذر إتمام تفويض Google Drive"
        "rate_limited" -> "تم تجاوز محاولات السحابة مؤقتاً"
        else -> "تعذر تنفيذ عملية Google Drive"
    }
}
