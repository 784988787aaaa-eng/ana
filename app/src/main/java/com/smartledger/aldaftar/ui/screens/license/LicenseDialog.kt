package com.smartledger.aldaftar.ui.screens.license

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import com.smartledger.aldaftar.domain.license.LicenseStatus
import com.smartledger.aldaftar.domain.license.RevocationReason
import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.WhatsAppGreen
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import kotlinx.coroutines.delay

private const val SUPPORT_WHATSAPP = "967774004399"
private const val MODE_LOCAL = 0
private const val MODE_CLOUD = 1

@Composable
fun LicenseDialog(
    viewModel: LicenseViewModel,
    onDismiss: () -> Unit
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.snapshot.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.isBusy.collectAsStateWithLifecycle()
    val deviceReplacedNotice by viewModel.deviceReplacedNotice.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // التفعيل المحلي هو الخيار الأول والافتراضي عند فتح النافذة من القائمة الجانبية.
    var selectedMode by rememberSaveable { mutableIntStateOf(MODE_LOCAL) }
    var activationCode by rememberSaveable { mutableStateOf("") }
    var signedToken by rememberSaveable { mutableStateOf("") }
    var localRequestIsTrial by rememberSaveable { mutableStateOf(false) }

    val deviceCode = remember { WesternDigits.normalize(viewModel.deviceCode()) }

    val slicFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                require(bytes.size <= 64 * 1024) { "حجم ملف الترخيص يتجاوز الحد المسموح" }
                String(bytes, Charsets.UTF_8).trim()
            }.orEmpty()
        }.onSuccess { content ->
            if (content.isBlank()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.license_toast_slic_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signedToken = content
                viewModel.applyToken(content)
            }
        }.onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.license_toast_slic_read_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
                WesternDigits.normalize("تعذر تسجيل الدخول بحساب Google: ${ex.localizedMessage ?: ex.message}"),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    if (!deviceReplacedNotice.isNullOrBlank()) {
        DeviceReplacedDialog(
            message = WesternDigits.normalize(deviceReplacedNotice!!),
            onDismiss = { viewModel.dismissDeviceReplacedNotice() },
            onReSignIn = {
                viewModel.dismissDeviceReplacedNotice()
                googleSignInLauncher.launch(googleClient.signInIntent)
            }
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
            maxWidth = 420.dp,
            maxHeight = 690.dp,
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Atmospheric Header Band
                CompactLicenseHeader(
                    title = title,
                    active = state.isPaid,
                    onDismiss = dismiss
                )

                // 2. Scrollable Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isPaid) {
                        ActiveLicenseCompactCard(
                            snapshot = state,
                            session = session,
                            onSignOut = { viewModel.signOutUnified() }
                        )
                    } else {
                        CompactStateBanner(
                            snapshot = state,
                            isExpired = state.isTrialExpired,
                            isRevoked = state.status == LicenseStatus.REVOKED,
                            isVerification = state.status == LicenseStatus.VERIFICATION_REQUIRED
                        )

                        CompactModeTabs(
                            selected = selectedMode,
                            onSelected = { mode ->
                                selectedMode = mode
                                viewModel.clearMessage()
                            }
                        )

                        if (selectedMode == MODE_LOCAL) {
                            SignedTokenCompactSection(
                                deviceCode = deviceCode,
                                token = signedToken,
                                busy = busy,
                                requestIsTrial = localRequestIsTrial,
                                onRequestTypeChange = { localRequestIsTrial = it },
                                onCopyDeviceCode = {
                                    clipboard.setText(AnnotatedString(deviceCode))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.license_toast_device_code_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onTokenChange = { signedToken = it },
                                onPasteFromClipboard = {
                                    val pasted = clipboard.getText()?.text?.trim().orEmpty()
                                    if (pasted.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.license_toast_clipboard_empty),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        signedToken = pasted
                                    }
                                },
                                onImportSlicFile = {
                                    slicFileLauncher.launch(arrayOf("*/*", "application/octet-stream", "text/plain"))
                                },
                                onRequestWhatsAppActivation = {
                                    openLocalActivationWhatsApp(
                                        context = context,
                                        deviceCode = deviceCode,
                                        isTrialRequest = localRequestIsTrial
                                    )
                                },
                                onActivate = {
                                    viewModel.applyToken(signedToken)
                                }
                            )
                        } else {
                            UnifiedAccountLoginSection(
                                session = session,
                                snapshot = state,
                                activationCode = activationCode,
                                busy = busy,
                                onActivationChange = { activationCode = it },
                                onSignInGoogle = {
                                    googleSignInLauncher.launch(googleClient.signInIntent)
                                },
                                onSignOutGoogle = {
                                    viewModel.signOutUnified()
                                },
                                onCheckCloudLicense = {
                                    viewModel.checkCloudLicense()
                                },
                                onPasteCode = {
                                    val pasted = clipboard.getText()?.text?.trim().orEmpty()
                                    if (pasted.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.license_toast_clipboard_empty),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        activationCode = pasted
                                    }
                                },
                                onActivate = {
                                    viewModel.activateWithCode(activationCode)
                                }
                            )

                            CompactDeviceCodeRow(
                                deviceCode = deviceCode,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(deviceCode))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.license_toast_device_code_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            CompactWhatsAppButton(
                                context = context,
                                email = session.email,
                                accountCode = session.accountCode ?: state.accountCode,
                                deviceCode = deviceCode
                            )
                        }
                    }

                    AnimatedVisibility(visible = busy) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!message.isNullOrBlank()) {
                        CompactMessageBanner(
                            message = WesternDigits.normalize(message!!),
                            isError = !state.isPaid
                        )
                    }

                    if (state.isPaid) {
                        Button(
                            onClick = dismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = MizanDialogTokens.buttonShape
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_close), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = dismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = MizanDialogTokens.buttonShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Text(
                                stringResource(R.string.license_continue_later),
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
}

@Composable
fun DeviceReplacedDialog(
    message: String,
    onDismiss: () -> Unit,
    onReSignIn: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.DevicesOther,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                stringResource(R.string.license_device_alert_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = WesternDigits.normalize(message),
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onReSignIn,
                shape = MizanDialogTokens.buttonShape
            ) {
                Text(stringResource(R.string.license_re_sign_in), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), fontSize = 12.sp)
            }
        },
        shape = MizanDialogTokens.shape,
    )
}

