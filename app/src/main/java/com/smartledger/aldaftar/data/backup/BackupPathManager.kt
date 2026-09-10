package com.smartledger.aldaftar.data.backup

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** مسارات النسخ الاحتياطية الداخلية غير الظاهرة للمستخدم. */
class BackupPathManager(context: Context) {
    companion object {
        const val EXTENSION = ".slb"
        const val PREFIX = "SMN_"
        const val ROOT_FOLDER = "backups"
    }

    private val root = File(context.applicationContext.filesDir, ROOT_FOLDER)

    fun monthFolder(date: Date = Date()): File {
        val month = SimpleDateFormat("yyyy-MM", Locale.US).format(date)
        return File(root, month)
    }

    fun automaticFile(date: Date = Date()): File {
        val dateName = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        return File(monthFolder(date), "$PREFIX$dateName$EXTENSION")
    }

    fun manualFile(date: Date = Date()): File {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(date)
        return File(monthFolder(date), "$PREFIX$stamp$EXTENSION")
    }

    fun isSupported(file: File): Boolean =
        file.isFile && isSafeBackupName(file.name)

    fun isSafeBackupName(name: String): Boolean =
        name.matches(Regex("SMN_\\d{4}-\\d{2}(?:-\\d{2}(?:_\\d{4})?)?\\.slb", RegexOption.IGNORE_CASE))
}
