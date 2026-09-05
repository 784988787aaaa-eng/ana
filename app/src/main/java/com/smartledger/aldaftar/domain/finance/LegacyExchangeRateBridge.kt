/**
 * =====================================================================
 * ملف: جسر أسعار الصرف التاريخية (.)
 * =====================================================================
 * هذا الجسر هو الحد الوحيد الذي يسمح بوجود  في مسار أسعار الصرف.
 * السبب هو أن أعمدة  التاريخية //
 *  مخزنة كـ ، وتغيير نوعها سيكسر التوافق مع قواعد المستخدمين.
 * لا تُجرى أي عملية مالية على ؛ التحويل يتم عند حدود التخزين فقط.
 */
package com.smartledger.aldaftar.domain.finance

import java.math.BigDecimal

/** واجهة صريحة لعزل التوافق مع أعمدة  القديمة عن الحسابات المالية. */
object LegacyExchangeRateBridge {
    /** يقرأ  القديم إلى  دقيق باستخدام تمثيل القيمة العشري القياسي. */
    fun fromLegacyDouble(value: Double): BigDecimal {
        require(value.isFinite()) { "Legacy exchange rate must be finite" }
        return FinancialMath.rate(BigDecimal.valueOf(value))
    }

    /** يكتب  إلى  القديم فقط عند عبور حدود  التاريخية. */
    fun toLegacyDouble(value: BigDecimal): Double {
        return FinancialMath.rate(value).toDouble()
    }
}
