package com.example.ui.screens.ledger.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.LicenseCheckResult
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LicenseGreenBg
import com.example.ui.theme.LicenseGreenText
import com.example.ui.theme.SoftRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.BackupSyncViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

/**
 * Sleek, agile, and beautifully compact Activation & Licensing Dialog.
 * Eliminates excess whitespace with smart segmented tab controls and responsive cards.
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
    val storedEmail by com.example.domain.GoogleAuthSessionManager.currentEmail.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Google / Cloud, 1: Product Key
    var activationCodeInput by remember { mutableStateOf("") }
    var isCodeError by remember { mutableStateOf(false) }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

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
        val intent = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && intent != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                val authCode = account?.serverAuthCode
                val email = account?.email ?: ""
                if (authCode != null && backupSyncViewModel != null) {
                    backupSyncViewModel.handleGoogleOAuthCode(authCode, email) { success ->
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.backup_toast_linked_success, email), Toast.LENGTH_LONG).show()
                            if (email.isNotEmpty()) {
                                viewModel.activateWithFirebaseEmail(email) { res ->
                                    actionFeedbackMessage = when (res) {
                                        is LicenseCheckResult.Success -> null
                                        is LicenseCheckResult.DeviceMismatch -> context.getString(R.string.licensing_fluent_mismatch_error)
                                        is LicenseCheckResult.OtpRequired -> res.message
                                        is LicenseCheckResult.NotLicensed -> res.message
                                        is LicenseCheckResult.Error -> res.message
                                    }
                                    if (res is LicenseCheckResult.Success) {
                                        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_active_success), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.backup_toast_connect_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (email.isNotEmpty()) {
                    backupSyncViewModel?.googleDriveSyncHelper?.storeEmail(email)
                    viewModel.activateWithFirebaseEmail(email) { res ->
                        actionFeedbackMessage = when (res) {
                            is LicenseCheckResult.Success -> null
                            is LicenseCheckResult.DeviceMismatch -> context.getString(R.string.licensing_fluent_mismatch_error)
                            is LicenseCheckResult.OtpRequired -> res.message
                            is LicenseCheckResult.NotLicensed -> res.message
                            is LicenseCheckResult.Error -> res.message
                        }
                        if (res is LicenseCheckResult.Success) {
                            Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_active_success), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is ApiException && (e.statusCode == com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED || e.statusCode == 12501 || e.statusCode == 16)) {
                    android.util.Log.i("DeviceActivationDialog", "Google sign in was cancelled by user")
                    Toast.makeText(context, context.getString(R.string.backup_toast_cancelled), Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.e("DeviceActivationDialog", "Google sign in error", e)
                    Toast.makeText(context, context.getString(R.string.backup_toast_connect_error, e.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        } else if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
            Toast.makeText(context, context.getString(R.string.backup_toast_cancelled), Toast.LENGTH_SHORT).show()
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
                // --- Compact Top Header ---
                CompactDialogHeader(
                    isActivated = isActivated,
                    isAutoTriggered = isAutoTriggered,
                    onDismiss = onDismiss
                )

                Spacer(modifier = Modifier.height(10.dp))

                // --- Compact Status Banner ---
                CompactStatusBanner(
                    isActivated = isActivated,
                    isAutoTriggered = isAutoTriggered
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- Main Body ---
                if (isActivated) {
                    // ========================================================
                    // 🌟 ACTIVATED STATE (Compact, Sleek, No Device ID)
                    // ========================================================
                    CompactActivatedBody(
                        storedEmail = storedEmail,
                        activatedEmail = activatedEmail,
                        onLogout = {
                            backupSyncViewModel?.googleDriveLogout {
                                viewModel.clearLocalActivationData()
                                actionFeedbackMessage = null
                                Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDismiss = onDismiss
                    )
                } else {
                    // ========================================================
                    // 🔒 UNACTIVATED / TRIAL EXPIRED STATE (Agile Tabs)
                    // ========================================================
                    // Segmented Tab Selector
                    AgileSegmentedTabs(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                            actionFeedbackMessage = null
                            isCodeError = false
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "TabContentTransition"
                    ) { tabIndex ->
                        if (tabIndex == 0) {
                            // --- TAB 1: Google Account & Cloud Sync ---
                            GoogleActivationTabContent(
                                storedEmail = storedEmail,
                                isLicenseLoading = isLicenseLoading,
                                onGoogleSignInClick = {
                                    val client = googleSignInClient
                                    if (client != null) {
                                        try {
                                            googleSignInLauncher.launch(client.signInIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_google_failed), Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_google_unavailable), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onGoogleActivateClick = {
                                    if (!storedEmail.isNullOrBlank()) {
                                        viewModel.activateWithFirebaseEmail(storedEmail!!) { res ->
                                            actionFeedbackMessage = when (res) {
                                                is LicenseCheckResult.Success -> null
                                                is LicenseCheckResult.DeviceMismatch -> context.getString(R.string.licensing_fluent_mismatch_error)
                                                is LicenseCheckResult.OtpRequired -> res.message
                                                is LicenseCheckResult.NotLicensed -> res.message
                                                is LicenseCheckResult.Error -> res.message
                                            }
                                            if (res is LicenseCheckResult.Success) {
                                                Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_active_success), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            // --- TAB 2: Product Key Manual Input ---
                            ManualKeyTabContent(
                                activationCodeInput = activationCodeInput,
                                isCodeError = isCodeError,
                                onCodeInputChange = {
                                    activationCodeInput = it
                                    isCodeError = false
                                },
                                onVerifyManualCode = {
                                    val cleanInput = activationCodeInput.trim().uppercase()
                                    val success = viewModel.activateLicense(cleanInput)
                                    if (success) {
                                        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_active_success), Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        isCodeError = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Compact Device ID Bar with Quick Copy & WhatsApp Request ---
                    CompactDeviceIdBar(
                        deviceId = deviceId,
                        onCopyClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DeviceID", deviceId)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, context.getString(R.string.licensing_fluent_copied_toast), Toast.LENGTH_SHORT).show()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onWhatsAppRequestClick = {
                            val googleEmailStr = storedEmail ?: context.getString(R.string.licensing_fluent_unregistered_email)
                            val msg = context.getString(R.string.licensing_whatsapp_email_request_template, googleEmailStr, deviceId)
                            openWhatsAppSupport(context, msg)
                        }
                    )

                    // --- Feedback Banner (e.g. Unregistered / Unauthorized Account) ---
                    AnimatedVisibility(
                        visible = actionFeedbackMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftRed.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, SoftRed.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SoftRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = actionFeedbackMessage ?: "",
                                            color = SoftRed,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick WhatsApp Request Action Button
                                    FilledTonalButton(
                                        onClick = {
                                            val googleEmailStr = storedEmail ?: context.getString(R.string.licensing_fluent_unregistered_email)
                                            val msg = context.getString(R.string.licensing_whatsapp_email_request_template, googleEmailStr, deviceId)
                                            openWhatsAppSupport(context, msg)
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                                            contentColor = WhatsAppGreen
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.licensing_btn_whatsapp_request),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Bottom Actions: WhatsApp Direct Support & Dismiss ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val googleEmailStr = storedEmail ?: context.getString(R.string.licensing_fluent_unregistered_email)
                                val msg = context.getString(R.string.licensing_whatsapp_email_request_template, googleEmailStr, deviceId)
                                openWhatsAppSupport(context, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("whatsapp_contact_button"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.licensing_whatsapp_short),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.licensing_browse_offline),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Compact Top Header
// -----------------------------------------------------------------------------
@Composable
private fun CompactDialogHeader(
    isActivated: Boolean,
    isAutoTriggered: Boolean,
    onDismiss: () -> Unit
) {
    val headerIcon = when {
        isActivated -> Icons.Default.Verified
        isAutoTriggered -> Icons.Default.WarningAmber
        else -> Icons.Default.Lock
    }
    val iconTint = when {
        isActivated -> EmeraldPrimary
        isAutoTriggered -> SoftRed
        else -> MaterialTheme.colorScheme.primary
    }
    val iconBg = when {
        isActivated -> EmeraldPrimary.copy(alpha = 0.12f)
        isAutoTriggered -> SoftRed.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }
    val titleText = when {
        isActivated -> stringResource(R.string.licensing_fluent_title_active)
        isAutoTriggered -> stringResource(R.string.licensing_fluent_title_trial)
        else -> stringResource(R.string.licensing_fluent_title_activate)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActivated) {
                    Text(
                        text = stringResource(R.string.licensing_fluent_subtitle_active),
                        fontSize = 10.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .testTag("dialog_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.licensing_fluent_btn_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Compact Status Banner
// -----------------------------------------------------------------------------
@Composable
private fun CompactStatusBanner(
    isActivated: Boolean,
    isAutoTriggered: Boolean
) {
    val bannerBg = when {
        isActivated -> LicenseGreenBg
        isAutoTriggered -> SoftRed.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val bannerBorder = when {
        isActivated -> EmeraldPrimary.copy(alpha = 0.3f)
        isAutoTriggered -> SoftRed.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }
    val textColor = when {
        isActivated -> LicenseGreenText
        isAutoTriggered -> SoftRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val descText = when {
        isActivated -> stringResource(R.string.licensing_fluent_desc_active)
        isAutoTriggered -> stringResource(R.string.licensing_fluent_desc_trial)
        else -> stringResource(R.string.licensing_fluent_desc_default)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = BorderStroke(1.dp, bannerBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = descText,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// Component: Agile Segmented Tabs
// -----------------------------------------------------------------------------
@Composable
private fun AgileSegmentedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Tab 0: Google Account
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent
                )
                .clickable { onTabSelected(0) }
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.licensing_tab_email),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tab 1: Product Key
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent
                )
                .clickable { onTabSelected(1) }
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.licensing_tab_offline),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Tab 1 (Google Activation Content)
// -----------------------------------------------------------------------------
@Composable
private fun GoogleActivationTabContent(
    storedEmail: String?,
    isLicenseLoading: Boolean,
    onGoogleSignInClick: () -> Unit,
    onGoogleActivateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!storedEmail.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = storedEmail,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.licensing_fluent_cloud_backup_only),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onGoogleActivateClick,
                        enabled = !isLicenseLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isLicenseLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.licensing_fluent_btn_activate_now),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onGoogleSignInClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("google_login_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_btn_google_signin),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Tab 2 (Manual Product Key Content)
// -----------------------------------------------------------------------------
@Composable
private fun ManualKeyTabContent(
    activationCodeInput: String,
    isCodeError: Boolean,
    onCodeInputChange: (String) -> Unit,
    onVerifyManualCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = activationCodeInput,
                    onValueChange = onCodeInputChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.licensing_code_placeholder),
                            fontSize = 11.sp
                        )
                    },
                    isError = isCodeError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onVerifyManualCode() }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("activation_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        errorBorderColor = SoftRed
                    )
                )

                Button(
                    onClick = onVerifyManualCode,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("verify_code_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.licensing_fluent_btn_activate_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }

            if (isCodeError) {
                Text(
                    text = stringResource(R.string.licensing_fluent_product_key_error),
                    color = SoftRed,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Compact Device ID Bar
// -----------------------------------------------------------------------------
@Composable
private fun CompactDeviceIdBar(
    deviceId: String,
    onCopyClick: () -> Unit,
    onWhatsAppRequestClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = deviceId,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("device_id_text"),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("copy_device_id_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.licensing_fluent_copy_device_id),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Activated State Body (Ultra-clean, Compact, No Device ID)
// -----------------------------------------------------------------------------
@Composable
private fun CompactActivatedBody(
    storedEmail: String?,
    activatedEmail: String,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_license_status_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.licensing_fluent_cloud_synced_badge),
                            color = EmeraldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                if (!storedEmail.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = storedEmail,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.licensing_fluent_license_type_digital),
                                    fontSize = 9.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = stringResource(R.string.licensing_fluent_btn_signout),
                                tint = SoftRed,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_license_type_offline),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Entitlements Feature Grid (2x2 Compact)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactFeatureBadge(
                    icon = Icons.Default.AllInclusive,
                    label = stringResource(R.string.licensing_fluent_feature_unlimited_tx),
                    modifier = Modifier.weight(1f)
                )
                CompactFeatureBadge(
                    icon = Icons.Default.CloudSync,
                    label = stringResource(R.string.licensing_fluent_feature_cloud_sync),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactFeatureBadge(
                    icon = Icons.Default.PictureAsPdf,
                    label = stringResource(R.string.licensing_fluent_feature_pdf_reports),
                    modifier = Modifier.weight(1f)
                )
                CompactFeatureBadge(
                    icon = Icons.Default.Update,
                    label = stringResource(R.string.licensing_fluent_feature_updates),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Close Button
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("dialog_close_active_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.licensing_fluent_btn_close),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Compact Feature Badge
// -----------------------------------------------------------------------------
@Composable
private fun CompactFeatureBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -----------------------------------------------------------------------------
// Helper: Open WhatsApp Support
// -----------------------------------------------------------------------------
private fun openWhatsAppSupport(context: Context, msg: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://api.whatsapp.com/send?phone=967774004399&text=" + Uri.encode(msg))
    )
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_whatsapp_missing), Toast.LENGTH_SHORT).show()
    }
}
