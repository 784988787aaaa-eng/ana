package com.smartledger.aldaftar.data.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudOperationExceptionTest {

    @Test
    fun `exception preserves status code and error code and message`() {
        val ex = CloudOperationException(
            statusCode = 404,
            errorCode = "not_found",
            userMessage = "جلسة ربط Google Drive غير موجودة أو ملغاة، أعد المحاولة."
        )

        assertEquals(404, ex.statusCode)
        assertEquals("not_found", ex.errorCode)
        assertEquals("جلسة ربط Google Drive غير موجودة أو ملغاة، أعد المحاولة.", ex.userMessage)
        assertEquals("جلسة ربط Google Drive غير موجودة أو ملغاة، أعد المحاولة.", ex.message)
        assertNull(ex.cause)
    }

    @Test
    fun `exception preserves cause when present`() {
        val rootCause = java.net.UnknownHostException("Unable to resolve host")
        val ex = CloudOperationException(
            statusCode = 0,
            errorCode = "network_offline",
            userMessage = "تعذر الاتصال بخدمة السحابة. تحقق من اتصال الإنترنت وحاول مرة أخرى.",
            cause = rootCause
        )

        assertEquals(0, ex.statusCode)
        assertEquals("network_offline", ex.errorCode)
        assertEquals(rootCause, ex.cause)
        assertTrue(ex.message!!.contains("تحقق من اتصال الإنترنت"))
    }
}
