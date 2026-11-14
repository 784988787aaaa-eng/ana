package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.BrandPrimaryLight
import com.smartledger.aldaftar.ui.theme.BrandSecondary
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.NeutralTextPrimaryDark

private val KEYPAD_ROW_1 = listOf("1", "2", "3")
private val KEYPAD_ROW_2 = listOf("4", "5", "6")
private val KEYPAD_ROW_3 = listOf("7", "8", "9")

private val LOCK_TEXT_COLOR = NeutralTextPrimaryDark
private val LOCK_TEXT_SECONDARY_COLOR = NeutralTextPrimaryDark.copy(alpha = 0.62f)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(BrandPrimaryLight.copy(alpha = 0.14f))
                    .border(
                        width = 1.dp,
                        color = BrandPrimaryLight.copy(alpha = 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.lock_app_locked_desc),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id = R.string.lock_ledger_locked),
                fontFamily = CairoFontFamily,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LOCK_TEXT_COLOR
            )

            Spacer(modifier = Modifier.height(6.dp))

            val isErrorOrLockout = lockoutTimeRemainingSec > 0L || !errorMessage.isNullOrBlank()
            val subtitleText = (when {
                lockoutTimeRemainingSec > 0L -> stringResource(id = R.string.lock_lockout_active, lockoutTimeRemainingSec)
                !errorMessage.isNullOrBlank() -> errorMessage
                else -> stringResource(id = R.string.lock_enter_pin_prompt)
            }).toEnglishDigits()

            val subtitleColor = when {
                isErrorOrLockout -> Color(0xFFFF8A80)
                else -> LOCK_TEXT_SECONDARY_COLOR
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isErrorOrLockout) Color(0xFFEF4444).copy(alpha = 0.18f)
                        else Color.White.copy(alpha = 0.06f)
                    )
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = subtitleText,
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = subtitleColor,
                    fontWeight = if (isErrorOrLockout) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PasscodeDotIndicators(
                enteredLength = enteredPasscode.length,
                shakeOffsetPx = shakeOffsetPx
            )
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                KeypadRow(row = KEYPAD_ROW_1, onKeyClick = onKeyPress)
                KeypadRow(row = KEYPAD_ROW_2, onKeyClick = onKeyPress)
                KeypadRow(row = KEYPAD_ROW_3, onKeyClick = onKeyPress)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBiometricSupported) {
                        KeypadIconButton(
                            icon = Icons.Default.Fingerprint,
                            contentDescription = stringResource(id = R.string.sec_biometric_title),
                            onClick = onBiometricClick
                        )
                    } else {
                        Box(modifier = Modifier.size(72.dp))
                    }

                    KeypadButton(text = "0", isFunctional = false) {
                        onKeyPress("0")
                    }

                    KeypadButton(text = stringResource(id = R.string.lock_delete_btn), isFunctional = true) {
                        onDeleteClick()
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.lock_forgot_pin),
                    fontFamily = CairoFontFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BrandSecondary.copy(alpha = 0.12f))
                        .border(0.8.dp, BrandSecondary.copy(alpha = 0.30f), RoundedCornerShape(50))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onForgotClick()
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("lock_forgot_pin_btn"),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
