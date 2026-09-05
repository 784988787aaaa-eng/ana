/**
 * =====================================================================
 * ملف: إدارة نقل وتصدير ملفات النسخ المحلي (FileTransferManager.kt)
 * =====================================================================
 * 
 * [الغرض من الملف]:
 * توفير واجهة مستخدم مبسطة ومباشرة لعمليات تصدير واستيراد النسخ الاحتياطية
 * المحلية بصيغة (.mzd) دون عرض قوائم شجرية مزدحمة.
 * 
 * [المسار المعتمد]:
 * الحفظ المركزي المباشر في: /storage/emulated/0/Documents/الدفتر الذكي/[yyyy-MM]/
 */
package com.smartledger.aldaftar.ui.screens.settings.components

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.shareBackupFile
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel

@Composable
fun FileTransferManager(
    backupSyncViewModel: BackupSyncViewModel,
    context: Context,
    safRestoreLauncher: ActivityResultLauncher<Array<String>>,
    checkBackupPermissionsGranted: () -> Boolean,
    onShowPermissionExplanation: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // تصدير واستيراد ملفات (.mzd)
        QuadBackupItem(
            title = stringResource(R.string.settings_backup_portable_title),
            description = stringResource(R.string.settings_backup_portable_desc),
            accentColor = MaterialTheme.colorScheme.primary,
            icon = {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // زر تصدير النسخة الاحتياطية
                Button(
                    onClick = {
                        val runBackup = {
                            backupSyncViewModel.exportLocalBackup(context) { result ->
                                val file = result.getOrNull()
                                if (file != null) {
                                    shareBackupFile(context, file)
                                }
                            }
                        }
                        if (checkBackupPermissionsGranted()) {
                            runBackup()
                        } else {
                            onShowPermissionExplanation(runBackup)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_export_mzd),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // زر استيراد النسخة الاحتياطية عبر SAF
                Button(
                    onClick = {
                        safRestoreLauncher.launch(arrayOf("application/*", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_import_mzd),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
