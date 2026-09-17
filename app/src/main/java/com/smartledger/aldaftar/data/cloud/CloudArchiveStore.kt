package com.smartledger.aldaftar.data.cloud

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
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Direct Google Drive REST client.
 * The encrypted .sna file leaves the device only after Google OAuth succeeds.
 * No Cloudflare/backend/proxy is used for Drive operations.
 */
class CloudArchiveStore(context: Context) {
    private val appContext = context.applicationContext
    private val connection = CloudConnectionStore(appContext)
    private val googleAuth = GoogleDriveInternalAuth(appContext)

    companion object {
        private const val DRIVE_API = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
        private const val BACKUP_FOLDER_NAME = "الدفتر الذكي برو"
        private const val MIME_FOLDER = "application/vnd.google-apps.folder"
        private const val MIME_BACKUP = "application/vnd.smartledger.backup"
        private const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"
    }

    fun connected(): Boolean = googleAuth.getLastSignedInAccount() != null

    fun email(): String? {
        val account = googleAuth.getLastSignedInAccount()
        val value = account?.email ?: connection.email()
        if (!value.isNullOrBlank()) connection.saveEmail(value)
        return value
    }

    fun saveEmail(email: String?) = connection.saveEmail(email)

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) { connected() }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { googleAuth.client().signOut() }
        connection.clear()
    }

    suspend fun list(search: String = ""): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        val rootId = findOrCreateFolder(BACKUP_FOLDER_NAME, "root")
        val folderJson = driveGet(
            "/files",
            mapOf(
                "q" to "'$rootId' in parents and mimeType = '$MIME_FOLDER' and trashed = false",
                "fields" to "files(id,name)",
                "pageSize" to "1000"
            )
        )
        val folders = folderJson.optJSONArray("files") ?: JSONArray()
        val result = mutableListOf<CloudBackupFile>()
        for (i in 0 until folders.length()) {
            val folder = folders.getJSONObject(i)
            val folderId = folder.getString("id")
            val month = folder.optString("name", "")
            val q = buildString {
                append("'$folderId' in parents and mimeType = '$MIME_BACKUP' and trashed = false")
                if (search.isNotBlank()) append(" and name contains '").append(search.replace("'", "\\'")).append("'")
            }
            val filesJson = driveGet(
                "/files",
                mapOf("q" to q, "fields" to "files(id,name,size,modifiedTime,createdTime,parents,mimeType)", "orderBy" to "modifiedTime desc", "pageSize" to "1000")
            )
            val files = filesJson.optJSONArray("files") ?: JSONArray()
            for (j in 0 until files.length()) {
                val item = files.getJSONObject(j)
                result += CloudBackupFile(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    size = item.optLong("size", 0L),
                    modifiedTime = parseDriveTime(item.optString("modifiedTime"), item.optString("createdTime")),
                    month = month
                )
            }
        }
        result.sortedByDescending { it.modifiedTime }
    }

    suspend fun upload(file: File, name: String): CloudBackupFile = withContext(Dispatchers.IO) {
        require(file.isFile) { "ملف النسخة غير موجود" }
        val monthFolder = monthFolderName()
        val rootId = findOrCreateFolder(BACKUP_FOLDER_NAME, "root")
        val monthId = findOrCreateFolder(monthFolder, rootId)
        val existing = findFile(name, monthId)
        val metadata = JSONObject().apply {
            put("name", name)
            put("mimeType", MIME_BACKUP)
            put("parents", JSONArray().put(monthId))
        }
        val boundary = "SmartLedger_${System.currentTimeMillis()}"
        val conn = authorizedConnection(
            URL(if (existing == null) "$DRIVE_UPLOAD?uploadType=multipart&fields=id,name,size,modifiedTime,createdTime" else "$DRIVE_UPLOAD/${existing.getString("id")}?uploadType=multipart&fields=id,name,size,modifiedTime,createdTime"),
            if (existing == null) "POST" else "PATCH"
        ).apply {
            setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            setFixedLengthStreamingMode(multipartLength(metadata, file.length(), boundary))
        }
        try {
            conn.outputStream.use { out ->
                val prefix = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${metadata}\r\n--$boundary\r\nContent-Type: $MIME_BACKUP\r\n\r\n".toByteArray(StandardCharsets.UTF_8)
                out.write(prefix)
                file.inputStream().use { input -> input.copyTo(out) }
                out.write("\r\n--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8))
            }
            val json = readJsonResponse(conn)
            CloudBackupFile(
                id = json.getString("id"),
                name = json.getString("name"),
                size = json.optLong("size", file.length()),
                modifiedTime = parseDriveTime(json.optString("modifiedTime"), json.optString("createdTime")),
                month = monthFolder
            )
        } finally { conn.disconnect() }
    }

    suspend fun download(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(fileId, "UTF-8")
        val conn = authorizedConnection(URL("$DRIVE_API/files/$encoded?alt=media"), "GET")
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw cloudError(code, readText(conn))
            conn.inputStream.use { it.readBytes() }
        } finally { conn.disconnect() }
    }

    suspend fun delete(ids: Set<String>): Int = withContext(Dispatchers.IO) {
        var count = 0
        ids.forEach { id ->
            val encoded = URLEncoder.encode(id, "UTF-8")
            val conn = authorizedConnection(URL("$DRIVE_API/files/$encoded"), "DELETE")
            try {
                val code = conn.responseCode
                if (code in 200..299 || code == 404) count++ else throw cloudError(code, readText(conn))
            } finally { conn.disconnect() }
        }
        count
    }

    private fun findOrCreateFolder(name: String, parentId: String): String {
        val escaped = name.replace("'", "\\'")
        val query = "name = '$escaped' and mimeType = '$MIME_FOLDER' and trashed = false and '$parentId' in parents"
        val json = driveGet("/files", mapOf("q" to query, "fields" to "files(id,name)", "pageSize" to "10"))
        val existing = json.optJSONArray("files")?.takeIf { it.length() > 0 }?.getJSONObject(0)
        if (existing != null) return existing.getString("id")
        val metadata = JSONObject().apply {
            put("name", name)
            put("mimeType", MIME_FOLDER)
            put("parents", JSONArray().put(parentId))
        }
        val created = driveJsonPost("/files", metadata)
        return created.getString("id")
    }

    private fun findFile(name: String, parentId: String): JSONObject? {
        val escaped = name.replace("'", "\\'")
        val query = "name = '$escaped' and trashed = false and '$parentId' in parents"
        return driveGet("/files", mapOf("q" to query, "fields" to "files(id,name,size,modifiedTime,createdTime)", "pageSize" to "10"))
            .optJSONArray("files")?.takeIf { it.length() > 0 }?.getJSONObject(0)
    }

    private fun driveGet(path: String, params: Map<String, String>): JSONObject {
        val query = params.entries.joinToString("&") { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val conn = authorizedConnection(URL("$DRIVE_API$path?$query"), "GET")
        return try { readJsonResponse(conn) } finally { conn.disconnect() }
    }

    private fun driveJsonPost(path: String, body: JSONObject): JSONObject {
        val conn = authorizedConnection(URL("$DRIVE_API$path"), "POST").apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        return try {
            conn.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { it.write(body.toString()) }
            readJsonResponse(conn)
        } finally { conn.disconnect() }
    }

    private fun authorizedConnection(url: URL, method: String): HttpURLConnection {
        val account = googleAuth.getLastSignedInAccount()
            ?: throw CloudOperationException(401, "not_connected", "يرجى ربط حساب Google Drive أولاً.")
        val token = try {
            GoogleAuthUtil.getToken(appContext, account.account, DRIVE_SCOPE)
        } catch (e: UserRecoverableAuthException) {
            throw CloudOperationException(401, "reauthorization_required", "انتهت صلاحية تفويض Google Drive؛ يرجى إعادة ربط الحساب.", e)
        }
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doInput = true
            connectTimeout = 20_000
            readTimeout = 120_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
        }
    }

    private fun readJsonResponse(conn: HttpURLConnection): JSONObject {
        val code = conn.responseCode
        val text = readText(conn)
        if (code !in 200..299) throw cloudError(code, text)
        return JSONObject(text.ifBlank { "{}" })
    }

    private fun readText(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
    }

    private fun cloudError(code: Int, body: String): CloudOperationException {
        val json = runCatching { JSONObject(body) }.getOrNull()
        val reason = json?.optJSONObject("error")?.optJSONArray("errors")?.optJSONObject(0)?.optString("reason")
        val message = when {
            code == 401 -> "انتهت جلسة Google Drive؛ يرجى إعادة ربط الحساب."
            code == 403 -> "ليس لدى Google Drive صلاحية كافية لهذا الحساب."
            code == 429 -> "تم تجاوز حد Google Drive مؤقتاً."
            code in 500..599 -> "تعذر الوصول إلى Google Drive مؤقتاً."
            else -> "تعذر تنفيذ عملية Google Drive${reason?.let { ": $it" } ?: ""}."
        }
        return CloudOperationException(code, reason ?: "drive_error", message)
    }

    private fun multipartLength(metadata: JSONObject, size: Long, boundary: String): Long {
        val prefix = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${metadata}\r\n--$boundary\r\nContent-Type: $MIME_BACKUP\r\n\r\n".toByteArray(StandardCharsets.UTF_8).size
        val suffix = "\r\n--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8).size
        return prefix + size + suffix
    }

    private fun monthFolderName(): String =
        java.text.SimpleDateFormat("yyyy_MM", java.util.Locale.US).format(java.util.Date())

    private fun parseDriveTime(value: String?, fallback: String?): Long {
        return runCatching { java.time.OffsetDateTime.parse(value ?: fallback).toInstant().toEpochMilli() }
            .getOrElse { System.currentTimeMillis() }
    }
}
