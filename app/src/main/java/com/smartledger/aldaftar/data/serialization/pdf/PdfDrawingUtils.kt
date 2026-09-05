/**
 * =====================================================================
 * ملف: أدوات الرسم ومعالجة النصوص والصور في  (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يقدم هذا الكائن مجموعة أدوات منخفضة المستوى للرسم على  ،
 * متخصصة في دعم النصوص العربية ثنائية الاتجاه ( )، وتنظيف الرموز
 * التعبيرية غير المتوافقة مع طباعة ، والتحجيم الذكي لصور الشعارات،
 * والقياس الدقيق لارتفاعات الأسطر متعددة الأسطر باستخدام [التخطيط الثابت].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. دعم وتنسيق النصوص العربية ( &   ):
 *    - استخدام [التخطيط الثابت] لتوزيع الأسطر والمحاذاة التلقائية دون تشوه في التشكيل أو اتصال الحروف.
 * 2. تنقية النصوص وإزالة الإيموجي (تنقية الرموز التعبيرية):
 *    - حماية محرك  من الرموز التعبيرية التي تسبب تشوهات بصرية عبر [__].
 * 3. المعالجة الآمنة واقتصاص وتصغير الشعارات:
 *    - تحميل صور الشعار، ضغطها إن كانت ضخمة، وحساب مقياس العرض والارتفاع المتناسب.
 * 4. إدارة دورة حياة وتدوير الصور النقطية [الصورة النقطية]:
 *    - توفير الكائن الأصلي الخام [الصورة النقطية] لضمان تفريغه من الذاكرة فور اكتمال التقرير.
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم الرسومات والبيتماب وتخطيط النصوص وإدخال وإخراج الملفات
// ---------------------------------------------------------------------
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * [نتيجة معالجة وتحجيم الشعار - ]:
 * يجمع كائنات الصورة النقطية والأبعاد المحسوبة.
 *
 * @  كائن الصورة المصغرة الجاهزة للرسم.
 * @ الصورة النقطية كائن الصورة النقطية الخام لتحريرها لاحقاً.
 * @  العرض المحسوب بالنقاط.
 * @  الارتفاع المحسوب بالنقاط.
 * @  ما إذا كانت الصورة صالحة وتم تحميلها بنجاح.
 */
data class LogoResult(
    val bitmap: Bitmap?,
    val rawBitmapToRecycle: Bitmap?,
    val width: Float,
    val height: Float,
    val hasLogo: Boolean
)

/**
 * [الكائن الأحادي لأدوات رسم مستندات  - ]:
 * يوفر دوال الرسم المعياري للنصوص العربية والشعارات.
 */
object PdfDrawingUtils {

    /** التعبير النمطي لاكتشاف الرموز التعبيرية واستبعادها من نصوص التقارير */
    private val EMOJI_CLEANER_REGEX = Regex(
        "[\uD83C-\uD83E][\uDC00-\uDFFF]" +
        "|[\u2600-\u27BF]" +
        "|[\u2300-\u23FF]" +
        "|[\u2B50-\u2B55]" +
        "|[\u200D]" +
        "|[\uFE0F]"
    )

    /**
     * [تنقية نصوص الـ  - ]:
     * يستبعد الإيموجي والرموز غير المدعومة في خطوط  القياسية.
     *
     * @  النص المدخل.
     * @ النص المنقى الجاهز للطباعة.
     */
    fun sanitizePdfText(text: CharSequence): CharSequence {
        if (text.isEmpty()) return text
        return if (EMOJI_CLEANER_REGEX.containsMatchIn(text)) {
            text.replace(EMOJI_CLEANER_REGEX, "")
        } else {
            text
        }
    }

