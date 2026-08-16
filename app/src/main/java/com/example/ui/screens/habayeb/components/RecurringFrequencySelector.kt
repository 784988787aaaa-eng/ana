package com.example.ui.screens.habayeb.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.InfoBlueBgDark
import com.example.ui.theme.InfoBlueBgLight
import com.example.ui.theme.InfoBlueTextDark
import com.example.ui.theme.InfoBlueTextLight
import com.example.ui.viewmodel.FinanceConstants
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringFrequencySelector(
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    selectedDaysOfWeek: Set<Int>,
    onDaysOfWeekChange: (Set<Int>) -> Unit,
    selectedDaysOfMonth: Set<Int>,
    onDaysOfMonthChange: (Set<Int>) -> Unit,
    activeThemeColor: Color,
    isDark: Boolean
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val arabicDaysOfWeek = remember(context) {
        listOf(
            Calendar.SATURDAY to context.getString(R.string.day_saturday),
            Calendar.SUNDAY to context.getString(R.string.day_sunday),
            Calendar.MONDAY to context.getString(R.string.day_monday),
            Calendar.TUESDAY to context.getString(R.string.day_tuesday),
            Calendar.WEDNESDAY to context.getString(R.string.day_wednesday),
            Calendar.THURSDAY to context.getString(R.string.day_thursday),
            Calendar.FRIDAY to context.getString(R.string.day_friday)
        )
    }

    val dailyLabel = stringResource(id = R.string.habayeb_recurring_daily)
    val weeklyLabel = stringResource(id = R.string.habayeb_recurring_weekly)
    val monthlyLabel = stringResource(id = R.string.habayeb_recurring_monthly)
    val options = remember(dailyLabel, weeklyLabel, monthlyLabel) {
        listOf(
            FinanceConstants.FREQ_DAILY to dailyLabel,
            FinanceConstants.FREQ_WEEKLY to weeklyLabel,
            FinanceConstants.FREQ_MONTHLY to monthlyLabel
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { (key, label) ->
            val selected = frequency == key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) activeThemeColor else Color.Transparent)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFrequencyChange(key)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Crossfade(targetState = frequency, label = "freqConfig") { targetFreq ->
        when (targetFreq) {
            FinanceConstants.FREQ_DAILY -> {
                DailyInfoBanner(isDark = isDark)
            }
            FinanceConstants.FREQ_WEEKLY -> {
                WeeklyDayPicker(
                    selectedDays = selectedDaysOfWeek,
                    daysOfWeek = arabicDaysOfWeek,
                    activeColor = activeThemeColor,
                    onDayToggle = { dayInt ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val isSelected = selectedDaysOfWeek.contains(dayInt)
                        val updated = if (isSelected) {
                            if (selectedDaysOfWeek.size > 1) selectedDaysOfWeek - dayInt else selectedDaysOfWeek
                        } else {
                            selectedDaysOfWeek + dayInt
                        }
                        onDaysOfWeekChange(updated)
                    }
                )
            }
            FinanceConstants.FREQ_MONTHLY -> {
                MonthlyDayGrid(
                    selectedDays = selectedDaysOfMonth,
                    activeColor = activeThemeColor,
                    onDayToggle = { dayNum ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val isSelected = selectedDaysOfMonth.contains(dayNum)
                        val updated = if (isSelected) {
                            if (selectedDaysOfMonth.size > 1) selectedDaysOfMonth - dayNum else selectedDaysOfMonth
                        } else {
                            selectedDaysOfMonth + dayNum
                        }
                        onDaysOfMonthChange(updated)
                    }
                )
            }
        }
    }
}

@Composable
private fun DailyInfoBanner(isDark: Boolean) {
    val bgColor = if (isDark) InfoBlueBgDark else InfoBlueBgLight
    val iconTint = if (isDark) InfoBlueTextDark else InfoBlueTextLight
    val textColor = if (isDark) InfoBlueTextDark else InfoBlueTextLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(id = R.string.habayeb_recurring_daily_desc),
            fontSize = 10.sp,
            color = textColor,
            lineHeight = 13.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeeklyDayPicker(
    selectedDays: Set<Int>,
    daysOfWeek: List<Pair<Int, String>>,
    activeColor: Color,
    onDayToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            daysOfWeek.forEach { (dayInt, name) ->
                val isSelected = selectedDays.contains(dayInt)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onDayToggle(dayInt) }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthlyDayGrid(
    selectedDays: Set<Int>,
    activeColor: Color,
    onDayToggle: (Int) -> Unit
) {
    val daysList = remember { (1..31).toList() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            daysList.forEach { dayNum ->
                val isSelected = selectedDays.contains(dayNum)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant)
                        .clickable { onDayToggle(dayNum) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNum.toString(),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
