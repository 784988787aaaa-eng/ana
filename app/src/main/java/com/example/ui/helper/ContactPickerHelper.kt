package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة الأدوات المساعدة للتفاعل مع النظام (System Integration Helpers)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على أدوات الربط مع خدمات نظام أندرويد مثل جهات الاتصال،
 * طلب الأذونات الديناميكية، والتكامل مع التطبيقات الخارجية.
 * =====================================================================================
 */

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

/*
 * =====================================================================================
 * دالة مساعدة لاختيار جهات الاتصال وطلب الإذن (rememberContactPicker)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * دالة Composable تُنشئ وتدير تدفق اختيار جهة اتصال من الهاتف:
 * 1. التحقق من إذن قراءة جهات الاتصال (Manifest.permission.READ_CONTACTS).
 * 2. طلب الإذن ديناميكياً في حال عدم منحه مسبقاً (Dynamic Runtime Permission).
 * 3. فتح منتقي جهات اتصال النظام (System Contact Picker) بعد الحصول على الإذن.
 * 4. استخراج اسم ورقم هاتف جهة الاتصال وتمريرهما عبر رد النداء onContactSelected.
 *
 * [المُدخلات]:
 * - onPermissionDenied: دالة رد نداء اختيارية تُستدعى عند رفض المستخدم لمنح الإذن.
 * - onContactSelected: دالة رد نداء تُمرر الاسم ورقم الهاتف بعد نجاح الاختيار.
 *
 * [المُخرجات]:
 * - تعيد دالة Lambda تُستدعى عند النقر على زر استيراد جهة اتصال لبدء التدفق.
 * =====================================================================================
 */
@Composable
fun rememberContactPicker(
    onPermissionDenied: (() -> Unit)? = null,
    onContactSelected: (name: String, phone: String) -> Unit
): () -> Unit {
    val context = LocalContext.current

    /*
     * ---------------------------------------------------------------------------------
     * مُطلق نشاط اختيار جهة الاتصال (Pick Contact Activity Launcher)
     * ---------------------------------------------------------------------------------
     * يستقبل مسار Uri لجهة الاتصال المختارة، ويستخرج تفاصيلها (الاسم ورقم الهاتف).
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * مُطلق طلب الإذن وقت التشغيل (Permission Request Launcher)
     * ---------------------------------------------------------------------------------
     * يطلب إذن قراءة جهات الاتصال من المستخدم، ويفتح المنتقي مباشرة إذا تمت الموافقة.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * الدالة التنفيذية المُعادة (Trigger Lambda)
     * ---------------------------------------------------------------------------------
     * تفحص حالة الإذن الحالية: تطلق المنتقي إذا كان الإذن ممنوحاً، أو تطلب الإذن أولاً.
     * ---------------------------------------------------------------------------------
     */
    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}