    /**
     * [إنشاء تخطيط ثابت للنص - التخطيط الثابت]:
     * يبني كائن [التخطيط الثابت] مع مراعاة إصدار الأندرويد لضبط تباعد الأسطر والمحاذاة.
     *
     * @  النص المراد تخطيطه.
     * @  أداة التلوين والخط.
     * @  أقصى عرض متاح بالنقاط.
     * @  نوع المحاذاة المطلوبة.
     * @ كائن [التخطيط الثابت] مجهز.
     */
    fun createStaticLayout(
        text: CharSequence,
        paint: Paint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): StaticLayout {
        val sanitized = sanitizePdfText(text)
        val textPaint = if (paint is TextPaint) paint else TextPaint(paint)
        val safeWidth = width.coerceAtLeast(1)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(sanitized, 0, sanitized.length, textPaint, safeWidth)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.05f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(sanitized, textPaint, safeWidth, alignment, 1.05f, 0f, false)
        }
    }

    /**
     * [قياس الارتفاع الرأسي للنص - ]:
     * يحسب الارتفاع الفعلي الذي سيشغله النص عند تقسيمه على الأسطر ضمن العرض المحدد.
     *
     * @  النص المراد قياسه.
     * @  أداة الخط والتلوين.
     * @  العرض المتاح بالنقاط.
     * @  المحاذاة.
     * @ الارتفاع الرأسي المحسوب بالنقاط.
     */
    fun measureTextHeight(
        text: CharSequence,
        paint: Paint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): Int {
        val layout = createStaticLayout(text, paint, width, alignment)
        return layout.height
    }

    /**
     * [رسم التخطيط الثابت على لوحة الرسم - التخطيط الثابت]:
     * ينقل إحداثيات  ويرسم التخطيط النصي بدقة.
     */
    fun drawStaticLayout(
        canvas: Canvas,
        layout: StaticLayout,
        x: Float,
        y: Float
    ) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    /**
     * [رسم النص العربي مع التقسيم التلقائي - ]:
     * يرسم النص العربي المنسق ويعيد الارتفاع الكلي المستهلك.
     *
     * @ ارتفاع النص المرسوم بالنقاط.
     */
    fun drawArabicText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: Paint,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): Int {
        val layout = createStaticLayout(text, paint, width, alignment)
        drawStaticLayout(canvas, layout, x, y)
        return layout.height
    }

    /**
     * [تحميل وتحجيم صورة الشعار مع التراجع لأيقونة التطبيق - ]:
     *
     * @  سياق التطبيق لجلب الأيقونة الاحتياطية.
     * @  مسار ملف الشعار المحلي.
     * @  أقصى عرض مسموح به بالنقاط.
     * @  أقصى ارتفاع مسموح به بالنقاط.
     * @ كائن [] مكتمل البيانات.
     */
    fun loadAndScaleLogo(context: android.content.Context, logoPath: String, maxW: Float = 70f, maxH: Float = 55f): LogoResult {
        var rawBitmap: Bitmap? = null
        var scaledLogo: Bitmap? = null
        var logoW = 0f
        var logoH = 0f
        var hasLogo = false
        try {
            if (logoPath.isNotEmpty()) {
                val logoFile = File(logoPath)
                if (logoFile.exists()) {
                    if (logoFile.length() > 1024 * 1024) {
                        val original = BitmapFactory.decodeFile(logoFile.absolutePath)
                        if (original != null) {
                            val stream = ByteArrayOutputStream()
                            original.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                            val byteArray = stream.toByteArray()
                            rawBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            original.recycle()
                        }
                    } else {
                        rawBitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                    }
                }
            }

            // استخدام أيقونة التطبيق عند تعذر تحميل الشعار المخصص
            if (rawBitmap == null) {
                rawBitmap = BitmapFactory.decodeResource(context.resources, com.smartledger.aldaftar.R.drawable.img_app_icon)
            }

            if (rawBitmap != null) {
                val originalWidth = rawBitmap.width.toFloat()
                val originalHeight = rawBitmap.height.toFloat()
                val scale = (maxW / originalWidth).coerceAtMost(maxH / originalHeight)
                val finalW = (originalWidth * scale).coerceAtLeast(1f)
                val finalH = (originalHeight * scale).coerceAtLeast(1f)

                scaledLogo = Bitmap.createScaledBitmap(rawBitmap, finalW.toInt(), finalH.toInt(), true)

                logoW = finalW
                logoH = finalH
                hasLogo = true
            }
        } catch (e: Exception) {
        }
        return LogoResult(scaledLogo, rawBitmap, logoW, logoH, hasLogo)
    }

    /**
     * [تحميل وتحجيم صورة الشعار - ]:
     * نسخة بدون سياق تطبيقي للتحميل المباشر من مسار ملف.
     */
    fun loadAndScaleLogo(logoPath: String, maxW: Float = 70f, maxH: Float = 55f): LogoResult {
        if (logoPath.isEmpty()) {
            return LogoResult(null, null, 0f, 0f, false)
        }
        var rawBitmap: Bitmap? = null
        var scaledLogo: Bitmap? = null
        var logoW = 0f
        var logoH = 0f
        var hasLogo = false
        try {
            val logoFile = File(logoPath)
            if (logoFile.exists()) {
                if (logoFile.length() > 1024 * 1024) {
                    val original = BitmapFactory.decodeFile(logoFile.absolutePath)
                    if (original != null) {
                        val stream = ByteArrayOutputStream()
                        original.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                        val byteArray = stream.toByteArray()
                        rawBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        original.recycle()
                    }
                } else {
                    rawBitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                }

                if (rawBitmap != null) {
                    val originalWidth = rawBitmap.width.toFloat()
                    val originalHeight = rawBitmap.height.toFloat()
                    val scale = (maxW / originalWidth).coerceAtMost(maxH / originalHeight)
                    val finalW = (originalWidth * scale).coerceAtLeast(1f)
                    val finalH = (originalHeight * scale).coerceAtLeast(1f)

                    scaledLogo = Bitmap.createScaledBitmap(rawBitmap, finalW.toInt(), finalH.toInt(), true)

                    logoW = finalW
                    logoH = finalH
                    hasLogo = true
                }
            }
        } catch (e: Exception) {
        }
        return LogoResult(scaledLogo, rawBitmap, logoW, logoH, hasLogo)
    }
}

