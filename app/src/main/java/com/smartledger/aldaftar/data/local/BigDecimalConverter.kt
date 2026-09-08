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
    fun toString(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        if (value == null || value.isNaN() || value.isInfinite()) return null
        return try {
            BigDecimal.valueOf(value)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun toDouble(value: BigDecimal?): Double? = value?.toDouble()

    companion object {
        fun cleanNumberString(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""

            val len = trimmed.length
            val sb = StringBuilder(len)
            var seenDot = false

            for (i in 0 until len) {
                val ch = trimmed[i]
                when {
                    ch in '0'..'9' -> sb.append(ch)
                    ch in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                    ch in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                    ch == '.' || ch == ',' || ch == '٫' -> {
                        if (!seenDot) {
                            sb.append('.')
                            seenDot = true
                        }
                    }
                    ch == '-' && sb.isEmpty() -> sb.append('-')
                }
            }
            val result = sb.toString()
            if (result == "-" || result == "." || result == "-.") return ""
            return result
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
