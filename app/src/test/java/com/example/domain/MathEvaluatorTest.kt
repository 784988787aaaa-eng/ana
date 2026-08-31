/*
 * التوثيق الهندسي العربي — الدفعة 13 / Automated Tests
 * الملف: MathEvaluatorTest.kt
 *
 * هذا الملف هو اختبار آلي من منظومة مشروع «الدفتر الذكي / ميزان الدار».
 * الغرض من التوثيق المضاف هنا هو شرح ما يختبره الملف وكيف يحمي العقود
 * الحسابية والبيانية أثناء التطوير، دون تغيير أي سطر تنفيذي أصلي.
 *
 * قاعدة الثبات: الكتلة البرمجية الأصلية أدناه محفوظة حرفياً؛ الإضافة الوحيدة
 * هي التعليقات التوثيقية والمعمارية.
 */

package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * اختبارات الحالات الطرفية والرياضية لتقييم التعبيرات الحسابية (MathEvaluator)
 *
 * التوثيق المعماري:
 * يتم فحص الأرقام المشرقية، أولوية العمليات الحسابية، القسمة على الصفر، والفواصل العشرية المتعددة
 * لضمان عدم حدوث أي انهيار أو حسابات خاطئة أثناء إدخال المبالغ المالية.
 */
class MathEvaluatorTest {

    @Test
    fun testSimpleAdditionAndSubtraction() {
        val result = evaluateSimpleExpression("100 + 50 - 25")
        assertEquals(BigDecimal("125"), result)
    }

    @Test
    fun testOperatorPrecedence() {
        // الضرب له أولوية على الجمع
        val result = evaluateSimpleExpression("10 + 5 * 2")
        assertEquals(BigDecimal("20"), result)

        val result2 = evaluateSimpleExpression("100 - 20 / 4")
        assertEquals(BigDecimal("95.0000000000"), result2)
    }

    @Test
    fun testEasternAndPersianNumerals() {
        // أرقام عربية مشرقية: ١٠٠ + ٥٠
        val easternResult = evaluateSimpleExpression("١٠٠ + ٥٠")
        assertEquals(BigDecimal("150"), easternResult)

        // أرقام فارسية: ۱۰۰ × ۲
        val persianResult = evaluateSimpleExpression("۱۰۰ × ۲")
        assertEquals(BigDecimal("200"), persianResult)
    }

    @Test
    fun testArabicDecimalSeparators() {
        // فاصلة عربية ٫ أو ,
        val result1 = evaluateSimpleExpression("10٫5 + 2,5")
        assertEquals(BigDecimal("13.0"), result1)
    }

    @Test
    fun testDivisionByZeroReturnsNull() {
        // حماية تامة ضد ArithmeticException عند القسمة على الصفر
        val result = evaluateSimpleExpression("500 / 0")
        assertNull(result)

        val result2 = evaluateSimpleExpression("100 ÷ 0")
        assertNull(result2)
    }

    @Test
    fun testMalformedAndEmptyExpressions() {
        assertNull(evaluateSimpleExpression(""))
        assertNull(evaluateSimpleExpression("   "))
        assertNull(evaluateSimpleExpression("abc + 123"))
        assertNull(evaluateSimpleExpression("++"))
        assertNull(evaluateSimpleExpression("100 +"))
        assertNull(evaluateSimpleExpression("100 .. 5"))
    }

    @Test
    fun testUnaryMinus() {
        val result = evaluateSimpleExpression("-50 + 100")
        assertEquals(BigDecimal("50"), result)
    }
}


// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) يُحافظ على استقلال الاختبار عن تفاصيل التنفيذ غير الضرورية قدر الإمكان.
// 2) يجب أن يبقى كل اختبار حامياً لعقد سلوكي قابل للملاحظة، لا لتفاصيل داخلية قابلة لإعادة الهيكلة.
// 3) عند إضافة حالات حدية جديدة، يُفضّل تغطية القيم الصفرية، السالبة، العشرية،
//    وفشل التحويل أو الترحيل بحسب طبيعة الوحدة التي يختبرها الملف.
// 4) أي تحسين مستقبلي يجب أن يتم في نسخة تطوير مستقلة، مع إبقاء هذا الملف
//    دون تعديل وظيفي أثناء مرحلة التوثيق الحالية.
