package com.smartledger.aldaftar.domain

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/** Verifies the server-signed offline license lease. */
object LicenseLeaseVerifier {
    private const val ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA256withRSA"

    // This PEM is the public half of the RSA-3072 license signing key used by the Worker.
    // Never put the private key in the Android project.
    private val SERVER_PUBLIC_KEY_PEM = """
-----BEGIN PUBLIC KEY-----
MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEApciKOI4mXx0i4nsb7km8
FGIPBgjFJrCuC0oI4AiQ581ZVFDLQLbxu5khQ7mJRMcyXjfMeAbMTDkKFM4XpRmF
hUC8CTqpdYj04KkIaYVzyAfr151ZXbPrhvBf1kh/RsAsUrPzA6YBt1VIpaExad7D
HXFOQs2b30Sn7Hbh3mtRYKlPUqn5IP+jG9thtwvxAHCAemKmDubRDfpVf2ARocsb
N+TnOpeiJiKme+8zhLaY2qZYYfpEb4kgSC7PwvnYinZqb0StLgKQpaDzZxqDCYmK
/yjM+wa/vxaHD9JX0AHn06W0OTJ305t1T12uPFOrD+hbW2oJ0iL65JXELt70pSHS
4Nv0mr8kc6SVmYIRhIoIxvjZCvBpku4slA6wyCQk+pk81jpMvXgvi23IjAtnFchH
MY86vXJX0/tPi7OnX8lP753agGy4As5mUqhpwmikohBnt9hODNdrj2kz63AAjBy6
XVyZtGhwx5bh9ffiy3OEuzmybkvdWGIiSy1aSPXRBzs3AgMBAAE=
-----END PUBLIC KEY-----
""".trimIndent()

    data class Lease(
        val uid: String,
        val sessionId: String,
        val deviceHash: String,
        val email: String,
        val issuedAtMs: Long,
        val expiresAtMs: Long,
        val licenseVersion: Long
    )

    fun parseAndVerify(payloadJson: String, signatureBase64: String): Lease? {
        return try {
            val payload = JSONObject(payloadJson)
            val keys = payload.keys().asSequence().toList().sorted()
            val canonical = keys.joinToString("&") { key -> "${key}=${payload.get(key)}" }
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initVerify(publicKey())
            signature.update(canonical.toByteArray(StandardCharsets.UTF_8))
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            if (!signature.verify(signatureBytes)) return null

            Lease(
                uid = payload.getString("uid"),
                sessionId = payload.getString("sid"),
                deviceHash = payload.getString("did"),
                email = payload.getString("email"),
                issuedAtMs = payload.getLong("iat"),
                expiresAtMs = payload.getLong("exp"),
                licenseVersion = payload.getLong("lv")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun isValidForDevice(
        payloadJson: String,
        signatureBase64: String,
        expectedEmail: String,
        expectedDeviceId: String,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        val lease = parseAndVerify(payloadJson, signatureBase64) ?: return false
        if (!lease.email.equals(expectedEmail.trim(), ignoreCase = true)) return false
        if (lease.expiresAtMs < nowMs) return false
        if (nowMs + 5 * 60 * 1000L < lease.issuedAtMs) return false
        val expectedHash = HashUtils.sha256Hex(expectedDeviceId)
        return HashUtils.secureEquals(lease.deviceHash, expectedHash)
    }

    private fun publicKey(): PublicKey {
        val normalized = SERVER_PUBLIC_KEY_PEM
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        require(normalized.isNotBlank() && !normalized.contains("REPLACE_WITH"))
        val encoded = Base64.decode(normalized, Base64.DEFAULT)
        return KeyFactory.getInstance(ALGORITHM).generatePublic(X509EncodedKeySpec(encoded))
    }
}
