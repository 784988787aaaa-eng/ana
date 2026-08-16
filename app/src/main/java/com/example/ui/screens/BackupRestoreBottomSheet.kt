package com.example.ui.screens

import android.content.Intent
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CloudSyncState
import com.example.data.local.entities.AppSettings
import com.example.ui.screens.settings.components.BackupResetConfirmationFlow
import com.example.ui.screens.settings.components.Base64PasteDialog
import com.example.ui.screens.settings.components.GoogleDriveSyncCard
import com.example.ui.screens.settings.components.RestoreWarningDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftRed
import com.example.ui.viewmodel.BackupSyncViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "BackupRestoreBottomSheet"
private const val CACHE_DIR_BACKUPS = "backups"
private const val MIME_TYPE_OCTET_STREAM = "application/octet-stream"
private const val FILE_PREFIX_MIZAN = "Mizan_"
private const val FILE_EXT_MZD = ".mzd"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    settings: AppSettings,
    backupSyncViewModel: BackupSyncViewModel,
    onExportMzd: () -> Unit,
    onImportMzd: () -> Unit,
    onImportBase64: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = remember(bgColor) { bgColor.luminance() < 0.5f }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val googleSyncState by backupSyncViewModel.googleDriveSyncState.collectAsStateWithLifecycle()
    val storedEmail by backupSyncViewModel.storedEmailState.collectAsStateWithLifecycle()
    val isConnected = remember(storedEmail, googleSyncState) {
        !storedEmail.isNullOrEmpty() || googleSyncState is CloudSyncState.Authenticated || googleSyncState is CloudSyncState.Success || googleSyncState is CloudSyncState.Skipped
    }

    var showExportOptions by remember { mutableStateOf(false) }
    var showImportOptions by remember { mutableStateOf(false) }
    var showCloudBackupsSheet by remember { mutableStateOf(false) }
    var isSyncLoggingOut by remember { mutableStateOf(false) }
    
    // Paste Base64 Dialog
    var showPasteDialog by remember { mutableStateOf(false) }
    
    // Reset confirmation Dialogs (Double confirmation modal)
    var showResetConfirm1 by remember { mutableStateOf(false) }

    // Restore confirmation dialog states
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var onConfirmRestoreAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Official Google Sign-In SDK configuration
    val googleSignInClient = remember {
        try {
            backupSyncViewModel.googleDriveSyncHelper.getGoogleSignInClient()
        } catch (e: Exception) {
            Log.e(TAG, "GoogleSignInClient creation error", e)
            null
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && intent != null) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val authCode = account?.serverAuthCode
                val email = account?.email ?: "account@google.com"
                if (authCode != null) {
                    backupSyncViewModel.handleGoogleOAuthCode(authCode, email) { success ->
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.backup_toast_linked_success, email), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.backup_toast_connect_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.backup_toast_invalid_code), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google sign in failed", e)
                Toast.makeText(context, context.getString(R.string.backup_toast_connect_error, e.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
        } else {
            var isCancelled = (result.resultCode == android.app.Activity.RESULT_CANCELED)
            var errorCode: Int? = null
            if (intent != null) {
                try {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
                    task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                } catch (e: com.google.android.gms.common.api.ApiException) {
                    val sc = e.statusCode
                    if (sc == com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED || sc == 12501 || sc == 16) {
                        isCancelled = true
                        Log.i(TAG, "Google sign in was cancelled by the user")
                    } else {
                        errorCode = sc
                        Log.w(TAG, "Google sign in returned status code $sc: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Google sign in exception on non-OK result", e)
                }
            }
            if (isCancelled) {
                Toast.makeText(context, context.getString(R.string.backup_toast_cancelled), Toast.LENGTH_SHORT).show()
            } else if (errorCode != null) {
                Toast.makeText(context, context.getString(R.string.backup_toast_config_error, errorCode), Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(googleSyncState) {
        if (googleSyncState is CloudSyncState.SessionExpired && googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val intent = googleSignInClient.signInIntent
                    if (intent != null) {
                        googleSignInLauncher.launch(intent)
                    }
                }
            }
        } else if (googleSyncState is CloudSyncState.Success) {
            Toast.makeText(context, context.getString(R.string.backup_toast_sync_success), Toast.LENGTH_SHORT).show()
        } else if (googleSyncState is CloudSyncState.Skipped) {
            Toast.makeText(context, context.getString(R.string.backup_toast_sync_success), Toast.LENGTH_SHORT).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                BackupSheetHeader(
                    isConnected = isConnected,
                    isDark = isDark
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // Cloud Sync Section (Google Drive Integration)
                GoogleDriveSyncCard(
                    googleSyncState = googleSyncState,
                    storedEmail = storedEmail,
                    isConnected = isConnected,
                    isDark = isDark,
                    isSyncLoggingOut = isSyncLoggingOut,
                    authUrlProvider = { backupSyncViewModel.googleDriveSyncHelper.getAuthUrl() },
                    onSignInClick = {
                        val intent = googleSignInClient?.signInIntent
                        if (intent != null) {
                            googleSignInLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.backup_toast_connect_failed), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onUploadBackupClick = {
                        backupSyncViewModel.uploadBackupToGoogleDrive { success -> }
                    },
                    onRestoreBackupClick = {
                        onConfirmRestoreAction = {
                            backupSyncViewModel.restoreFromGoogleDriveDirect(context) { success ->
                                if (success) {
                                    Toast.makeText(context, context.getString(R.string.toast_cloud_restore_success), Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_cloud_restore_failed_or_missing), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        showRestoreConfirmDialog = true
                    },
                    onBrowseArchivesClick = {
                        showCloudBackupsSheet = true
                    },
                    onLogoutClick = {
                        isSyncLoggingOut = true
                        backupSyncViewModel.googleDriveLogout {
                            isSyncLoggingOut = false
                            Toast.makeText(context, context.getString(R.string.backup_toast_gdrive_logout_success), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onManualAuthCodeSubmit = { finalCode ->
                        backupSyncViewModel.handleGoogleOAuthCode(finalCode, null, "http://localhost/oauth2callback") { success ->
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.backup_toast_oauth_success), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.backup_toast_oauth_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )

                // ACTION ONE: CREATE BACKUP BUTTON
                BackupExportSection(
                    showExportOptions = showExportOptions,
                    onToggleExportOptions = {
                        showExportOptions = !showExportOptions
                        showImportOptions = false
                    },
                    onExportMzd = {
                        onExportMzd()
                        showExportOptions = false
                    },
                    onCopyBase64 = {
                        backupSyncViewModel.getBackupJsonForClipboard { json ->
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val base64 = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
                                    withContext(Dispatchers.Main) {
                                        clipboardManager.setText(AnnotatedString(base64))
                                        Toast.makeText(context, context.getString(R.string.backup_toast_copied_success), Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to encode copy to clipboard", e)
                                }
                            }
                        }
                        showExportOptions = false
                    },
                    onShareBackupFile = {
                        showExportOptions = false
                        backupSyncViewModel.getBackupJsonForClipboard { json ->
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val cacheDir = File(context.cacheDir, CACHE_DIR_BACKUPS)
                                    if (!cacheDir.exists()) cacheDir.mkdirs()
                                    val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)
                                    val dateStr = sdf.format(Date())
                                    val file = File(cacheDir, "$FILE_PREFIX_MIZAN$dateStr$FILE_EXT_MZD")
                                    file.writeText(json)

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}$FILE_PROVIDER_SUFFIX",
                                        file
                                    )

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = MIME_TYPE_OCTET_STREAM
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    withContext(Dispatchers.Main) {
                                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_backup_chooser)))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, context.getString(R.string.export_backup_failed, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )

                // ACTION TWO: RESTORE DATABASE BUTTON
                BackupImportSection(
                    showImportOptions = showImportOptions,
                    onToggleImportOptions = {
                        showImportOptions = !showImportOptions
                        showExportOptions = false
                    },
                    onLocalImportClick = {
                        onConfirmRestoreAction = {
                            onImportMzd()
                        }
                        showRestoreConfirmDialog = true
                        showImportOptions = false
                    },
                    onPasteBase64Click = {
                        showPasteDialog = true
                        showImportOptions = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION THREE: RESET DATABASE BUTTON
                OutlinedButton(
                    onClick = { showResetConfirm1 = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SoftRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = SoftRed, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.backup_btn_delete_all), color = SoftRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // BASE64 PASTE DIALOG
    if (showPasteDialog) {
        Base64PasteDialog(
            isDark = isDark,
            onDismiss = { showPasteDialog = false },
            onConfirmDecodedJson = { decodedJson ->
                onConfirmRestoreAction = {
                    onImportBase64(decodedJson)
                }
                showRestoreConfirmDialog = true
            }
        )
    }

    // RESET DATABASE DOUBLE-CONFIRMATION MODALS
    if (showResetConfirm1) {
        BackupResetConfirmationFlow(
            viewModel = backupSyncViewModel,
            onDismiss = { showResetConfirm1 = false },
            onSuccessReset = {
                showResetConfirm1 = false
                onDismiss()
            }
        )
    }

    if (showCloudBackupsSheet) {
        CloudBackupsBottomSheet(
            viewModel = backupSyncViewModel,
            onDismiss = { showCloudBackupsSheet = false }
        )
    }

    if (showRestoreConfirmDialog) {
        RestoreWarningDialog(
            onDismiss = {
                showRestoreConfirmDialog = false
                onConfirmRestoreAction = null
            },
            onConfirm = {
                onConfirmRestoreAction?.invoke()
            }
        )
    }
}

@Composable
private fun BackupSheetHeader(
    isConnected: Boolean,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(R.string.backup_sheet_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val connectedBg = if (isDark) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.CreditContainerLight
        val connectedText = if (isDark) com.example.ui.theme.CreditGreenDark else com.example.ui.theme.CreditGreen
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isConnected) connectedBg else MaterialTheme.colorScheme.outlineVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) com.example.ui.theme.SelectionGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = if (isConnected) stringResource(R.string.backup_status_connected) else stringResource(R.string.backup_status_local),
                    fontSize = 9.sp,
                    color = if (isConnected) connectedText else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BackupExportSection(
    showExportOptions: Boolean,
    onToggleExportOptions: () -> Unit,
    onExportMzd: () -> Unit,
    onCopyBase64: () -> Unit,
    onShareBackupFile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onToggleExportOptions,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.backup_btn_main_backup), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = showExportOptions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportMzd,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text(stringResource(R.string.backup_btn_local_mzd), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = onCopyBase64,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text(stringResource(R.string.backup_btn_fast_encoded), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Button(
                    onClick = onShareBackupFile,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(stringResource(id = R.string.export_backup_title), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupImportSection(
    showImportOptions: Boolean,
    onToggleImportOptions: () -> Unit,
    onLocalImportClick: () -> Unit,
    onPasteBase64Click: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onToggleImportOptions,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.backup_btn_main_import), color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = showImportOptions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLocalImportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(40.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                        Text(stringResource(R.string.backup_btn_local_mzd), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onPasteBase64Click,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(40.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                        Text(stringResource(R.string.backup_btn_paste_encoded), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
