package com.smartledger.aldaftar.domain.admin

import android.util.Base64
import com.smartledger.aldaftar.BuildConfig
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID

object AdminLicenseSigner {

    private const val FALLBACK_PRIVATE_KEY_PEM = """-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmlwn9IEM4G5fZ
Tulab+g6q4/50QYOoqs4Y8VnOrEExB1s3c68/h5B8ofxOPf10u2yAO+aee1hGbi7
IOMsu56xIg1iuGK7vjqxV7t3Lr/L/6Rb4/DWvsMq2VGM7Jaao6LEBtzci/qqQ+As
U8veGJJ293Vy9nE8GWtBBUHCfSnXDStGBB1ilPZmciKLSKZkWAiOb4Cuasxb738t
nzl7cu/Jn1CyZjalQzsHQCNFJ7upMwvF5gYrcJHHTcmLtlHiTM9v2PncBtGYcQXj
GVYhMZPciy3gR03DgAOmogoCFZ5W4fXxvGvCcu/Wuo5Wvoohn+87Kwt0Hrs59WIO
Fm+kUtsfAgMBAAECggEABe570xD6g71rxzeJlIhGhwxDyJvaMNw253+Z8EaFNgu7
lnxBV7Zeom99yqeCILUddIiW/3OW8a2mR7dwj0+w41K4kkrJbTDlSxo6GRDHkH5u
Pj1b/nF1ZomtXyyvYhswePVbmNi9GKHpEfr719EdKfI+stS2jHLFQF/tCv1E55zI
Lq6RX2p/0019aFniXoB+W5AuRjL63ad4XhsmYMk8p0jupFnO6pALNI1qq+9yKbKt
4YF6n1BGKLAJl+2j/KKtGOz6nDryEa2Zh9lVlfcUPgMM5NVPq4pMlhaMRz3ow+51
WxGVEhfCU/nZ3AYDqbeMLwl/7cgxVhgdTAS6Xe3/KQKBgQDd3Kkn3SaXhaKR2Qpq
SalwljPsBDto6Dl2wFFIxgUsuNWGcTqHcZN+vgwXBwfJr3r8kXoFXYmfiUbWZ2LR
7EyLBiZwVGlhui455XnTw5X80bZxdRbRat7brMq8TIHXXB+q1QDPUPAVPs0XpNsV
Gp86EhdXMpF8hKy3yfez8tEJqQKBgQDAOSqupxH4p0o/sgvpA5qWbXMqNTXMl0Yh
RGHMgzm2gHXDT+BziSb0/l7BABKo8zVx+aJ1Vdb8qyncRZit18JP+cnYjN/w7J0s
C1noXBvQaKcbxDGVGe+HDIgAHXLdUn/jtf/jWXMCjkD341M6QRG5GcC3Bc2hMWwO
sBGGsBiLhwKBgQDOXvBV7Wd1cE/tPKdsgMKEwKLhenpMth6lsHSrrc6ob/HmkbJw
xJv/PAaA8QA0ge9zulp3XS+j34yt6RNJ03TLqNfeixZPWmCfaC5vZbJBrkz3soOc
Hr3YNx2KE9x/F+k4/dM4BMCd8oHR6X4EXnQYDzX43UfpPHxwpQStNUNuqQKBgQCk
Gem7Urv/6Gl1uT17vuzhUS0JMoXsVJS1X9iSLNE/YamnhPCcEdGlQ+eGotZLT94n
oDM2MoMoD8Pb3bp7Zv/nINtVuOaRmp6PdF1cBg5kvIM1LCgcHxki1OriFkHyejTe
JIkBAWMzIliKh5KM3/IOcNLaLOUgoL2cSGmd74X0zwKBgH8pELcS4dPA47Iq5Ll+
MaMIcvhi9MJooqMNIvfnhU0OfXBgFFZK1+e0IDfwMVj4+6RL/cy0XlhVTZ1LdZgd
hG2Ie6ewvE3fdn+Wq8Jv7M+XLRh7QvZ6bL2XDZ0MvVwHRoso9NFV+G8ohPyEPgwq
jOo/0FTtnUwuMztcE76EhZu2
-----END PRIVATE KEY-----"""

