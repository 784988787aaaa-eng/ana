/**
 * =====================================================================
 * ملف: المحلل الرياضي لتقييم المعادلات المالية (MathEvaluator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الملف محركاً حسابياً آمناً وعالي الدقة لحساب وتقييم التعابير الرياضية
 * المدخلة في حقول المبالغ المالية (مثل: 150 + 25 * 2).
 * يدعم الأرقام الغربية والمشرقية (العربية والفارسية)، والفواصل العشرية المتنوعة،
 * ويطبق أسبقية العمليات الحسابية القياسية (الضرب والقسمة قبل الجمع والطرح)
 * بالاعتماد على [BigDecimal] لتفادي أخطاء الدقة العشرية في العمليات النقدية.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. المعالجة والتحليل المعجمي للرموز (Lexical Tokenization):
 *    - تحويل الأرقام العربية المشرقية (٠-٩) والفارسية (۰-۹) إلى أرقام لاتينية معيارية.
 *    - توحيد الفواصل العشرية (الفاصلة العربية '٫' والفاصلة اللاتينية ',' والنقطة '.').
 * 2. دعم إشارة السالب الأحادية (Unary Minus Support):
 *    - السماح بإدخال الأرقام السالبة في بداية المعادلة دون حدوث أخطاء تحليلية.
 * 3. التقييم الحسابي ثنائي المراحل (Two-Pass Evaluation):
 *    - المرحلة الأولى (Pass 1): تنفيذ عمليات الضرب والقسمة ('*', '/', '×', '÷') مع فحص القسمة على صفر.
 *    - المرحلة الثانية (Pass 2): تنفيذ عمليات الجمع والطرح ('+', '-').
 * 4. الحساب المالي الدقيق والآمن (Safe Financial BigDecimal Math):
 *    - استخدام التقريب النصف للأعلى [RoundingMode.HALF_UP] بدقة 10 خانات عشرية لمنع الاستثناءات الحسابية.
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم الأرقام العشرية فائقة الدقة وأنماط التقريب
// ---------------------------------------------------------------------
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * [تقييم وحساب التعبير الرياضي البسيط - evaluateSimpleExpression]:
 * يحلل النص المدخل، ويستخرج الأرقام والمعاملات الحسابية، ثم يحسب الناتج النهائي
 * مع تطبيق أسبقية العمليات (الضرب والقسمة أولاً، ثم الجمع والطرح).
 *
 * @param expr النص الرياضي المراد حسابه (مثال: "50 + 10 * 2" أو "١٠٠ + ٥٠").
 * @return ناتج العملية الحسابية كـ [BigDecimal] عالي الدقة، أو null إذا كان التعبير غير صالح.
 */
fun evaluateSimpleExpression(expr: String): BigDecimal? {
    val trimmed = expr.trim()
    if (trimmed.isEmpty()) return null

    return runCatching {
        // مصفوفات لتخزين الأرقام والمعاملات الحسابية المستخرجة
        val numbers = ArrayList<BigDecimal>()
        val operators = ArrayList<Char>()
        val sb = StringBuilder(16)

        var seenDotInCurrentToken = false

        // -----------------------------------------------------------------
        // مرحلة التحليل المعجمي واستخراج الرموز (Token Parsing)
        // -----------------------------------------------------------------
        for (i in 0 until trimmed.length) {
            val c = trimmed[i]
            when {
                // تجاهل الفراغات البيضاء بين الرموز
                c.isWhitespace() -> continue
                // تحويل الأرقام القياسية الغربية
                c in '0'..'9' -> sb.append(c)
                // تحويل الأرقام العربية المشرقية (٠-٩) إلى أرقام قياسية
                c in '٠'..'٩' -> sb.append((c - '٠' + '0'.code).toChar())
                // تحويل الأرقام الفارسية (۰-۹) إلى أرقام قياسية
                c in '۰'..'۹' -> sb.append((c - '۰' + '0'.code).toChar())
                // معالجة الفواصل والنقاط العشرية المتنوعة
                c == '.' || c == ',' || c == '٫' -> {
                    if (!seenDotInCurrentToken) {
                        sb.append('.')
                        seenDotInCurrentToken = true
                    } else {
                        // تكرار الفاصلة العشرية في نفس الرقم يعتبر خطأ تعبيري
                        return null
                    }
                }
                // معالجة المعاملات الحسابية
                c in "+-*/×÷" -> {
                    if (sb.isEmpty()) {
                        // السماح بإشارة السالب في بداية التعبير للرقم الأول
                        if (c == '-' && numbers.isEmpty()) {
                            sb.append('-')
                            continue
                        }
                        return null
                    }
                    val numStr = sb.toString()
                    if (numStr == "-" || numStr == "." || numStr == "-.") return null
                    numbers.add(BigDecimal(numStr))
                    sb.setLength(0)
                    seenDotInCurrentToken = false

                    // توحيد رموز الضرب والقسمة
                    operators.add(when (c) {
                        '×' -> '*'
                        '÷' -> '/'
                        else -> c
                    })
                }
                else -> return null // محرف غير معروف يؤدي لرفض التعبير
            }
        }
        // إضافة الرقم الأخير المتبقي في المخزن
        if (sb.isNotEmpty()) {
            val numStr = sb.toString()
            if (numStr == "-" || numStr == "." || numStr == "-.") return null
            numbers.add(BigDecimal(numStr))
        }

        // التحقق من صحة عدد الأرقام مقارنة بالمعاملات (يجب أن يكون عدد الأرقام = المعاملات + 1)
        if (numbers.isEmpty() || numbers.size != operators.size + 1) return null

        // -----------------------------------------------------------------
        // المرحلة الأولى من الحساب: تنفيذ عمليات الضرب والقسمة (أسبقية عليا)
        // -----------------------------------------------------------------
        val numbers2 = ArrayList<BigDecimal>(numbers.size)
        val operators2 = ArrayList<Char>(operators.size)
        numbers2.add(numbers[0])

        for (i in 0 until operators.size) {
            val op = operators[i]
            val nextNum = numbers[i + 1]
            if (op == '*' || op == '/') {
                val prevNum = numbers2.removeAt(numbers2.size - 1)
                val res = if (op == '*') {
                    prevNum.multiply(nextNum)
                } else {
                    // التحقق من القسمة على صفر لمنع الاستثناءات
                    if (nextNum.compareTo(BigDecimal.ZERO) == 0) return null
                    prevNum.divide(nextNum, 10, RoundingMode.HALF_UP)
                }
                numbers2.add(res)
            } else {
                operators2.add(op)
                numbers2.add(nextNum)
            }
        }

        // -----------------------------------------------------------------
        // المرحلة الثانية من الحساب: تنفيذ عمليات الجمع والطرح
        // -----------------------------------------------------------------
        var result = numbers2[0]
        for (i in 0 until operators2.size) {
            val op = operators2[i]
            val nextNum = numbers2[i + 1]
            result = if (op == '+') result.add(nextNum) else result.subtract(nextNum)
        }
        result
    }.getOrNull()
}
