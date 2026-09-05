package com.smartledger.aldaftar.data.backup

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

/**
 * يمثل نسخة قديمة مرشحة للاستعادة مع الاحتفاظ بمعرّف المستند فقط.
 * يمنع هذا النموذج الاحتفاظ بمسار تخزين مكشوف خارج واجهة المستندات.
 */
data class LegacyBackupCandidate(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long?,
    val modifiedAt: Long?
)

/**
 * يكتشف النسخ القديمة داخل شجرة المستندات التي منح المستخدم التطبيق صلاحيتها.
 * يدعم المجلدات الفرعية ويضع حدوداً عددية وحجمية تمنع استهلاك الذاكرة أو القراءة المفرطة.
 */
object LegacyBackupDiscovery {
    private const val MAX_DISCOVERY_FILES = 100
    private const val MAX_DISCOVERY_DEPTH = 16
    private const val MAX_RESTORE_BYTES = 64L * 1024L * 1024L

    /**
     * يعيد النسخ المدعومة داخل الشجرة المختارة بترتيب الأحدث ثم الاسم.
     * لا يستخدم صلاحيات التخزين الواسعة ولا يخرج من نطاق الشجرة الممنوحة.
     */
    fun discover(resolver: ContentResolver, treeUri: Uri): List<LegacyBackupCandidate> {
        val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri) ?: return emptyList()
        val candidates = mutableListOf<LegacyBackupCandidate>()
        visitChildren(resolver, treeUri, treeDocumentId, 0, candidates)
        return candidates.sortedWith(
            compareByDescending<LegacyBackupCandidate> { it.modifiedAt ?: Long.MIN_VALUE }
                .thenBy { it.displayName }
        )
    }

    /**
     * يمسح مستوى واحداً من الشجرة ثم يتابع المجلدات الفرعية حتى الحد الآمن.
     * يتوقف فور بلوغ العدد الأقصى للمرشحين حتى لا تتحول عملية الاكتشاف إلى حمل غير محدود.
     */
    private fun visitChildren(
        resolver: ContentResolver,
        treeUri: Uri,
        documentId: String,
        depth: Int,
        candidates: MutableList<LegacyBackupCandidate>
    ) {
        if (depth > MAX_DISCOVERY_DEPTH || candidates.size >= MAX_DISCOVERY_FILES) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (cursor.moveToNext() && candidates.size < MAX_DISCOVERY_FILES) {
                val childId = if (idIndex >= 0) cursor.getString(idIndex) else null
                if (childId.isNullOrBlank()) continue
                val name = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
                val mime = if (mimeIndex >= 0) cursor.getString(mimeIndex).orEmpty() else ""
                val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
                val modified = if (modifiedIndex >= 0 && !cursor.isNull(modifiedIndex)) cursor.getLong(modifiedIndex) else null
                val directory = mime == DocumentsContract.Document.MIME_TYPE_DIR

                if (directory) {
                    visitChildren(resolver, treeUri, childId, depth + 1, candidates)
                    continue
                }

                val supported = name.endsWith(".mzd", ignoreCase = true) || name.endsWith(".json", ignoreCase = true)
                val sizeAllowed = size == null || size in 1L..MAX_RESTORE_BYTES
                if (supported && sizeAllowed) {
                    candidates += LegacyBackupCandidate(
                        uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId),
                        displayName = name,
                        sizeBytes = size,
                        modifiedAt = modified
                    )
                }
            }
        }
    }
}
