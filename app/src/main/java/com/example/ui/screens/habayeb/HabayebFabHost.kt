package com.example.ui.screens.habayeb

/*
 * =====================================================================================
 * حزمة مضيف الزر العائم لشاشة الحبايب (Habayeb FAB Host Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على وحدة إدارة دورة حياة الزر العائم (Floating Action Button):
 * - تنسيق موضع ومظهر الزر العائم عبر الشاشات المختلفة.
 * - دعم التزامن مع الطبقة العلوية (Overlay Layer) لإظهار الزر فوق شريط التنقل السفلي.
 * - إخفاء الزر تلقائياً أثناء أوضاع التحديد المتعدد (Multi-Select Mode).
 * =====================================================================================
 */

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.zIndex
import com.example.data.local.entities.HabayebCustomer
import com.example.ui.screens.habayeb.components.HabayebFab

/*
 * =====================================================================================
 * مضيف الزر العائم لشاشة الحبايب (HabayebFabHost)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يدير هذا المكون عرض وسلوك الزر العائم:
 * 1. إذا توفر رد نداء الطبقة العلوية (`onFabOverlayChanged`)، يقوم بتثبيت الزر في الطبقة العامة عبر `DisposableEffect`.
 * 2. إذا لم يتوفر، يرسم الزر مباشرة داخل الشجرة المحلية مع تعديل `zIndex`.
 * 3. يختفي الزر تلقائياً في حال كان التحديد المتعدد للعملاء أو لسجل القيود نشطاً لمنع تداخل الواجهات.
 *
 * [المُدخلات]:
 * - targetCustomer: العميل المستهدف (إن وجد) لإضافة قيد له، أو null لإضافة عميل جديد.
 * - contentPadding: هوامش التباعد الخاصة بالشاشة.
 * - activeThemeColor: اللون الرئيسي للسمة الحالية.
 * - activeSubColor: اللون الثانوي لخلفية الأزرار.
 * - haptic: مشغل ردود الفعل اللمسية والاهتزازية.
 * - isMultiSelectActive: هل وضع التحديد المتعدد للعملاء نشط.
 * - isHistoryTxMultiSelectActive: هل وضع التحديد المتعدد لقيود السجل نشط.
 * - onAddCustomerClick: رد نداء عند طلب إضافة عميل جديد.
 * - onAddTransactionForCustomer: رد نداء عند طلب إضافة قيد لعميل محدد.
 * - onFabOverlayChanged: رد نداء اختياري لتفويض رسم الزر في طبقة التطبيق العلوية.
 * =====================================================================================
 */
@Composable
fun HabayebFabHost(
    targetCustomer: HabayebCustomer?,
    contentPadding: PaddingValues,
    activeThemeColor: Color,
    activeSubColor: Color,
    haptic: HapticFeedback,
    isMultiSelectActive: Boolean,
    isHistoryTxMultiSelectActive: Boolean,
    onAddCustomerClick: () -> Unit,
    onAddTransactionForCustomer: (HabayebCustomer) -> Unit,
    onFabOverlayChanged: (((@Composable () -> Unit)?) -> Unit)? = null
) {
    if (onFabOverlayChanged != null) {
        // إدارة الزر عبر الطبقة العلوية المنسقة
        DisposableEffect(
            targetCustomer,
            contentPadding,
            activeThemeColor,
            activeSubColor,
            isMultiSelectActive,
            isHistoryTxMultiSelectActive
        ) {
            if (!isMultiSelectActive && !isHistoryTxMultiSelectActive) {
                onFabOverlayChanged.invoke {
                    HabayebFab(
                        targetCustomer = targetCustomer,
                        contentPadding = contentPadding,
                        primaryColor = activeThemeColor,
                        containerColor = activeSubColor,
                        haptic = haptic,
                        onAddCustomerClick = onAddCustomerClick,
                        onAddTransactionForCustomer = onAddTransactionForCustomer
                    )
                }
            } else {
                onFabOverlayChanged.invoke(null)
            }
            onDispose {
                onFabOverlayChanged.invoke(null)
            }
        }
    } else {
        // الرسم المباشر داخل الشاشة
        if (!isMultiSelectActive && !isHistoryTxMultiSelectActive) {
            HabayebFab(
                targetCustomer = targetCustomer,
                contentPadding = contentPadding,
                primaryColor = activeThemeColor,
                containerColor = activeSubColor,
                haptic = haptic,
                onAddCustomerClick = onAddCustomerClick,
                onAddTransactionForCustomer = onAddTransactionForCustomer,
                modifier = Modifier.zIndex(25f)
            )
        }
    }
}

