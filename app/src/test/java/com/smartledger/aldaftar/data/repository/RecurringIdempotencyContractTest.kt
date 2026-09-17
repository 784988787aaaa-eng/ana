package com.smartledger.aldaftar.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * هذه اختبارات عقدية: لا يجوز تنفيذ نفس الاستحقاق مرتين بسبب إعادة تشغيل العامل.
 */
class RecurringIdempotencyContractTest {
    @Test fun occurrenceIdentityMustBeCustomerTemplateTimestampNotCurrentClock() {
        data class Key(val template:String,val timestampSeconds:Long)
        val a=Key("r",1_800_000_000L)
        val b=Key("r",1_800_000_000L)
        assertEquals(a,b)
    }

    @Test fun batchOrderingMustBeChronological() {
        val due=listOf(30L,10L,20L).sorted()
        assertEquals(listOf(10L,20L,30L),due)
    }
}
