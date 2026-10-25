package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.RangeTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecurringDateTimeSection(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    startDateMillis: Long,
    onStartDateChange: (Long) -> Unit,
    endDateMillis: Long,
    onEndDateChange: (Long) -> Unit,
    activeThemeColor: Color
) {
    var showRangeDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(RangeTab.START) }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }

    val formattedStartDate = remember(startDateMillis) {
        dateFormatter.format(Date(startDateMillis))
    }
    val formattedEndDate = remember(endDateMillis) {
        dateFormatter.format(Date(endDateMillis))
    }

    val timeCalendar = remember(hour, minute) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
    }
    val formattedTime = remember(timeCalendar.timeInMillis) {
        timeFormatter.format(timeCalendar.time)
            .replace("AM", "ص")
            .replace("PM", "م")
    }

    if (showRangeDialog) {
        CustomDateRangePickerDialog(
            initialStartMillis = startDateMillis,
            initialEndMillis = endDateMillis,
            initialHour = hour,
            initialMinute = minute,
            includeTime = true,
            initialSelectedTab = selectedTab,
            onDismiss = { showRangeDialog = false },
            onRangeSelected = { start, end, h, m ->
                onStartDateChange(start)
                onEndDateChange(end)
                onTimeChange(h, m)
                showRangeDialog = false
            }
        )
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateTimePill(
                icon = Icons.Default.CalendarToday,
                text = stringResource(R.string.recurring_from_prefix, formattedStartDate),
                iconTint = activeThemeColor,
                onClick = {
                    selectedTab = RangeTab.START
                    showRangeDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            DateTimePill(
                icon = Icons.Default.CalendarMonth,
                text = stringResource(R.string.recurring_to_prefix, formattedEndDate),
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = {
                    selectedTab = RangeTab.END
                    showRangeDialog = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        DateTimePill(
            icon = Icons.Default.Schedule,
            text = "${stringResource(R.string.datetime_picker_time_section)}: $formattedTime",
            iconTint = activeThemeColor,
            onClick = {
                selectedTab = RangeTab.START
                showRangeDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DateTimePill(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(15.dp)
            )
            Text(
                text = text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
