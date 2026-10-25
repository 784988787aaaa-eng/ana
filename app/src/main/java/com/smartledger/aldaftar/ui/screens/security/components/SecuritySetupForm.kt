package com.smartledger.aldaftar.ui.screens.security.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors

private const val TEST_TAG_PIN_CODE_INPUT = "pin_code_input"
private const val TEST_TAG_PIN_CODE_CONFIRM_INPUT = "pin_code_confirm_input"
private const val TEST_TAG_RECOVERY_PHRASE_INPUT = "recovery_phrase_input"
private const val TEST_TAG_RECOVERY_HINT_INPUT = "recovery_hint_input"
private const val TEST_TAG_SECURITY_SAVE_BUTTON = "security_save_button"

@Composable
fun SecuritySetupForm(
    passcode: String,
    onPasscodeChange: (String) -> Unit,
    confirmPasscode: String,
    onConfirmPasscodeChange: (String) -> Unit,
    recoveryPhrase: String,
    onRecoveryPhraseChange: (String) -> Unit,
    recoveryHint: String,
    onRecoveryHintChange: (String) -> Unit,
    checkAcknowledged: Boolean,
    onCheckAcknowledgedChange: (Boolean) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    isInDialog: Boolean = false,
    onCancelEdit: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val mizanColors = MaterialTheme.mizanColors

    var passcodeVisible by remember { mutableStateOf(false) }
    var confirmPasscodeVisible by remember { mutableStateOf(false) }

    val passcodeFocus = remember { FocusRequester() }
    val confirmPasscodeFocus = remember { FocusRequester() }
    val recoveryPhraseFocus = remember { FocusRequester() }
    val recoveryHintFocus = remember { FocusRequester() }

    val toggleVisibilityDesc = stringResource(id = R.string.sec_desc_toggle_pin_visibility)
    val pinsComplete = passcode.length == 4 && confirmPasscode.length == 4
    val pinsMatch = pinsComplete && passcode == confirmPasscode
    val hasPinMismatch = confirmPasscode.isNotEmpty() && (
        (pinsComplete && passcode != confirmPasscode) ||
            (confirmPasscode.length <= passcode.length && !passcode.startsWith(confirmPasscode))
        )
    val hasIncompletePinWarning = passcode.isNotEmpty() && passcode.length < 4 && confirmPasscode.isNotEmpty()

    RequestFocusAndShowKeyboard(focusRequester = passcodeFocus, autoShow = true)

    val containerModifier = if (isInDialog) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .clip(MizanDialogTokens.innerCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = MizanDialogTokens.innerCardShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    }

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Section 1: Compact Single-Line PIN Setup Header + Live Match Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(26.dp),
                    shape = RoundedCornerShape(7.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.sec_setup_title),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val badgeBg = when {
                pinsMatch -> mizanColors.creditContainer
                hasPinMismatch -> mizanColors.debtContainer
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            }
            val badgeText = when {
                pinsMatch -> mizanColors.credit
                hasPinMismatch -> mizanColors.debt
                else -> MaterialTheme.colorScheme.primary
            }
            val badgeLabel = when {
                pinsMatch -> stringResource(id = R.string.sec_pin_match_ok)
                hasPinMismatch -> stringResource(id = R.string.sec_pin_match_err)
                passcode.isNotEmpty() -> "${passcode.length}/4"
                else -> stringResource(id = R.string.sec_pin_digits_badge)
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeBg
            ) {
                Text(
                    text = badgeLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }
        }

        // PIN & Confirm PIN Fields
        OutlinedTextField(
            value = passcode,
            onValueChange = { input ->
                val clean = input.toEnglishDigits()
                if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                    onPasscodeChange(clean)
                    if (clean.length == 4) {
                        confirmPasscodeFocus.requestFocus()
                    }
                }
            },
            label = { Text(stringResource(id = R.string.sec_label_code), fontSize = 11.5.sp) },
            placeholder = { Text(stringResource(id = R.string.sec_placeholder_code), fontSize = 11.5.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        passcodeVisible = !passcodeVisible
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (passcodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = toggleVisibilityDesc,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            visualTransformation = if (passcodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmPasscodeFocus.requestFocus() }),
            shape = MizanDialogTokens.inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passcodeFocus)
                .testTag(TEST_TAG_PIN_CODE_INPUT)
        )

        OutlinedTextField(
            value = confirmPasscode,
            onValueChange = { input ->
                val clean = input.toEnglishDigits()
                if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                    onConfirmPasscodeChange(clean)
                    if (clean.length == 4 && clean == passcode) {
                        recoveryPhraseFocus.requestFocus()
                    }
                }
            },
            label = { Text(stringResource(id = R.string.sec_label_confirm), fontSize = 11.5.sp) },
            placeholder = { Text(stringResource(id = R.string.sec_placeholder_confirm), fontSize = 11.5.sp) },
            singleLine = true,
            isError = hasPinMismatch,
            leadingIcon = {
                Icon(
                    imageVector = when {
                        pinsMatch -> Icons.Default.CheckCircle
                        hasPinMismatch -> Icons.Default.ErrorOutline
                        else -> Icons.Default.LockReset
                    },
                    contentDescription = null,
                    tint = when {
                        pinsMatch -> mizanColors.credit
                        hasPinMismatch -> mizanColors.debt
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        confirmPasscodeVisible = !confirmPasscodeVisible
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (confirmPasscodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = toggleVisibilityDesc,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            visualTransformation = if (confirmPasscodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { recoveryPhraseFocus.requestFocus() }),
            shape = MizanDialogTokens.inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (pinsMatch) mizanColors.credit else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (pinsMatch) mizanColors.creditBorder else MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasscodeFocus)
                .testTag(TEST_TAG_PIN_CODE_CONFIRM_INPUT)
        )

        // Explicit Inline Feedback for PIN Mismatch or Incomplete PIN
        AnimatedVisibility(visible = hasPinMismatch || hasIncompletePinWarning) {
            val errorText = if (hasPinMismatch) {
                stringResource(id = R.string.sec_pin_mismatch_inline)
            } else {
                stringResource(id = R.string.sec_pin_incomplete_inline)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(mizanColors.debtContainer.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = mizanColors.debt,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = errorText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = mizanColors.debt
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 0.6.dp,
            modifier = Modifier.padding(vertical = 1.dp)
        )

        // Section 2: Compact Single-Line Recovery Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(26.dp),
                    shape = RoundedCornerShape(7.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.sec_recovery_title),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (recoveryPhrase.isNotBlank()) mizanColors.creditContainer else MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
            ) {
                Text(
                    text = stringResource(id = R.string.sec_recovery_badge),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (recoveryPhrase.isNotBlank()) mizanColors.credit else MaterialTheme.colorScheme.secondary
                )
            }
        }

        OutlinedTextField(
            value = recoveryPhrase,
            onValueChange = onRecoveryPhraseChange,
            label = { Text(stringResource(id = R.string.sec_label_recovery), fontSize = 11.5.sp) },
            placeholder = { Text(stringResource(id = R.string.sec_placeholder_recovery), fontSize = 11.5.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 13.5.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { recoveryHintFocus.requestFocus() }),
            shape = MizanDialogTokens.inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(recoveryPhraseFocus)
                .testTag(TEST_TAG_RECOVERY_PHRASE_INPUT)
        )

        OutlinedTextField(
            value = recoveryHint,
            onValueChange = onRecoveryHintChange,
            label = { Text(stringResource(id = R.string.sec_label_hint), fontSize = 11.5.sp) },
            placeholder = { Text(stringResource(id = R.string.sec_placeholder_hint), fontSize = 11.5.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 13.5.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = MizanDialogTokens.inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(recoveryHintFocus)
                .testTag(TEST_TAG_RECOVERY_HINT_INPUT)
        )

        // Section 3: Compact Acknowledgment Row
        val ackBg = if (checkAcknowledged) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
        }
        val ackBorder = if (checkAcknowledged) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MizanDialogTokens.inputShape)
                .background(ackBg)
                .border(0.8.dp, ackBorder, MizanDialogTokens.inputShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckAcknowledgedChange(!checkAcknowledged)
                }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checkAcknowledged,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = BrandPrimary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(id = R.string.sec_checkbox_ack),
                fontSize = 11.sp,
                fontWeight = if (checkAcknowledged) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }

        val isValid = passcode.length == 4 &&
            confirmPasscode == passcode &&
            recoveryPhrase.isNotBlank() &&
            checkAcknowledged &&
            !isSaving

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSave()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            ),
            enabled = isValid,
            shape = MizanDialogTokens.buttonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(top = 1.dp)
                .testTag(TEST_TAG_SECURITY_SAVE_BUTTON)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sec_btn_activate),
                        color = if (isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        if (onCancelEdit != null) {
            TextButton(
                onClick = onCancelEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.sec_cancel_edit_btn),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
