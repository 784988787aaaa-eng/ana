package com.smartledger.aldaftar.ui.screens.license

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.domain.license.LicensePlan
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import com.smartledger.aldaftar.domain.license.LicenseStatus
import com.smartledger.aldaftar.domain.license.LicenseType
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel

@Composable
fun LicenseDialog(
    viewModel: LicenseViewModel,
    onDismiss: () -> Unit,
    forced: Boolean = false
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.snapshot.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.isBusy.collectAsStateWithLifecycle()
    val deviceReplacedNotice by viewModel.deviceReplacedNotice.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var activationCode by rememberSaveable { mutableStateOf("") }
    var signedToken by rememberSaveable { mutableStateOf("") }
    var showLicenseRequestDialog by rememberSaveable { mutableStateOf(false) }

    val googleClient = remember {
        GoogleDriveInternalAuth(context).client()
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            viewModel.signInWithGoogle(account, account.serverAuthCode)
        }.onFailure { ex ->
            Toast.makeText(
                context,
                context.getString(R.string.license_toast_google_signin_err, ex.localizedMessage ?: ex.message ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Device Replaced Alert Dialog
    if (!deviceReplacedNotice.isNullOrBlank()) {
        DeviceReplacedDialog(
            message = deviceReplacedNotice!!,
            onDismiss = { viewModel.dismissDeviceReplacedNotice() },
            onReSignIn = {
                viewModel.dismissDeviceReplacedNotice()
                googleSignInLauncher.launch(googleClient.signInIntent)
            }
        )
    }

    // Dedicated Artistic License Request Dialog (نافذة طلب ترخيص)
    if (showLicenseRequestDialog) {
        LicenseRequestDialog(
            email = session.email ?: state.email,
            accountCode = session.accountCode ?: state.accountCode,
            deviceCode = viewModel.deviceCode(),
            onDismiss = { showLicenseRequestDialog = false }
        )
    }

    val title = when {
        state.isLifetime -> stringResource(R.string.license_title_lifetime)
        state.isTrialActive -> stringResource(R.string.license_title_trial_active)
        state.isTrialExpired -> stringResource(R.string.license_title_trial_expired)
        state.status == LicenseStatus.VERIFICATION_REQUIRED -> stringResource(R.string.license_title_verification_required)
        state.status == LicenseStatus.REVOKED -> stringResource(R.string.license_title_revoked)
        else -> stringResource(R.string.license_title_activate)
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.maxWidth,
            maxHeight = 660.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Prestigious License Header
                CompactLicenseHeader(
                    title = title,
                    active = state.isPaid,
                    onDismiss = dismiss
                )

                if (state.isPaid) {
                    // 2. Active License Card (Lifetime or Active Trial)
                    ActiveLicenseCompactCard(
                        snapshot = state,
                        session = session,
                        onSignOut = { viewModel.signOutUnified() }
                    )

                    if (state.isTrialPlan) {
                        OpenLicenseRequestCard(
                            onClick = { showLicenseRequestDialog = true }
                        )
                    }
                } else {
                    // 3. State Banner (Trial Progress or Alert)
                    CompactStateBanner(
                        snapshot = state,
                        isExpired = state.isTrialExpired,
                        isRevoked = state.status == LicenseStatus.REVOKED,
                        isVerification = state.status == LicenseStatus.VERIFICATION_REQUIRED
                    )

                    // 4. Segmented Mode Selector Tabs
                    CompactModeTabs(
                        selected = selectedMode,
                        onSelected = {
                            selectedMode = it
                            viewModel.clearMessage()
                        }
                    )

                    // 5. Mode Content
                    if (selectedMode == 0) {
                        UnifiedAccountLoginSection(
                            session = session,
                            snapshot = state,
                            activationCode = activationCode,
                            busy = busy,
                            onActivationChange = { activationCode = it },
                            onPasteActivationCode = {
                                val pasted = clipboard.getText()?.text?.trim().orEmpty()
                                if (pasted.isNotEmpty()) activationCode = pasted
                            },
                            onSignInGoogle = {
                                googleSignInLauncher.launch(googleClient.signInIntent)
                            },
                            onSignOutGoogle = {
                                viewModel.signOutUnified()
                            },
                            onCheckCloudLicense = {
                                viewModel.checkCloudLicense()
                            },
                            onActivate = {
                                viewModel.activateWithCode(activationCode)
                            }
                        )
                    } else {
                        SignedTokenCompactSection(
                            token = signedToken,
                            busy = busy,
                            onTokenChange = { signedToken = it },
                            onPasteToken = {
                                val pasted = clipboard.getText()?.text?.trim().orEmpty()
                                if (pasted.isNotEmpty()) signedToken = pasted
                            },
                            onActivate = {
                                viewModel.applyToken(signedToken)
                            }
                        )
                    }

                    // 6. Refined Device Code Card
                    CompactDeviceCodeRow(
                        deviceCode = viewModel.deviceCode(),
                        onCopy = {
                            clipboard.setText(AnnotatedString(viewModel.deviceCode()))
                            Toast.makeText(
                                context,
                                context.getString(R.string.license_toast_device_code_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    // 7. Artistic License Request Card (in App Theme Colors)
                    OpenLicenseRequestCard(
                        onClick = { showLicenseRequestDialog = true }
                    )
                }

                // 8. Progress Bar
                if (busy) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 9. Message / Error Banner
                if (!message.isNullOrBlank()) {
                    CompactMessageBanner(
                        message = message!!,
                        isError = !state.isPaid
                    )
                }

                // 10. Bottom Action Button
                if (state.isPaid) {
                    Button(
                        onClick = dismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.license_btn_close),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = dismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = stringResource(R.string.license_btn_continue_later),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * نافذة طلب ترخيص (License Request Window)
 * مصممة بألوان التطبيق الرسمية، وبلا نصوص صلبة، وبأسلوب فني راقٍ ومبدع.
 */
@Composable
fun LicenseRequestDialog(
    email: String?,
    accountCode: String?,
    deviceCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val mizanColors = MaterialTheme.mizanColors

    var selectedType by rememberSaveable { mutableStateOf(LicenseType.ACCOUNT) }
    var selectedPlan by rememberSaveable { mutableStateOf(LicensePlan.LIFETIME) }

    val supportWhatsapp = stringResource(R.string.support_whatsapp_number)
    val typeCloudTitle = stringResource(R.string.license_req_type_cloud_title)
    val typeLocalTitle = stringResource(R.string.license_req_type_local_title)
    val planLifetimeTitle = stringResource(R.string.license_req_plan_lifetime_title)
    val planTrialTitle = stringResource(R.string.license_req_plan_trial_title)

    fun buildFormattedRequestMessage(): String {
        val chosenTypeLabel = if (selectedType == LicenseType.ACCOUNT) typeCloudTitle else typeLocalTitle
        val chosenPlanLabel = if (selectedPlan == LicensePlan.LIFETIME) planLifetimeTitle else planTrialTitle
        return buildString {
            append(context.getString(R.string.license_req_msg_header)).append("\n")
            append(context.getString(R.string.license_req_msg_type_fmt, chosenTypeLabel)).append("\n")
            append(context.getString(R.string.license_req_msg_plan_fmt, chosenPlanLabel)).append("\n")
            if (!email.isNullOrBlank()) {
                append(context.getString(R.string.license_req_msg_email_fmt, email.trim())).append("\n")
            }
            if (!accountCode.isNullOrBlank()) {
                append(context.getString(R.string.license_req_msg_account_code_fmt, accountCode.trim())).append("\n")
            }
            append(context.getString(R.string.license_req_msg_device_code_fmt, deviceCode))
        }
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.maxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                // 1. Artistic Header in App Theme Colors
                MizanDialogHeader(
                    title = stringResource(R.string.license_req_dialog_title),
                    subtitle = stringResource(R.string.license_req_dialog_subtitle),
                    icon = Icons.Default.WorkspacePremium,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onCloseClick = dismiss
                )

                // 2. Creative Benefits Strip in App Theme Colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val features = listOf(
                        stringResource(R.string.license_req_feature_1),
                        stringResource(R.string.license_req_feature_2),
                        stringResource(R.string.license_req_feature_3)
                    )
                    features.forEach { featureText ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = featureText,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 3. Step 1: License Binding Type Selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.license_req_step_type_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LicenseRequestOptionCard(
                        selected = selectedType == LicenseType.ACCOUNT,
                        title = stringResource(R.string.license_req_type_cloud_title),
                        description = stringResource(R.string.license_req_type_cloud_desc),
                        icon = Icons.Default.CloudDone,
                        badgeText = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedType = LicenseType.ACCOUNT
                        }
                    )

                    LicenseRequestOptionCard(
                        selected = selectedType == LicenseType.LOCAL,
                        title = stringResource(R.string.license_req_type_local_title),
                        description = stringResource(R.string.license_req_type_local_desc),
                        icon = Icons.Default.PhoneAndroid,
                        badgeText = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedType = LicenseType.LOCAL
                        }
                    )
                }

                // 4. Step 2: License Plan Selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.license_req_step_plan_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LicensePlanPillCard(
                            modifier = Modifier.weight(1f),
                            selected = selectedPlan == LicensePlan.LIFETIME,
                            title = stringResource(R.string.license_req_plan_lifetime_title),
                            badge = stringResource(R.string.license_req_plan_lifetime_badge),
                            icon = Icons.Default.WorkspacePremium,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedPlan = LicensePlan.LIFETIME
                            }
                        )

                        LicensePlanPillCard(
                            modifier = Modifier.weight(1f),
                            selected = selectedPlan == LicensePlan.TRIAL,
                            title = stringResource(R.string.license_req_plan_trial_title),
                            badge = null,
                            icon = Icons.Default.Schedule,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedPlan = LicensePlan.TRIAL
                            }
                        )
                    }
                }

                // 5. Auto-Attached Identity Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.license_req_summary_title),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.6.dp
                        )

                        // Device Code Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.license_req_summary_device_label),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = deviceCode,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Linked Account Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.license_req_summary_account_label),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = email?.takeIf(String::isNotBlank)
                                    ?: stringResource(R.string.license_req_summary_account_unlinked),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!email.isNullOrBlank()) mizanColors.credit else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 6. Primary & Secondary Actions (100% App Theme Colors)
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val msg = buildFormattedRequestMessage()
                        sendLicenseRequestMessage(context, supportWhatsapp, msg)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = MizanDialogTokens.buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.license_req_btn_send),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            clipboard.setText(AnnotatedString(buildFormattedRequestMessage()))
                            Toast.makeText(
                                context,
                                context.getString(R.string.license_req_toast_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1.15f)
                            .height(42.dp),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.license_req_btn_copy_details),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = dismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = MizanDialogTokens.buttonShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.license_req_btn_back),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun LicenseRequestOptionCard(
    selected: Boolean,
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String?,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) activeColor.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 0.9.dp,
            color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) activeColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = activeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) activeColor else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LicensePlanPillCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    badge: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = if (selected) activeColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 0.9.dp,
            color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!badge.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = activeColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceReplacedDialog(
    message: String,
    onDismiss: () -> Unit,
    onReSignIn: () -> Unit
) {
    MizanAnimatedDialog(onDismissRequest = onDismiss) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.license_device_alert_title),
                icon = Icons.Default.DevicesOther,
                iconTint = MaterialTheme.colorScheme.error,
                onCloseClick = dismiss
            )

            Text(
                text = message,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            MizanDialogActions(
                confirmText = stringResource(R.string.license_btn_resign_in),
                confirmIcon = Icons.Default.AccountCircle,
                onConfirm = onReSignIn,
                cancelText = stringResource(R.string.license_btn_close),
                onCancel = dismiss
            )
        }
    }
}

