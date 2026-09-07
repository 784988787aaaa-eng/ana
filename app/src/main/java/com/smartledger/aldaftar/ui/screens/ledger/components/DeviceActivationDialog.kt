package com.smartledger.aldaftar.ui.screens.ledger.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.LicenseCheckResult
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityAndLicenseViewModel

/**
 * Unified Facade for Device Activation & Licensing Dialog.
 * Beautifully refactored into modular subcomponents for maximum maintainability.
 */
@Composable
fun DeviceActivationDialog(
    deviceId: String,
    viewModel: SecurityAndLicenseViewModel,
    backupSyncViewModel: BackupSyncViewModel? = null,
    onDismiss: () -> Unit,
    isAutoTriggered: Boolean = false
) {
    val isActivated by viewModel.isActivatedState.collectAsStateWithLifecycle()
    val activatedEmail by viewModel.activatedEmailState.collectAsStateWithLifecycle()
    val isLicenseLoading by viewModel.isLicenseLoading.collectAsStateWithLifecycle()
    val supportState by viewModel.supportIdentityState.collectAsStateWithLifecycle()
    val storedEmail by com.smartledger.aldaftar.domain.GoogleAuthSessionManager.currentEmail.collectAsStateWithLifecycle()

    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSupportIdentity()
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val googleSignInClient = remember(backupSyncViewModel) {
        try {
            backupSyncViewModel?.googleDriveSyncHelper?.getGoogleSignInClient()
        } catch (e: Exception) {
            android.util.Log.e("DeviceActivationDialog", "GoogleSignInClient creation error", e)
            null
        }
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
                    actionFeedbackMessage = null
                    viewModel.loadSupportIdentity()
                    val toastMsg = if (outcome.isDriveAuthorized) {
                        context.getString(R.string.backup_toast_linked_success, outcome.email)
                    } else {
                        context.getString(R.string.licensing_google_account_connected)
                    }
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }
                is com.smartledger.aldaftar.domain.GoogleSignInOutcome.Cancelled -> {
                    Toast.makeText(context, context.getString(R.string.backup_toast_cancelled), Toast.LENGTH_SHORT).show()
                }
                is com.smartledger.aldaftar.domain.GoogleSignInOutcome.Failed -> {
                    Toast.makeText(context, outcome.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(22.dp))
                .testTag("unified_activation_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                ActivationHeaderSection(
                    isActivated = isActivated,
                    isAutoTriggered = isAutoTriggered,
                    onDismiss = onDismiss
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Banner
                ActivationStatusBanner(
                    isActivated = isActivated,
                    isAutoTriggered = isAutoTriggered,
                    storedEmail = storedEmail
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isActivated) {
                    // Activated State Body
                    ActivationActivatedBody(
                        storedEmail = storedEmail,
                        activatedEmail = activatedEmail,
                        onLogout = {
                            viewModel.unlinkCurrentDevice {
                                backupSyncViewModel?.googleDriveLogout(
                                    onComplete = {
                                        actionFeedbackMessage = null
                                        Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = {
                                        actionFeedbackMessage = null
                                        Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onDismiss = onDismiss
                    )
                } else {
                    ActivationGoogleTabContent(
                        storedEmail = storedEmail,
                        isLicenseLoading = isLicenseLoading,
                        onGoogleSignInClick = {
                            val client = googleSignInClient
                            if (client != null) {
                                try {
                                    com.smartledger.aldaftar.domain.GoogleAuthSessionManager.setSigningIn()
                                    googleSignInLauncher.launch(client.signInIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_google_failed), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_google_unavailable), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onGoogleSignOutClick = {
                            backupSyncViewModel?.googleDriveLogout(
                                onComplete = {
                                    actionFeedbackMessage = null
                                    Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    actionFeedbackMessage = null
                                    Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onGoogleActivateClick = {
                            storedEmail?.takeIf { it.isNotBlank() }?.let { email ->
                                viewModel.activateWithFirebaseEmail(email) { res ->
                                    actionFeedbackMessage = when (res) {
                                        is LicenseCheckResult.Success -> null
                                        is LicenseCheckResult.NotLicensed -> res.message
                                        is LicenseCheckResult.NetworkOutage -> res.message
                                        is LicenseCheckResult.Error -> res.message
                                    }
                                    if (res is LicenseCheckResult.Success) {
                                        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_active_success), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Support ID Card
                    ActivationSupportIdCard(
                        supportState = supportState,
                        onCopyClick = {
                            val supportId = (supportState as? com.smartledger.aldaftar.domain.SupportIdentityState.Available)?.supportId
                            if (!supportId.isNullOrBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SupportID", supportId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, context.getString(R.string.licensing_support_id_copied_toast), Toast.LENGTH_SHORT).show()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                Toast.makeText(context, context.getString(R.string.licensing_support_id_pending_toast), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRetryClick = {
                            viewModel.loadSupportIdentity()
                        }
                    )

                    // Feedback Banner
                    ActivationFeedbackBanner(
                        actionFeedbackMessage = actionFeedbackMessage,
                        onWhatsAppRequestClick = {
                            val supportId = (supportState as? com.smartledger.aldaftar.domain.SupportIdentityState.Available)?.supportId
                            val msg = if (!supportId.isNullOrBlank()) {
                                context.getString(R.string.licensing_whatsapp_support_request_template, supportId)
                            } else {
                                context.getString(R.string.licensing_whatsapp_support_request_pending_template)
                            }
                            openWhatsAppSupportDirect(context, msg)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Actions
                    ActivationActionsFooter(
                        onWhatsAppClick = {
                            val supportId = (supportState as? com.smartledger.aldaftar.domain.SupportIdentityState.Available)?.supportId
                            val msg = if (!supportId.isNullOrBlank()) {
                                context.getString(R.string.licensing_whatsapp_support_request_template, supportId)
                            } else {
                                context.getString(R.string.licensing_whatsapp_support_request_pending_template)
                            }
                            openWhatsAppSupportDirect(context, msg)
                        },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}
