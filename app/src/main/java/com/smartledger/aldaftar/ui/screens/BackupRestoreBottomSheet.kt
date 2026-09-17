package com.smartledger.aldaftar.ui.screens

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.ui.helper.VibrationHelper
import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    backupSyncViewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: (AppSettings) -> Unit = {}
) {
    val connected by backupSyncViewModel.cloudConnected.collectAsStateWithLifecycle()
    val email by backupSyncViewModel.cloudEmail.collectAsStateWithLifecycle()
    val busy by backupSyncViewModel.isBusy.collectAsStateWithLifecycle()
    val busyMessage by backupSyncViewModel.busyMessage.collectAsStateWithLifecycle()
    val error by backupSyncViewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    fun showBackupSnackbar(message: String) {
        snackbarScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Indefinite) }
            delay(2_000L)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    var manualOptions by remember { mutableStateOf(false) }
    var archiveOpen by remember { mutableStateOf(false) }
    var resetOpen by remember { mutableStateOf(false) }
    var directRestoreFile by remember { mutableStateOf<CloudBackupFile?>(null) }
    var pendingExportName by remember { mutableStateOf<String?>(null) }

    // SAF Launchers for Local Backup Export & Import
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            backupSyncViewModel.exportBackupBytes { bytes ->
                if (bytes != null) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(bytes)
                            os.flush()
                        }
                        VibrationHelper.triggerBackupSuccessVibration(context)
                        showBackupSnackbar("تم حفظ الأرشيف: ${pendingExportName ?: "SNA"}")
                    }.onFailure {
                        Toast.makeText(context, context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    backupSyncViewModel.restoreFromBytes(bytes) { success, settings, errorMsg ->
                        if (success) {
                            VibrationHelper.triggerSuccessVibration(context)
                            Toast.makeText(context, context.getString(R.string.msg_restore_complete), Toast.LENGTH_SHORT).show()
                            if (settings != null) {
                                onRestoreSuccess(settings)
                            }
                            onDismiss()
                        } else {
                            Toast.makeText(context, errorMsg ?: context.getString(R.string.backup_toast_delete_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    fun ensureBackupStorageAccess(onGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            onGranted()
            return
        }
        val required = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val missing = required.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (missing.isEmpty()) onGranted() else legacyStoragePermissionLauncher.launch(missing)
    }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val googleClient = remember { GoogleDriveInternalAuth(context).client() }
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            backupSyncViewModel.signInWithGoogle(account, null) { success ->
                if (success) {
                    VibrationHelper.triggerSuccessVibration(context)
                    Toast.makeText(context, context.getString(R.string.backup_toast_linked_success, account.email ?: ""), Toast.LENGTH_SHORT).show()
                }
            }
        }.onFailure { ex ->
            Toast.makeText(context, context.getString(R.string.backup_toast_connect_error, ex.localizedMessage ?: ex.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 10.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Header with Title and Connection Pill Badge
                BackupMainHeader(connected = connected)

                // 1. Google Drive Cloud Sync Card
                CloudSyncCard(
                    connected = connected,
                    email = email,
                    busy = busy,
                    manualOptions = manualOptions,
                    onInternalConnect = {
                        signInLauncher.launch(googleClient.signInIntent)
                    },
                    onManualToggle = {
                        manualOptions = !manualOptions
                    },
                    onManualConnect = {
                        backupSyncViewModel.connectCloud()
                    },
                    onCreateCloudBackup = {
                        ensureBackupStorageAccess {
                            backupSyncViewModel.createCloudBackup { remote, file ->
                                if (remote != null) {
                                    VibrationHelper.triggerBackupSuccessVibration(context)
                                    showBackupSnackbar("تم حفظ الأرشيف: ${file?.name ?: remote.name}")
                                } else {
                                    Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onRestoreLatest = {
                        backupSyncViewModel.restoreLatestCloudBackup(
                            onConfirmationRequired = { latestItem ->
                                directRestoreFile = latestItem
                            },
                            onError = { resId ->
                                Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onOpenArchive = {
                        archiveOpen = true
                    },
                    onDisconnect = {
                        backupSyncViewModel.disconnectCloud()
                    }
                )

                // 2. Local Backup Card
                LocalBackupCard(
                    busy = busy,
                    onExportLocal = {
                        val timestamp = WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date()))
                        pendingExportName = context.getString(R.string.backup_export_file_name, timestamp)
                        createDocumentLauncher.launch(pendingExportName!!)
                    },
                    onImportLocal = {
                        openDocumentLauncher.launch(arrayOf("*/*"))
                    }
                )

                // 3. Clear All Data & Reset Button (Danger)
                ResetAllDataButton(
                    busy = busy,
                    onClick = {
                        resetOpen = true
                    }
                )

                // Operation Status / Busy Indicator
                if (busy) {
                    OperationStatusBanner(text = busyMessage ?: stringResource(R.string.msg_processing))
                }

                // Error Message Display
                error?.takeIf(String::isNotBlank)?.let {
                    Text(
                        text = it,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        // Direct Cloud Restore Confirmation Dialog
        directRestoreFile?.let { item ->
            DirectCloudRestoreDialog(
                file = item,
                onDismiss = { directRestoreFile = null },
                onConfirmRestore = {
                    backupSyncViewModel.restoreCloud(item) { ok, settings, err ->
                        directRestoreFile = null
                        if (ok) {
                            VibrationHelper.triggerSuccessVibration(context)
                            Toast.makeText(context, context.getString(R.string.msg_restore_complete), Toast.LENGTH_SHORT).show()
                            if (settings != null) {
                                onRestoreSuccess(settings)
                            }
                            onDismiss()
                        } else {
                            Toast.makeText(context, err ?: context.getString(R.string.msg_google_connect_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        // Cloud Archive Bottom Sheet
        if (archiveOpen) {
            CloudArchiveBottomSheet(
                vm = backupSyncViewModel,
                onDismiss = { archiveOpen = false },
                onRestoreSuccess = {
                    onRestoreSuccess(it)
                    archiveOpen = false
                },
                showBackupSnackbar = ::showBackupSnackbar,
                onConnect = {
                    archiveOpen = false
                    signInLauncher.launch(googleClient.signInIntent)
                }
            )
        }

        // Reset Confirmation Dialog
        if (resetOpen) {
            MizanAnimatedDialog(
                onDismissRequest = { resetOpen = false }
            ) { dismiss ->
                MizanDialogCard(
                    maxWidth = MizanDialogTokens.compactMaxWidth,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    MizanDialogHeader(
                        title = stringResource(R.string.dialog_wipe_title),
                        icon = Icons.Default.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        onCloseClick = dismiss
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_wipe_desc),
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        MizanDialogActions(
                            confirmText = stringResource(R.string.dialog_btn_confirm_wipe),
                            onConfirm = {
                                VibrationHelper.triggerDeleteVibration(context)
                                backupSyncViewModel.clearLocalCopyAndWipeMemory {
                                    resetOpen = false
                                    Toast.makeText(context, context.getString(R.string.backup_toast_reset_success), Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            confirmContainerColor = MaterialTheme.colorScheme.error,
                            confirmContentColor = MaterialTheme.colorScheme.onError,
                            cancelText = stringResource(R.string.common_cancel),
                            onCancel = dismiss
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun BackupMainHeader(connected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side (RTL Start): Cloud icon + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.backup_screen_title),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Left side (RTL End): Connection status pill
        Surface(
            shape = RoundedCornerShape(50),
            color = if (connected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = BorderStroke(
                0.8.dp,
                if (connected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
                Text(
                    text = stringResource(if (connected) R.string.backup_status_connected else R.string.backup_status_disconnected),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CloudSyncCard(
    connected: Boolean,
    email: String?,
    busy: Boolean,
    manualOptions: Boolean,
    onInternalConnect: () -> Unit,
    onManualToggle: () -> Unit,
    onManualConnect: () -> Unit,
    onCreateCloudBackup: () -> Unit,
    onRestoreLatest: () -> Unit,
    onOpenArchive: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.backup_cloud_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (!connected) {
                // Not connected UI
                Button(
                    onClick = onInternalConnect,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Link, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.backup_btn_connect_google),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onManualToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(if (manualOptions) R.string.backup_manual_toggle_hide else R.string.backup_manual_toggle_show),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (manualOptions) {
                    ManualConnectionCard(busy = busy, onConnect = onManualConnect)
                }
            } else {
                // Connected Account Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = email ?: stringResource(R.string.backup_cloud_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Subtitle Info
                Text(
                    text = stringResource(R.string.backup_cloud_desc),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // Row of 2 Primary Actions: Cloud Upload & Restore Latest
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Right button: نسخ احتياطي سحابي (Filled Primary)
                    Button(
                        onClick = onCreateCloudBackup,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.backup_btn_cloud_upload),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Left button: استعادة أحدث نسخة (Tonal PrimaryContainer)
                    FilledTonalButton(
                        onClick = onRestoreLatest,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.backup_btn_restore_latest),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Full Width Outlined Button: محفوظات النسخ الاحتياطي
                OutlinedButton(
                    onClick = onOpenArchive,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.backup_btn_view_history),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Disconnect Link Button
                TextButton(
                    onClick = onDisconnect,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.backup_btn_disconnect),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectCloudRestoreDialog(
    file: CloudBackupFile,
    onDismiss: () -> Unit,
    onConfirmRestore: () -> Unit
) {
    val context = LocalContext.current
    val formattedDateTime = remember(file.modifiedTime) {
        runCatching {
            WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd | hh:mm a", Locale.ENGLISH).format(Date(file.modifiedTime)))
        }.getOrElse {
            WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd | hh:mm a", Locale.ENGLISH).format(Date(file.modifiedTime)))
        }
    }
    val sizeText = remember(file.size) {
        val kb = file.size / 1024.0
        WesternDigits.normalize("${"%.1f".format(Locale.US, kb)} ${context.getString(R.string.backup_unit_kb)}")
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.dialog_restore_title),
                icon = Icons.Default.CloudDownload,
                iconTint = MaterialTheme.colorScheme.primary,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Details Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDateTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sizeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.dialog_restore_desc),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(R.string.dialog_btn_restore),
                    onConfirm = {
                        onConfirmRestore()
                    },
                    cancelText = stringResource(R.string.common_cancel),
                    onCancel = dismiss
                )
            }
        }
    }
}

@Composable
private fun LocalBackupCard(
    busy: Boolean,
    onExportLocal: () -> Unit,
    onImportLocal: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.backup_local_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Subtitle
            Text(
                text = stringResource(R.string.backup_local_desc),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row of 2 Buttons: Export & Import
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Right button: تصدير ملف (FilledTonalButton)
                FilledTonalButton(
                    onClick = onExportLocal,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FileUpload, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.backup_btn_local_export),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Left button: استيراد ملف (OutlinedButton)
                OutlinedButton(
                    onClick = onImportLocal,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.backup_btn_local_import),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetAllDataButton(
    busy: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.backup_btn_wipe_data),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedButton(
                onClick = onClick,
                enabled = !busy,
                modifier = Modifier.heightIn(min = 34.dp).wrapContentHeight(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_btn_wipe_data),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OperationStatusBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ManualConnectionCard(busy: Boolean, onConnect: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.backup_manual_steps_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.backup_manual_step_1),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.backup_manual_step_2),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onConnect,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_manual_btn),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudArchiveBottomSheet(
    vm: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: (AppSettings) -> Unit,
    showBackupSnackbar: (String) -> Unit,
    onConnect: () -> Unit
) {
    val connected by vm.cloudConnected.collectAsStateWithLifecycle()
    val email by vm.cloudEmail.collectAsStateWithLifecycle()
    val items by vm.cloudBackups.collectAsStateWithLifecycle()
    val busy by vm.isBusy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var restoreItem by remember { mutableStateOf<CloudBackupFile?>(null) }
    var deleteItem by remember { mutableStateOf<CloudBackupFile?>(null) }
    var deleteMany by remember { mutableStateOf(false) }

    LaunchedEffect(connected) { if (connected) vm.refreshCloud() }
    LaunchedEffect(search) { vm.setCloudSearch(search) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!searchActive) {
                ArchiveHeader(
                    onDismiss = onDismiss
                )
            } else {
                SearchHeader(
                    search = search,
                    onSearchChange = { search = it },
                    onClose = {
                        searchActive = false
                        search = ""
                    }
                )
            }

            // Subbar with items count badge and action icons
            ArchiveSubBar(
                count = items.size,
                selectionMode = selectionMode,
                onToggleSearch = { searchActive = !searchActive },
                onToggleSelection = {
                    selectionMode = !selectionMode
                    selected = emptySet()
                },
                onRefresh = {
                    if (selectionMode) {
                        selected = if (selected.size == items.size) emptySet() else items.map { it.id }.toSet()
                    } else {
                        vm.refreshCloud()
                    }
                }
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (!connected) {
                EmptyCloudState(onConnect)
            } else if (busy && items.isEmpty()) {
                LoadingCloudState()
            } else {
                if (items.isEmpty()) {
                    EmptyListState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            CloudBackupRow(
                                item = item,
                                selection = selectionMode,
                                checked = selected.contains(item.id),
                                onClick = {
                                    if (selectionMode) {
                                        selected = if (selected.contains(item.id)) selected - item.id else selected + item.id
                                    } else {
                                        restoreItem = item
                                    }
                                },
                                onDelete = { deleteItem = item }
                            )
                        }
                    }
                }
            }

            // Bottom Actions inside Archive
            if (selectionMode && selected.isNotEmpty()) {
                Button(
                    onClick = { deleteMany = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.backup_delete_selected_btn, selected.size),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = {
                        ensureBackupStorageAccess { vm.createCloudBackup { remote, file ->
                            if (remote != null) {
                                VibrationHelper.triggerBackupSuccessVibration(context)
                                showBackupSnackbar("تم حفظ الأرشيف: ${file?.name ?: remote.name}")
                            }
                        } }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history_btn_create_new),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    restoreItem?.let { item ->
        DirectCloudRestoreDialog(
            file = item,
            onDismiss = { restoreItem = null },
            onConfirmRestore = {
                vm.restoreCloud(item) { ok, settings, err ->
                    restoreItem = null
                    if (ok) {
                        VibrationHelper.triggerSuccessVibration(context)
                        Toast.makeText(context, context.getString(R.string.msg_restore_complete), Toast.LENGTH_SHORT).show()
                        if (settings != null) onRestoreSuccess(settings)
                    } else {
                        Toast.makeText(context, err ?: context.getString(R.string.msg_google_connect_error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    deleteItem?.let { item ->
        MizanAnimatedDialog(
            onDismissRequest = { deleteItem = null }
        ) { dismiss ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(R.string.backup_delete_title),
                    icon = Icons.Default.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    onCloseClick = dismiss
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_delete_single_confirm, item.name),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    MizanDialogActions(
                        confirmText = stringResource(R.string.backup_btn_delete_confirm),
                        onConfirm = {
                            VibrationHelper.triggerDeleteVibration(context)
                            vm.deleteCloudBackups(setOf(item.id)) { deleteItem = null }
                        },
                        confirmContainerColor = MaterialTheme.colorScheme.error,
                        confirmContentColor = MaterialTheme.colorScheme.onError,
                        cancelText = stringResource(R.string.common_cancel),
                        onCancel = dismiss
                    )
                }
            }
        }
    }

    if (deleteMany) {
        MizanAnimatedDialog(
            onDismissRequest = { deleteMany = false }
        ) { dismiss ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(R.string.backup_delete_title),
                    icon = Icons.Default.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    onCloseClick = dismiss
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history_delete_selected_confirm),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    MizanDialogActions(
                        confirmText = stringResource(R.string.backup_btn_delete_confirm),
                        onConfirm = {
                            VibrationHelper.triggerDeleteVibration(context)
                            vm.deleteCloudBackups(selected) {
                                deleteMany = false
                                selected = emptySet()
                                selectionMode = false
                            }
                        },
                        confirmContainerColor = MaterialTheme.colorScheme.error,
                        confirmContentColor = MaterialTheme.colorScheme.onError,
                        cancelText = stringResource(R.string.common_cancel),
                        onCancel = dismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudSync, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(R.string.history_sheet_title),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.common_cancel),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArchiveSubBar(
    count: Int,
    selectionMode: Boolean,
    onToggleSearch: () -> Unit,
    onToggleSelection: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side count badge
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = WesternDigits.normalize(stringResource(R.string.history_items_count, count)),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Left side action icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onToggleSelection, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (selectionMode) Icons.Default.CheckCircle else Icons.Default.Checklist,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.history_btn_refresh),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(search: String, onSearchChange: (String) -> Unit, onClose: () -> Unit) {
    val searchFocusRequester = remember { FocusRequester() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = stringResource(R.string.backup_search_close),
                modifier = Modifier.size(18.dp)
            )
        }
        BasicTextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f).focusRequester(searchFocusRequester),
            textStyle = TextStyle(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            decorationBox = { inner ->
                if (search.isBlank()) {
                    Text(
                        text = stringResource(R.string.backup_search_hint),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        )
        RequestFocusAndShowKeyboard(focusRequester = searchFocusRequester, autoShow = true)

        if (search.isNotBlank()) {
            IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.backup_search_clear),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CloudBackupRow(
    item: CloudBackupFile,
    selection: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val formattedDateTime = remember(item.modifiedTime) {
        runCatching {
            WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd | hh:mm a", Locale.ENGLISH).format(Date(item.modifiedTime)))
        }.getOrElse {
            WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd | hh:mm a", Locale.ENGLISH).format(Date(item.modifiedTime)))
        }
    }
    val sizeText = remember(item.size) {
        val kb = item.size / 1024.0
        WesternDigits.normalize("${"%.1f".format(Locale.US, kb)} ${context.getString(R.string.backup_unit_kb)}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (checked) 1.5.dp else 0.8.dp,
                if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = formattedDateTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sizeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selection) {
                    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDelete),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.backup_delete_title),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCloudState(onConnect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = stringResource(R.string.backup_empty_cloud_title),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.backup_empty_cloud_desc),
            fontSize = 11.5.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Link, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.backup_btn_connect_google),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingCloudState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.5.dp)
        Text(
            text = stringResource(R.string.backup_loading_cloud),
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyListState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.BackupTable,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = stringResource(R.string.backup_empty_list_title),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.backup_empty_list_desc),
            fontSize = 11.5.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
