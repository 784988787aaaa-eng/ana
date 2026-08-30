package com.example.ui.screens.habayeb.components.datetime

/*
 * =====================================================================================
 * أزرار التحكم والإجراءات لنوافذ الحوار (Dialog Action Buttons Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون واجهة رسومي موحد يوفر زري الإجراء القياسيين (إلغاء / Cancel) و (تأكيد / Confirm)
 * لنوافذ حوار اختيار التواريخ والأوقات، مع توزيع متناسق للمساحات وتصميم عصري بحواف مستديرة.
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/*
 * =====================================================================================
 * دالة أزرار إجراءات الحوار (DialogActionButtons)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - onDismiss: رد نداء يُستدعى عند النقر على زر الإلغاء لإغلاق الحوار دون تطبيق التغييرات.
 * - onConfirm: رد نداء يُستدعى عند النقر على زر التأكيد لاعتماد القيم المختارة.
 * =====================================================================================
 */
@Composable
fun DialogActionButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // زر الإلغاء المحاط بإطار (Outlined Button)
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.common_cancel),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // زر التأكيد الممتلئ (Filled Primary Button)
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.datetime_picker_confirm),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

