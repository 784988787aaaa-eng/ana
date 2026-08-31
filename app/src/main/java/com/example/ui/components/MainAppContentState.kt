/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/components/MainAppContentState.kt
 * المسؤولية: حالة العرض المشتركة التي تحدد ما يحتاجه المحتوى الرئيسي من اختيار أو تنقل أو حوار.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 30: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 42: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.

package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// تم فصل إدارة الحالة عن تركيب الواجهة للحفاظ على وضوح مسؤوليات المكونات.
class MainAppContentState(
    initialIsDrawerOpen: Boolean = false,
    private val onMenuClickAction: (() -> Unit)? = null
) {
    var isDrawerOpen by mutableStateOf(initialIsDrawerOpen)

    fun handleMenuClick() {
        onMenuClickAction?.invoke()
    }

    fun openDrawer() {
        isDrawerOpen = true
        onMenuClickAction?.invoke()
    }

    fun closeDrawer() {
        isDrawerOpen = false
    }

    fun toggleDrawer() {
        if (isDrawerOpen) {
            closeDrawer()
        } else {
            openDrawer()
        }
    }

    fun updateDrawerState(isOpen: Boolean) {
        isDrawerOpen = isOpen
    }
}

@Composable
fun rememberMainAppContentState(
    isDrawerOpen: Boolean = false,
    onMenuClick: (() -> Unit)? = null
): MainAppContentState {
    val state = remember(isDrawerOpen, onMenuClick) {
        MainAppContentState(initialIsDrawerOpen = isDrawerOpen, onMenuClickAction = onMenuClick)
    }
    state.updateDrawerState(isDrawerOpen)
    return state
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
