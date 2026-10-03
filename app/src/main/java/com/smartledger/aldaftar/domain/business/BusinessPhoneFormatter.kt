package com.smartledger.aldaftar.domain.business

import com.smartledger.aldaftar.platform.contacts.StringUtils.toWesternDigits

/**
 * CountryCode
 * تمثيل مفتاح الدولة مع العلم والاسم المترجم
 */
data class CountryCode(
    val code: String, // e.g. "+967"
    val countryNameAr: String, // e.g. "اليمن"
    val flagEmoji: String // e.g. "🇾🇪"
)

sealed interface PhoneValidationResult {
    object Empty : PhoneValidationResult
    object Valid : PhoneValidationResult
    data class Invalid(val message: String) : PhoneValidationResult
}

data class ParsedPhone(
    val countryCode: String,
    val nationalNumber: String
)

object BusinessPhoneFormatter {

    val SUPPORTED_COUNTRY_CODES = listOf(
        CountryCode("+967", "اليمن", "🇾🇪"),
        CountryCode("+966", "السعودية", "🇸🇦"),
        CountryCode("+971", "الإمارات", "🇦🇪"),
        CountryCode("+20", "مصر", "🇪🇬"),
        CountryCode("+965", "الكويت", "🇰🇼"),
        CountryCode("+968", "عمان", "🇴🇲"),
        CountryCode("+973", "البحرين", "🇧🇭"),
        CountryCode("+962", "الأردن", "🇯🇴"),
        CountryCode("+964", "العراق", "🇮🇶"),
        CountryCode("+249", "السودان", "🇸🇩"),
        CountryCode("+212", "المغرب", "🇲🇦"),
        CountryCode("+213", "الجزائر", "🇩🇿"),
        CountryCode("+216", "تونس", "🇹🇳"),
        CountryCode("+218", "ليبيا", "🇱🇾"),
        CountryCode("+970", "فلسطين", "🇵🇸"),
        CountryCode("+1", "أمريكا / كندا", "🇺🇸"),
        CountryCode("+44", "المملكة المتحدة", "🇬🇧"),
        CountryCode("+49", "ألمانيا", "🇩🇪"),
        CountryCode("+33", "فرنسا", "🇫🇷"),
        CountryCode("+90", "تركيا", "🇹🇷")
    )

    const val DEFAULT_COUNTRY_CODE = "+967"

    /**
     * تحويل الأرقام المشرقية والتنظيف الأولي
     */
    fun cleanDigits(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input.trim().toWesternDigits().replace(Regex("[^0-9+]"), "")
    }

    /**
     * تحليل الرقم المخزن واستخراج مفتاح الدولة والرقم المحلي
     */
    fun parseRawPhone(raw: String?, defaultCode: String = DEFAULT_COUNTRY_CODE): ParsedPhone {
        if (raw.isNullOrBlank()) {
            return ParsedPhone(defaultCode, "")
        }

        val cleaned = cleanDigits(raw)
        if (cleaned.isBlank()) {
            return ParsedPhone(defaultCode, "")
        }

        // البحث عن مفتاح الدولة المطابق بفرز الطول تنازلياً
        val sortedCodes = SUPPORTED_COUNTRY_CODES.sortedByDescending { it.code.length }
        for (cc in sortedCodes) {
            val codeNoPlus = cc.code.removePrefix("+")
            if (cleaned.startsWith(cc.code)) {
                val num = cleaned.substring(cc.code.length).removePrefix("0")
                return ParsedPhone(cc.code, num)
            } else if (cleaned.startsWith("00$codeNoPlus")) {
                val num = cleaned.substring(("00$codeNoPlus").length).removePrefix("0")
                return ParsedPhone(cc.code, num)
            } else if (cleaned.startsWith(codeNoPlus) && cleaned.length >= codeNoPlus.length + 6) {
                val num = cleaned.substring(codeNoPlus.length).removePrefix("0")
                return ParsedPhone(cc.code, num)
            }
        }

        // إذا لم يتطابق مع مفاتيح الدول، نعتبره رقماً محلياً مع المفتاح الافتراضي
        val localNum = cleaned.replace("+", "").removePrefix("0")
        return ParsedPhone(defaultCode, localNum)
    }

    /**
     * دمج مفتاح الدولة مع الرقم المحلي للتركيب النهائي
     * يرجع null إذا كان الرقم فارغاً
     */
    fun formatCombinedPhone(countryCode: String, nationalNumber: String): String? {
        val cleanNational = cleanDigits(nationalNumber).replace("+", "").removePrefix("0")
        if (cleanNational.isBlank()) return null

        val normCode = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
        return "$normCode$cleanNational"
    }

    /**
     * التحقق من صحة الرقم المحلي
     */
    fun validateNationalNumber(nationalNumber: String): PhoneValidationResult {
        val cleanNational = cleanDigits(nationalNumber).replace("+", "").removePrefix("0")
        if (cleanNational.isBlank()) {
            return PhoneValidationResult.Empty
        }
        if (cleanNational.length !in 6..12) {
            return PhoneValidationResult.Invalid("رقم الهاتف يجب أن يتكون من 6 إلى 12 رقماً")
        }
        return PhoneValidationResult.Valid
    }
}
