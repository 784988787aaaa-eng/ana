package com.example.ui.screens.cloud.components

/*
 * =====================================================================================
 * حزمة صف عنصر النسخة السحابية (Cloud Backup Item Row Component Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المكون البصري المخصص لعرض بطاقة نسخة احتياطية سحابية واحدة:
 * - عرض تاريخ ووقت إنشاء النسخة وحجمها التخزيني بالكيلوبايت.
 * - دعم التفاعل بالقائمة المنبثقة (خيارات الاستعادة والحذف الفردي).
 * - دعم وضع التحديد المتعدد (Selection Mode) مع مؤشرات الاختيار التفاعلية والضغط المطول.
 * =====================================================================================
 */

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CloudBackupFile

import androidx.compose.ui.graphics.luminance

/*
 * =====================================================================================
 * بطاقة عرض عنصر النسخة الاحتياطية السحابية (CloudBackupItemRow)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة مضغوطة عالية الوضوح لعرض بيانات ملف النسخة السحابية:
 * 1. استخراج وتنسيق تاريخ ووقت الملف عبر دالة `formatBackupDateTime`.
 * 2. حساب حجم الملف بالكيلوبايت وعرضه بشكل مناسب.
 * 3. قائمة خيارات منبثقة تتضمن: استعادة هذه النسخة أو حذفها من Google Drive.
 * 4. دعم الضغط المطول والنقر لتفعيل وتعديل التحديد عند تشغيل وضع الحذف المتعدد.
 *
 * [المُدخلات]:
 * - backup: كائن بيانات النسخة السحابية (CloudBackupFile).
 * - menuExpanded: هل قائمة الخيارات المنبثقة لهذا العنصر مفتوحة.
 * - onMenuToggle: رد نداء لتبديل حالة فتح/إغلاق القائمة.
 * - onRestoreClick: رد نداء عند طلب استرجاع وتنزيل النسخة.
 * - onDeleteClick: رد نداء عند طلب حذف النسخة السحابية.
 * - isSelectionMode: هل وضع التحديد المتعدد نشط حالياً.
 * - isSelected: هل هذا العنصر محدد ضمن العناصر المختارة.
 * - onSelectedChange: رد نداء تبديل حالة تحديد هذا العنصر.
 * - onLongClick: رد نداء الضغط المطول لتفعيل وضع التحديد المتعدد.
 * =====================================================================================
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CloudBackupItemRow(
    backup: CloudBackupFile,
    menuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectedChange: (Boolean) -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val (dateStr, timeStr) = remember(backup.name, backup.createdTime) {
        formatBackupDateTime(context, backup.name, backup.createdTime)
    }

    val unknownSize = stringResource(R.string.cloud_size_unknown)
    val sizeKbPattern = stringResource(R.string.cloud_size_kb_pattern)

    // حساب وعرض حجم الملف بتنسيق دقيق بالكيلوبايت
    val displaySize = remember(backup.size, unknownSize, sizeKbPattern) {
        if (backup.size <= 0L) {
            unknownSize
        } else {
            String.format(java.util.Locale.US, sizeKbPattern, backup.size / 1024.0)
        }
    }

    // بطاقة الحاوية مع تمييز لوني عند التحديد
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondary.copy(alpha = if (isDark) 0.22f else 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.8.dp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onSelectedChange(!isSelected)
                    } else {
                        onMenuToggle(true)
                    }
                },
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // الجانب الأيسر: الحجم والقائمة المنبثقة أو مؤشر التحديد
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = displaySize,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isSelectionMode) {
                    Box {
                        IconButton(
                            onClick = { onMenuToggle(true) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("backup_menu_${backup.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cloud_desc_file_options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // قائمة الخيارات المنبثقة (استعادة / حذف)
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { onMenuToggle(false) },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = stringResource(R.string.cloud_menu_restore_this),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = onRestoreClick
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = stringResource(R.string.cloud_menu_delete_this),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                onClick = onDeleteClick
                            )
                        }
                    }
                } else {
                    // مؤشر التحديد الدائري أثناء وضع التحديد المتعدد
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 2.dp)
                    )
                }
            }

            // الجانب الأيمن: التاريخ والوقت
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

