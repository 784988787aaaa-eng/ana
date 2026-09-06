package com.smartledger.aldaftar.ui.screens.settings.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.smartledger.aldaftar.data.CloudSyncState
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.theme.SoftRed
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import kotlinx.coroutines.launch

private const val TAG = "QuadBackupCard"

@Composable
fun QuadBackupCard(
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    onRestoreSuccess: (AppSettings) -> Unit,
    onDiscoverLegacy: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = MaterialTheme.isDark

    val googleCloudSyncState by backupSyncViewModel.googleDriveSyncState.collectAsStateWithLifecycle()
    
    var showBackupPermissionExplanationDialog by remember { mutableStateOf(false) }
    var onPermissionGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // منتقي إنشاء ملف النسخة عبر نظام اختيار المستندات
    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            backupSyncViewModel.getBackupJsonForClipboard { jsonStr ->
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonStr.toByteArray())
                            launch(kotlinx.coroutines.Dispatchers.Main) {
                                Toast.makeText(context, context.getString(R.string.settings_toast_synced_desc), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        launch(kotlinx.coroutines.Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // منتقي ملف الاستعادة يمنح التطبيق معرّف المحتوى فقط؛ القراءة والتحقق يتمان خارج الواجهة.
    val safRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            backupSyncViewModel.readLocalBackupFromUri(context, uri) { jsonText ->
                if (jsonText.isNullOrBlank()) {
                    Toast.makeText(context, context.getString(R.string.toast_restore_invalid_file), Toast.LENGTH_LONG).show()
                } else {
                    pendingRestoreJson = jsonText
                    showRestoreWarningDialog = true
                }
            }
        }
    }

    // النسخ التلقائي محصور في المساحة الخاصة بالتطبيق؛ لا توجد صلاحيات تخزين مطلوبة.
    val checkBackupPermissionsGranted = remember { { true } }

    // تهيئة حزمة تسجيل الدخول الرسمية من جوجل
    val googleSignInClient = remember {
        backupSyncViewModel.googleDriveSyncHelper.getGoogleSignInClient()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        com.smartledger.aldaftar.domain.GoogleAuthSessionManager.handleSignInActivityResult(
            resultCode = result.resultCode,
            data = result.data,
            context = context,
            backupSyncViewModel = backupSyncViewModel
        ) { outcome ->
            when (outcome) {
                is com.smartledger.aldaftar.domain.GoogleSignInOutcome.Success -> {
                    if (outcome.isDriveAuthorized) {
                        Toast.makeText(context, context.getString(R.string.settings_gdrive_link_success_pattern, outcome.email), Toast.LENGTH_LONG).show()
                    } else if (outcome.serverAuthCode == null) {
                        Toast.makeText(context, context.getString(R.string.settings_gdrive_link_failed_invalid_code), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.settings_gdrive_link_failed_network), Toast.LENGTH_LONG).show()
                    }
                }
                is com.smartledger.aldaftar.domain.GoogleSignInOutcome.Cancelled -> {
                    Toast.makeText(context, context.getString(R.string.settings_gdrive_link_cancelled), Toast.LENGTH_SHORT).show()
                }
                is com.smartledger.aldaftar.domain.GoogleSignInOutcome.Failed -> {
                    Toast.makeText(context, outcome.message, Toast.LENGTH_LONG).show()
                    backupSyncViewModel.updateCloudSyncState(
                        com.smartledger.aldaftar.data.CloudSyncState.Error(outcome.message)
                    )
                }
            }
        }
    }

    LaunchedEffect(googleCloudSyncState) {
        if (googleCloudSyncState is CloudSyncState.SessionExpired) {
            Toast.makeText(context, context.getString(R.string.toast_reconnect_cloud), Toast.LENGTH_LONG).show()
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess()
            }
        } else if (googleCloudSyncState is CloudSyncState.Success) {
            Toast.makeText(context, context.getString(R.string.settings_gdrive_sync_success), Toast.LENGTH_SHORT).show()
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
                // ١. المزامنة السحابية في أعلى القسم
                CloudBackupSection(
                    backupSyncViewModel = backupSyncViewModel,
                    isDark = isDark,
                    context = context,
                    googleCloudSyncState = googleCloudSyncState,
                    googleSignInClient = googleSignInClient,
                    googleSignInLauncher = googleSignInLauncher,
                    safExportLauncher = safExportLauncher
                )

                // 2. تصدير واستيراد النسخ الاحتياطية المحلية
                FileTransferManager(
                    backupSyncViewModel = backupSyncViewModel,
                    context = context,
                    safRestoreLauncher = safRestoreLauncher,
                    onDiscoverLegacy = onDiscoverLegacy,
                    checkBackupPermissionsGranted = checkBackupPermissionsGranted,
                    onShowPermissionExplanation = { callback ->
                        onPermissionGrantedCallback = callback
                        showBackupPermissionExplanationDialog = true
                    }
                )

                // ٣. زر مسح كافة البيانات وإعادة الضبط
                Button(
                    onClick = { showResetConfirmationFlow = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftRed.copy(alpha = 0.08f),
                        contentColor = SoftRed
                    ),
                    border = BorderStroke(1.dp, SoftRed.copy(alpha = 0.35f)),
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
                            tint = SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.backup_btn_delete_all),
                            color = SoftRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showRestoreWarningDialog && pendingRestoreJson != null) {
        RestoreWarningDialog(
            onDismiss = {
                showRestoreWarningDialog = false
                pendingRestoreJson = null
            },
            onConfirm = {
                val json = pendingRestoreJson
                showRestoreWarningDialog = false
                pendingRestoreJson = null
                if (!json.isNullOrBlank()) {
                    backupSyncViewModel.executeMasterRestore(json, context) { success, restoredSettings ->
                        if (success && restoredSettings != null) {
                            onRestoreSuccess(restoredSettings)
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
                // لا نطلب صلاحيات التخزين؛ يختار المستخدم موقع التصدير من منتقي المستندات عند الحاجة.
                onPermissionGrantedCallback?.invoke()
            },
            onUseInternalStorage = {
                onPermissionGrantedCallback?.invoke()
            }
        )
    }
}
