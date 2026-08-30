package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * خط فاصل الانتقال بين الشهور (Month Transition Line Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * عنصر بصري فاصل يوضع بين الشهور المتتالية في دفتر الأستاذ:
 * 1. يرسم خطاً منقطاً أنيقاً باستخدام Canvas و DashPathEffect باللون الأخضر المالي.
 * 2. يضع في المنتصف شارة نصية مع خلفية معتمة تبرز نص "بداية شهر جديد" لتوضيح التسلسل الزمني.
 * =====================================================================================
 */

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary

@Composable
fun MonthTransitionLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        val labelColor = MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier.fillMaxWidth(0.8f).matchParentSize()
        ) {
            drawLine(
                color = lineColor,
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                pathEffect = pathEffect,
                strokeWidth = 2.dp.toPx()
            )
        }
        Text(
            text = stringResource(id = R.string.ledger_beginning_new_month),
            color = labelColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp)
        )
    }
}
