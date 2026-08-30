package com.example.ui.screens.habayeb.components.datetime

/*
 * =====================================================================================
 * عنصر العداد المتدحرج التفاعلي (Interactive Rolling Dial Picker Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * عنصر واجهة مستخدم متقدم وشامل لاختيار الأرقام (مثل الأيام، الأشهر، السنين، الساعات، الدقائق).
 *
 * [طرق التفاعل المدعومة]:
 * 1. النقر على أسهم الزيادة والنقصان العلوية والسفلية مع اهتزاز لمسي خفيف (Haptic Feedback).
 * 2. السحب الرأسي (Vertical Drag Gestures) للأعلى وللأسفل مع مجمع إزاحة سلس وحد عتبة (Threshold).
 * 3. النقر المباشر على الرقم للتحويل الفوري إلى حقل إدخال نصي عبر لوحة المفاتيح الرقمية.
 * 4. التفاف دائري تلقائي عند تجاوز الحد الأدنى أو الأقصى للنطاق (Circular Wrap-around).
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.util.Locale

/*
 * =====================================================================================
 * دالة العداد المتدحرج (RollingDialPicker)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - label: التسمية التوضيحية للعداد (مثل: يوم، شهر، سنة).
 * - value: القيمة العددية الحالية.
 * - range: النطاق الرقمي المسموح به (مثلاً 1..31).
 * - onValueChange: رد نداء عند تغيير القيمة.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * - format: نمط تنسيق الرقم (مثلاً "%02d" للدقائق أو "%d" للسنوات).
 * =====================================================================================
 */
@Composable
fun RollingDialPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: String = "%d"
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(value) { mutableStateOf(value.toString()) }
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // نص التسمية التوضيحية
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        // سهم الزيادة العلوي (Increment Button)
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = stringResource(id = R.string.datetime_picker_increase),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val next = if (value + 1 > range.last) range.first else value + 1
                    onValueChange(next)
                }
        )

        var dragAccumulator = 0f

        // مربع عرض القيمة مع دعم السحب والنقر للتحرير
        Box(
            modifier = Modifier
                .padding(vertical = 1.dp)
                .width(42.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    RoundedCornerShape(6.dp)
                )
                .pointerInput(range) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onDragEnd = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                            val threshold = 18f
                            if (dragAccumulator > threshold) {
                                val prev = if (currentValue - 1 < range.first) range.last else currentValue - 1
                                currentOnValueChange(prev)
                                dragAccumulator = 0f
                            } else if (dragAccumulator < -threshold) {
                                val next = if (currentValue + 1 > range.last) range.last else currentValue + 1
                                currentOnValueChange(next)
                                dragAccumulator = 0f
                            }
                        }
                    )
                }
                .clickable {
                    isEditing = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (isEditing) {
                // وضع التحرير المباشر عبر لوحة المفاتيح
                BasicTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.isEmpty() || (input.all { it.isDigit() } && input.length <= range.last.toString().length)) {
                            textValue = input
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toIntOrNull()
                            if (parsed != null && parsed in range) {
                                onValueChange(parsed)
                            }
                            isEditing = false
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                // وضع العرض العادي المنسق
                val formattedText = remember(value, format) {
                    String.format(Locale.US, format, value)
                }
                Text(
                    text = formattedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // سهم النقصان السفلي (Decrement Button)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(id = R.string.datetime_picker_decrease),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val prev = if (value - 1 < range.first) range.last else value - 1
                    onValueChange(prev)
                }
        )
    }
}