    private fun getPrivateKey(): PrivateKey {
        val configured = try {
            BuildConfig.SMARTLEDGER_LOCAL_LICENSE_PRIVATE
        } catch (e: Throwable) {
            ""
        }
        val pem = if (configured.isNotBlank() && configured.contains("PRIVATE KEY")) {
            configured
        } else {
            FALLBACK_PRIVATE_KEY_PEM
        }
        val clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
            .replace("\\n", "")
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        val spec = PKCS8EncodedKeySpec(bytes)
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }

    fun generateLicenseId(): String {
        val rand = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        return "SL-$rand"
    }

    fun generateShortActivationCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        fun randomChunk(len: Int) = (1..len).map { chars.random() }.joinToString("")
        return "${randomChunk(4)}-${randomChunk(4)}-${randomChunk(4)}"
    }

    fun signLicense(
        deviceOrAccountCode: String,
        email: String,
        customerName: String,
        licenseType: String, // "LOCAL", "ACCOUNT", "FULL"
        plan: String,        // "LIFETIME", "TRIAL"
        maxDevices: Int = 1,
        trialDays: Int = 0,
        notes: String = ""
    ): AdminIssuedLicense {
        val licenseId = generateLicenseId()
        val shortCode = generateShortActivationCode()
        val now = System.currentTimeMillis()

        // 1. Build Header
        val kid = if (licenseType == "ACCOUNT") "ACCOUNT1" else "LOCAL1"
        val headerJson = JSONObject().apply {
            put("alg", "RS256")
            put("kid", kid)
        }

        // 2. Build Payload
        val code = deviceOrAccountCode.trim()
        val bodyJson = JSONObject().apply {
            put("product", "SMARTLEDGER")
            // Even if marked as FULL, local verification requires type LOCAL
            val resolvedType = if (licenseType == "ACCOUNT") "ACCOUNT" else "LOCAL"
            put("type", resolvedType)
            put("plan", plan)
            put("licenseType", plan)
            put("licenseId", licenseId)
            put("deviceCode", code)
            put("accountCode", code)
            if (email.isNotBlank()) {
                put("email", email.trim().lowercase())
            }
            put("customerName", customerName.trim())
            put("maxDevices", maxDevices.coerceAtLeast(1))
            put("active", true)
            put("issuedAt", now)
            if (plan == "TRIAL" && trialDays > 0) {
                val endsAt = now + (trialDays.toLong() * 24L * 3600L * 1000L)
                put("trialEndsAt", endsAt)
            }
            put("offlineUntil", 253402300799000L) // Far future (Year 9999)
        }

        // 3. Base64 URL encode
        val headerB64 = Base64.encodeToString(headerJson.toString().toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
        val bodyB64 = Base64.encodeToString(bodyJson.toString().toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
        val signInput = "$headerB64.$bodyB64"

        // 4. RSA-SHA256 signature
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(getPrivateKey())
        signer.update(signInput.toByteArray(Charsets.UTF_8))
        val sigBytes = signer.sign()
        val sigB64 = Base64.encodeToString(sigBytes, Base64.URL_SAFE or Base64.NO_WRAP)

        val fullToken = "$signInput.$sigB64"

        return AdminIssuedLicense(
            licenseId = licenseId,
            accountCode = code,
            customerEmail = email.trim(),
            customerName = customerName.trim(),
            licenseType = licenseType,
            plan = plan,
            maxDevices = maxDevices,
            trialDays = trialDays,
            activationToken = fullToken,
            shortActivationCode = shortCode,
            notes = notes.trim(),
            issuedAt = now,
            isActive = true
        )
    }
}
