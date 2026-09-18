package com.smartledger.aldaftar.ui.screens.security.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
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
    val context = LocalContext.current
    val mizanColors = MaterialTheme.mizanColors
    val haptic = LocalHapticFeedback.current
    var pendingAction by remember { mutableStateOf<SecurityActiveAction?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val shieldBg = mizanColors.creditContainer
            val shieldBorder = mizanColors.creditBorder
            val shieldTint = mizanColors.credit
            val activeText = mizanColors.credit
            val deactivateContent = mizanColors.debt
            val deactivateBorder = mizanColors.debtBorder

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(shieldBg, CircleShape)
                    .border(width = 1.dp, color = shieldBorder, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = shieldTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.sec_toast_active_success),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = activeText
            )

            Text(
                text = stringResource(id = R.string.sec_card_desc_warning),
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.isBiometricSupported) {
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_desc),
                                    fontSize = 11.sp,
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    pendingAction = SecurityActiveAction.CHANGE_PIN
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                border = androidx.compose.foundation.BorderStroke(1.dp, deactivateBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
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
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = if (action == SecurityActiveAction.CHANGE_PIN) stringResource(id = com.smartledger.aldaftar.R.string.sec_verify_change_pin) else stringResource(id = com.smartledger.aldaftar.R.string.sec_verify_disable_lock),
                subtitle = if (!showRecoveryMode) stringResource(id = com.smartledger.aldaftar.R.string.sec_verify_pin_prompt) else stringResource(id = com.smartledger.aldaftar.R.string.sec_verify_recovery_prompt),
                icon = Icons.Default.Lock,
                iconTint = MaterialTheme.colorScheme.primary,
                isCentered = true,
                onCloseClick = dismissDialog
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        label = { Text(stringResource(id = com.smartledger.aldaftar.R.string.sec_current_pin_label), fontSize = 12.sp) },
                        placeholder = { Text(stringResource(id = com.smartledger.aldaftar.R.string.sec_placeholder_code), fontSize = 12.sp) },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                        shape = MizanDialogTokens.inputShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(pinFocusRequester)
                    )

                    RequestFocusAndShowKeyboard(focusRequester = pinFocusRequester, enabled = !showRecoveryMode, autoShow = true)

                    TextButton(
                        onClick = { showRecoveryMode = true },
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = stringResource(id = com.smartledger.aldaftar.R.string.sec_forgot_pin_recovery_link),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it.toEnglishDigits() },
                        label = { Text(stringResource(id = com.smartledger.aldaftar.R.string.sec_recovery_phrase_label), fontSize = 12.sp) },
                        placeholder = { Text(stringResource(id = com.smartledger.aldaftar.R.string.sec_recovery_phrase_placeholder), fontSize = 12.sp) },
                        singleLine = true,
                        shape = MizanDialogTokens.inputShape,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showHint = !showHint }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = mizanColors.warning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showHint) stringResource(id = com.smartledger.aldaftar.R.string.sec_hint_display_pattern, recoveryHint) else stringResource(id = com.smartledger.aldaftar.R.string.sec_hint_toggle_show),
                                fontSize = 12.sp,
                                color = mizanColors.warning,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    TextButton(onClick = { showRecoveryMode = false }) {
                        Text(stringResource(id = com.smartledger.aldaftar.R.string.sec_back_to_passcode_btn), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(id = com.smartledger.aldaftar.R.string.sec_btn_confirm),
                    onConfirm = {
                        val targetInput = if (!showRecoveryMode) pinInput else recoveryInput
                        if (targetInput.isNotBlank() && onVerify(targetInput)) {
                            onSuccess()
                        } else {
                            val msg = if (!showRecoveryMode) context.getString(com.smartledger.aldaftar.R.string.sec_toast_incorrect_current_pin) else context.getString(com.smartledger.aldaftar.R.string.sec_toast_incorrect_recovery)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (!showRecoveryMode) pinInput = "" else recoveryInput = ""
                        }
                    },
                    confirmEnabled = if (!showRecoveryMode) pinInput.length == 4 else recoveryInput.isNotBlank(),
                    cancelText = stringResource(id = com.smartledger.aldaftar.R.string.sec_btn_cancel),
                    onCancel = dismissDialog
                )
            }
        }
    }
}
