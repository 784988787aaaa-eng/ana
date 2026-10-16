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
    modifier: Modifier = Modifier
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

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Active Protection Hero Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = shieldBg.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, shieldBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(shieldTint.copy(alpha = 0.14f), CircleShape)
                            .border(width = 1.5.dp, color = shieldBorder, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = shieldTint,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = shieldTint.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.sec_active_shield_badge),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeText
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.sec_toast_active_success),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeText,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(id = R.string.sec_card_desc_warning),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Biometric Quick Unlock Card
            if (viewModel.isBiometricSupported) {
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                Surface(
                    shape = RoundedCornerShape(14.dp),
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
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_title),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_desc),
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), thickness = 0.6.dp)

            // 3. Action Buttons: Change PIN & Deactivate Lock
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sec_btn_change_pin),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sec_deactivate_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    val activePendingAction = pendingAction
    if (activePendingAction != null) {
        VerifyOldPinDialog(
            action = activePendingAction,
            recoveryHint = currentSettings.recoveryHint,
            onVerify = { input ->
                viewModel.verifyCredentials(input)
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
    onVerify: (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val mizanColors = MaterialTheme.mizanColors
    var pinInput by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    var showRecoveryMode by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }

    val pinFocusRequester = remember { FocusRequester() }
    val recoveryFocusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
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
                onCloseClick = dismissDialog
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
                                val clean = it.toEnglishDigits()
                                if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                                    pinInput = clean
                                }
                            },
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
                                IconButton(onClick = { pinVisible = !pinVisible }) {
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
                                focusManager.clearFocus()
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
                            onClick = { showRecoveryMode = true },
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
                            onValueChange = { recoveryInput = it.toEnglishDigits() },
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
                                focusManager.clearFocus()
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
                            onClick = { showRecoveryMode = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.sec_back_to_passcode_btn),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.sec_btn_confirm),
                    confirmIcon = Icons.Default.Check,
                    onConfirm = {
                        val targetInput = if (!showRecoveryMode) pinInput else recoveryInput
                        if (targetInput.isNotBlank() && onVerify(targetInput)) {
                            onSuccess()
                        } else {
                            val msg = if (!showRecoveryMode) {
                                context.getString(R.string.sec_toast_incorrect_current_pin)
                            } else {
                                context.getString(R.string.sec_toast_incorrect_recovery)
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (!showRecoveryMode) pinInput = "" else recoveryInput = ""
                        }
                    },
                    confirmEnabled = if (!showRecoveryMode) pinInput.length == 4 else recoveryInput.isNotBlank(),
                    cancelText = stringResource(id = R.string.sec_btn_cancel),
                    onCancel = dismissDialog
                )
            }
        }
    }
}
