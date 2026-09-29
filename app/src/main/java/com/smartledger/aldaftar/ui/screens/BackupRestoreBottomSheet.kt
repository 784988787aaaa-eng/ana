package com.smartledger.aldaftar.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.MizanSelectionBar
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.helper.VibrationHelper
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
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
    val ioScope = rememberCoroutineScope()

    fun showBackupSnackbar(message: String) {
        snackbarScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Indefinite) }
            delay(2_000L)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    var connectionFailedAttempts by remember { mutableStateOf(false) }
    var manualOptionsOpen by remember { mutableStateOf(false) }
    var archiveOpen by remember { mutableStateOf(false) }
    var resetOpen by remember { mutableStateOf(false) }
    var directRestoreFile by remember { mutableStateOf<CloudBackupFile?>(null) }
    var pendingExportName by remember { mutableStateOf<String?>(null) }

    // SAF Launchers for Local Backup Export & Import
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            VibrationHelper.triggerClickVibration(context)
            backupSyncViewModel.exportBackupBytes { bytes ->
                if (bytes != null) {
                    ioScope.launch(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openOutputStream(uri)?.use { os ->
                                os.write(bytes)
                                os.flush()
                            }
                        }.onSuccess {
                            launch(Dispatchers.Main) {
                                VibrationHelper.triggerBackupSuccessVibration(context)
                                showBackupSnackbar("تم حفظ الأرشيف بنجاح: ${pendingExportName ?: "SNA"}")
                            }
                        }.onFailure {
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
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
            VibrationHelper.triggerClickVibration(context)
            ioScope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.onSuccess { bytes ->
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
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }.onFailure {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var googleClientId by remember { mutableStateOf<String?>(null) }
    val googleClientIdLoader = remember(backupSyncViewModel) {
        { backupSyncViewModel.googleClientId { googleClientId = it?.takeIf(String::isNotBlank) } }
    }
    LaunchedEffect(Unit) { googleClientIdLoader() }

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

    val googleClient = remember(googleClientId) {
        GoogleDriveInternalAuth(context).client(googleClientId)
    }
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            connectionFailedAttempts = true
            return@rememberLauncherForActivityResult
        }
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            connectionFailedAttempts = false
            backupSyncViewModel.signInWithGoogle(account, account.serverAuthCode) { success ->
                if (success) {
                    VibrationHelper.triggerSuccessVibration(context)
                    Toast.makeText(context, context.getString(R.string.backup_toast_linked_success, account.email ?: ""), Toast.LENGTH_SHORT).show()
                } else {
                    connectionFailedAttempts = true
                }
            }
        }.onFailure { ex ->
            connectionFailedAttempts = true
            Toast.makeText(context, context.getString(R.string.backup_toast_connect_error, ex.localizedMessage ?: ex.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(42.dp)
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Prestigious Unified Top Header
                    BackupMainHeader(
                        connected = connected,
                        onDismiss = onDismiss
                    )

                    // 2. Google Drive Cloud Section (Vault)
                    CloudSyncCard(
                        connected = connected,
                        email = email,
                        busy = busy,
                        hasFailedAttempt = connectionFailedAttempts || error != null,
                        manualOptionsOpen = manualOptionsOpen,
                        onInternalConnect = {
                            VibrationHelper.triggerClickVibration(context)
                            signInLauncher.launch(googleClient.signInIntent)
                        },
                        onManualToggle = {
                            manualOptionsOpen = !manualOptionsOpen
                        },
                        onManualConnect = {
                            VibrationHelper.triggerClickVibration(context)
                            backupSyncViewModel.connectCloud()
                        },
                        onCreateCloudBackup = {
                            VibrationHelper.triggerClickVibration(context)
                            ensureBackupStorageAccess {
                                backupSyncViewModel.createCloudBackup { remote, file ->
                                    if (remote != null) {
                                        VibrationHelper.triggerBackupSuccessVibration(context)
                                        showBackupSnackbar("تم حفظ النسخة السحابية: ${file?.name ?: remote.name}")
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.backup_toast_cloud_download_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onRestoreLatest = {
                            VibrationHelper.triggerClickVibration(context)
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
                            VibrationHelper.triggerClickVibration(context)
                            archiveOpen = true
                        },
                        onDisconnect = {
                            VibrationHelper.triggerClickVibration(context)
                            backupSyncViewModel.disconnectCloud()
                        }
                    )

                    // 3. Local Backup Section (Storage & Files)
                    LocalBackupCard(
                        busy = busy,
                        onExportLocal = {
                            VibrationHelper.triggerClickVibration(context)
                            val timestamp = WesternDigits.normalize(SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date()))
                            pendingExportName = context.getString(R.string.backup_export_file_name, timestamp)
                            createDocumentLauncher.launch(pendingExportName!!)
                        },
                        onImportLocal = {
                            VibrationHelper.triggerClickVibration(context)
                            openDocumentLauncher.launch(arrayOf("*/*"))
                        }
                    )

                    // 4. Sensitive Zone: Wipe & Reset Data (Discrete & Professional)
                    SensitiveResetCard(
                        busy = busy,
                        onClick = {
                            VibrationHelper.triggerClickVibration(context)
                            resetOpen = true
                        }
                    )

                    // Operation Status / Busy Indicator
                    if (busy) {
                        OperationStatusBanner(text = busyMessage ?: stringResource(R.string.msg_processing))
                    }

                    // Error Message Display
                    error?.takeIf(String::isNotBlank)?.let {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Direct Cloud Restore Confirmation Dialog
            directRestoreFile?.let { item ->
                DirectCloudRestoreDialog(
                    file = item,
                    onDismiss = { directRestoreFile = null },
                    onConfirmRestore = {
                        VibrationHelper.triggerClickVibration(context)
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
                    },
                    ensureBackupStorageAccess = ::ensureBackupStorageAccess
                )
            }

            // Reset Confirmation Dialog (Mizan High-Security Standard)
            if (resetOpen) {
                MizanAnimatedDialog(
                    onDismissRequest = { resetOpen = false }
                ) { dismiss ->
                    MizanDialogCard(
                        maxWidth = MizanDialogTokens.compactMaxWidth,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                                lineHeight = 19.sp,
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
private fun BackupMainHeader(
    connected: Boolean,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side: Icon + Title & Subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = stringResource(R.string.backup_screen_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.backup_subtitle),
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Left side: Status Pill Badge
        Surface(
            shape = RoundedCornerShape(50),
            color = if (connected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = BorderStroke(
                0.8.dp,
                if (connected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.5.dp)
                        .clip(CircleShape)
                        .background(
                            if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                )
                Text(
                    text = stringResource(if (connected) R.string.backup_status_connected else R.string.backup_status_disconnected),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
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
    hasFailedAttempt: Boolean,
    manualOptionsOpen: Boolean,
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!connected) {
                // Not Connected State: Hero Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.backup_cloud_title),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.backup_cloud_hero_desc),
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Primary Connect Button
                Button(
                    onClick = onInternalConnect,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Link, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.backup_btn_connect_google),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Manual connection fallback: ONLY visible if automatic sign-in failed
                if (hasFailedAttempt) {
                    TextButton(
                        onClick = onManualToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(if (manualOptionsOpen) R.string.backup_manual_toggle_hide else R.string.backup_manual_fallback_link),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (manualOptionsOpen) {
                        ManualConnectionCard(busy = busy, onConnect = onManualConnect)
                    }
                }
            } else {
                // Connected State: Account Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = email ?: stringResource(R.string.backup_cloud_title),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.backup_cloud_active_status),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Disconnect Icon Button
                        IconButton(
                            onClick = onDisconnect,
                            enabled = !busy,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(R.string.backup_btn_disconnect),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Dual Primary Actions: Cloud Upload & Restore Latest
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Right button: نسخ سحابي (Primary)
                    Button(
                        onClick = onCreateCloudBackup,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.backup_btn_cloud_upload),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Left button: استعادة أحدث نسخة (Tonal)
                    FilledTonalButton(
                        onClick = onRestoreLatest,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CloudDownload, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.backup_btn_restore_latest),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Navigation Banner: Cloud Archive (Clean, modern list navigation item)
                Surface(
                    onClick = onOpenArchive,
                    enabled = !busy,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FolderShared,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = stringResource(R.string.backup_history_nav_title),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.backup_history_nav_desc),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.backup_local_section_title),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.backup_local_section_desc),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dual Buttons: Export & Import
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Export Button (FilledTonalButton)
                FilledTonalButton(
                    onClick = onExportLocal,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FileUpload, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.backup_btn_local_export),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Import Button (OutlinedButton)
                OutlinedButton(
                    onClick = onImportLocal,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
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
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.backup_btn_local_import),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
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
private fun SensitiveResetCard(
    busy: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.backup_reset_section_title),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.backup_reset_section_desc),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onClick,
                enabled = !busy,
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_reset_action_btn),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
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
        val datePart = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(file.modifiedTime))
        val timePart = SimpleDateFormat("hh:mm a", Locale.US).format(Date(file.modifiedTime))
            .replace("AM", "ص")
            .replace("PM", "م")
        WesternDigits.normalize("$datePart | $timePart")
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.dialog_restore_title),
                icon = Icons.Default.CloudDownload,
                iconTint = MaterialTheme.colorScheme.primary,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Details Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDateTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = sizeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.dialog_restore_desc),
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(R.string.dialog_btn_restore),
                    onConfirm = onConfirmRestore,
                    cancelText = stringResource(R.string.common_cancel),
                    onCancel = dismiss
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                fontSize = 11.5.sp,
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.backup_manual_steps_title),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.backup_manual_step_1),
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.backup_manual_step_2),
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onConnect,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_manual_btn),
                    fontSize = 11.5.sp,
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
    onConnect: () -> Unit,
    ensureBackupStorageAccess: (() -> Unit) -> Unit
) {
    val connected by vm.cloudConnected.collectAsStateWithLifecycle()
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
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!searchActive) {
                ArchiveHeader(onDismiss = onDismiss)
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

            HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

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
                            .heightIn(max = 420.dp)
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
                                onRestore = { restoreItem = item },
                                onDelete = { deleteItem = item }
                            )
                        }
                    }
                }
            }

            // Bottom Actions: Selection bar or Create New Backup button
            if (selectionMode) {
                MizanSelectionBar(
                    selectedCount = selected.size,
                    totalCount = items.size,
                    onDismiss = {
                        selectionMode = false
                        selected = emptySet()
                    },
                    onToggleAll = {
                        selected = if (selected.size == items.size) emptySet() else items.map { it.id }.toSet()
                    },
                    onDelete = { deleteMany = true }
                )
            } else {
                Button(
                    onClick = {
                        VibrationHelper.triggerClickVibration(context)
                        ensureBackupStorageAccess {
                            vm.createCloudBackup { remote, file ->
                                if (remote != null) {
                                    VibrationHelper.triggerBackupSuccessVibration(context)
                                    showBackupSnackbar("تم حفظ النسخة السحابية: ${file?.name ?: remote.name}")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history_btn_create_new),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
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
                VibrationHelper.triggerClickVibration(context)
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                        lineHeight = 19.sp,
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                        lineHeight = 19.sp,
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
private fun ArchiveHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CloudSync, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = stringResource(R.string.history_sheet_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp)
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
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Text(
                text = WesternDigits.normalize(stringResource(R.string.history_items_count, count)),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Left side action icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onToggleSelection, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (selectionMode) Icons.Default.CheckCircle else Icons.Default.Checklist,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(38.dp)) {
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
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.backup_search_close),
                modifier = Modifier.size(18.dp)
            )
        }
        BasicTextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(searchFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
            }),
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
            IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(36.dp)) {
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
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateText = remember(item.modifiedTime) {
        WesternDigits.normalize(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(item.modifiedTime)))
    }
    val timeText = remember(item.modifiedTime) {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(item.modifiedTime))
            .replace("AM", "ص")
            .replace("PM", "م")
            .let(WesternDigits::normalize)
    }
    val sizeText = remember(item.size) {
        val kb = item.size / 1024.0
        val value = java.lang.String.format(Locale.US, "%.1f", kb)
        WesternDigits.normalize(value + " " + context.getString(R.string.backup_unit_kb))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (checked) 1.5.dp else 0.8.dp,
            if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = dateText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeText,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = sizeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                if (selection) {
                    Surface(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClick),
                        color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (checked) {
                                Icon(Icons.Default.Check, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Quick Restore Button
                        FilledTonalButton(
                            onClick = {
                                VibrationHelper.triggerClickVibration(context)
                                onRestore()
                            },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, null, Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.dialog_btn_restore),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                VibrationHelper.triggerClickVibration(context)
                                onDelete()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.backup_btn_delete_confirm),
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
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
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = stringResource(R.string.backup_empty_cloud_title),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Link, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.backup_btn_connect_google),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingCloudState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.5.dp)
        Text(
            text = stringResource(R.string.backup_loading_cloud),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyListState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.BackupTable,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        Text(
            text = stringResource(R.string.backup_empty_list_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.backup_empty_list_desc),
            fontSize = 11.5.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
