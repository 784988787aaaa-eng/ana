/**
 * =====================================================================
 * ملف: جسر أسعار الصرف التاريخية (LegacyExchangeRateBridge.kt)
 * =====================================================================
 * هذا الجسر هو الحد الوحيد الذي يسمح بوجود Double في مسار أسعار الصرف.
 * السبب هو أن أعمدة Room التاريخية exchangeRateSar/exchangeRateUsd/
 * exchangeRateYer مخزنة كـ REAL، وتغيير نوعها سيكسر التوافق مع قواعد المستخدمين.
 * لا تُجرى أي عملية مالية على Double؛ التحويل يتم عند حدود التخزين فقط.
 */
package com.smartledger.aldaftar.domain.finance

import java.math.BigDecimal

/** واجهة صريحة لعزل التوافق مع أعمدة Room القديمة عن الحسابات المالية. */
object LegacyExchangeRateBridge {
    /** يقرأ REAL القديم إلى BigDecimal دقيق باستخدام تمثيل القيمة العشري القياسي. */
    fun fromLegacyDouble(value: Double): BigDecimal {
        require(value.isFinite()) { "Legacy exchange rate must be finite" }
        return FinancialMath.rate(BigDecimal.valueOf(value))
    }

    /** يكتب BigDecimal إلى REAL القديم فقط عند عبور حدود Room التاريخية. */
    fun toLegacyDouble(value: BigDecimal): Double {
        return FinancialMath.rate(value).toDouble()
    }
}
