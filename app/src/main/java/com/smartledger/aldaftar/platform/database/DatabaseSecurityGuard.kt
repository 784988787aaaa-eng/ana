package com.smartledger.aldaftar.platform.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.smartledger.aldaftar.platform.security.AppSecurityManager
import java.io.File

object DatabaseSecurityGuard {

    private val SQLITE_HEADER_BYTES = "SQLite format 3".toByteArray(Charsets.US_ASCII)

    fun secureEqual(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        if (a.length != b.length) return false

        var result = 0
        for (i in 0 until a.length) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    fun verifyDatabaseIntegrity(context: Context, databaseName: String): Boolean {
        val dbFile = context.getDatabasePath(databaseName)
        if (!dbFile.exists()) return true

        if (dbFile.length() < 16) return false
        return try {
            val isHeaderValid = dbFile.inputStream().use { input ->
                val header = ByteArray(16)
                val read = input.read(header)
                if (read == 16) {
                    var matches = true
                    for (i in SQLITE_HEADER_BYTES.indices) {
                        if (header[i] != SQLITE_HEADER_BYTES[i]) {
                            matches = false
                            break
                        }
                    }
                    matches
                } else false
            }

            if (!isHeaderValid) return false

            var quickCheckPassed = true
            try {
                val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
                db.use { database ->
                    database.rawQuery("PRAGMA quick_check", null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val result = cursor.getString(0)
                            quickCheckPassed = result.equals("ok", ignoreCase = true)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("DatabaseSecurityGuard", "Quick check skipped or database locked: ${e.javaClass.simpleName}")
            }
            quickCheckPassed
        } catch (e: Exception) {
            android.util.Log.w("DatabaseSecurityGuard", "Database integrity verification failed safely: ${e.javaClass.simpleName}")
            false
        }
    }

    fun preventUnauthorizedExport(context: Context, enteredPin: String?): Boolean {
        val secManager = AppSecurityManager.getInstance(context)
        if (!secManager.isFastPasscodeEnabled() || !secManager.hasAdminPin()) {
            return true
        }
        if (enteredPin.isNullOrBlank()) {
            return false
        }
        return secManager.validateAdminPin(enteredPin)
    }

    fun performLocalSandboxHealthCheck(context: Context): List<String> {
        val dbDir = File(context.applicationInfo.dataDir, "databases")
        if (dbDir.exists() && (!dbDir.canRead() || !dbDir.canWrite())) {
            return listOf("SANDBOX_IO_FAILURE")
        }
        return emptyList()
    }
}


