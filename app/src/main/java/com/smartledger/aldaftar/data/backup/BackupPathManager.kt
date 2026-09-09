package com.smartledger.aldaftar.data.backup

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupPathManager {
    companion object {
        const val EXTENSION = ".slb"
        const val PREFIX = "SMN_"
        const val ROOT_FOLDER = "الدفتر الذكي"
    }

    fun monthFolder(date: Date = Date()): File {
        val month = SimpleDateFormat("yyyy-MM", Locale.US).format(date)
        return File(documentsRoot(), "$ROOT_FOLDER/$month")
    }

    fun automaticFile(date: Date = Date()): File {
        val dateName = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        return File(monthFolder(date), "$PREFIX$dateName$EXTENSION")
    }

    fun manualFile(date: Date = Date()): File {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(date)
        return File(monthFolder(date), "$PREFIX$stamp$EXTENSION")
    }

    fun documentsRoot(): File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    fun ensureMonthFolder(date: Date = Date()): File = monthFolder(date).apply { mkdirs() }

    fun isSupported(file: File): Boolean =
        file.isFile && isSafeBackupName(file.name)

    fun isSafeBackupName(name: String): Boolean =
        name.matches(Regex("SMN_\\d{4}-\\d{2}(?:-\\d{2}(?:_\\d{4})?)?\\.slb", RegexOption.IGNORE_CASE))
}
