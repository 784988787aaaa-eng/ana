package com.smartledger.aldaftar.ui.screens.habayeb.components.datetime

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class RangeTab { START, END }

@Composable
fun CustomDateRangePickerContent(
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
                shape = MizanDialogTokens.shape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = MizanDialogTokens.maxWidth)
                    .padding(horizontal = 4.dp)
                    .imePadding(),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 14.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = title ?: if (includeTime) {
                                stringResource(id = R.string.datetime_picker_schedule_title)
                            } else {
                                stringResource(id = R.string.datetime_picker_filter_title)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isStart = activeTab == RangeTab.START
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { activeTab = RangeTab.START },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isStart) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if (isStart) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.datetime_picker_range_tab_start),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = DateTimeArabicHelper.formatArabicDateFull(startCalendar),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${DateTimeArabicHelper.getDayOfWeekArabic(startCalendar.get(Calendar.DAY_OF_WEEK))} • ${WesternDigits.normalize(dateFormatter.format(startCalendar.time))}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isStart) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            }
                        }

                        val isEnd = activeTab == RangeTab.END
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { activeTab = RangeTab.END },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEnd) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if (isEnd) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isEnd) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.datetime_picker_range_tab_end),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEnd) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = DateTimeArabicHelper.formatArabicDateFull(endCalendar),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEnd) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${DateTimeArabicHelper.getDayOfWeekArabic(endCalendar.get(Calendar.DAY_OF_WEEK))} • ${WesternDigits.normalize(dateFormatter.format(endCalendar.time))}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isEnd) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeCalendar = if (activeTab == RangeTab.START) startCalendar else endCalendar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                        )
                    ) {
                        DateAndTimeSection(
                            calendar = activeCalendar,
                            onCalendarChange = { updated ->
                                if (activeTab == RangeTab.START) {
                                    startCalendar = updated
                                    if (endCalendar.timeInMillis < updated.timeInMillis) {
                                        endCalendar = (updated.clone() as Calendar).apply {
                                            add(Calendar.DAY_OF_MONTH, 30)
                                        }
                                    }
                                } else {
                                    endCalendar = updated
                                    if (updated.timeInMillis < startCalendar.timeInMillis) {
                                        startCalendar = (updated.clone() as Calendar)
                                    }
                                }
                            },
                            showTime = false
                        )
                    }

                    if (includeTime) {
                        Spacer(modifier = Modifier.height(8.dp))

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

                    val diffDays = remember(startCalendar.timeInMillis, endCalendar.timeInMillis) {
                        DateTimeArabicHelper.calculateDaysBetween(startCalendar, endCalendar)
                    }
                    val durationFormatted = remember(diffDays) {
                        DateTimeArabicHelper.formatDaysCountArabic(diffDays)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val now = Calendar.getInstance()
                                    if (activeTab == RangeTab.START) {
                                        startCalendar = now
                                        if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
                                            endCalendar = (startCalendar.clone() as Calendar).apply {
                                                add(Calendar.DAY_OF_MONTH, 30)
                                            }
                                        }
                                    } else {
                                        endCalendar = now
                                        if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
                                            startCalendar = (endCalendar.clone() as Calendar)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(id = R.string.datetime_picker_quick_today),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (diffDays != 30L) {
                                        endCalendar = (startCalendar.clone() as Calendar).apply {
                                            add(Calendar.DAY_OF_MONTH, 30)
                                        }
                                    } else {
                                        endCalendar = (startCalendar.clone() as Calendar).apply {
                                            add(Calendar.DAY_OF_MONTH, 7)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "المدة: $durationFormatted",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
