package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة نافذة تعيين التصنيف الجماعي للعملاء (Habayeb Bulk Assign Dialog Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على نافذة حوارية تتيح تعيين تصنيف مخصص لمجموعة من العملاء المحددين:
 * 1. شريط أفقي قابل للتمرير يحتوي على رقائق التصنيفات المخصصة (FilterChips).
 * 2. خيار إلغاء التصنيف ("بدون تصنيف").
 * 3. تطبيق التصنيف على الفور وإغلاق النافذة.
 * =====================================================================================
 */

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CustomCategory

/*
 * =====================================================================================
 * نافذة تعيين التصنيف الجماعي (HabayebBulkAssignDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية تتيح اختيار تصنيف لتطبيقه على جميع الحسابات المحددة حالياً.
 *
 * [المُدخلات]:
 * - customCategories: قائمة التصنيفات المخصصة المتاحة في التطبيق.
 * - currentSelectedCategory: اسم التصنيف المحدد حالياً (إن وجد).
 * - onDismiss: رد نداء عند إلغاء أو إغلاق الحوار.
 * - onAssign: رد نداء عند اختيار وتعيين تصنيف (يمرر اسم التصنيف أو null لإزالة التصنيف).
 * =====================================================================================
 */
@Composable
fun HabayebBulkAssignDialog(
    customCategories: List<CustomCategory>,
    currentSelectedCategory: String? = null,
    onDismiss: () -> Unit,
    onAssign: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.habayeb_bulk_assign_category_title), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.habayeb_bulk_assign_category_desc), fontSize = 12.sp)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (currentSelectedCategory == null),
                            onClick = { onAssign(null) },
                            label = { Text(stringResource(R.string.habayeb_bulk_assign_no_category), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        customCategories.forEach { category ->
                            FilterChip(
                                selected = (currentSelectedCategory == category.name),
                                onClick = { onAssign(category.name) },
                                label = { Text(category.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.habayeb_bulk_assign_cancel), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