@Composable
private fun CompactLicenseHeader(
    title: String,
    active: Boolean,
    onDismiss: () -> Unit
) {
    val mizanColors = MaterialTheme.mizanColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (active) mizanColors.creditContainer else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(
                    0.9.dp,
                    if (active) mizanColors.creditBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (active) Icons.Default.VerifiedUser else Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        tint = if (active) mizanColors.credit else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        if (active) R.string.license_subtitle_active else R.string.license_subtitle_inactive
                    ),
                    fontSize = 11.sp,
                    color = if (active) mizanColors.credit else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.desc_close),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactStateBanner(
    snapshot: LicenseSnapshot,
    isExpired: Boolean,
    isRevoked: Boolean,
    isVerification: Boolean
) {
    val isLimitReached = snapshot.trialUsed >= snapshot.trialLimit
    val isAlert = isExpired || isRevoked || isVerification || isLimitReached
    val bgColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f)
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
    val borderColor = if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val textColor = if (isAlert) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onPrimaryContainer

    val description = when {
        isLimitReached -> stringResource(R.string.license_banner_limit_reached, snapshot.trialUsed, snapshot.trialLimit)
        isExpired -> stringResource(R.string.license_banner_trial_expired)
        snapshot.isDeviceReplaced -> snapshot.revocationMessage ?: stringResource(R.string.license_banner_device_replaced)
        isRevoked -> snapshot.revocationMessage ?: stringResource(R.string.license_banner_revoked)
        isVerification -> stringResource(R.string.license_banner_verification_required)
        snapshot.isTrialPlan && snapshot.trialEndsAt != null -> {
            val days = snapshot.calculateRemainingDays() ?: 0
            stringResource(R.string.license_banner_trial_days_remaining, days)
        }
        else -> {
            val remainingOps = (snapshot.trialLimit - snapshot.trialUsed).coerceAtLeast(0)
            stringResource(R.string.license_banner_trial_ops_remaining, remainingOps, snapshot.trialLimit)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(0.9.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isAlert) Icons.Default.Info else Icons.Default.Verified,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactModeTabs(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactTabItem(
            modifier = Modifier.weight(1f),
            selected = selected == 0,
            icon = Icons.Default.AccountCircle,
            label = stringResource(R.string.license_tab_account),
            onClick = { onSelected(0) }
        )
        CompactTabItem(
            modifier = Modifier.weight(1f),
            selected = selected == 1,
            icon = Icons.Default.VpnKey,
            label = stringResource(R.string.license_tab_local_token),
            onClick = { onSelected(1) }
        )
    }
}

@Composable
private fun CompactTabItem(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UnifiedAccountLoginSection(
    session: UnifiedAccountSession,
    snapshot: LicenseSnapshot,
    activationCode: String,
    busy: Boolean,
    onActivationChange: (String) -> Unit,
    onPasteActivationCode: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onCheckCloudLicense: () -> Unit,
    onActivate: () -> Unit
) {
    val activationFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!session.isSignedIn) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.license_google_signin_desc),
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Button(
                        onClick = onSignInGoogle,
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.license_btn_google_signin),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Connected Google Account Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = session.email ?: stringResource(R.string.license_google_connected_default),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(
                                    if (snapshot.activationRequired) {
                                        R.string.license_google_status_needs_code
                                    } else {
                                        R.string.license_google_status_waiting_cloud
                                    }
                                ),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    TextButton(
                        onClick = onSignOutGoogle,
                        enabled = !busy,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.license_btn_signout),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Cloud License Check Button
            FilledTonalButton(
                onClick = onCheckCloudLicense,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = MizanDialogTokens.buttonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.CloudSync, null, Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.license_btn_check_cloud),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Manual Activation Code Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (snapshot.activationRequired) {
                                R.string.license_prompt_enter_first_code
                            } else {
                                R.string.license_prompt_enter_link_code
                            }
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = activationCode,
                        onValueChange = onActivationChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(activationFocusRequester),
                        singleLine = true,
                        enabled = !busy,
                        label = { Text(stringResource(R.string.license_field_activation_code_label), fontSize = 11.5.sp) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.license_field_activation_code_placeholder),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (activationCode.isNotBlank()) {
                                IconButton(onClick = { onActivationChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.license_btn_clear_token),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = onPasteActivationCode) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = stringResource(R.string.license_btn_paste_token),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = MizanDialogTokens.inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onActivate()
                        })
                    )

                    RequestFocusAndShowKeyboard(
                        focusRequester = activationFocusRequester,
                        enabled = session.isSignedIn && !busy,
                        autoShow = true
                    )

                    Button(
                        onClick = onActivate,
                        enabled = !busy && activationCode.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.license_btn_activate_account),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedTokenCompactSection(
    token: String,
    busy: Boolean,
    onTokenChange: (String) -> Unit,
    onPasteToken: () -> Unit,
    onActivate: () -> Unit
) {
    val tokenFocusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.license_local_section_desc),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    onClick = onPasteToken,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.license_btn_paste_token),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(tokenFocusRequester),
                minLines = 2,
                maxLines = 3,
                enabled = !busy,
                label = { Text(stringResource(R.string.license_field_local_token_label), fontSize = 11.5.sp) },
                placeholder = { Text(stringResource(R.string.license_field_local_token_placeholder), fontSize = 11.5.sp) },
                trailingIcon = {
                    if (token.isNotBlank()) {
                        IconButton(onClick = { onTokenChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.license_btn_clear_token),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = MizanDialogTokens.inputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onActivate()
                })
            )

            RequestFocusAndShowKeyboard(focusRequester = tokenFocusRequester, enabled = !busy, autoShow = true)

            Button(
                onClick = onActivate,
                enabled = !busy && token.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                shape = MizanDialogTokens.buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.VpnKey, null, Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.license_btn_activate_local),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompactDeviceCodeRow(
    deviceCode: String,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.license_device_code_label),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deviceCode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onCopy,
                shape = RoundedCornerShape(9.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.license_btn_copy),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.license_btn_copy),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenLicenseRequestCard(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.license_btn_open_request_window),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.license_btn_open_request_sub),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveLicenseCompactCard(
    snapshot: LicenseSnapshot,
    session: UnifiedAccountSession,
    onSignOut: () -> Unit
) {
    val isLifetime = snapshot.isLifetime
    val isTrial = snapshot.isTrialPlan
    val mizanColors = MaterialTheme.mizanColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = mizanColors.creditContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, mizanColors.creditBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = mizanColors.credit.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = mizanColors.credit
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(
                                if (isLifetime) R.string.license_active_lifetime_badge else R.string.license_active_trial_badge
                            ),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isTrial) {
                            val remaining = snapshot.calculateRemainingDays() ?: snapshot.remainingDays ?: 0
                            Text(
                                text = stringResource(R.string.license_active_remaining_days, remaining),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = mizanColors.credit
                            )
                        }
                    }
                }

                if (session.isSignedIn) {
                    TextButton(
                        onClick = onSignOut,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.license_btn_signout),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = mizanColors.creditBorder.copy(alpha = 0.5f))

            if (!session.email.isNullOrBlank() || !snapshot.email.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.license_active_account_fmt, session.email ?: snapshot.email ?: ""),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!snapshot.accountCode.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.license_active_code_fmt, snapshot.accountCode),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.license_active_devices_fmt, snapshot.maxDevices),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.license_active_unlimited_note),
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactMessageBanner(
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(
            0.8.dp,
            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                fontSize = 11.5.sp,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun sendLicenseRequestMessage(
    context: Context,
    supportPhone: String,
    formattedMessage: String
) {
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$supportPhone&text=${Uri.encode(formattedMessage)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.license_req_toast_whatsapp_err),
                Toast.LENGTH_SHORT
            ).show()
        }
}
