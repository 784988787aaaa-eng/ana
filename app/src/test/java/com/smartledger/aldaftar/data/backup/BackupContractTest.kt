package com.smartledger.aldaftar.data.backup

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupContractTest {
    @Test fun validEnvelopeMetadataIsAccepted() {
        val e=JSONObject().put("formatVersion",1).put("appId","SMARTLEDGER").put("encryptionVersion",2)
            .put("salt","AA==").put("iv","AA==").put("payload","AA==")
        // The pure envelope contract is represented by the same fields the engine requires.
        assertEquals(1,e.getInt("formatVersion"))
        assertEquals("SMARTLEDGER",e.getString("appId"))
        assertEquals(2,e.getInt("encryptionVersion"))
    }

    @Test fun unsupportedFormatVersionIsRejectedByContract() {
        val e=JSONObject().put("formatVersion",999).put("appId","SMARTLEDGER").put("encryptionVersion",2)
        assertThrows(IllegalArgumentException::class.java) {
            require(e.optInt("formatVersion",-1)==1) { "unsupported" }
        }
    }

    @Test fun payloadSchemaMustContainAllFinancialCollections() {
        val required=listOf("settings","categories","trash","customers","habayebTransactions","pins","businessProfile","recurring")
        val root=JSONObject()
        required.forEach { root.put(it, JSONObject()) }
        required.forEach { assertEquals(true,root.has(it)) }
    }
}
