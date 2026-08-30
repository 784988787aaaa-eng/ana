package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * بطاقة يوم المعاملات في دفتر الأستاذ (Day Card Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون رئيسي متكامل يمثل سجلاً يومياً لجميع المعاملات المالية لدفتر الأستاذ:
 * 1. يعرض شريط الرأس لليوم مع التاريخ وصافي الحركة اليومية (فائض أخضر أو عجز أحمر).
 * 2. يدعم توسيع وطي البطاقة بحركات انسيابية (Expand/Collapse Animations).
 * 3. يحتوي على قائمة الصفوف اليومية للمعاملات (DayCardTransactionRow) مع دعم التحديد المتعدد.
 * 4. يتضمن شريط المقاييس الثلاثية المدمج (الدخل، المصروف، الصافي) وزر مشاركة التقرير عبر واتساب.
 * 5. يدعم وضع تحديد الأيام وتأكيد حذف المعاملات عبر الحوار المخصص.
 * =====================================================================================
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.TransactionDb
import com.example.domain.model.TransactionType
import com.example.ui.theme.SelectionGreen
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import com.example.ui.viewmodel.DayLedger
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * =====================================================================================
 * دالة مساعدة لتحويل الأرقام المشرقية إلى غربية (toWesternDigits)
 * =====================================================================================
 */
private fun String.toWesternDigits(): String {
    var result = this
    val eastern = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    for (i in 0..9) {
        result = result.replace(eastern[i], western[i])
    }
    return result
}

/*
 * =====================================================================================
 * دالة العرض لبطاقة اليوم (DayCard Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - dayLedger: كائن بيانات حركة اليوم وسجل معاملاته.
 * - dayKey: المفتاح التعريفي الفريد لليوم (مثل YYYY-MM-DD).
 * - isDaySelected: راية توضح إذا ما كان اليوم محدداً في وضع تحديد الأيام.
 * - isDaySelectionMode: راية توضح تفعيل وضع تحديد الأيام الجماعي.
 * - isExpanded: راية توضح إذا ما كانت البطاقة مفتوحة لعرض المعاملات.
 * - haptic: مشغل الاهتزاز التفاعلي.
 * - currencySymbol: رمز العملة المعتمد.
 * - formatCurrency: دالة تنسيق المبالغ المالية.
 * - onDeleteTransaction: رد النداء لحذف معاملة محددة.
 * - onEditTransaction: رد النداء لبدء تعديل معاملة.
 * - onDayClick: رد النداء عند النقر على بطاقة اليوم.
 * - onDayLongClick: رد النداء عند النقر المطول على بطاقة اليوم.
 * - isSelectionMode: راية تفعيل وضع تحديد المعاملات المفردة.
 * - selectedTxIds: قائمة المعاملات المحددة حالياً.
 * - onTransactionSelectToggle: رد النداء لتبديل حالة تحديد معاملة.
 * =====================================================================================
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCard(
    dayLedger: DayLedger,
    dayKey: String,
    isDaySelected: Boolean,
    isDaySelectionMode: Boolean,
    isExpanded: Boolean,
    haptic: HapticFeedback,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: (TransactionDb) -> Unit,
    onDayClick: (String) -> Unit,
    onDayLongClick: (String) -> Unit,
    isSelectionMode: Boolean = false,
    selectedTxIds: List<String> = emptyList(),
    onTransactionSelectToggle: (String) -> Unit = {}
) {
    var txIdToDelete by remember { mutableStateOf<String?>(null) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // تنسيق ترويسة التاريخ: [اسم اليوم] [اليوم/الشهر] (مثال: الأربعاء 19/08)
    val formattedDateHeader = remember(dayLedger) {
        val tx = dayLedger.transactions.firstOrNull()
        if (tx != null) {
            val ms = if (tx.timestamp in 1..999999999999L) tx.timestamp * 1000L else tx.timestamp
            try {
                val sdf = SimpleDateFormat("EEEE dd/MM", Locale.forLanguageTag("ar"))
                sdf.format(Date(ms)).toWesternDigits()
            } catch (e: Exception) {
                "${dayLedger.dayOfWeek} ${String.format(Locale.ENGLISH, "%02d", dayLedger.dayNumber)}"
            }
        } else {
            "${dayLedger.dayOfWeek} ${String.format(Locale.ENGLISH, "%02d", dayLedger.dayNumber)}"
        }
    }

    val formattedNetAmount = remember(dayLedger.netAmount, currencySymbol) {
        val sign = if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) > 0) "▲ +"
        else if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) < 0) "▼ "
        else ""
        sign + formatCurrency(dayLedger.netAmount, currencySymbol).toWesternDigits()
    }

    val netHeaderColor = if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) > 0) {
        financialCreditColor(isDark)
    } else if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) < 0) {
        financialDebtColor(isDark)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDaySelected) {
                if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isDaySelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.5.dp)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDayClick(dayKey)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDayLongClick(dayKey)
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. ترويسة بطاقة اليوم المدمجة
            DayCardHeader(
                formattedDateHeader = formattedDateHeader,
                formattedNetAmount = formattedNetAmount,
                netHeaderColor = netHeaderColor,
                isExpanded = isExpanded,
                isDaySelected = isDaySelected,
                isDaySelectionMode = isDaySelectionMode
            )

            // 2. المحتوى القابل للتوسيع: قائمة المعاملات، ملخص المقاييس الثلاثية، وزر مشاركة واتساب
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )

                    val sortedTxs = remember(dayLedger.transactions) {
                        dayLedger.transactions.sortedByDescending { it.timestamp }
                    }

                    if (sortedTxs.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.ledger_no_txs_today),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // صفوف المعاملات المصغرة والأنيقة
                        sortedTxs.forEachIndexed { index, tx ->
                            val isSelected = selectedTxIds.contains(tx.id)

                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
                                    thickness = 0.4.dp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }

                            DayCardTransactionRow(
                                tx = tx,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                isDark = isDark,
                                currencySymbol = currencySymbol,
                                formatCurrency = formatCurrency,
                                haptic = haptic,
                                onEditTransaction = onEditTransaction,
                                onDeleteRequest = { id -> txIdToDelete = id },
                                onTransactionSelectToggle = onTransactionSelectToggle
                            )
                        }

                        // العمليات الحسابية: إجماليات اليوم
                        val (dailyIncome, dailyExpense) = remember(dayLedger.transactions) {
                            var inc = BigDecimal.ZERO
                            var exp = BigDecimal.ZERO
                            for (t in dayLedger.transactions) {
                                if (t.type == TransactionType.INCOME.value) {
                                    inc = inc.add(t.amount)
                                } else if (t.type == TransactionType.EXPENSE.value) {
                                    exp = exp.add(t.amount)
                                }
                            }
                            Pair(inc, exp)
                        }
                        val dailyNet = remember(dailyIncome, dailyExpense) {
                            dailyIncome.subtract(dailyExpense)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // 3. شريط المقاييس الثلاثية المصغر (دخل، صرف، صافي)
                            DayCardSummaryBar(
                                dailyIncome = dailyIncome,
                                dailyExpense = dailyExpense,
                                dailyNet = dailyNet,
                                isDark = isDark,
                                currencySymbol = currencySymbol,
                                formatCurrency = formatCurrency
                            )

                            // 4. زر مشاركة التقرير اليومي عبر واتساب
                            DayCardWhatsAppShareButton(
                                dayLedger = dayLedger,
                                dailyIncome = dailyIncome,
                                dailyExpense = dailyExpense,
                                currencySymbol = currencySymbol,
                                formatCurrency = formatCurrency
                            )
                        }
                    }
                }
            }
        }
    }

    val currentTxToDelete = txIdToDelete
    if (currentTxToDelete != null) {
        DayCardDeleteDialog(
            txId = currentTxToDelete,
            onConfirm = { id ->
                onDeleteTransaction(id)
                txIdToDelete = null
            },
            onDismiss = { txIdToDelete = null }
        )
    }
}

