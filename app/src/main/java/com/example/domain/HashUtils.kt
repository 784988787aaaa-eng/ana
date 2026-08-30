/**
 * =====================================================================
 * ملف: أدوات التجزئة التشفيرية ومسح الذاكرة الآمن (HashUtils.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن دوال التجزئة والتشفير الأمني (Cryptographic Hashing) المعتمدة على
 * خوارزمية SHA-256 المعززة بملح ثابت وسري للتطبيق (Application Pepper) وملح ديناميكي
 * مستخرج من بصمة الجهاز (Device Salt) لحماية كلمات المرور ورموز PIN من هجمات قواميس
 * التشفير وجداول قوس قزح (Rainbow Tables).
 * كما يوفر دوال مقارنة بزمن ثابت ومسح آمن للبيانات الحساسة من ذاكرة الوصول العشوائي (RAM).
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. التجزئة المشفرة المحصنة (Salted & Peppered SHA-256 Hashing):
 *    - دمج النص المدخل مع ملح أمني ثابت وملح جهاز ديناميكي قبل حساب التجزئة.
 * 2. أمان الخيوط في محركات التشفير (Thread-Safe MessageDigest):
 *    - عزل كائنات [MessageDigest] داخل [ThreadLocal] لمنع التداخل بين الخيوط المتزامنة.
 * 3. المقارنة الزمنية المنيعة (Constant-Time Verification):
 *    - حماية المقارنات من هجمات قياس التوقيت (Timing Attacks).
 * 4. التنظيف الفوري للذاكرة الحساسة (Memory Scrubbing / Wiping):
 *    - تصفير مصفوفات المحارف والبايتات الحساسة فور الانتهاء لمنع استخراجها عبر تفريغ الذاكرة (Memory Dump).
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد محرك التجزئة الأمني القياسي في منصة جافا
// ---------------------------------------------------------------------
import java.security.MessageDigest

/**
 * [الكائن الأحادي لأدوات التجزئة والتشفير - HashUtils]:
 * يحتوي على خوارزميات التجزئة والمقارنة الأمنية وتطهير الذاكرة.
 */
object HashUtils {

    /** ملح أمني سري وثابت خاص بالتطبيق لمنع جداول التجزئة الجاهزة (Rainbow Tables) */
    private const val APP_PEPPER = "SmartMakhzanSecurityGuard_2026_#!"

    /** جدول المحارف الست عشرية لتحويل البايتات إلى نصوص بسرعة فائقة */
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /** كائن تجزئة SHA-256 معزول لكل خيط لتفادي مشاكل التزامن */
    private val sha256Digest = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }

    /**
     * [توليد تجزئة SHA-256 محصنة بالملح - hashString]:
     * يدمج النص المدخل مع الملح الثابت وملح الجهاز، ثم يحسب تجزئة SHA-256 ويعيدها كنص سداسي عشري (Hex).
     *
     * @param input النص المراد تجزئته (مثل رمز PIN أو كلمة المرور).
     * @param deviceSalt الملح الديناميكي المشتق من بصمة الجهاز (اختياري).
     * @return التجزئة الناتجة كنص سداسي عشري من 64 محرفاً.
     */
    fun hashString(input: String, deviceSalt: String = ""): String {
        // تجهيز الملح الديناميكي أو استخدام القيمة الافتراضية
        val dynamicSalt = if (deviceSalt.isNotEmpty()) deviceSalt.reversed() else "DefaultDeviceSalt2026#$"
        val saltedInput = input + APP_PEPPER + dynamicSalt
        val bytes = saltedInput.toByteArray(Charsets.UTF_8)
        
        // جلب محرك التجزئة الخاص بالخيط الحالي وإعادة ضبطه
        val md = sha256Digest.get()
        md.reset()
        val digest = md.digest(bytes)
        
        // تحويل مصفوفة البايتات المشفرة إلى تمثيل ست عشري (Hexadecimal)
        val hexChars = CharArray(digest.size * 2)
        for (i in digest.indices) {
            val v = digest[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_CHARS[v ushr 4]
            hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * [المقارنة الآمنة زمنياً للنصوص المشفرة - secureEquals]:
     * تنفذ مقارنة بزمن ثابت لمنع استنتاج الرموز عبر هجمات قياس التوقيت.
     *
     * @param a النص الأول.
     * @param b النص الثاني.
     * @return true إذا كان النصان متطابقين تماماً.
     */
    fun secureEquals(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        if (a.length != b.length) return false
        var result = 0
        for (i in 0 until a.length) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    /**
     * [تطهير مصفوفة المحارف الحساسة من الذاكرة - wipeCharArray]:
     * يملأ المصفوفة بمحارف فارغة (\u0000) فور الانتهاء من استخدام كلمة المرور لحمايتها في الذاكرة.
     *
     * @param array مصفوفة المحارف المراد مسحها.
     */
    fun wipeCharArray(array: CharArray) {
        array.fill('\u0000')
    }

    /**
     * [تطهير مصفوفة البايتات الحساسة من الذاكرة - wipeByteArray]:
     * يملأ المصفوفة بالأصفار فور الانتهاء لمنع استخراج المفاتيح من الذاكرة العشوائية.
     *
     * @param array مصفوفة البايتات المراد تصفيرها.
     */
    fun wipeByteArray(array: ByteArray) {
        array.fill(0)
    }
}


