package com.smartledger.aldaftar.domain.communication

import com.smartledger.aldaftar.platform.contacts.StringUtils.toWesternDigits

/**
 * محرك التحقق والتهيئة لأرقام الهواتف الموجهة لقنوات التواصل (WhatsApp و SMS).
 * يطبق سياسة الأمان والخصوصية:
 * - التحقق الصارم من وجود وجهة صالحة.
 * - تنظيف وتوحيد التنسيق.
 * - تحويل الأرقام المشرقية إلى غربية تلقائياً.
 */
object CustomerPhoneHelper {

    private const val DEFAULT_COUNTRY_CODE = "967" // اليمن (الافتراضي للمشروع)
    private val NON_DIGIT_REGEX = Regex("[^0-9+]")

    /**
     * هل الرقم صالح كوجهة إرسال؟
     * يجب أن يحتوي بعد التنظيف على 7 إلى 15 رقماً صالحاً.
     */
    fun isValidDestination(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        val digits = phone.toWesternDigits().replace(Regex("[^0-9]"), "")
        return digits.length in 7..15
    }

    /**
     * ينظف الرقم من الرموز والمسافات ويحول الأرقام المشرقية إلى غربية.
     */
    fun cleanRaw(phone: String): String {
        return phone.trim().toWesternDigits().replace(NON_DIGIT_REGEX, "")
    }

    /**
     * يجهز الرقم لرابط واتساب (wa.me/xxx) بأرقام دولية بدون علامة + وبدون مسافات.
     */
    fun normalizeForWhatsApp(phone: String, defaultCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        val clean = cleanRaw(phone)
        val digitsOnly = clean.replace("+", "").trimStart('0')
        return when {
            // يبدأ بمفتاح اليمن المحلي المكون من 9 أرقام (مثل 77xxxxxxx أو 73xxxxxxx أو 71xxxxxxx أو 70xxxxxxx)
            digitsOnly.length == 9 && digitsOnly.startsWith("7") -> defaultCountryCode + digitsOnly
            // يبدأ برقم سعودي محلي (مثل 5xxxxxxxx)
            digitsOnly.length == 9 && digitsOnly.startsWith("5") -> "966$digitsOnly"
            // يحتوي مسبقاً على مفتاح دولة كامل
            digitsOnly.length >= 10 -> digitsOnly
            // رقم محلي آخر مكون من 7 أو 8 أرقام
            else -> defaultCountryCode + digitsOnly
        }
    }

    /**
     * يجهز الرقم لإرسال رسالة SMS عبر URI smsto:
     */
    fun normalizeForSms(phone: String, defaultCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        val clean = cleanRaw(phone)
        return when {
            clean.startsWith("+") -> clean
            clean.startsWith("00") -> "+" + clean.substring(2)
            clean.length == 9 && clean.startsWith("7") -> "+$defaultCountryCode$clean"
            clean.length == 9 && clean.startsWith("5") -> "+966$clean"
            clean.length >= 10 -> "+$clean"
            else -> clean
        }
    }
}
