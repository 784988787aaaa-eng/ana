package com.smartledger.aldaftar.data.backup

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.DocumentsContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * يتحقق من اكتشاف النسخ القديمة عبر واجهة المستندات مع رفض الأنواع والحجوم غير المسموحة.
 */
class LegacyBackupDiscoveryTest {

    @Test
    fun discoverFiltersAndSortsSupportedBackups() {
        val context = android.app.Application()
        val treeUri = Uri.parse("content://com.example.documents/tree/primary%3ADocuments%2F%D8%A7%D9%84%D8%AF%D9%81%D8%AA%D8%B1%20%D8%A7%D9%84%D8%B0%D9%83%D9%8A")
        val resolver = object : ContentResolver(context) {
            override fun query(
                uri: Uri,
                projection: Array<out String>?,
                selection: String?,
                selectionArgs: Array<out String>?,
                sortOrder: String?
            ) = MatrixCursor(projection ?: emptyArray()).apply {
                addRow(arrayOf("doc-json", "قديم.json", "application/json", 100L, 100L))
                addRow(arrayOf("doc-mzd", "أحدث.mzd", "application/octet-stream", 200L, 200L))
                addRow(arrayOf("doc-txt", "مرفوض.txt", "text/plain", 100L, 300L))
                addRow(arrayOf("doc-big", "كبير.mzd", "application/octet-stream", 70L * 1024L * 1024L, 400L))
                addRow(arrayOf("dir", "مجلد", DocumentsContract.Document.MIME_TYPE_DIR, 0L, 500L))
            }
        }

        val result = LegacyBackupDiscovery.discover(resolver, treeUri)

        assertEquals(2, result.size)
        assertEquals("أحدث.mzd", result[0].displayName)
        assertEquals("قديم.json", result[1].displayName)
        assertTrue(result[0].uri.toString().contains("doc-mzd"))
        assertTrue(result[1].uri.toString().contains("doc-json"))
    }
}
