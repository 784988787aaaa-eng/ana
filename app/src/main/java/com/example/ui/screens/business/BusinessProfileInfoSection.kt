package com.example.ui.screens.business

/*
 * =====================================================================================
 * حزمة قسم البيانات الأساسية للملف التجاري (Business Profile Info Section Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المكون البصري الخاص بإدخال اسم المنشأة والوصف التعريفي للنشاط:
 * - حقول نصوص محددة الطول (Character Limit Constraints).
 * - نقل التركيز التلقائي بين الحقول باستخدام لوحة المفاتيح.
 * - دعم العرض داخل شاشة مستقلة أو داخل حوار منبثق (Dialog Mode).
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/*
 * ثوابت معمارية تحدد حدود إدخال النصوص لقسم الملف التعريفي للنشاط التجاري.
 * تضمن اتساق سياسة التحقق من صحة الإدخال ومطابقتها لواجهة العرض.
 */
private const val MAX_BIZ_NAME_LENGTH = 40
private const val MAX_BIZ_DESC_LENGTH = 45

/*
 * =====================================================================================
 * قسم البيانات والمعلومات الأساسية للمنشأة (BusinessProfileInfoSection)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة إدخال مخصصة لاسم المتجر أو المؤسسة والوصف الترويجي القصير:
 * 1. حقل إدخال اسم النشاط التجاري مع عداد الحروف والحد الأقصى (40 حرف).
 * 2. حقل إدخال الوصف الترويجي مع عداد الحروف والحد الأقصى (45 حرف).
 * 3. تنسيق بصري يتكيف مع سِمة الألوان النشطة ويدعم الكتابة باللغة العربية (RTL).
 *
 * [المُدخلات]:
 * - bizName: القيمة الحالية لاسم النشاط التجاري.
 * - onBizNameChange: رد نداء عند تعديل اسم النشاط.
 * - bizDesc: القيمة الحالية لوصف النشاط التجاري.
 * - onBizDescChange: رد نداء عند تعديل وصف النشاط.
 * - isDialog: هل يتم العرض داخل نافذة حوار منبثقة (لضبط الارتفاع والظلال).
 * - activeThemeColor: لون السِمة المخصص النشط لتلوين الحواف والمؤشر.
 * =====================================================================================
 */
@Composable
fun BusinessProfileInfoSection(
    bizName: String,
    onBizNameChange: (String) -> Unit,
    bizDesc: String,
    onBizDescChange: (String) -> Unit,
    isDialog: Boolean,
    activeThemeColor: Color
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDialog) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.biz_details_section),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // حقل إدخال اسم المنشأة / النشاط التجاري
            OutlinedTextField(
                value = bizName,
                onValueChange = { if (it.length <= MAX_BIZ_NAME_LENGTH) onBizNameChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_name), fontSize = 13.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_name), fontSize = 13.sp) },
                supportingText = {
                    Text(
                        text = "${bizName.length}/$MAX_BIZ_NAME_LENGTH",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
            )

            // حقل إدخال وصف النشاط أو مجال العمل
            OutlinedTextField(
                value = bizDesc,
                onValueChange = { if (it.length <= MAX_BIZ_DESC_LENGTH) onBizDescChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_desc), fontSize = 13.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_desc), fontSize = 13.sp) },
                supportingText = {
                    Text(
                        text = "${bizDesc.length}/$MAX_BIZ_DESC_LENGTH",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                },
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
            )
        }
    }
}

