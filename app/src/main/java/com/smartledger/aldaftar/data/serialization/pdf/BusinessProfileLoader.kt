/**
 * =====================================================================
 * ملف: محمل الملف التعريفي للمنشأة والتجارة (BusinessProfileLoader.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن آلية استرجاع وتحميل بيانات وهوية النشاط التجاري (اسم المنشأة،
 * الوصف أو الشعار اللفظي، أرقام الهواتف، ومسار الشعار) من التفضيلات المشتركة
 * مع دعم التوافق مع المفاتيح البديلة، وتحميل وتحجيم صورة الشعار كصورة نقطية [Bitmap]
 * مجهزة للطباعة والرسم في ترويسات تقارير PDF وإدارتها في الذاكرة.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. القراءة المرنة للتفضيلات والهوية:
 *    - استرجاع إعدادات الهوية التجارية ودعم المفاتيح الأساسية والبديلة (Fallback Preference Keys).
 * 2. تحليل مصفوفات الهواتف بصيغة JSON:
 *    - استخراج أرقام الاتصال وقراءتها كمصفوفة نصوص ودمجها في نص واحد منسق.
 * 3. المعالجة الآمنة للصور والتحجيم:
 *    - تفويض تحميل وتصغير الشعار إلى [PdfDrawingUtils.loadAndScaleLogo] وتوفير كائن البيتماب الخام للتدوير لاحقاً.
 * 4. تجهيز وعاء البيانات الموحد [BusinessHeaderData].
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات والسجلات والموارد ومصفوفات JSON
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.smartledger.aldaftar.R
import org.json.JSONArray

/**
 * [وعاء بيانات ترويسة المنشأة في التقارير - BusinessHeaderData]:
 * يجمع البيانات النصية والبصرية اللازمة لرسم الترويسة العلوية لكشوف الحسابات.
 *
 * @property displayedName اسم المنشأة أو التطبيق المعروض.
 * @property displayedDesc الوصف التجاري أو الشعار اللفظي.
 * @property phonesStr أرقام الاتصال المنسقة نصياً.
 * @property hasLogo ما إذا كان للمنشأة شعار بصري صالح.
 * @property logoW عرض الشعار بالنقاط.
 * @property logoH ارتفاع الشعار بالنقاط.
 * @property scaledLogo صورة الشعار المصغرة والجاهزة للرسم.
 * @property rawBitmap صورة الشعار الأصلية لإعادة تدويرها في الذاكرة.
 */
data class BusinessHeaderData(
    val displayedName: String,
    val displayedDesc: String,
    val phonesStr: String,
    val hasLogo: Boolean,
    val logoW: Float,
    val logoH: Float,
    val scaledLogo: Bitmap?,
    val rawBitmap: Bitmap?
)

/**
 * [الكائن الأحادي لمحمل الملف التجاري - BusinessProfileLoader]:
 * يتولى استرجاع بيانات المنشأة وشعارها من التفضيلات المحلية وتجهيزها للتقارير.
 */
object BusinessProfileLoader {

    /** وسم السجلات التشخيصية */
    private const val TAG = "BusinessProfileLoader"
    /** اسم ملف التفضيلات الأساسي للمنشأة */
    private const val PREF_BUSINESS_PROFILE = "business_profile"
    /** اسم ملف التفضيلات البديل للتوافق */
    private const val PREF_BUSINESS_PROFILE_ALT = "business_profile_prefs"

    // مفاتيح التفضيلات الأساسية
    private const val KEY_BIZ_NAME = "biz_name"
    private const val KEY_BIZ_DESC = "biz_desc"
    private const val KEY_BIZ_LOGO_PATH = "biz_logo_path"
    private const val KEY_BIZ_PHONES = "biz_phones"

    // مفاتيح التفضيلات البديلة
    private const val KEY_ALT_NAME = "business_name"
    private const val KEY_ALT_SLOGAN = "business_slogan"
    private const val KEY_ALT_LOGO_PATH = "logo_path"
    private const val KEY_ALT_PHONE = "business_phone"

    /**
     * [تحميل بيانات الترويسة التجارية - load]:
     * يقرأ تفضيلات المنشأة مع التراجع التلقائي للقيم الافتراضية عند الغياب.
     *
     * @param context سياق التطبيق للوصول للتفضيلات والموارد.
     * @return كائن [BusinessHeaderData] مكتمل البيانات.
     */
    fun load(context: Context): BusinessHeaderData {
        val prefs = context.getSharedPreferences(PREF_BUSINESS_PROFILE, Context.MODE_PRIVATE)
        val altPrefs = context.getSharedPreferences(PREF_BUSINESS_PROFILE_ALT, Context.MODE_PRIVATE)

        var bizName = prefs.getString(KEY_BIZ_NAME, "")?.trim().orEmpty()
        if (bizName.isBlank()) {
            bizName = altPrefs.getString(KEY_ALT_NAME, "")?.trim().orEmpty()
        }

        var bizDesc = prefs.getString(KEY_BIZ_DESC, "")?.trim().orEmpty()
        if (bizDesc.isBlank()) {
            bizDesc = altPrefs.getString(KEY_ALT_SLOGAN, "")?.trim().orEmpty()
        }

        var bizLogoPath = prefs.getString(KEY_BIZ_LOGO_PATH, "")?.trim().orEmpty()
        if (bizLogoPath.isBlank()) {
            bizLogoPath = altPrefs.getString(KEY_ALT_LOGO_PATH, "")?.trim().orEmpty()
        }

        val bizPhones = mutableListOf<String>()
        try {
            val phonesJson = prefs.getString(KEY_BIZ_PHONES, "[]") ?: "[]"
            val jsonArray = JSONArray(phonesJson)
            for (i in 0 until jsonArray.length()) {
                val p = jsonArray.getString(i).trim()
                if (p.isNotBlank()) bizPhones.add(p)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading business phones", e)
        }

        if (bizPhones.isEmpty()) {
            val altPhone = altPrefs.getString(KEY_ALT_PHONE, "")?.trim().orEmpty()
            if (altPhone.isNotBlank()) {
                bizPhones.add(altPhone)
            }
        }

        val displayedName = if (bizName.isNotBlank()) bizName else context.getString(R.string.app_name)
        val displayedDesc = if (bizDesc.isNotBlank()) bizDesc else context.getString(R.string.pdf_default_desc)

        val logoResult = PdfDrawingUtils.loadAndScaleLogo(context, bizLogoPath)
        val phonesToDraw = if (bizPhones.isNotEmpty()) bizPhones else listOf(context.getString(R.string.pdf_certified_identity))
        val phonesStr = if (bizPhones.isNotEmpty()) context.getString(R.string.pdf_phone_prefix) + " " + phonesToDraw.joinToString(" - ") else phonesToDraw.joinToString(" - ")

        return BusinessHeaderData(
            displayedName = displayedName,
            displayedDesc = displayedDesc,
            phonesStr = phonesStr,
            hasLogo = logoResult.hasLogo,
            logoW = logoResult.width,
            logoH = logoResult.height,
            scaledLogo = logoResult.bitmap,
            rawBitmap = logoResult.rawBitmapToRecycle
        )
    }
}

