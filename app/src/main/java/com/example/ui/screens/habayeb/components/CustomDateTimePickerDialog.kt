package com.example.ui.screens.habayeb.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Standard Single Date/Time Picker Dialog for Accounts, Transactions and general entry.
 */
@Composable
fun CustomDateTimePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateTimeSelected: (Long) -> Unit,
    showTime: Boolean = true,
    title: String? = null
) {
    var calendarState by remember { 
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialMillis }) 
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Title
                    Text(
                        text = title ?: stringResource(id = R.string.datetime_picker_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Single Date & Time layout
                    DateAndTimeSection(
                        calendar = calendarState,
                        onCalendarChange = { calendarState = it },
                        showTime = showTime
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (إلغاء وموافق)
                    DialogActionButtons(
                        onDismiss = onDismiss,
                        onConfirm = { onDateTimeSelected(calendarState.timeInMillis) }
                    )
                }
            }
        }
    }
}

/**
 * Intelligent Date Range and Time Picker Dialog.
 * Used for Scheduling/Recurring transactions (From, To, Execution Time)
 * and History Filtering (From, To).
 */
@Composable
fun CustomDateRangePickerDialog(
    initialStartMillis: Long,
    initialEndMillis: Long,
    initialHour: Int? = null,
    initialMinute: Int? = null,
    includeTime: Boolean = true,
    initialSelectedTab: RangeTab = RangeTab.START,
    title: String? = null,
    onDismiss: () -> Unit,
    onRangeSelected: (startMillis: Long, endMillis: Long, hour: Int, minute: Int) -> Unit
) {
    var activeTab by remember { mutableStateOf(initialSelectedTab) }

    var startCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = initialStartMillis
            if (initialHour != null && initialMinute != null) {
                set(Calendar.HOUR_OF_DAY, initialHour)
                set(Calendar.MINUTE, initialMinute)
            }
        })
    }

    var endCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = initialEndMillis
            if (initialHour != null && initialMinute != null) {
                set(Calendar.HOUR_OF_DAY, initialHour)
                set(Calendar.MINUTE, initialMinute)
            }
        })
    }

    // Common Time Calendar for Execution Time if includeTime is true
    var timeCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            val h = initialHour ?: startCalendar.get(Calendar.HOUR_OF_DAY)
            val m = initialMinute ?: startCalendar.get(Calendar.MINUTE)
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        })
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(horizontal = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 14.dp, horizontal = 14.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Title
                    Text(
                        text = title ?: if (includeTime) {
                            stringResource(id = R.string.datetime_picker_schedule_title)
                        } else {
                            stringResource(id = R.string.datetime_picker_filter_title)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Range Switcher Tabs (من / إلى)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Start Date Tab (من)
                        val isStart = activeTab == RangeTab.START
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isStart) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { activeTab = RangeTab.START },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.datetime_picker_range_tab_start),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFormatter.format(startCalendar.time),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isStart) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // End Date Tab (إلى)
                        val isEnd = activeTab == RangeTab.END
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isEnd) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { activeTab = RangeTab.END },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.datetime_picker_range_tab_end),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEnd) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFormatter.format(endCalendar.time),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isEnd) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Tab Content
                    val activeCalendar = if (activeTab == RangeTab.START) startCalendar else endCalendar
                    DateAndTimeSection(
                        calendar = activeCalendar,
                        onCalendarChange = { updated ->
                            if (activeTab == RangeTab.START) {
                                startCalendar = updated
                                // Ensure end date is not before start date
                                if (endCalendar.timeInMillis < updated.timeInMillis) {
                                    endCalendar = (updated.clone() as Calendar).apply {
                                        add(Calendar.DAY_OF_MONTH, 30)
                                    }
                                }
                            } else {
                                endCalendar = updated
                            }
                        },
                        showTime = false // Time is handled centrally below if includeTime is true
                    )

                    // Optional Quick Adjust & Execution Time section for Scheduling
                    if (includeTime) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Time Picker Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.datetime_picker_time_section),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                TimeDialPickersRow(
                                    timeCalendar = timeCalendar,
                                    onTimeCalendarChange = { timeCalendar = it }
                                )
                            }
                        }
                    }

                    // Quick Action Chips (اليوم / +30 يوم)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    if (activeTab == RangeTab.START) {
                                        startCalendar = Calendar.getInstance()
                                    } else {
                                        endCalendar = Calendar.getInstance()
                                    }
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.datetime_picker_quick_today),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    endCalendar = (startCalendar.clone() as Calendar).apply {
                                        add(Calendar.DAY_OF_MONTH, 30)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.datetime_picker_quick_plus_month),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog Actions (إلغاء وموافق)
                    DialogActionButtons(
                        onDismiss = onDismiss,
                        onConfirm = {
                            val h = timeCalendar.get(Calendar.HOUR_OF_DAY)
                            val m = timeCalendar.get(Calendar.MINUTE)
                            onRangeSelected(
                                startCalendar.timeInMillis,
                                endCalendar.timeInMillis,
                                h,
                                m
                            )
                        }
                    )
                }
            }
        }
    }
}

