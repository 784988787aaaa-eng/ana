package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.mizanColors

private val KEYPAD_ROW_1 = listOf("1", "2", "3")
private val KEYPAD_ROW_2 = listOf("4", "5", "6")
private val KEYPAD_ROW_3 = listOf("7", "8", "9")

private const val LOCK_HEADER_SCALE_LABEL = "lockHeaderScale"


/**
 * يبني شاشة إدخال رمز القفل مع الرأس والمؤشرات ولوحة الأرقام وإجراءات الاسترداد.
 * تبقى قيمة الرمز في طبقة الحالة ولا تحفظها هذه الواجهة في تخزين مكشوف.
 */
@Composable
fun PasscodeKeypadContent(
    enteredPasscode: String,
    isCheckingPasscode: Boolean,
    shakeOffsetPx: Float,
    isBiometricSupported: Boolean,
    onKeyPress: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // رأس الشاشة مع حركة بصرية خفيفة دون كشف بيانات الرمز
        val lockHeaderScale by animateFloatAsState(
            targetValue = if (isCheckingPasscode) 1.15f else if (enteredPasscode.isNotEmpty()) 1.05f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = LOCK_HEADER_SCALE_LABEL
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(lockHeaderScale)
                    .clip(CircleShape)
                    .background(mizanColors.securityIndicatorFilled.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(id = R.string.lock_app_locked_desc),
                    tint = mizanColors.securityIndicatorFilled,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.lock_ledger_locked),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = mizanColors.securityForeground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.lock_enter_pin_prompt),
                fontSize = 12.sp,
                color = mizanColors.securityForegroundMuted
            )
        }

        // مؤشرات طول الرمز الأربعة
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PasscodeDotIndicators(
                enteredLength = enteredPasscode.length,
                shakeOffsetPx = shakeOffsetPx
            )
        }

        // منطقة لوحة الأرقام
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                KeypadRow(row = KEYPAD_ROW_1, onKeyClick = onKeyPress)
                KeypadRow(row = KEYPAD_ROW_2, onKeyClick = onKeyPress)
                KeypadRow(row = KEYPAD_ROW_3, onKeyClick = onKeyPress)

                // الصف الأخير مع الإجراء الحيوي والصفر والحذف
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
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

                    KeypadButton(text = stringResource(id = R.string.calc_default_zero), isFunctional = false) {
                        onKeyPress(stringResource(id = R.string.calc_default_zero))
                    }

                    KeypadButton(text = stringResource(id = R.string.lock_delete_btn), isFunctional = true) {
                        onDeleteClick()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.lock_forgot_pin),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = mizanColors.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onForgotClick() }
                        .padding(8.dp)
                        .testTag("lock_forgot_pin_btn"),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
