package com.example.ui.screens.habayeb.components.row

/*
 * =====================================================================================
 * أقسام ومكونات صف المعاملة المالية (Transaction Row Sections Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * ملف يحتوي على الأقسام الفرعية الثلاثة التي يتكون منها كل صف معاملة مالية في جدول حبايب:
 *
 * 1. [قسم التاريخ والتسلسل - TransactionRowDateSection]:
 *    - يعرض رقم المعاملة التسلسلي (#Seq) ومؤشر التحديد (Check) عند تفعيل التحديد المتعدد.
 *    - يعرض مؤشر التكرار الجدولي إن كانت المعاملة مصدر تكرار نشط.
 *    - يعرض اسم اليوم والتاريخ بصيغة ملونة ومميزة والوقت بنظام 12 ساعة.
 *
 * 2. [قسم التفاصيل والملاحظات - TransactionRowDetailsSection]:
 *    - يعرض نوع المعاملة (لنا / علينا / دفعة منه / دفعة له) بلون مميز.
 *    - يعرض الملاحظات أو نص بديل عند عدم وجود ملاحظات.
 *    - يعرض الشارات التوضيحية (معاملة تكرارية، معاملة فرعية منشأة آلياً).
 *    - يوفر زر تفاعلي لعرض وتعديل سعر الصرف للعملات الأجنبية المحولة.
 *
 * 3. [قسم المبالغ والعملات - TransactionRowAmountSection]:
 *    - يعرض سهم اتجاه الحركة المالية (صاعد/هابط) مع المبلغ المالي المنسق.
 *    - يستعين بمكون AutoSizeText لضبط حجم الخط تلقائياً عند كبر الأرقام.
 *    - يعرض المبلغ المكافئ بالعملة الأساسية بين قوسين إن كانت المعاملة أجنبية.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.HabayebTransaction
import com.example.ui.screens.habayeb.components.AutoSizeText
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.FinanceConstants

/*
 * =====================================================================================
 * قسم التاريخ ورقم التسلسل (TransactionRowDateSection)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - isSelected: هل الصف محدد حالياً في وضع التحديد المتعدد.
 * - txSeqNo: الرقم التسلسلي للمعاملة ضمن حساب العميل.
 * - hasActiveRecurring: هل المعاملة مرتبطة بجدول تكرار دوري نشط.
 * - activeThemeColor: اللون الأساسي النشط للسمة.
 * - cached: كائن البيانات المنسقة مسبقاً.
 * - isDark: هل الوضع الليلي مفعل.
 * - tx: كائن المعاملة الكامل.
 * - onScheduleClick: رد نداء عند النقر على مؤشر الجدولة.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun TransactionRowDateSection(
    isSelected: Boolean,
    txSeqNo: Int,
    hasActiveRecurring: Boolean,
    activeThemeColor: Color,
    cached: TransactionRowCachedData,
    isDark: Boolean,
    tx: HabayebTransaction,
    onScheduleClick: (HabayebTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // مؤشر التحديد (Check Icon)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(activeThemeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(3.dp))
            }

            // رقم المعاملة التسلسلي
            Text(
                text = "#$txSeqNo",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = activeThemeColor,
                modifier = Modifier
                    .background(activeThemeColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )

            // أيقونة التكرار الدوري القابلة للنقر
            if (hasActiveRecurring) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(activeThemeColor.copy(alpha = 0.12f), CircleShape)
                        .clickable { onScheduleClick(tx) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = stringResource(id = R.string.habayeb_recurring_source),
                        tint = activeThemeColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // نص التاريخ المنسق مع تلوين اسم اليوم
        val dayName = stringResource(id = cached.dayNameResId)
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val annotatedDate = remember(dayName, cached.dateStr, activeThemeColor, onSurfaceColor) {
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = activeThemeColor, fontWeight = FontWeight.Bold)) {
                    append(dayName)
                }
                withStyle(style = SpanStyle(color = activeThemeColor.copy(alpha = 0.5f))) {
                    append(" • ")
                }
                withStyle(style = SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
                    append(cached.dateStr)
                }
            }
        }

        Text(
            text = annotatedDate,
            fontSize = 8.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(0.5.dp))

        // نص الوقت بنظام 12 ساعة
        Text(
            text = cached.timeStr,
            fontSize = 8.sp,
            color = RowColors.mutedGray(isDark),
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/*
 * =====================================================================================
 * قسم تفاصيل المعاملة والشارات (TransactionRowDetailsSection)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - cached: كائن البيانات المنسقة مسبقاً.
 * - isDark: هل الوضع الليلي مفعل.
 * - hasActiveRecurring: هل المعاملة مصدر تكرار دوري.
 * - parentTxSeq: الرقم التسلسلي للمعاملة الرئيسية المرتبطة.
 * - tx: كائن المعاملة الكامل.
 * - currencySymbol: رمز العملة الافتراضية.
 * - onExchangeRateClick: رد نداء عند النقر على زر سعر الصرف.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun TransactionRowDetailsSection(
    cached: TransactionRowCachedData,
    isDark: Boolean,
    hasActiveRecurring: Boolean,
    parentTxSeq: Int?,
    tx: HabayebTransaction,
    currencySymbol: String,
    onExchangeRateClick: (HabayebTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // نص نوع المعاملة
        Text(
            text = stringResource(id = cached.typeResId),
            fontSize = 8.5.sp,
            color = cached.indicatorColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(1.dp))

        // نص الملاحظات أو نص بديل
        Text(
            text = cached.cleanDescription.ifEmpty { stringResource(id = R.string.habayeb_no_notes) },
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // صف الشارات وأزرار التحويل
        Row(
            modifier = Modifier.padding(top = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            // شارة مصدر التكرار الدوري
            if (hasActiveRecurring) {
                Box(
                    modifier = Modifier
                        .background(RowColors.alertGoldBg(isDark), RoundedCornerShape(4.dp))
                        .border(0.5.dp, RowColors.alertGoldBorder(isDark), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_recurring_source),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = RowColors.alertGoldText(isDark)
                    )
                }
            } else if (parentTxSeq != null && parentTxSeq > 0 && !tx.linkedMainTxId.isNullOrBlank() && !tx.linkedMainTxId.equals("null", ignoreCase = true) && tx.linkedMainTxId != tx.id) {
                // شارة المعاملة الفرعية المنشأة آلياً
                Box(
                    modifier = Modifier
                        .background(RowColors.infoBlueBg(isDark), RoundedCornerShape(4.dp))
                        .border(0.5.dp, RowColors.infoBlueBorder(isDark), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_auto_generated_sub, parentTxSeq),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = RowColors.infoBlueText(isDark)
                    )
                }
            }

            // زر وحالة سعر الصرف للعملات الأجنبية
            val targetCurrency = currencySymbol
            val isSelfConversion = (cached.displayCurrency == targetCurrency)
            val showToggle = !isSelfConversion && ((tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) || cached.isTxForeign || cached.isCalculated)
            if (showToggle) {
                val isCalculated = cached.isCalculated
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isCalculated) RowColors.successGreenBg(isDark)
                            else if (isDark) Slate800.copy(alpha = 0.5f) else Slate100
                        )
                        .border(
                            0.5.dp,
                            if (isCalculated) RowColors.successGreenBorder(isDark)
                            else if (isDark) Slate600.copy(alpha = 0.6f) else Slate300,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onExchangeRateClick(tx) }
                        .padding(horizontal = 5.dp, vertical = 1.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCalculated) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCalculated) RowColors.successGreenBorder(isDark) else RowColors.warningRedBorder(isDark).copy(alpha = 0.6f),
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isCalculated) {
                            stringResource(id = R.string.habayeb_rate_active, com.example.ui.helper.HabayebMathHelper.formatRate(tx.exchangeRate))
                        } else {
                            stringResource(id = R.string.habayeb_rate_inactive_clean)
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = if (isCalculated) RowColors.successGreenBorder(isDark) else RowColors.darkGray(isDark).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/*
 * =====================================================================================
 * قسم المبلغ المالي والعملات (TransactionRowAmountSection)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - cached: كائن البيانات المنسقة مسبقاً بما فيه السهم والمبلغ والعملة والمبلغ المكافئ.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun TransactionRowAmountSection(
    cached: TransactionRowCachedData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // سهم اتجاه الحركة المالية
            Icon(
                imageVector = cached.txArrow,
                contentDescription = null,
                tint = cached.indicatorColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // نص المبلغ والعملة مع تكيف الحجم التلقائي
            AutoSizeText(
                text = "${cached.formattedAmount} ${cached.displayCurrency}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = cached.indicatorColor
            )
        }
        // نص المبلغ المكافئ بالعملة الأساسية إن وجد
        if (cached.equivalentAmountText != null) {
            AutoSizeText(
                text = cached.equivalentAmountText,
                fontSize = 9.sp,
                color = cached.indicatorColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

