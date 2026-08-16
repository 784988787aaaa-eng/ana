package com.example.ui.screens.settings.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.viewmodel.BackupSyncViewModel

@Composable
fun BackupBase64Handler(
    backupSyncViewModel: BackupSyncViewModel,
    context: Context,
    clipboardManager: ClipboardManager,
    onRestoreSuccess: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPasteDialog by remember { mutableStateOf(false) }
    var pastedBackupText by remember { mutableStateOf("") }

    // 2. الخيار الثاني: الاستنساخ النصي المشفر
    QuadBackupItem(
        title = stringResource(R.string.settings_backup_base64_title),
        description = stringResource(R.string.settings_backup_base64_desc),
        accentColor = Color(0xFF6366F1),
        icon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp)) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    backupSyncViewModel.getBackupJsonForClipboard { jsonStr ->
                        clipboardManager.setText(AnnotatedString(jsonStr))
                        Toast.makeText(context, context.getString(R.string.settings_toast_base64_copied), Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text(stringResource(R.string.settings_copy_encrypted), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    pastedBackupText = ""
                    showPasteDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text(stringResource(R.string.settings_restore_paste), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Paste Text Dialog
    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = {
                Text(stringResource(R.string.settings_dialog_restore_paste_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column {
                    Text(stringResource(R.string.settings_dialog_restore_paste_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pastedBackupText,
                        onValueChange = { pastedBackupText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text(stringResource(R.string.settings_placeholder_paste_encoded)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pastedBackupText.isNotBlank()) {
                        backupSyncViewModel.executeMasterRestore(pastedBackupText, context) { success, restoredSettings ->
                            if (success && restoredSettings != null) {
                                onRestoreSuccess(restoredSettings)
                                pastedBackupText = ""
                                showPasteDialog = false
                            } else {
                                Toast.makeText(context, context.getString(R.string.settings_toast_paste_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.settings_btn_restore_now), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text(stringResource(R.string.settings_btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
