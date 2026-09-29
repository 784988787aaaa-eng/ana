package com.smartledger.aldaftar.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Publishes the already-encrypted SNA archive into the user's Documents folder. */
class PublicBackupStore(private val context: Context) {
    companion object {
        const val ROOT_FOLDER = "الدفتر الذكي برو"
        private const val MIME = "application/vnd.smartledger.backup"
    }

    fun publish(file: File, date: Date = Date()): Uri {
        require(file.isFile) { "ملف النسخة غير موجود" }
        val monthFolder = monthFolderName(date)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            publishScoped(file, monthFolder)
        } else {
            publishLegacy(file, monthFolder)
        }
    }

    private fun publishScoped(file: File, monthFolder: String): Uri {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/$ROOT_FOLDER/$monthFolder/"

        // Idempotent publication: a retry of the same SNA filename replaces the
        // previous public copy instead of creating duplicates.
        resolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
            arrayOf(file.name, relativePath),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                resolver.delete(ContentUris.withAppendedId(collection, cursor.getLong(idColumn)), null, null)
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("تعذر إنشاء ملف النسخة في مجلد المستندات")

        try {
            resolver.openOutputStream(uri, "w")!!.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }, null, null)
            return uri
        } catch (t: Throwable) {
            resolver.delete(uri, null, null)
            throw t
        }
    }

    @Suppress("DEPRECATION")
    private fun publishLegacy(file: File, monthFolder: String): Uri {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            ROOT_FOLDER
        )
        val destination = File(File(root, monthFolder), file.name)
        destination.parentFile?.mkdirs()
        file.inputStream().use { input -> destination.outputStream().use { output -> input.copyTo(output) } }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destination)
    }

    private fun monthFolderName(date: Date): String {
        val month = SimpleDateFormat("MM", Locale.US).format(date).toInt()
        return "شهر ${month.toString().padStart(2, '0')}"
    }
}
