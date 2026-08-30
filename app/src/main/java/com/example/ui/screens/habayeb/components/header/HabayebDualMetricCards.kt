package com.example.ui.screens.habayeb.components.header

/*
 * =====================================================================================
 * بطاقات المقاييس المالية المزدوجة (Habayeb Dual Metric Cards Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون واجهة رسومي يعرض بطاقتين تفاعليتين في ترويسة الشاشة:
 * 1. بطاقة "لنا" (الديون المستحقة لنا على الآخرين / Owed By Them) باللون الأحمر ومؤشرات التصفية.
 * 2. بطاقة "علينا" (الالتزامات المستحقة للآخرين علينا / Owed To Them) باللون الأخضر ومؤشرات التصفية.
 *
 * [الخصائص والمزايا]:
 * - تعمل كل بطاقة كزر تصفية سريع (Filter Tab) لتبديل عرض السجلات حسب النوع (الكل = 0، لنا = 1، علينا = 2).
 * - تكيف ذكي مع ألوان السمة (الوضع الليلي والنهاري) وتغيير لون الخلفية والحدود عند التحديد.
 * - دعم تغيير حجم النص تلقائياً (AutoScaleText) ليناسب المبالغ الكبيرة دون اقتطاع أو تشويه بصري.
 * - ردود فعل لمسية (Haptic Feedback) وتأثير تموج (Ripple) ملون عند النقر.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.helper.AutoScaleText
import com.example.ui.theme.*

/*
 * =====================================================================================
 * دالة بطاقات المقاييس المزدوجة (HabayebDualMetricCards)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - selectedFilterTab: التبويب المحدد حالياً (0 = الكل، 1 = لنا، 2 = علينا).
 * - onFilterTabSelected: رد نداء عند النقر على إحدى البطاقات لتغيير حالة التصفية.
 * - formattedOwedByThem: إجمالي المبالغ المستحقة لنا بصيغة نصية منسقة مع العملة.
 * - formattedOwedToThem: إجمالي المبالغ المستحقة علينا بصيغة نصية منسقة مع العملة.
 * - isDark: مفتاح منطقي لتحديد ما إذا كان الوضع الليلي مفعلاً.
 * - greenColor: اللون الأخضر النشط لعناصر "علينا".
 * - redColor: اللون الأحمر النشط لعناصر "لنا".
 * - haptic: محرك الاهتزاز اللمسي لتأكيد النقر.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun HabayebDualMetricCards(
    selectedFilterTab: Int,
    onFilterTabSelected: (Int) -> Unit,
    formattedOwedByThem: String,
    formattedOwedToThem: String,
    isDark: Boolean,
    greenColor: Color,
    redColor: Color,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // بطاقة اليمين: "لنا" (Red Color - مبالغ مستحقة لنا)
        val isOwedBySelected = selectedFilterTab == 1
        val owedByCardBg = if (isDark) {
            if (isOwedBySelected) ChipRedBgDarkSelected else DebtContainerDark
        } else {
            if (isOwedBySelected) ChipRedBgLightSelected else DebtContainerLight
        }
        val owedByBorderColor = if (isDark) {
            if (isOwedBySelected) redColor else DebtBorderDark
        } else {
            if (isOwedBySelected) redColor else DebtBorderLight
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = owedByCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isOwedBySelected) 3.dp else 1.dp),
            border = BorderStroke(
                width = if (isOwedBySelected) 1.5.dp else 1.dp,
                color = owedByBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = redColor)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilterTabSelected(if (isOwedBySelected) 0 else 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_filter_owed_by),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = redColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AutoScaleText(
                        text = formattedOwedByThem,
                        baseFontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = redColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // بطاقة اليسار: "علينا" (Emerald Green Color - مبالغ مستحقة علينا)
        val isOwedToSelected = selectedFilterTab == 2
        val owedToCardBg = if (isDark) {
            if (isOwedToSelected) ChipGreenBgDarkSelected else CreditContainerDark
        } else {
            if (isOwedToSelected) ChipGreenBgLightSelected else CreditContainerLight
        }
        val owedToBorderColor = if (isDark) {
            if (isOwedToSelected) greenColor else CreditBorderDark
        } else {
            if (isOwedToSelected) greenColor else CreditBorderLight
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = owedToCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isOwedToSelected) 3.dp else 1.dp),
            border = BorderStroke(
                width = if (isOwedToSelected) 1.5.dp else 1.dp,
                color = owedToBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = greenColor)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilterTabSelected(if (isOwedToSelected) 0 else 2)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_filter_owed_to),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AutoScaleText(
                        text = formattedOwedToThem,
                        baseFontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

