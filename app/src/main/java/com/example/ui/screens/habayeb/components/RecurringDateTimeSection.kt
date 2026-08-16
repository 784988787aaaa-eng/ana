package com.example.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ActivePicker { TIME, START_DATE, END_DATE }

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
    var activePicker by remember { mutableStateOf<ActivePicker?>(null) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale("ar")) }

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

    when (activePicker) {
        ActivePicker.TIME -> {
            CustomDateTimePickerDialog(
                initialMillis = timeCalendar.timeInMillis,
                onDismiss = { activePicker = null },
                onDateTimeSelected = { selectedMillis ->
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                    onTimeChange(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                    activePicker = null
                }
            )
        }
        ActivePicker.START_DATE -> {
            CustomDateTimePickerDialog(
                initialMillis = startDateMillis,
                onDismiss = { activePicker = null },
                onDateTimeSelected = { selectedMillis ->
                    onStartDateChange(selectedMillis)
                    activePicker = null
                }
            )
        }
        ActivePicker.END_DATE -> {
            CustomDateTimePickerDialog(
                initialMillis = endDateMillis,
                onDismiss = { activePicker = null },
                onDateTimeSelected = { selectedMillis ->
                    onEndDateChange(selectedMillis)
                    activePicker = null
                }
            )
        }
        null -> {}
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Picker Pill
        DateTimePill(
            icon = Icons.Default.Schedule,
            text = timeFormatter.format(timeCalendar.time),
            iconTint = activeThemeColor,
            onClick = { activePicker = ActivePicker.TIME },
            modifier = Modifier.weight(1f)
        )

        // Start Date Pill
        DateTimePill(
            icon = Icons.Default.CalendarToday,
            text = stringResource(R.string.recurring_from_prefix, formattedStartDate),
            iconTint = activeThemeColor,
            onClick = { activePicker = ActivePicker.START_DATE },
            modifier = Modifier.weight(1.1f)
        )

        // End Date Pill
        DateTimePill(
            icon = Icons.Default.CalendarToday,
            text = stringResource(R.string.recurring_to_prefix, formattedEndDate),
            iconTint = MaterialTheme.colorScheme.onSurface,
            onClick = { activePicker = ActivePicker.END_DATE },
            modifier = Modifier.weight(1.1f)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
