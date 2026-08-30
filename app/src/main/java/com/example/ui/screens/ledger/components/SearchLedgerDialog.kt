/**
 * =====================================================================
 * ملف: SearchLedgerDialog.kt
 * الحزمة: com.example.ui.screens.ledger.components
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الملف نافذة الحوار المخصصة للبحث المتقدم في دفتر اليومية المالي العام.
 * تتيح هذه الواجهة للمستخدم كتابة نصوص أو كلمات مفتاحية للبحث الفوري في سجل
 * العمليات المالية السابقة (الإيرادات والمصروفات)، وعرض النتائج المطابقة بشكل حي
 * مع تفاصيل التاريخ، الوقت، المبالغ المنسقة، والفواصل الزمنية بين العمليات.
 * 
 * [تدفق البيانات وتكامل الواجهة]:
 * - تستقبل النافذة نص البحث الحالي وقائمة النتائج المُصفاة مباشرة من الـ ViewModel.
 * - تقوم بالتركيز التلقائي على حقل البحث وإظهار لوحة المفاتيح فور فتح النافذة لتحسين تجربة المستخدم.
 * - تعرض النتائج بتنسيق مالي أنيق وتمايز بصري بين الإيرادات (أخضر) والمصروفات (أحمر).
 * =====================================================================
 */
package com.example.ui.screens.ledger.components

// ---------------------------------------------------------------------
// استيراد الأدوات البرمجية ومكونات التصميم
// ---------------------------------------------------------------------
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.R
import com.example.data.local.entities.TransactionDb
import com.example.domain.DateUtils
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import java.math.BigDecimal

/**
 * وسم تسجيل السجلات التشخيصية لتتبع أحداث نافذة البحث ومعالجة الأخطاء.
 */
private const val TAG = "SearchLedgerDialog"

/**
 * =====================================================================
 * [واجهة حوار البحث في الدفتر - SearchLedgerDialog]:
 * 
 * [الهدف والغرض]:
 * نافذة حوار منبثقة متكاملة لإجراء عمليات البحث السريع والتفاعلي في قيود الدفتر اليومي.
 * 
 * [البيانات المستلمة]:
 * @param query نص البحث المدخل حالياً بواسطة المستخدم.
 * @param onQueryChange دالة الاستدعاء الارتجاعي التي تُخطر الـ ViewModel بكل حرف جديد لتحديث النتائج.
 * @param results قائمة العمليات المالية المطابقة للبحث المسترجعة من قاعدة البيانات.
 * @param formatCurrency دالة تنسيق العملة المعتمدة لتحويل المبالغ إلى نصوص مقروءة مع رمز العملة.
 * @param onDismiss دالة إغلاق نافذة الحوار عند الضغط خارجها أو على زر الإغلاق.
 * =====================================================================
 */
