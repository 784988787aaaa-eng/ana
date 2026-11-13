package com.smartledger.aldaftar.ui.screens.license

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.smartledger.aldaftar.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import com.smartledger.aldaftar.domain.license.LicenseStatus
import com.smartledger.aldaftar.domain.license.RevocationReason
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.theme.WhatsAppGreen
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard

private const val SUPPORT_WHATSAPP = "967774004399"

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

    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var activationCode by rememberSaveable { mutableStateOf("") }
    var signedToken by rememberSaveable { mutableStateOf("") }
    var localRequestIsTrial by rememberSaveable { mutableStateOf(false) }

    val deviceCode = remember { viewModel.deviceCode() }

    val slicFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                require(bytes.size <= 64 * 1024) { "ملف الترخيص كبير جداً" }
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
            Toast.makeText(context, "تعذر تسجيل الدخول بحساب Google: ${ex.localizedMessage ?: ex.message}", Toast.LENGTH_LONG).show()
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
            maxHeight = 680.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Header
                CompactLicenseHeader(
                    title = title,
                    active = state.isPaid,
                    onDismiss = dismiss
                )

                if (state.isPaid) {
                    // 2. Active License Card (Lifetime or active Trial)
                    ActiveLicenseCompactCard(
                        snapshot = state,
                        session = session,
                        onSignOut = { viewModel.signOutUnified() }
                    )
                } else {
                    // 3. State Banner (Trial count or Alert)
                    CompactStateBanner(
                        snapshot = state,
                        isExpired = state.isTrialExpired,
                        isRevoked = state.status == LicenseStatus.REVOKED,
                        isVerification = state.status == LicenseStatus.VERIFICATION_REQUIRED
                    )

                    // 4. Mode Selector Tabs
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
                    } else {
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
                    }
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
                        shape = MizanDialogTokens.buttonShape
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_close), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = dismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            stringResource(R.string.license_continue_later),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                text = message,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (active) Icons.Default.VerifiedUser else Icons.Default.VpnKey,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (active) {
                        stringResource(R.string.license_subtitle_active)
                    } else {
                        stringResource(R.string.license_subtitle_inactive)
                    },
                    fontSize = 12.sp,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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

