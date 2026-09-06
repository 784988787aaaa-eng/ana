package com.smartledger.aldaftar.data.local

import androidx.room.TypeConverter
import java.math.BigDecimal

/** محول القيم المالية الدقيقة بين قاعدة البيانات والكائنات الحسابية. */
class BigDecimalConverter {

    /** يحول النص المخزن إلى قيمة مالية مع رفض أي صيغة قد تخفي تلفاً في البيانات. */
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return null
        val cleaned = cleanNumberString(value)
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned)
        } catch (_: NumberFormatException) {
            null
        }
    }

    /** يحفظ القيمة المالية كنص عشري كامل للحفاظ على القيمة والمقياس دون تدوين علمي. */
    @TypeConverter
    fun toString(value: BigDecimal?): String? = value?.toPlainString()

    /** يحول القيمة العشرية القديمة إلى قيمة دقيقة للتوافق مع قواعد البيانات التاريخية. */
    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        if (value == null || !value.isFinite()) return null
        return try {
            BigDecimal.valueOf(value)
        } catch (_: NumberFormatException) {
            null
        }
    }

    /** يوفر حد توافق صريحاً مع الطبقات التاريخية التي لا تزال تتطلب قيمة عشرية عائمة. */
    @TypeConverter
    fun toDouble(value: BigDecimal?): Double? {
        if (value == null) return null
        val converted = value.toDouble()
        return converted.takeUnless { !it.isFinite() }
    }

    companion object {
        /** ينظف رقماً عربياً أو معيارياً دون إسقاط محارف غير صالحة أو تغيير قيمته بصمت. */
        fun cleanNumberString(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""

            val result = StringBuilder(trimmed.length)
            var separatorSeen = false
            var digitSeen = false

            trimmed.forEachIndexed { index, ch ->
                when {
                    ch in '0'..'9' -> {
                        result.append(ch)
                        digitSeen = true
                    }
                    ch in '٠'..'٩' -> {
                        result.append((ch - '٠' + '0'.code).toChar())
                        digitSeen = true
                    }
                    ch in '۰'..'۹' -> {
                        result.append((ch - '۰' + '0'.code).toChar())
                        digitSeen = true
                    }
                    ch == '.' || ch == ',' || ch == '٫' -> {
                        if (separatorSeen || index == trimmed.lastIndex) return ""
                        if (!digitSeen) {
                            if (result.isNotEmpty() && result[0] == '-') result.append('0')
                            else if (result.isEmpty()) result.append('0')
                        }
                        result.append('.')
                        separatorSeen = true
                    }
                    ch == '-' -> {
                        if (index != 0) return ""
                        result.append('-')
                    }
                    else -> return ""
                }
            }

            if (!digitSeen) return ""
            return result.toString()
        }
    }
}
