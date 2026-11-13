package com.smartledger.aldaftar.platform.license

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class LicenseCrypto(private val context: Context) {
    fun verify(token: String): JSONObject {
        val parts = token.trim().split('.')
        require(parts.size == 3) { "رمز الترخيص غير صالح" }
        val body = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
        val signature = Base64.decode(parts[2], Base64.URL_SAFE or Base64.NO_WRAP)
        val header = JSONObject(String(Base64.decode(parts[0], Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8))
        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(publicKey(header.optString("kid")))
        verifier.update("${parts[0]}.${parts[1]}".toByteArray(Charsets.UTF_8))
        require(verifier.verify(signature)) { "توقيع الترخيص غير صالح" }
        return JSONObject(String(body, Charsets.UTF_8))
    }

    private fun publicKey(kid: String): PublicKey {
        val asset = when (kid) {
            "ACCOUNT1" -> ACCOUNT_KEY_ASSET
            "LOCAL1" -> LOCAL_KEY_ASSET
            else -> error("إصدار مفتاح الترخيص غير مدعوم")
        }
        val pem = try {
            context.assets.open(asset).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            if (kid == "LOCAL1") {
                FALLBACK_LOCAL_PUBLIC_KEY
            } else {
                throw e
            }
        }
        val clean = pem.lines().filter { !it.startsWith("---") }.joinToString("")
        return KeyFactory.getInstance("RSA").generatePublic(
            X509EncodedKeySpec(Base64.decode(clean, Base64.DEFAULT))
        )
    }

    companion object {
        private const val ACCOUNT_KEY_ASSET = "license_account_public.pem"
        private const val LOCAL_KEY_ASSET = "license_local_public.pem"
        private const val FALLBACK_LOCAL_PUBLIC_KEY = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppcJ/SBDOBuX2U7pWm/o
OquP+dEGDqKrOGPFZzqxBMQdbN3OvP4eQfKH8Tj39dLtsgDvmnntYRm4uyDjLLue
sSINYrhiu746sVe7dy6/y/+kW+Pw1r7DKtlRjOyWmqOixAbc3Iv6qkPgLFPL3hiS
dvd1cvZxPBlrQQVBwn0p1w0rRgQdYpT2ZnIii0imZFgIjm+ArmrMW+9/LZ85e3Lv
yZ9QsmY2pUM7B0AjRSe7qTMLxeYGK3CRx03Ji7ZR4kzPb9j53AbRmHEF4xlWITGT
3Ist4EdNw4ADpqIKAhWeVuH18bxrwnLv1rqOVr6KIZ/vOysLdB67OfViDhZvpFLb
HwIDAQAB
"""
    }
}
