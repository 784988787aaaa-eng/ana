package com.smartledger.aldaftar.domain.notifications

/**
 * محرك تطبيق بادئة اسم النشاط التجاري على رسائل المعاملات والكشوفات.
 * القواعد الصارمة:
 * - إذا كان اسم النشاط مدخلاً فعلياً وغير فارغ: يضاف الاسم في بداية الرسالة متبوعاً بنقطتين وسطر جديد.
 * - إذا لم يكن مدخلاً أو كان فارغاً: يبقى النص الأساسي حرفياً كما هو دون أي تغيير أو رمز إضافي.
 */
object BusinessNamePrefixer {

    fun prefix(message: String, businessName: String?): String {
        val cleanName = businessName?.trim()
        return if (!cleanName.isNullOrBlank()) {
            "$cleanName:\n$message"
        } else {
            message
        }
    }
}