enum class RangeTab { START, END }

@Composable
private fun DateAndTimeSection(
    calendar: Calendar,
    onCalendarChange: (Calendar) -> Unit,
    showTime: Boolean
) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    fun updateCalendar(field: Int, newValue: Int) {
        val newCal = (calendar.clone() as Calendar).apply {
            if (field == Calendar.MONTH) {
                set(Calendar.MONTH, newValue - 1)
            } else if (field == Calendar.HOUR) {
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
        onCalendarChange(newCal)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Block
        Box(
            modifier = Modifier
                .weight(if (showTime) 1.2f else 1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                    RoundedCornerShape(14.dp)
                )
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
                    // Day
                    RollingDialPicker(
                        label = stringResource(id = R.string.datetime_picker_day),
                        value = day,
                        range = 1..maxDays,
                        onValueChange = { updateCalendar(Calendar.DAY_OF_MONTH, it) }
                    )

                    // Month
                    RollingDialPicker(
                        label = stringResource(id = R.string.datetime_picker_month),
                        value = month,
                        range = 1..12,
                        onValueChange = { updateCalendar(Calendar.MONTH, it) }
                    )

                    // Year
                    RollingDialPicker(
                        label = stringResource(id = R.string.datetime_picker_year),
                        value = year,
                        range = 1980..2100,
                        onValueChange = { updateCalendar(Calendar.YEAR, it) }
                    )
                }
            }
        }

        if (showTime) {
            // Divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .height(64.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // Time Block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    )
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
                    TimeDialPickersRow(
                        timeCalendar = calendar,
                        onTimeCalendarChange = onCalendarChange
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeDialPickersRow(
    timeCalendar: Calendar,
    onTimeCalendarChange: (Calendar) -> Unit
) {
    val hour12 = timeCalendar.get(Calendar.HOUR)
    val displayHour = if (hour12 == 0) 12 else hour12
    val minute = timeCalendar.get(Calendar.MINUTE)
    val amPm = timeCalendar.get(Calendar.AM_PM)

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour
        RollingDialPicker(
            label = stringResource(id = R.string.datetime_picker_hour),
            value = displayHour,
            range = 1..12,
            onValueChange = { newHour ->
                val newCal = (timeCalendar.clone() as Calendar).apply {
                    val currentAmPm = get(Calendar.AM_PM)
                    val hour24 = if (currentAmPm == Calendar.PM) {
                        if (newHour == 12) 12 else newHour + 12
                    } else {
                        if (newHour == 12) 0 else newHour
                    }
                    set(Calendar.HOUR_OF_DAY, hour24)
                }
                onTimeCalendarChange(newCal)
            }
        )

        // Minute
        RollingDialPicker(
            label = stringResource(id = R.string.datetime_picker_minute),
            value = minute,
            range = 0..59,
            onValueChange = { newMin ->
                val newCal = (timeCalendar.clone() as Calendar).apply {
                    set(Calendar.MINUTE, newMin)
                }
                onTimeCalendarChange(newCal)
            },
            format = "%02d"
        )

        // AM / PM Switch
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Text(
                text = stringResource(id = R.string.datetime_picker_period),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        val newCal = (timeCalendar.clone() as Calendar).apply {
                            val currentAmPm = get(Calendar.AM_PM)
                            set(Calendar.AM_PM, if (currentAmPm == Calendar.AM) Calendar.PM else Calendar.AM)
                        }
                        onTimeCalendarChange(newCal)
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
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DialogActionButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
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
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
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
    val haptic = LocalHapticFeedback.current

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(2.dp))

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
