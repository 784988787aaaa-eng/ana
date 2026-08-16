package com.example.ui.screens.habayeb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import java.util.Calendar

@Composable
fun CustomDateTimePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateTimeSelected: (Long) -> Unit
) {
    var calendarState by remember { 
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialMillis }) 
    }

    // Date Values
    val year = calendarState.get(Calendar.YEAR)
    val month = calendarState.get(Calendar.MONTH) + 1 // 1-12
    val day = calendarState.get(Calendar.DAY_OF_MONTH)
    val maxDays = calendarState.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Time Values
    val hour12 = calendarState.get(Calendar.HOUR)
    val displayHour = if (hour12 == 0) 12 else hour12
    val minute = calendarState.get(Calendar.MINUTE)
    val amPm = calendarState.get(Calendar.AM_PM) // 0: AM, 1: PM

    fun updateCalendar(field: Int, newValue: Int) {
        val newCal = (calendarState.clone() as Calendar).apply {
            if (field == Calendar.MONTH) {
                set(Calendar.MONTH, newValue - 1)
            } else if (field == Calendar.HOUR) {
                // If 12-hour display, we need to respect AM/PM
                val currentAmPm = get(Calendar.AM_PM)
                val hour24 = if (currentAmPm == Calendar.PM) {
                    if (newValue == 12) 12 else newValue + 12
                } else {
                    if (newValue == 12) 0 else newValue
                }
                set(Calendar.HOUR_OF_DAY, hour24)
            } else {
                set(field, newValue)
            }
        }
        calendarState = newCal
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title - Compact & Clean
                Text(
                    text = stringResource(id = R.string.datetime_picker_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Main Elegant Layout: Date on the left, breathing space in the middle, Time on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DATE Section (Left) - Takes Date Values
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.datetime_picker_date),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Day (اليوم)
                                RollingDialPicker(
                                    label = stringResource(id = R.string.datetime_picker_day),
                                    value = day,
                                    range = 1..maxDays,
                                    onValueChange = { updateCalendar(Calendar.DAY_OF_MONTH, it) }
                                )

                                // Month (الشهر - رقمي فقط)
                                RollingDialPicker(
                                    label = stringResource(id = R.string.datetime_picker_month),
                                    value = month,
                                    range = 1..12,
                                    onValueChange = { updateCalendar(Calendar.MONTH, it) }
                                )

                                // Year (السنة)
                                RollingDialPicker(
                                    label = stringResource(id = R.string.datetime_picker_year),
                                    value = year,
                                    range = 1980..2100,
                                    onValueChange = { updateCalendar(Calendar.YEAR, it) }
                                )
                            }
                        }
                    }

                    // Breathing visual space
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(64.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )

                    // TIME Section (Right) - Takes Time Values
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.datetime_picker_time),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Hour (الساعة)
                                RollingDialPicker(
                                    label = stringResource(id = R.string.datetime_picker_hour),
                                    value = displayHour,
                                    range = 1..12,
                                    onValueChange = { updateCalendar(Calendar.HOUR, it) }
                                )

                                // Minute (الدقيقة)
                                RollingDialPicker(
                                    label = stringResource(id = R.string.datetime_picker_minute),
                                    value = minute,
                                    range = 0..59,
                                    onValueChange = { updateCalendar(Calendar.MINUTE, it) },
                                    format = "%02d"
                                )

                                // AM/PM (الفترة ص/م) - Highly Compact Switch
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.datetime_picker_period),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(18.dp)) // Equalizer spacer
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .clickable {
                                                val newCal = (calendarState.clone() as Calendar).apply {
                                                    val currentAmPm = get(Calendar.AM_PM)
                                                    set(Calendar.AM_PM, if (currentAmPm == Calendar.AM) Calendar.PM else Calendar.AM)
                                                }
                                                calendarState = newCal
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (amPm == Calendar.AM) stringResource(id = R.string.datetime_picker_am) else stringResource(id = R.string.datetime_picker_pm),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(18.dp)) // Equalizer spacer
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Cancel and Confirm (إلغاء وموافق)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.common_cancel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { onDateTimeSelected(calendarState.timeInMillis) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.datetime_picker_confirm),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RollingDialPicker(
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

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Very subtle field label
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(2.dp))

        // Tiny elegant chevron up
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = stringResource(id = R.string.datetime_picker_increase),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable {
                    val next = if (value + 1 > range.last) range.first else value + 1
                    onValueChange(next)
                }
        )

        var dragAccumulator = 0f

        // Center rolling and clickable box
        Box(
            modifier = Modifier
                .padding(vertical = 1.dp)
                .width(42.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .pointerInput(range) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onDragEnd = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                            val threshold = 20f // Low threshold for high sensitivity and ball rolling feel
                            if (dragAccumulator > threshold) {
                                // Dragged down -> Decrement
                                val prev = if (currentValue - 1 < range.first) range.last else currentValue - 1
                                currentOnValueChange(prev)
                                dragAccumulator = 0f
                            } else if (dragAccumulator < -threshold) {
                                // Dragged up -> Increment
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
                val formattedText = remember(value, format) {
                    String.format(java.util.Locale.US, format, value)
                }
                Text(
                    text = formattedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Tiny elegant chevron down
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(id = R.string.datetime_picker_decrease),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable {
                    val prev = if (value - 1 < range.first) range.last else value - 1
                    onValueChange(prev)
                }
        )
    }
}
