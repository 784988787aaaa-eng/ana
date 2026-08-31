/**
 * =====================================================================
 * ملف: أدوات الرسم ومعالجة النصوص والصور في PDF (PdfDrawingUtils.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يقدم هذا الكائن مجموعة أدوات منخفضة المستوى للرسم على Android Canvas،
 * متخصصة في دعم النصوص العربية ثنائية الاتجاه (RTL BiDi)، وتنظيف الرموز
 * التعبيرية غير المتوافقة مع طباعة PDF، والتحجيم الذكي لصور الشعارات،
 * والقياس الدقيق لارتفاعات الأسطر متعددة الأسطر باستخدام [StaticLayout].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. دعم وتنسيق النصوص العربية (RTL & BiDi Text Rendering):
 *    - استخدام [StaticLayout] لتوزيع الأسطر والمحاذاة التلقائية دون تشوه في التشكيل أو اتصال الحروف.
 * 2. تنقية النصوص وإزالة الإيموجي (Emoji Sanitization):
 *    - حماية محرك PDF من الرموز التعبيرية التي تسبب تشوهات بصرية عبر [EMOJI_CLEANER_REGEX].
 * 3. المعالجة الآمنة واقتصاص وتصغير الشعارات:
 *    - تحميل صور الشعار، ضغطها إن كانت ضخمة، وحساب مقياس العرض والارتفاع المتناسب.
 * 4. إدارة دورة حياة وتدوير الصور النقطية [Bitmap]:
 *    - توفير الكائن الأصلي الخام [rawBitmapToRecycle] لضمان تفريغه من الذاكرة فور اكتمال التقرير.
 */
package com.example.data.serialization.pdf

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
 * [نتيجة معالجة وتحجيم الشعار - LogoResult]:
 * يجمع كائنات الصورة النقطية والأبعاد المحسوبة.
 *
 * @property bitmap كائن الصورة المصغرة الجاهزة للرسم.
 * @property rawBitmapToRecycle كائن الصورة النقطية الخام لتحريرها لاحقاً.
 * @property width العرض المحسوب بالنقاط.
 * @property height الارتفاع المحسوب بالنقاط.
 * @property hasLogo ما إذا كانت الصورة صالحة وتم تحميلها بنجاح.
 */
data class LogoResult(
    val bitmap: Bitmap?,
    val rawBitmapToRecycle: Bitmap?,
    val width: Float,
    val height: Float,
    val hasLogo: Boolean
)

/**
 * [الكائن الأحادي لأدوات رسم مستندات PDF - PdfDrawingUtils]:
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
     * [تنقية نصوص الـ PDF - sanitizePdfText]:
     * يستبعد الإيموجي والرموز غير المدعومة في خطوط PDF القياسية.
     *
     * @param text النص المدخل.
     * @return النص المنقى الجاهز للطباعة.
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
     * [إنشاء تخطيط ثابت للنص - createStaticLayout]:
     * يبني كائن [StaticLayout] مع مراعاة إصدار الأندرويد لضبط تباعد الأسطر والمحاذاة.
     *
     * @param text النص المراد تخطيطه.
     * @param paint أداة التلوين والخط.
     * @param width أقصى عرض متاح بالنقاط.
     * @param alignment نوع المحاذاة المطلوبة.
     * @return كائن [StaticLayout] مجهز.
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
     * [قياس الارتفاع الرأسي للنص - measureTextHeight]:
     * يحسب الارتفاع الفعلي الذي سيشغله النص عند تقسيمه على الأسطر ضمن العرض المحدد.
     *
     * @param text النص المراد قياسه.
     * @param paint أداة الخط والتلوين.
     * @param width العرض المتاح بالنقاط.
     * @param alignment المحاذاة.
     * @return الارتفاع الرأسي المحسوب بالنقاط.
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
     * [رسم التخطيط الثابت على لوحة الرسم - drawStaticLayout]:
     * ينقل إحداثيات Canvas ويرسم التخطيط النصي بدقة.
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
     * [رسم النص العربي مع التقسيم التلقائي - drawArabicText]:
     * يرسم النص العربي المنسق ويعيد الارتفاع الكلي المستهلك.
     *
     * @return ارتفاع النص المرسوم بالنقاط.
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
     * [تحميل وتحجيم صورة الشعار مع التراجع لأيقونة التطبيق - loadAndScaleLogo]:
     *
     * @param context سياق التطبيق لجلب الأيقونة الاحتياطية.
     * @param logoPath مسار ملف الشعار المحلي.
     * @param maxW أقصى عرض مسموح به بالنقاط.
     * @param maxH أقصى ارتفاع مسموح به بالنقاط.
     * @return كائن [LogoResult] مكتمل البيانات.
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

            // Fallback to app icon if no custom logo was loaded
            if (rawBitmap == null) {
                rawBitmap = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_app_icon)
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
            e.printStackTrace()
        }
        return LogoResult(scaledLogo, rawBitmap, logoW, logoH, hasLogo)
    }

    /**
     * [تحميل وتحجيم صورة الشعار - loadAndScaleLogo]:
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
            e.printStackTrace()
        }
        return LogoResult(scaledLogo, rawBitmap, logoW, logoH, hasLogo)
    }
}