@Composable
private fun CompactLicenseHeader(
    title: String,
    active: Boolean,
    onDismiss: () -> Unit
) {
    val headerBrush = Brush.linearGradient(
        colors = if (active) {
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
            )
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBrush)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (active) Icons.Default.VerifiedUser else Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (active) {
                            stringResource(R.string.license_subtitle_active)
                        } else {
                            stringResource(R.string.license_subtitle_inactive)
                        },
                        fontSize = 11.5.sp,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.desc_close),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
    val bgColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    val borderColor = if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val textColor = if (isAlert) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onPrimaryContainer

    val remainingCount = (snapshot.trialLimit - snapshot.trialUsed).coerceAtLeast(0)
    val showQuotaProgress = !isAlert && !snapshot.isTrialPlan && snapshot.trialLimit > 0
    val quotaProgress = if (snapshot.trialLimit > 0) {
        (snapshot.trialUsed.toFloat() / snapshot.trialLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val rawDescription = when {
        isLimitReached -> "استنفدت حد النسخة التجريبية (${snapshot.trialLimit}/${snapshot.trialLimit} عملية). فعّل النسخة الكاملة للمتابعة."
        isExpired -> "انتهت المهلة التجريبية لهذا الترخيص. فعّل نسختك للمتابعة."
        snapshot.revocationReason == RevocationReason.CLOCK_ROLLED_BACK ->
            snapshot.revocationMessage ?: "تم اكتشاف تراجع في تاريخ أو وقت الجهاز. يرجى ضبط الساعة للمتابعة."
        snapshot.isDeviceReplaced ->
            snapshot.revocationMessage ?: "تم نقل الحساب وتفعيله على جهاز آخر، فتوقف تفعيله على هذا الجهاز."
        isRevoked ->
            snapshot.revocationMessage ?: "تعذر اعتماد الترخيص الحالي. يرجى إدخال ترخيص صالح للمتابعة."
        isVerification -> "يلزم تأكيد الترخيص عبر الاتصال بالإنترنت لمتابعة العمليات."
        snapshot.isTrialPlan && snapshot.trialEndsAt != null -> {
            val days = snapshot.calculateRemainingDays() ?: 0
            "ترخيص تجريبي نشط (متبقي $days يوماً)."
        }
        else -> "الرصيد التجريبي المتاح: $remainingCount من أصل ${snapshot.trialLimit} عملية."
    }
    val description = WesternDigits.normalize(rawDescription)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isAlert) Icons.Default.Info else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            if (showQuotaProgress) {
                LinearProgressIndicator(
                    progress = { quotaProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun CompactModeTabs(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactTabItem(
                modifier = Modifier
                    .weight(1f)
                    .testTag("license_tab_local_btn"),
                selected = selected == MODE_LOCAL,
                icon = Icons.Default.VpnKey,
                label = stringResource(R.string.license_tab_local_token),
                onClick = { onSelected(MODE_LOCAL) }
            )
            CompactTabItem(
                modifier = Modifier
                    .weight(1f)
                    .testTag("license_tab_cloud_btn"),
                selected = selected == MODE_CLOUD,
                icon = Icons.Default.CloudQueue,
                label = stringResource(R.string.license_tab_account),
                onClick = { onSelected(MODE_CLOUD) }
            )
        }
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
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "tabContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 160),
        label = "tabContentColor"
    )

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        shadowElevation = if (selected) 1.5.dp else 0.dp,
        border = if (selected) BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SignedTokenCompactSection(
    deviceCode: String,
    token: String,
    busy: Boolean,
    requestIsTrial: Boolean,
    onRequestTypeChange: (Boolean) -> Unit,
    onCopyDeviceCode: () -> Unit,
    onTokenChange: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onImportSlicFile: () -> Unit,
    onRequestWhatsAppActivation: () -> Unit,
    onActivate: () -> Unit
) {
    val tokenFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Device Identifier Card
        CompactDeviceCodeRow(
            deviceCode = deviceCode,
            onCopy = onCopyDeviceCode
        )

        // 2. Token Entry Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = token,
                    onValueChange = onTokenChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(tokenFocusRequester)
                        .testTag("license_local_token_input"),
                    minLines = 2,
                    maxLines = 4,
                    enabled = !busy,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textDirection = TextDirection.Ltr
                    ),
                    label = { Text(stringResource(R.string.license_local_token_label), fontSize = 11.5.sp) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.license_local_token_placeholder),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (token.isNotEmpty()) {
                            IconButton(
                                onClick = { onTokenChange("") },
                                enabled = !busy
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.backup_search_clear),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onPasteFromClipboard,
                                enabled = !busy
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = stringResource(R.string.license_paste_from_clipboard),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (token.isNotBlank()) onActivate()
                    })
                )

                RequestFocusAndShowKeyboard(focusRequester = tokenFocusRequester, enabled = !busy, autoShow = true)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPasteFromClipboard,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("license_paste_btn"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.ContentPaste, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.license_paste_from_clipboard),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onImportSlicFile,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("license_import_slic_btn"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.UploadFile, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.license_import_slic_btn),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onActivate,
                    enabled = !busy && token.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("license_activate_local_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.license_activate_token_btn),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. WhatsApp License Request Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.license_local_request_type_label),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlanChoiceChip(
                        modifier = Modifier.weight(1f),
                        selected = !requestIsTrial,
                        label = stringResource(R.string.license_local_plan_lifetime),
                        onClick = { onRequestTypeChange(false) }
                    )
                    PlanChoiceChip(
                        modifier = Modifier.weight(1f),
                        selected = requestIsTrial,
                        label = stringResource(R.string.license_local_plan_trial),
                        onClick = { onRequestTypeChange(true) }
                    )
                }

                Button(
                    onClick = onRequestWhatsAppActivation,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("license_whatsapp_local_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.license_whatsapp_local_request),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanChoiceChip(
    modifier: Modifier,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.2.dp else 0.8.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onCheckCloudLicense: () -> Unit,
    onPasteCode: () -> Unit,
    onActivate: () -> Unit
) {
    val activationFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!session.isSignedIn) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.license_google_sign_in_hint),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onSignInGoogle,
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.license_google_sign_in_btn),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        Column {
                            Text(
                                text = session.email ?: stringResource(R.string.license_google_connected_fallback),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (snapshot.activationRequired) {
                                    stringResource(R.string.license_account_needs_activation)
                                } else {
                                    stringResource(R.string.license_account_awaiting_cloud)
                                },
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    TextButton(
                        onClick = onSignOutGoogle,
                        enabled = !busy,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.license_sign_out),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onCheckCloudLicense,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
            ) {
                Icon(Icons.Default.CloudSync, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.license_check_cloud_btn),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (snapshot.activationRequired) {
                            stringResource(R.string.license_enter_code_first_time)
                        } else {
                            stringResource(R.string.license_enter_code_link_account)
                        },
                        fontSize = 11.5.sp,
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
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            textDirection = TextDirection.Ltr
                        ),
                        label = { Text(stringResource(R.string.license_activation_code_label), fontSize = 11.5.sp) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.license_activation_code_placeholder),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (activationCode.isNotEmpty()) {
                                IconButton(onClick = { onActivationChange("") }, enabled = !busy) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.backup_search_clear),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = onPasteCode, enabled = !busy) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = stringResource(R.string.license_paste_from_clipboard),
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (activationCode.isNotBlank()) onActivate()
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
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.license_activate_account_btn),
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
private fun CompactDeviceCodeRow(
    deviceCode: String,
    onCopy: () -> Unit
) {
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1800)
            justCopied = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                justCopied = true
                onCopy()
            },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(
            0.8.dp,
            if (justCopied) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.license_device_code_label),
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = WesternDigits.normalize(deviceCode),
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textDirection = TextDirection.Ltr
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = {
                    justCopied = true
                    onCopy()
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("license_copy_device_code_btn")
            ) {
                Icon(
                    imageVector = if (justCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.license_copy_desc),
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompactWhatsAppButton(
    context: Context,
    email: String?,
    accountCode: String?,
    deviceCode: String
) {
    Button(
        onClick = {
            openDeveloperWhatsApp(context, email, accountCode, deviceCode)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(18.dp), tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.license_whatsapp_request_help),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (isLifetime) {
                                stringResource(R.string.license_status_lifetime_active)
                            } else {
                                stringResource(R.string.license_status_trial_active)
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isTrial) {
                            val remaining = snapshot.calculateRemainingDays() ?: snapshot.remainingDays ?: 0
                            Text(
                                text = WesternDigits.normalize(stringResource(R.string.license_remaining_days, remaining)),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (session.isSignedIn) {
                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.license_sign_out),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (!session.email.isNullOrBlank() || !snapshot.email.isNullOrBlank()) {
                Text(
                    text = WesternDigits.normalize(
                        stringResource(R.string.license_account_prefix, session.email ?: snapshot.email ?: "")
                    ),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!snapshot.accountCode.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = WesternDigits.normalize(
                            stringResource(R.string.license_account_code_prefix, snapshot.accountCode)
                        ),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = WesternDigits.normalize(
                            stringResource(R.string.license_max_devices_prefix, snapshot.maxDevices)
                        ),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.license_active_protection_note),
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
        color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(
            0.8.dp,
            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
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
                text = WesternDigits.normalize(message),
                fontSize = 11.5.sp,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun openDeveloperWhatsApp(
    context: Context,
    email: String?,
    accountCode: String?,
    deviceCode: String
) {
    val msg = WesternDigits.normalize(
        buildString {
            append("مرحباً، أريد تفعيل ترخيص تطبيق الدفتر الذكي.\n")
            if (!email.isNullOrBlank()) {
                append("الحساب: ${email.trim()}\n")
            }
            if (!accountCode.isNullOrBlank() && accountCode != "غير مرتبط") {
                append("كود الحساب: ${accountCode.trim()}\n")
            }
            append("رمز الجهاز: $deviceCode")
        }
    )
    launchWhatsApp(context, msg)
}

private fun openLocalActivationWhatsApp(
    context: Context,
    deviceCode: String,
    isTrialRequest: Boolean
) {
    val requestType = if (isTrialRequest) "ترخيص محلي مؤقت (تجريبي)" else "ترخيص محلي دائم (مدى الحياة)"
    val msg = WesternDigits.normalize(
        buildString {
            append("مرحباً، أطلب تفعيل محلي لتطبيق الدفتر الذكي.\n")
            append("نوع الطلب: $requestType\n")
            append("رمز الجهاز: $deviceCode")
        }
    )
    launchWhatsApp(context, msg)
}

private fun launchWhatsApp(context: Context, message: String) {
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$SUPPORT_WHATSAPP&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.license_whatsapp_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
}
