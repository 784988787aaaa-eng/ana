package com.smartledger.aldaftar.platform.license

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class LicenseCrypto(private val context: Context) {
    companion object {
        private const val ACCOUNT_KEY_ASSET = "license_account_public.pem"
        private const val LOCAL_KEY_ASSET = "license_local_public.pem"
    }

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
        val pem = context.assets.open(asset).bufferedReader().use { it.readText() }
        val clean = pem.lines().filter { !it.startsWith("---") }.joinToString("")
        return KeyFactory.getInstance("RSA").generatePublic(
            X509EncodedKeySpec(Base64.decode(clean, Base64.DEFAULT))
        )
    }
}
