package com.example.ui.screens.cloud.components

/*
 * =====================================================================================
 * حزمة قسم قائمة النسخ السحابية (Cloud Backups List Section Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المكون البصري المخصص لعرض وإدارة قائمة النسخ السحابية:
 * - مؤشر جلب البيانات والتحميل الأولي.
 * - واجهة الحالة الفارغة التوجيهية عند عدم وجود أي نسخ احتياطية.
 * - القائمة التمريرية الكسولة (LazyColumn) لعرض البطاقات وتفويض أحداث الاستعادة والحذف والتحديد.
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackupTable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CloudBackupFile
import com.example.ui.theme.EmeraldPrimary

/*
 * =====================================================================================
 * قسم قائمة ملفات النسخ السحابية (CloudBackupsListSection)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يدير هذا المكون عرض الحالات المختلفة لقائمة النسخ السحابية:
 * 1. حالة التحميل (Loading): مؤشر دائري ونص وصفي أثناء جلب قائمة الملفات من Google Drive.
 * 2. الحالة الفارغة (Empty State): أيقونة ورسالة إرشادية للمستخدم في حال عدم وجود نسخ سحابية.
 * 3. قائمة البيانات (LazyColumn): عرض قائمة منظمة ومفهرسة للملفات مع تفويض التفاعلات.
 *
 * [المُدخلات]:
 * - isFetching: هل عملية جلب البيانات جارية حالياً من السحابة.
 * - cloudBackups: قائمة ملفات النسخ السحابية المسترجعة.
 * - selectedFileIdsSet: مجموعة معرفات الملفات المحددة أثناء وضع التحديد.
 * - isSelectionMode: هل وضع التحديد المتعدد نشط.
 * - menuExpandedFileId: معرّف الملف الذي تم فتح قائمته المنبثقة (أو null).
 * - onMenuToggle: رد نداء لتغيير القائمة المفتوحة.
 * - onRestoreClick: رد نداء عند الضغط على استعادة ملف محدد.
 * - onDeleteClick: رد نداء عند الضغط على حذف ملف محدد.
 * - onItemSelectToggle: رد نداء لتبديل حالة تحديد ملف محدد.
 * - onItemLongClick: رد نداء عند الضغط المطول على ملف لبدء التحديد المتعدد.
 * =====================================================================================
 */
@Composable
fun CloudBackupsListSection(
    isFetching: Boolean,
    cloudBackups: List<CloudBackupFile>,
    selectedFileIdsSet: Set<String>,
    isSelectionMode: Boolean,
    menuExpandedFileId: String?,
    onMenuToggle: (String?) -> Unit,
    onRestoreClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onItemSelectToggle: (String, Boolean) -> Unit,
    onItemLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isFetching && cloudBackups.isEmpty()) {
        // حالة جلب البيانات لأول مرة
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = stringResource(R.string.cloud_fetching_list),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else if (cloudBackups.isEmpty()) {
        // حالة عدم وجود نسخ احتياطية سحابية
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BackupTable,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = stringResource(R.string.cloud_empty_backups),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.cloud_empty_backups_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // قائمة النسخ الاحتياطية التمريرية
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .testTag("cloud_backups_lazy_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cloudBackups, key = { it.id }) { backupFile ->
                CloudBackupItemRow(
                    backup = backupFile,
                    menuExpanded = menuExpandedFileId == backupFile.id,
                    onMenuToggle = { expanded ->
                        onMenuToggle(if (expanded) backupFile.id else null)
                    },
                    onRestoreClick = {
                        onMenuToggle(null)
                        onRestoreClick(backupFile.id)
                    },
                    onDeleteClick = {
                        onMenuToggle(null)
                        onDeleteClick(backupFile.id)
                    },
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedFileIdsSet.contains(backupFile.id),
                    onSelectedChange = { selected ->
                        onItemSelectToggle(backupFile.id, selected)
                    },
                    onLongClick = {
                        onItemLongClick(backupFile.id)
                    }
                )
            }
        }
    }
}

