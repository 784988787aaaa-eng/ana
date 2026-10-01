package com.smartledger.aldaftar.data.local

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {

    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return null
        val cleaned = cleanNumberString(value)
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        if (value == null || value.isNaN() || value.isInfinite()) return null
        return BigDecimal.valueOf(value)
    }

    @TypeConverter
    fun toString(value: BigDecimal?): String? = value?.toPlainString()

    companion object {
        /**
         * Parses persisted decimal text without changing its financial meaning.
         * Room writes canonical values with '.' via [BigDecimal.toPlainString], so
         * comma is intentionally treated as a grouping separator rather than a
         * decimal separator. Arabic-Indic digits and Arabic decimal separator are
         * accepted for legacy/imported values.
         *
         * Unexpected characters are rejected instead of being silently stripped;
         * silently turning malformed financial text into a different number is
         * unsafe at a persistence boundary.
         */
        fun cleanNumberString(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""

            val sb = StringBuilder(trimmed.length)
            var seenDecimal = false
            var seenDigit = false
            var signAllowed = true

            for (ch in trimmed) {
                when {
                    ch in '0'..'9' -> {
                        sb.append(ch)
                        seenDigit = true
                        signAllowed = false
                    }
                    ch in '٠'..'٩' -> {
                        sb.append((ch - '٠' + '0'.code).toChar())
                        seenDigit = true
                        signAllowed = false
                    }
                    ch in '۰'..'۹' -> {
                        sb.append((ch - '۰' + '0'.code).toChar())
                        seenDigit = true
                        signAllowed = false
                    }
                    (ch == '.' || ch == '٫') && !seenDecimal -> {
                        sb.append('.')
                        seenDecimal = true
                        signAllowed = false
                    }
                    (ch == ',' || ch == '،') && !seenDecimal -> {
                        // Grouping separators are never decimal separators here.
                    }
                    ch == '-' && signAllowed && !seenDigit -> {
                        sb.append('-')
                        signAllowed = false
                    }
                    else -> return ""
                }
            }

            val result = sb.toString()
            return if (!seenDigit || result == "-" || result == "." || result == "-.") "" else result
        }
    }
}



class IntListConverter {
    @TypeConverter fun fromStorage(value: String?): List<Int> = value?.split(',')?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    @TypeConverter fun toStorage(value: List<Int>?): String = value.orEmpty().joinToString(",")
}
class StringListConverter {
    @TypeConverter fun fromStorage(value: String?): List<String> = value?.split('\u001F')?.filter { it.isNotBlank() } ?: emptyList()
    @TypeConverter fun toStorage(value: List<String>?): String = value.orEmpty().joinToString("\u001F")
}
