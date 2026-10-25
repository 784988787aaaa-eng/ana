package com.smartledger.aldaftar.ui.screens.security.components

import android.widget.Toast
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.MizanDialogInnerCard
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.launch

enum class SecurityActiveAction {
    CHANGE_PIN,
    DEACTIVATE
}

@Composable
fun SecurityActivePanel(
    currentSettings: AppSettings,
    viewModel: SecurityViewModel,
    onChangePasscode: () -> Unit,
    onDeactivateSecurity: () -> Unit,
    modifier: Modifier = Modifier,
    isInDialog: Boolean = false
) {
    val mizanColors = MaterialTheme.mizanColors
    val haptic = LocalHapticFeedback.current
    var pendingAction by remember { mutableStateOf<SecurityActiveAction?>(null) }

    val shieldBg = mizanColors.creditContainer
    val shieldBorder = mizanColors.creditBorder
    val shieldTint = mizanColors.credit
    val activeText = mizanColors.credit
    val deactivateContent = mizanColors.debt
    val deactivateBorder = mizanColors.debtBorder

    val containerModifier = if (isInDialog) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    }

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Sleek Horizontal Active Protection Status Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = shieldBg.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, shieldBorder)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(shieldTint.copy(alpha = 0.14f), CircleShape)
                            .border(width = 1.2.dp, color = shieldBorder, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = shieldTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.sec_toast_active_success),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = shieldTint.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.sec_active_shield_badge),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeText
                                )
                            }

                            val hasHint = !currentSettings.recoveryHint.isNullOrBlank()
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = if (hasHint) R.string.sec_hint_configured_badge else R.string.sec_hint_not_configured_badge
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(id = R.string.sec_card_desc_warning),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.5.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Biometric Quick Unlock Card
        if (viewModel.isBiometricSupported) {
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    if (isBiometricEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(9.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = stringResource(id = R.string.sec_biometric_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(id = R.string.sec_biometric_desc),
                                fontSize = 10.sp,
                                lineHeight = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // 3. Action Buttons Side-by-Side: Change PIN & Deactivate Lock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    pendingAction = SecurityActiveAction.CHANGE_PIN
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.sec_btn_change_pin),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pendingAction = SecurityActiveAction.DEACTIVATE
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                    contentColor = deactivateContent
                ),
                border = BorderStroke(1.dp, deactivateBorder),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.sec_deactivate_btn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    val activePendingAction = pendingAction
    if (activePendingAction != null) {
        VerifyOldPinDialog(
            action = activePendingAction,
            recoveryHint = currentSettings.recoveryHint,
            onVerifySuspend = { input, isRecovery ->
                if (isRecovery) {
                    viewModel.verifyRecoveryPhraseOffMainThread(input)
                } else {
                    viewModel.verifyPinOffMainThread(input)
                }
            },
            onSuccess = {
                val action = pendingAction
                pendingAction = null
                if (action == SecurityActiveAction.CHANGE_PIN) {
                    onChangePasscode()
                } else if (action == SecurityActiveAction.DEACTIVATE) {
                    onDeactivateSecurity()
                }
            },
            onDismiss = { pendingAction = null }
        )
    }
}

@Composable
fun VerifyOldPinDialog(
    action: SecurityActiveAction,
    recoveryHint: String?,
    onVerifySuspend: suspend (input: String, isRecoveryMode: Boolean) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val mizanColors = MaterialTheme.mizanColors
    val coroutineScope = rememberCoroutineScope()
    var pinInput by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    var showRecoveryMode by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pinFocusRequester = remember { FocusRequester() }
    val recoveryFocusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    fun triggerVerification(dismissDialog: () -> Unit) {
        if (isVerifying) return
        val targetInput = if (!showRecoveryMode) pinInput else recoveryInput
        if (targetInput.isBlank()) return
        isVerifying = true
        errorMessage = null
        focusManager.clearFocus()
        coroutineScope.launch {
            val matched = onVerifySuspend(targetInput, showRecoveryMode)
            isVerifying = false
            if (matched) {
                onSuccess()
            } else {
                val msg = if (!showRecoveryMode) {
                    context.getString(R.string.sec_toast_incorrect_current_pin)
                } else {
                    context.getString(R.string.sec_toast_incorrect_recovery)
                }
                errorMessage = msg
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (!showRecoveryMode) pinInput = "" else recoveryInput = ""
            }
        }
    }

    MizanAnimatedDialog(
        onDismissRequest = { if (!isVerifying) onDismiss() }
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = if (action == SecurityActiveAction.CHANGE_PIN) {
                    stringResource(id = R.string.sec_verify_change_pin)
                } else {
                    stringResource(id = R.string.sec_verify_disable_lock)
                },
                subtitle = if (!showRecoveryMode) {
                    stringResource(id = R.string.sec_verify_pin_prompt)
                } else {
                    stringResource(id = R.string.sec_verify_recovery_prompt)
                },
                icon = if (!showRecoveryMode) Icons.Default.Lock else Icons.Default.Key,
                iconTint = MaterialTheme.colorScheme.primary,
                isCentered = false,
                onCloseClick = { if (!isVerifying) dismissDialog() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MizanDialogInnerCard(
                    contentPadding = PaddingValues(12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                ) {
                    if (!showRecoveryMode) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (!isVerifying) {
                                    val clean = it.toEnglishDigits()
                                    if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                                        pinInput = clean
                                        errorMessage = null
                                    }
                                }
                            },
                            enabled = !isVerifying,
                            isError = errorMessage != null,
                            label = { Text(stringResource(id = R.string.sec_current_pin_label), fontSize = 12.sp) },
                            placeholder = { Text(stringResource(id = R.string.sec_placeholder_code), fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Password,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }, enabled = !isVerifying) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = stringResource(id = R.string.sec_desc_toggle_pin_visibility),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (pinInput.length == 4 && !isVerifying) {
                                    triggerVerification(dismissDialog)
                                } else {
                                    focusManager.clearFocus()
                                }
                            }),
                            shape = MizanDialogTokens.inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(pinFocusRequester)
                        )

                        RequestFocusAndShowKeyboard(focusRequester = pinFocusRequester, enabled = !showRecoveryMode, autoShow = true)

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = {
                                if (!isVerifying) {
                                    errorMessage = null
                                    showRecoveryMode = true
                                }
                            },
                            enabled = !isVerifying,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.sec_forgot_pin_recovery_link),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = recoveryInput,
                            onValueChange = {
                                if (!isVerifying) {
                                    recoveryInput = it.toEnglishDigits()
                                    errorMessage = null
                                }
                            },
                            enabled = !isVerifying,
                            isError = errorMessage != null,
                            label = { Text(stringResource(id = R.string.sec_recovery_phrase_label), fontSize = 12.sp) },
                            placeholder = { Text(stringResource(id = R.string.sec_recovery_phrase_placeholder), fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            shape = MizanDialogTokens.inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (recoveryInput.isNotBlank() && !isVerifying) {
                                    triggerVerification(dismissDialog)
                                } else {
                                    focusManager.clearFocus()
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(recoveryFocusRequester)
                        )

                        RequestFocusAndShowKeyboard(focusRequester = recoveryFocusRequester, enabled = showRecoveryMode, autoShow = true)

                        if (!recoveryHint.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = mizanColors.warningContainer.copy(alpha = 0.45f),
                                border = BorderStroke(0.8.dp, mizanColors.warningBorder.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showHint = !showHint }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = mizanColors.warning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showHint) {
                                            stringResource(id = R.string.sec_hint_display_pattern, recoveryHint)
                                        } else {
                                            stringResource(id = R.string.sec_hint_toggle_show)
                                        },
                                        fontSize = 11.5.sp,
                                        color = mizanColors.warning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = {
                                if (!isVerifying) {
                                    errorMessage = null
                                    showRecoveryMode = false
                                }
                            },
                            enabled = !isVerifying,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.sec_back_to_passcode_btn),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val activeError = errorMessage
                    if (!activeError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeError,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (isVerifying) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.lock_verifying_pin),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.sec_btn_confirm),
                    confirmIcon = Icons.Default.Check,
                    onConfirm = { triggerVerification(dismissDialog) },
                    confirmEnabled = !isVerifying && (if (!showRecoveryMode) pinInput.length == 4 else recoveryInput.isNotBlank()),
                    cancelText = stringResource(id = R.string.sec_btn_cancel),
                    onCancel = { if (!isVerifying) dismissDialog() }
                )
            }
        }
    }
}
