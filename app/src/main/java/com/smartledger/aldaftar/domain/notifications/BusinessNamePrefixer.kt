package com.smartledger.aldaftar.domain.notifications

/**
 * محرك تطبيق بادئة اسم النشاط التجاري على رسائل المعاملات والكشوفات.
 * القواعد الصارمة:
 * - إذا كان اسم النشاط مدخلاً وغير فارغ: يضاف الاسم في بداية الرسالة بخط سميك بارز (*اسم المحل:*)
 *   بدون أي رمز متجر إضافي.
 * - إذا لم يكن مدخلاً أو كان فارغاً: يبقى النص الأساسي حرفياً كما هو دون أي تغيير.
 */
object BusinessNamePrefixer {

    fun prefix(message: String, businessName: String?, isRichText: Boolean = true): String {
        val cleanName = businessName?.trim()
        return if (!cleanName.isNullOrBlank()) {
            if (isRichText) {
                "*$cleanName:*\n$message"
            } else {
                "*$cleanName:*\n$message"
            }
        } else {
            message
        }
    }
}
