/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/screens/trash/components/TrashItemCard.kt
 * المسؤولية: مكوّن عرض لقائمة أو بطاقة عنصر محذوف مع إبراز المعلومات الأساسية وإجراءات المستخدم.
 *
 * القراءة التعليمية: يبدأ هذا الملف بالعقد الحزمي والاستيرادات، ثم الأنواع/الحالة،
 * ثم نقاط Compose والتوابع ومسارات العرض والتفاعل. التعليقات المضافة تشرح وظيفة
 * العناصر الأصلية، بينما الكتلة التنفيذية نفسها محفوظة حرفياً دون إعادة صياغة.
 * الرؤية البصرية: كل قيمة حالة تقود في النهاية إلى بطاقة أو صف أو حوار أو طبقة
 * على شاشة الهاتف؛ لذا يُقرأ الملف كمسار من «بيانات المحذوف» إلى «المشهد المرئي».
 * قاعدة السلامة: Zero Code Alteration — لا تعديل على أي رمز تنفيذي أصلي.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي: يحدد المسار المنطقي لهذا الملف داخل بنية التطبيق.
// توثيق السطر 3: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 13: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 15: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 18: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 20: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 21: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 22: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 23: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 24: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 25: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 26: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 27: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 28: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 29: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 30: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 31: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 32: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 33: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 34: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 35: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 36: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 37: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 38: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 39: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 40: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 41: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 42: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 43: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 44: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 45: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 46: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 47: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 48: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 49: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 50: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 51: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 52: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 53: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 54: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 55: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 56: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 57: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 59: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 60: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 61: الدالة التالية هي نقطة تنفيذ أصلية؛ تستقبل مدخلاتها وتنفذ مسؤوليتها كما في المصدر دون تعديل.
// توثيق السطر 73: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 75: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 76: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 77: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 78: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 79: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 81: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 82: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 84: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 89: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 90: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 94: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 95: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 96: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 97: كتلة التفرع التالية توزع السلوك وفق الحالة/القيمة الأصلية.
// توثيق السطر 100: الفرع البديل التالي جزء من مسار التنفيذ الأصلي.
// توثيق السطر 104: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 116: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 119: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 155: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 225: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 248: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 249: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 250: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 251: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 282: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 283: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 284: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 296: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 306: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 357: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.

package com.example.ui.screens.trash.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.DeletedItemEntity
import com.example.ui.helper.getInitialColor
import com.example.ui.screens.trash.utils.ParsedTrashData
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebtRed
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashItemCard(
    item: DeletedItemEntity,
    parsedData: ParsedTrashData,
    isSelected: Boolean,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onOpenCustomerOverlay: () -> Unit = {},
    onOpenTransactionDetail: () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)

    val isBundleOrCustomer = item.originalTableName == "habayeb_bundle" || item.originalTableName == "habayeb_customers"
    val isTransaction = item.originalTableName == "habayeb_transactions"

    val avatarKey = if (isTransaction && parsedData.customerName.isNotEmpty()) {
        parsedData.customerName
    } else {
        parsedData.titleText
    }
    val avatarColor = remember(avatarKey) { getInitialColor(avatarKey) }
    val firstLetter = remember(avatarKey) {
        avatarKey.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }

    val transactionLabel = stringResource(R.string.trash_type_transaction)
    val accountLabel = stringResource(R.string.trash_type_account)
    val typeLabel = remember(item.originalTableName, transactionLabel, accountLabel) {
        when (item.originalTableName) {
            "habayeb_transactions" -> transactionLabel
            "habayeb_customers", "habayeb_bundle" -> accountLabel
            else -> item.originalTableName
        }
    }

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, primaryColor)
    } else {
        BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onClick()
                    } else {
                        if (isBundleOrCustomer) {
                            onOpenCustomerOverlay()
                        } else if (isTransaction) {
                            onOpenTransactionDetail()
                        } else {
                            onClick()
                        }
                    }
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                primaryColor.copy(alpha = if (isDark) 0.2f else 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Start: Avatar & Text Content
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar Circle or Selection Checkmark
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarColor.copy(alpha = 0.12f))
                            .border(1.dp, avatarColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstLetter,
                            color = avatarColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Middle Text Hierarchy: Title, Customer/Account Line, Date & Foreign Metadata
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Line 1: Title & Type Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = parsedData.titleText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Line 2: Full Context / Associated Account Subtext (Clean & Unsquished)
                    if (parsedData.subText.isNotEmpty()) {
                        Text(
                            text = parsedData.subText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Line 3: Date & Foreign Currency Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = parsedData.parsedDate,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1
                        )

                        if (parsedData.isForeign) {
                            val badgeColor = if (parsedData.isRateCalculated) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            val badgeBg = if (parsedData.isRateCalculated) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                            val badgeText = if (parsedData.isRateCalculated) {
                                stringResource(R.string.trash_exchange_active, parsedData.exchangeRateVal)
                            } else {
                                stringResource(R.string.trash_exchange_inactive)
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // End: Financial Amount & Clean Frameless Icon Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Financial Amount with directional arrow
                if (parsedData.amountText.isNotEmpty()) {
                    val amountColor = if (parsedData.isExpense) debtColor else creditColor
                    val arrow = if (parsedData.isExpense) "↗️" else "↙️"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = parsedData.amountText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = amountColor
                        )
                        if (isTransaction) {
                            Text(
                                text = arrow,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Actions: Sleek, Frameless, Clean Icon Buttons (Zero Bloat, Zero Clunky Frames)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else if (!isSelectionMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frameless Clean Restore Icon Button
                        IconButton(
                            onClick = onRestore,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestoreFromTrash,
                                contentDescription = stringResource(id = R.string.trash_action_restore_btn),
                                tint = CreditGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Frameless Clean Delete Permanently Icon Button
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = stringResource(id = R.string.trash_delete_permanently),
                                tint = DebtRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        onPermanentDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.trash_delete_permanently),
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = stringResource(id = R.string.trash_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.trash_delete_warning_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.trash_delete_warning_desc),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على التنفيذ الأصلي دون تعديل، والحفاظ على أسماء الأنواع والدوال والمتغيرات والعقود.
// 2) ينبغي مستقبلاً تقييم فصل parsing عن presentation عندما تتوسع أنواع عناصر سلة المحذوفات، مع عدم تغيير النسخة الحالية.
// 3) يوصى بمراقبة كلفة إعادة التحليل والفرز على القوائم الكبيرة، خصوصاً عند استخدام Compose وDispatchers.Default.
// 4) أي تحسين لاحق يجب أن يمر باختبارات تكافؤ سلوكي قبل اعتماده، لأن سلة المحذوفات جزء حساس من استرجاع البيانات.
// 5) هذه الملاحظات توصيات مستقبلية فقط ولا تمثل تعديلاً على الشيفرة الحالية.
