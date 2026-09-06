/**
 * =====================================================================
 * ملف: أدوات معالجة النصوص وتنسيق العملات (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يحتوي هذا الملف على وحدتين رئيسيتين:
 * 1. []: لمعالجة النصوص وتطبيع الحروف العربية (إزالة التشكيل، وتوحيد الهمزات والياءات)،
 *    وتحويل الأرقام المشرقية إلى أرقام غربية، واستخراج بيانات جهات الاتصال من دفتر الهاتف.
 * 2. []: لتنسيق وعرض المبالغ النقدية والأرقام المالية بدقة متناهية مع إضافة رموز
 *    العملات المحلية (مثل: ر.ي، $، إلخ) وبطريقة آمنة لتعدد الخيوط (-).
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. تطبيع النصوص للبحث الذكي (  ):
 *    - إزالة التشكيل وتوحيد أشكال الألف (أ، إ، آ، ٱ -> ا) والتاء المربوطة والياء لتسهيل البحث.
 * 2. توحيد الأرقام والمعاملات ( ):
 *    - تحويل الأرقام العربية (٠-٩) والفارسية (۰-۹) والفواصل إلى الصيغة القياسية للحسابات البرمجية.
 * 3. قراءة جهات الاتصال (  ):
 *    - الاستعلام الآمن عن أسماء وأرقام الهواتف عبر موفر محتوى النظام [].
 * 4. التنسيق المالي الآمن للخيوط (-  ):
 *    - استخدام [] لعزل كائنات [] ومنع تضارب التنسيق المتزامن.
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق ومزود محتوى أندرويد، ومكتبات الرياضيات والتنسيق
// ---------------------------------------------------------------------
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.smartledger.aldaftar.data.local.entities.DatabaseDefaults
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// =========================================================================
// قسم: أدوات معالجة وتطبيع النصوص العربية ( &  )
// =========================================================================

/**
 * [الكائن الأحادي لأدوات معالجة النصوص - ]:
 * يوفر دوال تطبيع الحروف العربية وتحويل الأرقام والتعامل مع جهات الاتصال.
 */
object StringUtils {

    /** تعبير نمطي لتنظيف أرقام الهواتف من الرموز والمسافات مع الإبقاء على الأرقام وإشارة الجمع (+) */
    private val PHONE_CLEANUP_REGEX = Regex("[^0-9+]")

