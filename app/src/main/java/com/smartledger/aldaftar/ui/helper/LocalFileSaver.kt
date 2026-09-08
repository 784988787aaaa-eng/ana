package com.smartledger.aldaftar.ui.helper

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.smartledger.aldaftar.R
import java.io.File

object LocalFileSaver {
    private const val TAG = "LocalFileSaver"

    fun saveFileToPublicDownloads(
        context: Context,
        cachedFile: File,
        mimeType: String,
        displayName: String
    ): Boolean {
        if (!cachedFile.exists() || cachedFile.length() == 0L) {
            return false
        }
        return try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val uri = resolver.insert(collectionUri, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            cachedFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val targetFile = File(targetDir, displayName)
                cachedFile.inputStream().use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save file to public downloads", e)
            false
        }
    }

    fun saveAndShowToast(
        context: Context,
        cachedFile: File,
        mimeType: String,
        displayName: String
    ) {
        val success = saveFileToPublicDownloads(context, cachedFile, mimeType, displayName)
        if (success) {
            Toast.makeText(
                context,
                context.getString(R.string.autobackup_notification_title_local) + "\n" + displayName,
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.toast_save_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