@Composable
private fun CompactStateBanner(
    snapshot: LicenseSnapshot,
    isExpired: Boolean,
    isRevoked: Boolean,
    isVerification: Boolean
) {
    val isLimitReached = snapshot.trialUsed >= snapshot.trialLimit
    val isAlert = isExpired || isRevoked || isVerification || isLimitReached
    val bgColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val borderColor = if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val textColor = if (isAlert) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onPrimaryContainer

    val description = when {
        isLimitReached -> "انتهت النسخة التجريبية (100/100 عملية) .. يرجى تفعيل النسخة الكاملة لمتابعة التسجيل"
        isExpired -> "انتهت الفترة التجريبية لهذا الترخيص. يرجى التفعيل للمتابعة."
        snapshot.revocationReason == RevocationReason.CLOCK_ROLLED_BACK ->
            snapshot.revocationMessage ?: "تم اكتشاف تراجع في ساعة الجهاز. يرجى ضبط التاريخ والوقت الصحيحين للمتابعة."
        snapshot.isDeviceReplaced -> snapshot.revocationMessage ?: "تم تفعيل حساب SmartLedger على جهاز آخر، وتم إلغاء تفعيل هذا الجهاز."
        isRevoked -> snapshot.revocationMessage ?: "تعذر اعتماد الترخيص الحالي. يرجى تفعيل ترخيص صالح للمتابعة."
        isVerification -> "يلزم إعادة التحقق من الترخيص عبر الإنترنت لمتابعة العمليات."
        snapshot.isTrialPlan && snapshot.trialEndsAt != null -> {
            val days = snapshot.calculateRemainingDays() ?: 0
            "فترة تجريبية نشطة (متبقي $days يوماً)."
        }
        else -> "المتبقي من النسخة التجريبية: ${(snapshot.trialLimit - snapshot.trialUsed).coerceAtLeast(0)} عملية من أصل ${snapshot.trialLimit}."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
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
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
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
    onActivate: () -> Unit
) {
    val activationFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!session.isSignedIn) {
            // Unconnected State
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.license_google_sign_in_hint),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Button(
                        onClick = onSignInGoogle,
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.license_google_sign_in_btn),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Connected Google Account Card (Unlicensed or awaiting activation)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
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
                                fontSize = 11.5.sp,
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
                                fontSize = 9.5.sp,
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
                            stringResource(R.string.license_sign_out),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Cloud License Check Button
            OutlinedButton(
                onClick = onCheckCloudLicense,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.CloudSync, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.license_check_cloud_btn),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Manual Activation code field (Only shown for unlicensed accounts)
            Text(
                text = if (snapshot.activationRequired) {
                    stringResource(R.string.license_enter_code_first_time)
                } else {
                    stringResource(R.string.license_enter_code_link_account)
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = activationCode,
                onValueChange = onActivationChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .focusRequester(activationFocusRequester),
                singleLine = true,
                enabled = !busy,
                label = { Text(stringResource(R.string.license_activation_code_label), fontSize = 11.sp) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.license_activation_code_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                },
                shape = RoundedCornerShape(10.dp),
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
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.LockOpen, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.license_activate_account_btn), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactDeviceCodeRow(
            deviceCode = deviceCode,
            onCopy = onCopyDeviceCode
        )

        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .focusRequester(tokenFocusRequester)
                .testTag("license_local_token_input"),
            minLines = 2,
            maxLines = 3,
            enabled = !busy,
            label = { Text(stringResource(R.string.license_local_token_label), fontSize = 11.sp) },
            placeholder = { Text(stringResource(R.string.license_local_token_placeholder), fontSize = 11.sp) },
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onActivate()
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
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ContentPaste, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.license_paste_from_clipboard),
                    fontSize = 11.sp,
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
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.UploadFile, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.license_import_slic_btn),
                    fontSize = 11.sp,
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
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.VpnKey, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.license_activate_token_btn), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.license_local_request_type_label),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChip(
                selected = !requestIsTrial,
                onClick = { onRequestTypeChange(false) },
                label = { Text(stringResource(R.string.license_local_plan_lifetime), fontSize = 11.sp) }
            )
            FilterChip(
                selected = requestIsTrial,
                onClick = { onRequestTypeChange(true) },
                label = { Text(stringResource(R.string.license_local_plan_trial), fontSize = 11.sp) }
            )
        }

        Button(
            onClick = onRequestWhatsAppActivation,
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("license_whatsapp_local_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.license_whatsapp_local_request),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.license_device_code_label),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = deviceCode,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("license_copy_device_code_btn")
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.license_copy_desc),
                    modifier = Modifier.size(18.dp),
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
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(16.dp), tint = Color.White)
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.license_whatsapp_request_help),
            fontSize = 12.sp,
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
        shape = RoundedCornerShape(14.dp),
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
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (isLifetime) {
                                stringResource(R.string.license_status_lifetime_active)
                            } else {
                                stringResource(R.string.license_status_trial_active)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isTrial) {
                            val remaining = snapshot.calculateRemainingDays() ?: snapshot.remainingDays ?: 0
                            Text(
                                text = stringResource(R.string.license_remaining_days, remaining),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (session.isSignedIn) {
                    TextButton(
                        onClick = onSignOut,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            stringResource(R.string.license_sign_out),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (!session.email.isNullOrBlank() || !snapshot.email.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.license_account_prefix, session.email ?: snapshot.email ?: ""),
                    fontSize = 11.sp,
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
                        text = stringResource(R.string.license_account_code_prefix, snapshot.accountCode),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.license_max_devices_prefix, snapshot.maxDevices),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.license_active_protection_note),
                fontSize = 9.5.sp,
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
        shape = RoundedCornerShape(8.dp),
        color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(
            0.8.dp,
            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                fontSize = 11.sp,
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
    val msg = buildString {
        append("مرحباً، أريد تفعيل ترخيص تطبيق الدفتر الذكي.\n")
        if (!email.isNullOrBlank()) {
            append("الحساب: ${email.trim()}\n")
        }
        if (!accountCode.isNullOrBlank() && accountCode != "غير مرتبط") {
            append("كود الحساب: ${accountCode.trim()}\n")
        }
        append("رمز الجهاز: $deviceCode")
    }
    launchWhatsApp(context, msg)
}

private fun openLocalActivationWhatsApp(
    context: Context,
    deviceCode: String,
    isTrialRequest: Boolean
) {
    val requestType = if (isTrialRequest) "ترخيص محلي مؤقت (تجريبي)" else "ترخيص محلي دائم (مدى الحياة)"
    val msg = buildString {
        append("مرحباً، أطلب تفعيل محلي لتطبيق الدفتر الذكي.\n")
        append("نوع الطلب: $requestType\n")
        append("رمز الجهاز: $deviceCode")
    }
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