    /**
     * [تطبيع النصوص العربية للبحث والمطابقة - ]:
     * يزيل التشكيل ويوحد رسم الهمزات والتاء المربوطة لتسهيل الفلترة والبحث الذكي.
     *
     * @  النص العربي الأصلي.
     * @ النص المطبع والموحد.
     */
    @JvmStatic
    fun normalizeArabic(text: String): String {
        if (text.isEmpty()) return text
        val trimmed = text.trim()
        val len = trimmed.length
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            when (val char = trimmed[i]) {
                // توحيد كافة صور الألف إلى ألف مجردة
                '\u0622', '\u0623', '\u0625', '\u0671' -> sb.append('ا')
                // توحيد التاء المربوطة إلى هاء
                '\u0629' -> sb.append('ه')
                // توحيد الألف المقصورة إلى ياء
                '\u0649' -> sb.append('ي')
                // حذف حركات التشكيل والتنوين والشدة
                '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0670' -> {}
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * واجهة مطابقة لـ [] مع إمكانية تمرير السياق للتوافق البرمجي.
     */
    @JvmStatic
    fun normalizeArabic(text: String, context: Context?): String = normalizeArabic(text)

    /**
     * [استخراج اسم ورقم جهة الاتصال من دفتر الهاتف - ]:
     * يستعلم عن سجل جهة الاتصال عبر الـ  ويستخرج الاسم والرقم النظيف.
     *
     * @  سياق التطبيق للوصول لمزود المحتوى.
     * @  المعرف الموحد لجهة الاتصال ().
     * @ زوج يحتوي على (الاسم، رقم الهاتف) أو  عند التعذر.
     */
    @JvmStatic
    fun getContactDetails(context: Context, contactUri: Uri): Pair<String, String>? {
        var name = ""
        var phone = ""
        try {
            val cr = context.contentResolver
            cr.query(
                contactUri,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex) ?: ""
                    }
                    
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIndex >= 0) {
                        val contactId = cursor.getString(idIndex)
                        val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val hasPhone = if (hasPhoneIndex >= 0) cursor.getString(hasPhoneIndex) else null
                        
                        // إذا كانت جهة الاتصال تحتوي على أرقام هواتف
                        if (hasPhone == "1") {
                            cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )?.use { phoneCursor ->
                                if (phoneCursor.moveToFirst()) {
                                    val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex >= 0) {
                                        phone = phoneCursor.getString(numberIndex) ?: ""
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }

        val cleanedPhone = runCatching { phone.replace(PHONE_CLEANUP_REGEX, "") }.getOrDefault("")
        if (name.isNotEmpty()) {
            return Pair(name, cleanedPhone)
        }
        return null
    }

    /**
     * دالة امتدادية للتحويل السريع للأرقام اللاتينية.
     */
    @JvmStatic
    fun String.toEnglishDigits(): String = toWesternDigits()

    /**
     * [تحويل الأرقام المشرقية والفارسية إلى أرقام لاتينية - ]:
     * دالة امتدادية تفحص النص وتستبدل أي رقم عربي/فارسي بمقابله اللاتيني المعياري.
     */
    @JvmStatic
    fun String.toWesternDigits(): String {
        val len = length
        var hasArabicDigit = false
        for (i in 0 until len) {
            val c = this[i]
            if (c in '٠'..'٩' || c in '۰'..'۹') {
                hasArabicDigit = true
                break
            }
        }
        if (!hasArabicDigit) return this

        val chars = CharArray(len)
        for (i in 0 until len) {
            val c = this[i]
            chars[i] = when (c) {
                in '٠'..'٩' -> (c - '٠' + '0'.code).toChar()
                in '۰'..'۹' -> (c - '۰' + '0'.code).toChar()
                else -> c
            }
        }
        return String(chars)
    }

    /**
     * [توحيد الأرقام والفواصل العشرية - ]:
     * يحول الأرقام المشرقية إلى لاتينية ويستبدل الفواصل العشرية بنقاط.
     *
     * @  النص الرقمي المدخل.
     * @ النص المحول إلى أرقام قياسية ونقاط عشرية.
     */
    @JvmStatic
    fun normalizeDigits(input: String): String {
        if (input.isEmpty()) return input
        val len = input.length
        val sb = java.lang.StringBuilder(len)
        for (i in 0 until len) {
            val ch = input[i]
            val replacement = when (ch) {
                ',' -> '.'
                in '٠'..'٩' -> (ch - '٠' + '0'.code).toChar()
                in '۰'..'۹' -> (ch - '۰' + '0'.code).toChar()
                else -> ch
            }
            sb.append(replacement)
        }
        return sb.toString()
    }
}

// =========================================================================
// قسم: أدوات تنسيق العملات والأرقام المالية ( )
// =========================================================================

/**
 * [الكائن الأحادي لتنسيق القيم المالية - ]:
 * يوفر منسقات أرقام معزولة لكل خيط لعرض العملات والمبالغ المالية بدقة وجمالية.
 */
object FormatUtils {
    /** رموز التنسيق الرقمي باللغة الإنجليزية لمنع الفواصل غير المرغوبة */
    private val DECIMAL_SYMBOLS = DecimalFormatSymbols(Locale.ENGLISH)

    /** منسق الأعداد الصحيحة المعزول للخيوط (#,##0) */
    private val formatterInteger = ThreadLocal.withInitial { DecimalFormat("#,##0", DECIMAL_SYMBOLS) }
    /** منسق الأعداد العشرية المعزول للخيوط (#,##0.##) */
    private val formatterDecimal = ThreadLocal.withInitial { DecimalFormat("#,##0.##", DECIMAL_SYMBOLS) }

    /**
     * [التنسيق الداخلي للقيم المالية - ]:
     * يحدد ما إذا كان الرقم يحتوي على كسور عشرية لاختيار المنسق المناسب.
     */
    private fun formatNumberInternal(value: BigDecimal): String {
        return runCatching {
            val rounded = value.setScale(2, RoundingMode.HALF_EVEN)
            val hasFraction = rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0
            val formatter = if (hasFraction) formatterDecimal.get() else formatterInteger.get()
            formatter.format(rounded)
        }.getOrDefault(value.toPlainString())
    }

    /**
     * [تنسيق المبلغ النقدي مع الرمز - ]:
     * ينسق المبلغ كـ [] مع إضافة رمز العملة (أو العملة الافتراضية).
     *
     * @  القيمة المالية.
     * @  رمز العملة المخصص (اختياري).
     * @  سياق التطبيق لجلب العملة الافتراضية من الموارد.
     * @ نص المبلغ المنسق مع العملة.
     */
    @JvmStatic
    fun formatCurrency(amount: BigDecimal, symbol: String = "", context: Context? = null): String {
        val finalSymbol = symbol.ifEmpty { context?.getString(com.smartledger.aldaftar.R.string.currency_yer) ?: DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL }
        return formatBigDecimal(amount, finalSymbol)
    }

    /**
     * [تنسيق المبلغ النقدي من نوع  - ]:
     * ينسق المبلغ العشري المزدوج مع إضافة رمز العملة المناسب.
     *
     * @  القيمة المالية من نوع .
     * @  رمز العملة (اختياري).
     * @  سياق التطبيق للوصول للموارد.
     * @ نص المبلغ المنسق مع العملة.
     */
    @JvmStatic
    fun formatDoubleCurrency(amount: Double, symbol: String = "", context: Context? = null): String {
        val finalSymbol = symbol.ifEmpty { context?.getString(com.smartledger.aldaftar.R.string.currency_yer) ?: DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL }
        return formatDouble(amount, finalSymbol)
    }

    /**
     * [تنسيق رقم  مع الرمز - ]:
     * يحول الرقم إلى  وينسقه مع الرمز بدقة وأمان.
     */
    @JvmStatic
    fun formatDouble(value: Double, symbol: String = ""): String {
        return runCatching {
            formatBigDecimal(BigDecimal.valueOf(value), symbol)
        }.getOrElse {
            val formatted = value.toString()
            if (symbol.isNotEmpty()) "$formatted $symbol" else formatted
        }
    }

    /**
     * [تنسيق  مع الرمز - ]:
     * ينسق قيمة [] مع إلحاق رمز العملة في النهاية.
     */
    @JvmStatic
    fun formatBigDecimal(value: BigDecimal, symbol: String = ""): String {
        val formatted = formatNumberInternal(value)
        return if (symbol.isNotEmpty()) "$formatted $symbol" else formatted
    }
}
