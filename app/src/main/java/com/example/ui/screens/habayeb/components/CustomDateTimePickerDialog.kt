package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة نوافذ اختيار التاريخ والوقت المخصصة (Custom Date & Time Picker Dialogs Package)
 * -------------------------------------------------------------------------------------
 * توفر هذه الفئة واجهات حوارية تفاعلية لاختيار التواريخ والأوقات بالتقويمين الهجري والميلادي:
 * 1. نافذة اختيار تاريخ ووقت مفرد (CustomDateTimePickerDialog) للمعاملات والحسابات.
 * 2. نافذة اختيار نطاق زمني وفترة محددة (CustomDateRangePickerDialog) لتقارير الفترات والجدولة.
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.R
import com.example.ui.screens.habayeb.components.datetime.CustomDateRangePickerContent
import com.example.ui.screens.habayeb.components.datetime.DateAndTimeSection
import com.example.ui.screens.habayeb.components.datetime.DialogActionButtons
import com.example.ui.screens.habayeb.components.datetime.RangeTab
import java.util.Calendar

// إعادة تصدير تعداد RangeTab لضمان توافق واستقرار الاستدعاءات عبر الشاشات
typealias RangeTab = RangeTab

/*
 * =====================================================================================
 * نافذة اختيار تاريخ ووقت مفرد (CustomDateTimePickerDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوار موحدة تتيح اختيار تاريخ محدد مع إمكانية تفعيل أو تعطيل اختيار الوقت (الساعة/الدقيقة).
 *
 * [المُدخلات]:
 * - initialMillis: التوقيت المبدئي بالمللي ثانية.
 * - onDismiss: رد نداء عند إغلاق أو إلغاء النافذة.
 * - onDateTimeSelected: رد نداء عند تأكيد التوقيت المختار.
 * - showTime: مؤشر إظهار أو إخفاء قسم اختيار الوقت.
 * - title: عنوان مخصص للنافذة (اختياري).
 * =====================================================================================
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // عنوان النافذة (Header Title)
                    Text(
                        text = title ?: stringResource(id = R.string.datetime_picker_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // قسم التاريخ والوقت المفرد (Single Date & Time section)
                    DateAndTimeSection(
                        calendar = calendarState,
                        onCalendarChange = { calendarState = it },
                        showTime = showTime
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // أزرار العمليات (إلغاء وموافق)
                    DialogActionButtons(
                        onDismiss = onDismiss,
                        onConfirm = { onDateTimeSelected(calendarState.timeInMillis) }
                    )
                }
            }
        }
    }
}

/*
 * =====================================================================================
 * نافذة اختيار النطاق الزمني والتاريخ الذكي (CustomDateRangePickerDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة مخصصة لاختيار فترات زمنية (من تاريخ - إلى تاريخ) مع وقت التنفيذ، وتستخدم في الجدولة والتقارير.
 *
 * [المُدخلات]:
 * - initialStartMillis: بداية النطاق بالمللي ثانية.
 * - initialEndMillis: نهاية النطاق بالمللي ثانية.
 * - initialHour / initialMinute: ساعة ودقيقة البدء.
 * - includeTime: هل يتضمن النطاق تحديد الوقت بدقة.
 * - initialSelectedTab: التبويب المحدد مبدئياً (بداية/نهاية).
 * - title: عنوان مخصص للنافذة.
 * - onDismiss: رد نداء عند الإلغاء.
 * - onRangeSelected: رد نداء تأكيد النطاق والوقت المختار.
 * =====================================================================================
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

