/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/helper/ContactPickerHelper.kt
 * المسؤولية: أدوات مساعدة لفتح منتقي جهات الاتصال واستخراج البيانات اللازمة للواجهة.
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
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 24: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 26: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 35: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 38: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 47: الشرط التالي يحافظ على قرار التنفيذ الأصلي.

package com.example.ui.helper

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.R
import com.example.domain.StringUtils

@Composable
fun rememberContactPicker(
    onPermissionDenied: (() -> Unit)? = null,
    onContactSelected: (name: String, phone: String) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val details = StringUtils.getContactDetails(context, uri)
            if (details != null) {
                onContactSelected(details.first, details.second)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            if (onPermissionDenied != null) {
                onPermissionDenied()
            } else {
                Toast.makeText(context, context.getString(R.string.habayeb_contact_picker), Toast.LENGTH_SHORT).show()
            }
        }
    }

    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