@Composable
fun SearchLedgerDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<TransactionDb>,
    formatCurrency: (BigDecimal) -> String,
    onDismiss: () -> Unit
) {
    // -----------------------------------------------------------------
    // إنشاء حاوية الحوار الرئيسية وتخصيص إعدادات النوافذ
    // -----------------------------------------------------------------
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // إدارة التركيز على حقل الإدخال والتحكم التلقائي في لوحة المفاتيح
        val searchFocusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val view = LocalView.current

        // ضبط سلوك إظهار لوحة المفاتيح مع نافذة الحوار لتفادي إخفائها المفاجئ
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            onDispose {}
        }

        // توجيه التركيز البرمجي لحقل الإدخال بعد فتح الحوار بفارق زمني بسيط لضمان اكتمال بناء الواجهة
        LaunchedEffect(Unit) {
            try {
                kotlinx.coroutines.delay(150)
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to request focus or show keyboard: ${e.message}")
            }
        }

        // بطاقة الحوار ذات الحواف المنحنية والألوان المتناسقة مع ثيم التطبيق
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // -------------------------------------------------------------
                // شريط العنوان العلوي وزر الإغلاق
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.habayeb_close_search))
                    }
                    Text(
                        stringResource(id = R.string.ledger_search_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // -------------------------------------------------------------
                // حقل إدخال نص البحث مع أيقونة البحث وتنسيق اتجاه النص العربي
                // -------------------------------------------------------------
                val subColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(id = R.string.ledger_search_subtitle), color = subColor) },
                    modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = subColor) },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Right),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedPlaceholderColor = subColor,
                        unfocusedPlaceholderColor = subColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // -------------------------------------------------------------
                // منطقة عرض النتائج: إما شاشة فارغة توجيهية أو قائمة العناصر المطابقة
                // -------------------------------------------------------------
                if (results.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            if (query.isBlank()) stringResource(id = R.string.ledger_search_empty_state) else stringResource(id = R.string.ledger_search_no_results),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // عرض عداد النتائج المطابقة المكتشفة
                    Text(
                        stringResource(id = R.string.ledger_search_results_count, results.size),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // قائمة النتائج القابلة للتمرير مع ربط المفاتيح الفريدة لكل عنصر
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(items = results, key = { _, tx -> tx.id }) { index, tx ->
                            SearchResultItem(
                                tx = tx,
                                nextTx = if (index < results.size - 1) results[index + 1] else null,
                                formatCurrency = formatCurrency
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * =====================================================================
 * [عنصر نتيجة البحث - SearchResultItem]:
 * 
 * [الهدف والغرض]:
 * يمثل بطاقة العرض الفردية لكل حركة مالية مطابقة للبحث، مع إظهار المبلغ،
 * نوع الحركة (إيراد/مصروف)، بيان الحركة، والتوقيت، بالإضافة إلى الفاصل الزمني
 * بين هذه العملية والعملية السابقة لها في الترتيب إن وُجدت.
 * 
 * [البيانات المستلمة]:
 * @param tx كائن العملية المالية الحالية المعروضة.
 * @param nextTx كائن العملية المالية التالية في القائمة لحساب الفارق الزمني بينهما.
 * @param formatCurrency دالة تنسيق المبالغ المالية.
 * =====================================================================
 */
@Composable
fun SearchResultItem(
    tx: TransactionDb,
    nextTx: TransactionDb?,
    formatCurrency: (BigDecimal) -> String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val incomeColor = financialCreditColor(isDark)
    val expenseColor = financialDebtColor(isDark)

    val context = LocalContext.current
    
    // حساب التنسيقات النصية للتاريخ والوقت والمبالغ مرة واحدة بالذاكرة لكل قيد
    val dayName = remember(tx.timestamp) { DateUtils.getDayOfWeekArabic(tx.timestamp) }
    val fullDate = remember(tx.timestamp) { DateUtils.formatDateFull(tx.timestamp) }
    val timeStr = remember(tx.timestamp) { DateUtils.formatTime24Or12(tx.timestamp) }
    val formattedAmount = remember(tx.amount, formatCurrency) { formatCurrency(tx.amount) }
    val interval = remember(tx.timestamp, nextTx?.timestamp) {
        if (nextTx != null) DateUtils.formatDurationBetween(tx.timestamp, nextTx.timestamp, context) else ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // بطاقة تفاصيل الحركة المالية
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // الجانب الأيسر: المبلغ المنسق مع تمييز اللون (أخضر للإيراد / أحمر للمصروف) والوقت
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = formattedAmount,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.type == "INCOME") incomeColor else expenseColor,
                        fontSize = 13.sp
                    )
                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // الجانب الأيمن: وصف الحركة واسم اليوم مع التاريخ الميلادي/الهجري الكامل
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tx.description.ifBlank { if (tx.type == "INCOME") stringResource(id = R.string.ledger_category_overall_income) else stringResource(id = R.string.ledger_category_expense) },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "$dayName - $fullDate",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // مؤشر الفاصل الزمني الفاصل بين عمليتين متعاقبتين
        // -------------------------------------------------------------
        if (nextTx != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interval,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }
        }
    }
}

