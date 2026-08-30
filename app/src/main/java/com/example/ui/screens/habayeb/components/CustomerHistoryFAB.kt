package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة زر الإجراء العائم لسجل الحركات (Customer History FAB Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على زر الإجراء العائم (Floating Action Button) المخصص لإضافة حركة مالية جديدة:
 * - ظهور واختفاء ناعم عبر الرسوم المتحركة (AnimatedVisibility).
 * - حساب ذكي للمسافات السفلية متوافق مع شريط التنقل وحواف الشاشة.
 * - ألوان ديناميكية متوافقة مع السمة النشطة وحواف منحنية أنيقة.
 * =====================================================================================
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R

/*
 * =====================================================================================
 * زر الإجراء العائم لسجل الحركات (CustomerHistoryFAB)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * زر عائم يظهر في أسفل شاشة كشف حساب العميل لفتح نافذة إضافة معاملة مالية جديدة.
 *
 * [المُدخلات]:
 * - isVisible: مؤشر تحديد ما إذا كان الزر ظاهراً أم مخفياً.
 * - activeThemeColor: لون السمة النشط لخلفية الزر العائم.
 * - contentPadding: هوامش التباعد الخاصة بالشاشة ومحتوياتها.
 * - onClick: رد نداء عند النقر على الزر.
 * - modifier: مغير التنسيق الخارجي.
 * =====================================================================================
 */
@Composable
fun CustomerHistoryFAB(
    isVisible: Boolean,
    activeThemeColor: Color,
    contentPadding: PaddingValues,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 20.dp
            )
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.habayeb_add_tx_desc),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
