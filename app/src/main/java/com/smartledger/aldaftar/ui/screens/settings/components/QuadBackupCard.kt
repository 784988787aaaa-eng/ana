package com.smartledger.aldaftar.ui.screens.settings.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import com.smartledger.aldaftar.ui.theme.isDark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.theme.DebtRed
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "QuadBackupCard"

@Composable
fun QuadBackupCard(
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    onRestoreSuccess: (AppSettings) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = MaterialTheme.isDark

    var showBackupPermissionExplanationDialog by remember { mutableStateOf(false) }
    var onPermissionGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }
    var pendingRestoreBytes by remember { mutableStateOf<ByteArray?>(null) }

    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            backupSyncViewModel.exportBackupBytes { bytes ->
                if (bytes != null) {
                    coroutineScope.launch {
                        val exported = try {
                            withContext(Dispatchers.IO) {
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    outputStream.write(bytes)
                                    true
                                } ?: false
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            false
                        }
                        val message = if (exported) R.string.toast_backup_export_success else R.string.toast_backup_export_failed
                        Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val safRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bytes = try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read backup from SAF OpenDocument", e)
                    null
                }
                if (bytes != null && bytes.isNotEmpty()) {
                    pendingRestoreBytes = bytes
                    showRestoreWarningDialog = true
                }
            }
        }
    }

    val checkBackupPermissionsGranted = remember(context) {
        {
            val hasWrite = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
            
            val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasNotification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
            
            val hasManage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                android.os.Environment.isExternalStorageManager()
            } else true
            
            hasWrite && hasRead && hasNotification && hasManage
        }
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val writeGranted = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            results[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        } else true
        
        val readGranted = results[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Toast.makeText(context, context.getString(R.string.settings_toast_permission_manage_files), Toast.LENGTH_LONG).show()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    context.startActivity(intent)
                }
            } else {
                onPermissionGrantedCallback?.invoke()
            }
        } else {
            if (writeGranted && readGranted) {
                onPermissionGrantedCallback?.invoke()
            } else {
                Toast.makeText(context, context.getString(R.string.settings_toast_permission_denied_err), Toast.LENGTH_LONG).show()
            }
        }
    }

    var showResetConfirmationFlow by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FileTransferManager(
                    backupSyncViewModel = backupSyncViewModel,
                    context = context,
                    safRestoreLauncher = safRestoreLauncher,
                    checkBackupPermissionsGranted = checkBackupPermissionsGranted,
                    onShowPermissionExplanation = { callback ->
                        onPermissionGrantedCallback = callback
                        showBackupPermissionExplanationDialog = true
                    }
                )

                Button(
                    onClick = { showResetConfirmationFlow = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DebtRed.copy(alpha = 0.08f),
                        contentColor = DebtRed
                    ),
                    border = BorderStroke(1.dp, DebtRed.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = DebtRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.backup_btn_delete_all),
                            color = DebtRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showRestoreWarningDialog && pendingRestoreBytes != null) {
        RestoreWarningDialog(
            onDismiss = {
                showRestoreWarningDialog = false
                pendingRestoreBytes = null
            },
            onConfirm = {
                val bytes = pendingRestoreBytes
                showRestoreWarningDialog = false
                pendingRestoreBytes = null
                if (bytes != null) {
                    backupSyncViewModel.restoreFromBytes(bytes) { success, restoredSettings, err ->
                        if (success && restoredSettings != null) {
                            onRestoreSuccess(restoredSettings)
                            Toast.makeText(context, context.getString(R.string.toast_restore_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, err ?: context.getString(R.string.toast_sync_decrypt_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }

    if (showResetConfirmationFlow) {
        BackupResetConfirmationFlow(
            viewModel = backupSyncViewModel,
            onDismiss = { showResetConfirmationFlow = false },
            onSuccessReset = {
                showResetConfirmationFlow = false
                onRestoreSuccess(AppSettings())
            }
        )
    }

    if (showBackupPermissionExplanationDialog) {
        BackupPermissionExplanationDialog(
            onDismiss = { showBackupPermissionExplanationDialog = false },
            onGrantPermissions = {
                val permissions = mutableListOf<String>()
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                    permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                multiplePermissionsLauncher.launch(permissions.toTypedArray())
            },
            onUseInternalStorage = {
                onPermissionGrantedCallback?.invoke()
            }
        )
    }
}
