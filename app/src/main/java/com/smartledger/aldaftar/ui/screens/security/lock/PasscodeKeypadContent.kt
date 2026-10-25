package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.BrandPrimaryLight
import com.smartledger.aldaftar.ui.theme.BrandSecondary
import com.smartledger.aldaftar.ui.theme.mizanColors

private val KEYPAD_ROW_1 = listOf("1", "2", "3")
private val KEYPAD_ROW_2 = listOf("4", "5", "6")
private val KEYPAD_ROW_3 = listOf("7", "8", "9")

@Composable
fun PasscodeKeypadContent(
    enteredPasscode: String,
    isCheckingPasscode: Boolean,
    shakeOffsetPx: Float,
    isBiometricSupported: Boolean,
    errorMessage: String? = null,
    onKeyPress: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier,
    lockoutTimeRemainingSec: Long = 0L
) {
    val haptic = LocalHapticFeedback.current
    val mizanColors = MaterialTheme.mizanColors
    val isErrorOrLockout = lockoutTimeRemainingSec > 0L || !errorMessage.isNullOrBlank()
    val isKeypadInteractive = !isCheckingPasscode && lockoutTimeRemainingSec <= 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 420.dp)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Clean, Focused Lock Identity Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = mizanColors.securityKeyBackground,
                border = BorderStroke(0.8.dp, mizanColors.securityKeyBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BrandSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.lock_security_badge),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = mizanColors.securityForegroundMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        if (isErrorOrLockout) {
                            mizanColors.error.copy(alpha = 0.18f)
                        } else {
                            BrandPrimaryLight.copy(alpha = 0.18f)
                        }
                    )
                    .border(
                        width = 1.2.dp,
                        color = if (isErrorOrLockout) {
                            mizanColors.error.copy(alpha = 0.45f)
                        } else {
                            mizanColors.securityKeyBorder
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isErrorOrLockout) Icons.Default.ErrorOutline else Icons.Default.Lock,
                    contentDescription = stringResource(id = R.string.lock_app_locked_desc),
                    tint = if (isErrorOrLockout) mizanColors.error else BrandSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.lock_ledger_locked),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = mizanColors.securityForeground
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isCheckingPasscode -> {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BrandSecondary.copy(alpha = 0.14f),
                        border = BorderStroke(0.8.dp, BrandSecondary.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("lock_verifying_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 1.8.dp,
                                color = BrandSecondary
                            )
                            Text(
                                text = stringResource(id = R.string.lock_verifying_pin),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = mizanColors.securityForeground
                            )
                        }
                    }
                }
                isErrorOrLockout -> {
                    val statusText = if (lockoutTimeRemainingSec > 0L) {
                        stringResource(id = R.string.lock_lockout_active, lockoutTimeRemainingSec)
                    } else {
                        errorMessage.orEmpty()
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = mizanColors.error.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, mizanColors.error.copy(alpha = 0.40f)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("lock_error_banner")
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = mizanColors.error,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.lock_enter_pin_prompt),
                        fontSize = 12.5.sp,
                        color = mizanColors.securityForegroundMuted,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // 2. 4-Digit PIN Progress Indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PasscodeDotIndicators(
                enteredLength = enteredPasscode.length,
                shakeOffsetPx = shakeOffsetPx,
                hasError = isErrorOrLockout,
                isChecking = isCheckingPasscode
            )
        }

        // 3. Balanced Numeric Keypad & Recovery Link
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                KeypadRow(
                    row = KEYPAD_ROW_1,
                    onKeyClick = onKeyPress,
                    enabled = isKeypadInteractive
                )
                KeypadRow(
                    row = KEYPAD_ROW_2,
                    onKeyClick = onKeyPress,
                    enabled = isKeypadInteractive
                )
                KeypadRow(
                    row = KEYPAD_ROW_3,
                    onKeyClick = onKeyPress,
                    enabled = isKeypadInteractive
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBiometricSupported) {
                        KeypadIconButton(
                            icon = Icons.Default.Fingerprint,
                            contentDescription = stringResource(id = R.string.sec_biometric_title),
                            onClick = onBiometricClick,
                            enabled = isKeypadInteractive
                        )
                    } else {
                        Box(modifier = Modifier.size(70.dp))
                    }

                    KeypadButton(
                        text = "0",
                        isFunctional = false,
                        enabled = isKeypadInteractive
                    ) {
                        onKeyPress("0")
                    }

                    KeypadButton(
                        text = stringResource(id = R.string.lock_delete_btn),
                        isFunctional = true,
                        enabled = !isCheckingPasscode && enteredPasscode.isNotEmpty()
                    ) {
                        onDeleteClick()
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = mizanColors.securityKeyBackground.copy(alpha = 0.55f),
                    border = BorderStroke(0.8.dp, mizanColors.securityKeyBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isCheckingPasscode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onForgotClick()
                        }
                        .testTag("lock_forgot_pin_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.lock_forgot_pin),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

