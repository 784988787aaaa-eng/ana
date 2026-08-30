package com.example.ui.screens.habayeb.components.header

/*
 * =====================================================================================
 * شريط البحث المضمن في ترويسة الشاشة (Habayeb Header Search Bar Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * شريط بحث شفاف وشبه دائري (Capsule Pill Shape) مدمج مباشرة في الترويسة الرئيسية.
 *
 * [المزايا والوظائف]:
 * 1. زر إغلاق مع اهتزاز لمسي لإنهاء وضع البحث والعودة للترويسة العادية.
 * 2. حقل إدخال نصي مخصص (BasicTextField) مع نص تلميحي ومحاذاة تدعم اللغة العربية.
 * 3. فتح تلقائي للوحة المفاتيح البرمجية مع طلب التركيز (Auto-focus & Show Keyboard) عند فتح الشريط.
 * 4. مؤشر كتابة بلون أبيض ناصع متناسق مع خلفية الترويسة الملونة.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.android.awaitFrame

/*
 * =====================================================================================
 * دالة شريط بحث الترويسة (HabayebHeaderSearchBar)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - searchQuery: نص الاستعلام المدخل حالياً للبحث.
 * - onSearchQueryChanged: رد نداء عند تغير نص البحث لتحديث الحالة.
 * - onCloseSearch: رد نداء لإغلاق شريط البحث ومسح التصفية.
 * - haptic: محرك الاهتزاز اللمسي لتأكيد النقر على الإجراءات.
 * - modifier: مُعدِّل التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun HabayebHeaderSearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onCloseSearch: () -> Unit,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(46.dp)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(23.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // زر إغلاق شريط البحث
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCloseSearch()
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.habayeb_close_search),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // حقل إدخال نص البحث
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Right
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.habayeb_search_hint),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            }
        )

        // أيقونة البحث التوضيحية
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
    }

    // التركيز التلقائي وفتح لوحة المفاتيح عند ظهور الشريط
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        try {
            awaitFrame()
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

