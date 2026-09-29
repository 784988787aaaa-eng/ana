package com.smartledger.aldaftar.presentation.formatters

/**
 * يفرض الأرقام الغربية 0-9 في واجهات النسخ الاحتياطي، بغض النظر عن لغة/منطقة الجهاز.
 * يدعم الأرقام العربية-الهندية والفارسية حتى لا تتسرب إلى أسماء النسخ أو تواريخها أو أحجامها.
 */
object WesternDigits {
    fun normalize(value: String): String {
        if (value.isEmpty()) return value
        val out = StringBuilder(value.length)
        value.forEach { c ->
            out.append(
                when (c) {
                    in '٠'..'٩' -> ('0'.code + (c.code - '٠'.code)).toChar()
                    in '۰'..'۹' -> ('0'.code + (c.code - '۰'.code)).toChar()
                    else -> c
                }
            )
        }
        return out.toString()
    }
}
