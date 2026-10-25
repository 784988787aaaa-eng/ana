package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.CustomDateRangePickerContent
import com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.DateAndTimeSection
import com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.DialogActionButtons
import com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.RangeTab
import java.util.Calendar

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

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.smartledger.aldaftar.ui.components.MizanDialogCard(
                maxWidth = MizanDialogTokens.maxWidth,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = MizanDialogTokens.dialogHorizontal,
                    vertical = MizanDialogTokens.dialogVertical
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll( androidx.compose.foundation.rememberScrollState() ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.smartledger.aldaftar.ui.components.MizanDialogHeader(
                        title = title ?: stringResource(id = R.string.datetime_picker_title),
                        subtitle = com.smartledger.aldaftar.ui.screens.habayeb.components.datetime.DateTimeArabicHelper.formatArabicDateFull(calendarState),
                        icon = androidx.compose.material.icons.Icons.Default.CalendarToday,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onCloseClick = dismissDialog
                    )

                    DateAndTimeSection(
                        calendar = calendarState,
                        onCalendarChange = { calendarState = it },
                        showTime = showTime
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        onClick = {
                            calendarState = Calendar.getInstance()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Today,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 6.dp).height(15.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.datetime_picker_quick_today),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DialogActionButtons(
                        onDismiss = dismissDialog,
                        onConfirm = { onDateTimeSelected(calendarState.timeInMillis) }
                    )
                }
            }
        }
    }
}

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
    CustomDateRangePickerContent(
        initialStartMillis = initialStartMillis,
        initialEndMillis = initialEndMillis,
        initialHour = initialHour,
        initialMinute = initialMinute,
        includeTime = includeTime,
        initialSelectedTab = initialSelectedTab,
        title = title,
        onDismiss = onDismiss,
        onRangeSelected = onRangeSelected
    )
}
