package com.smartledger.aldaftar.presentation.formatters

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * يفرض الأرقام الغربية (0-9) في جميع أنحاء التطبيق بغض النظر عن لغة أو إقليم الجهاز،
 * مع الحفاظ على اللغة العربية لاتجاه الواجهة وأسماء الأيام والأشهر.
 */
object WesternDigits {

    val ARABIC_LATN_LOCALE: Locale = Locale.forLanguageTag("ar-u-nu-latn")

    fun applyAppWesternLocale() {
        Locale.setDefault(ARABIC_LATN_LOCALE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.setDefault(Locale.Category.DISPLAY, ARABIC_LATN_LOCALE)
            Locale.setDefault(Locale.Category.FORMAT, ARABIC_LATN_LOCALE)
        }
    }

    fun wrapContextWithWesternDigits(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(ARABIC_LATN_LOCALE)
        config.setLayoutDirection(ARABIC_LATN_LOCALE)
        return context.createConfigurationContext(config)
    }

    fun format(template: String, vararg args: Any?): String =
        normalize(String.format(ARABIC_LATN_LOCALE, template, *args))

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
