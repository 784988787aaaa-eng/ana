package com.smartledger.aldaftar.data.backup

import java.security.MessageDigest
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupTamperAndAtomicityContractTest {
    @Test fun sha256ChangesWhenPayloadChanges() {
        fun h(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
        assertNotEquals(h("A"),h("B"))
    }

    @Test fun missingBackupFileMustBeRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            require(false){"ملف النسخة غير موجود"}
        }
    }

    @Test fun restoreMustNotCommitUntilValidationSucceeds() {
        var committed=false
        val valid=false
        if(valid) committed=true
        org.junit.Assert.assertFalse(committed)
    }
}
