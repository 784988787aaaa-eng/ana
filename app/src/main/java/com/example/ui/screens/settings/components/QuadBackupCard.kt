package com.example.ui.screens.settings.components

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
import com.example.ui.theme.isDark
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
import com.example.R
import com.example.data.CloudSyncState
import com.example.data.local.entities.AppSettings
import com.example.ui.theme.SoftRed
import com.example.ui.viewmodel.BackupSyncViewModel
import kotlinx.coroutines.launch

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

    val googleCloudSyncState by backupSyncViewModel.googleDriveSyncState.collectAsStateWithLifecycle()
    
    var showBackupPermissionExplanationDialog by remember { mutableStateOf(false) }
    var onPermissionGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // Backup SAF Create Document launcher
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

    // Backup SAF Open Document launcher
    val safRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (jsonText.isNotBlank()) {
                    pendingRestoreJson = jsonText
                    showRestoreWarningDialog = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore backup from SAF OpenDocument: ${e.message}")
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

    // Official Google Sign-In SDK configuration
    val googleSignInClient = remember {
        backupSyncViewModel.googleDriveSyncHelper.getGoogleSignInClient()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        if (result.resultCode == Activity.RESULT_OK && intent != null) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val authCode = account?.serverAuthCode
                val email = account?.email ?: "account@google.com"
                if (!authCode.isNullOrEmpty()) {
                    Log.d("QuadBackupCard", "Google Sign-In successful: serverAuthCode received (length=${authCode.length})")
                    backupSyncViewModel.handleGoogleOAuthCode(authCode, email) { success ->
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.settings_gdrive_link_success_pattern, email), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.settings_gdrive_link_failed_network), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.w("QuadBackupCard", "Google Sign-In succeeded for account but serverAuthCode is null. Ensure valid Web Client ID is configured.")
                    backupSyncViewModel.googleDriveSyncHelper.storeEmail(email)
                    val msg = context.getString(R.string.settings_gdrive_link_success_pattern, email)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Log.e("QuadBackupCard", "Google Sign-In ApiException: statusCode=${e.statusCode}")
                val errText = when (e.statusCode) {
                    12501 -> "فشل مصادقة Google (رمز 12501): لم تكتمل المصادقة. يرجى التحقق من تسجيل بصمة SHA-1 واسم الحزمة في Google Cloud Console."
                    10 -> "خطأ في تكوين المطور (رمز 10): تحقق من تسجيل SHA-1 للشهادة في Google Cloud."
                    7 -> context.getString(R.string.settings_gdrive_link_failed_network)
                    else -> context.getString(R.string.settings_gdrive_link_failed_api_code_pattern, e.statusCode)
                }
                Toast.makeText(context, errText, Toast.LENGTH_LONG).show()
                backupSyncViewModel.updateCloudSyncState(com.example.data.CloudSyncState.Error(errText))
            } catch (e: Exception) {
                Log.e("QuadBackupCard", "Google Sign-In unexpected error: ${e.javaClass.simpleName}")
                Toast.makeText(context, context.getString(R.string.settings_gdrive_link_failed_error_pattern, e.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
                backupSyncViewModel.updateCloudSyncState(com.example.data.CloudSyncState.Error(e.localizedMessage ?: "Sign-in error"))
            }
        } else {
            var statusCode: Int? = null
            if (intent != null) {
                try {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
                    task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                } catch (e: com.google.android.gms.common.api.ApiException) {
                    statusCode = e.statusCode
                } catch (e: Exception) {
                    Log.w("QuadBackupCard", "Error parsing non-OK sign-in result", e)
                }
            }

            Log.e("QuadBackupCard", "Google Sign-In non-OK result: resultCode=${result.resultCode}, statusCode=$statusCode")

            if (statusCode == null && result.resultCode == Activity.RESULT_CANCELED && intent == null) {
                Toast.makeText(context, context.getString(R.string.settings_gdrive_link_cancelled), Toast.LENGTH_SHORT).show()
            } else {
                val diagnosticError = when (statusCode) {
                    12501 -> "فشل إتمام المصادقة (رمز 12501). السبب الأرجح: عدم تطابق بصمة SHA-1 للشهادة مع عميل Android OAuth في Google Cloud Console."
                    10 -> "خطأ تكوين المطور (رمز 10 Developer Error): يرجى تسجيل SHA-1 للشهادة واسم الحزمة com.aistudio.mizanaldar.ptwqxs في Google Cloud."
                    7 -> context.getString(R.string.settings_gdrive_link_failed_network)
                    12500 -> "خطأ في خدمات Google Play (رمز 12500)."
                    12502 -> "عملية تسجيل الدخول قيد التنفيذ (رمز 12502)."
                    16 -> "تم إيقاف عملية المصادقة من النظام (رمز 16)."
                    null -> if (result.resultCode == Activity.RESULT_CANCELED) "تم إلغاء اختيار الحساب." else "فشل تسجيل الدخول (رمز النتيجة: ${result.resultCode})."
                    else -> "فشل تسجيل الدخول عبر Google (رمز الحالة: $statusCode)."
                }
                Toast.makeText(context, diagnosticError, Toast.LENGTH_LONG).show()
                backupSyncViewModel.updateCloudSyncState(com.example.data.CloudSyncState.Error(diagnosticError))
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
                // 1. المزامنة السحابية في أعلى الجميع (Cloud Sync & Backup Section)
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
                    checkBackupPermissionsGranted = checkBackupPermissionsGranted,
                    onShowPermissionExplanation = { callback ->
                        onPermissionGrantedCallback = callback
                        showBackupPermissionExplanationDialog = true
                    }
                )

                // 3. زر مسح كافة البيانات وإعادة الضبط (Danger Zone)
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
