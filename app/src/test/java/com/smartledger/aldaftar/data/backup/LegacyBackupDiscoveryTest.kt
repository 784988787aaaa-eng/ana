package com.smartledger.aldaftar.data.backup

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * يتحقق من اكتشاف النسخ القديمة عبر واجهة المستندات مع رفض الأنواع والحجوم غير المسموحة.
 */
@RunWith(RobolectricTestRunner::class)
class LegacyBackupDiscoveryTest {

    private class TestDocumentsProvider : ContentProvider() {
        override fun onCreate(): Boolean = true

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
        ): Cursor {
            val cursor = MatrixCursor(projection ?: arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ))
            cursor.addRow(arrayOf<Any?>("doc-json", "قديم.json", "application/json", 100L, 100L))
            cursor.addRow(arrayOf<Any?>("doc-mzd", "أحدث.mzd", "application/octet-stream", 200L, 200L))
            cursor.addRow(arrayOf<Any?>("doc-txt", "مرفوض.txt", "text/plain", 100L, 300L))
            cursor.addRow(arrayOf<Any?>("doc-big", "كبير.mzd", "application/octet-stream", 70L * 1024L * 1024L, 400L))
            return cursor
        }

        override fun getType(uri: Uri): String? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    }

    @Test
    fun discoverFiltersAndSortsSupportedBackups() {
        Robolectric.buildContentProvider(TestDocumentsProvider::class.java).create("com.example.documents").get()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = context.contentResolver
        val treeUri = Uri.parse("content://com.example.documents/tree/primary%3ADocuments%2F%D8%A7%D9%84%D8%AF%D9%81%D8%AA%D8%B1%20%D8%A7%D9%84%D8%B0%D9%83%D9%8A")

        val result = LegacyBackupDiscovery.discover(resolver, treeUri)

        assertEquals(2, result.size)
        assertEquals("أحدث.mzd", result[0].displayName)
        assertEquals("قديم.json", result[1].displayName)
        assertTrue(result[0].uri.toString().contains("doc-mzd"))
        assertTrue(result[1].uri.toString().contains("doc-json"))
    }
}

