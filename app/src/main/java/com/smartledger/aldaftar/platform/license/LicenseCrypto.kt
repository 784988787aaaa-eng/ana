package com.smartledger.aldaftar.platform.license

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

class LicenseCrypto(
    private val context: Context,
    private val localEcPublicKeyOverride: PublicKey? = null,
    private val localEcKeyringOverride: Map<String, PublicKey>? = null,
    private val localRsaPublicKeyOverride: PublicKey? = null,
    private val accountRsaPublicKeyOverride: PublicKey? = null
) {
    fun sanitizeToken(rawToken: String): String {
        var cleaned = rawToken
            .replace(INVISIBLE_CHARS_REGEX, "")
            .trim()
            .trim('"', '\'', '«', '»', '`', '*')
            .trim()

        val embeddedSlk = SLK1_EMBEDDED_REGEX.find(cleaned)
        if (embeddedSlk != null) {
            return embeddedSlk.value
        }

        if (cleaned.startsWith("$SLK1_PREFIX.") || cleaned.startsWith("ey")) {
            cleaned = cleaned.replace(Regex("\\s+"), "")
        }

        return cleaned
    }

    fun verify(rawToken: String): JSONObject {
        val token = sanitizeToken(rawToken)
        require(token.isNotBlank()) { "يرجى إدخال رمز التفعيل" }

        val parts = token.split('.')
        require(parts.size == 3 && parts.all { it.isNotBlank() }) {
            "صيغة كود التفعيل غير مكتملة أو غير صالحة"
        }

        return if (parts[0] == SLK1_PREFIX) {
            verifySlk1Ecdsa(parts)
        } else {
            verifyLegacyJwtRsa(parts)
        }
    }

    private fun verifySlk1Ecdsa(parts: List<String>): JSONObject {
        val payloadB64 = parts[1]
        val sigB64 = parts[2]
        val bodyBytes = decodeBase64Url(payloadB64)
        val rawOrDerSig = decodeBase64Url(sigB64)
        require(rawOrDerSig.isNotEmpty()) { "توقيع الترخيص غير صالح" }

        val json = runCatching { JSONObject(String(bodyBytes, Charsets.UTF_8)) }
            .getOrElse { throw IllegalArgumentException("محتوى كود التفعيل غير صالح") }

        val kid = json.optString("kid").trim().ifBlank { KID_LOCAL2 }
        require(LOCAL_EC_KID_REGEX.matches(kid) && kid != KID_LOCAL1) {
            "إصدار مفتاح الترخيص غير مدعوم"
        }

        val ecKey = resolveLocalEcPublicKey(kid)
        val derSignature = toDerEcdsaSignature(rawOrDerSig)
        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(ecKey)
        verifier.update("${parts[0]}.$payloadB64".toByteArray(Charsets.UTF_8))
        require(verifier.verify(derSignature)) { "توقيع الترخيص غير صالح" }

        require(json.optString("product") == PRODUCT_SMARTLEDGER) { "الترخيص يخص منتجاً آخر" }
        require(json.optString("type").uppercase() == "LOCAL") { "نوع الترخيص لا يطابق صيغة التفعيل المحلي" }
        return json
    }

    private fun verifyLegacyJwtRsa(parts: List<String>): JSONObject {
        val headerBytes = decodeBase64Url(parts[0])
        val bodyBytes = decodeBase64Url(parts[1])
        val signature = decodeBase64Url(parts[2])
        require(signature.isNotEmpty()) { "توقيع الترخيص غير صالح" }

        val header = runCatching { JSONObject(String(headerBytes, Charsets.UTF_8)) }
            .getOrElse { throw IllegalArgumentException("ترويسة رمز الترخيص غير صالحة") }

        val alg = header.optString("alg")
        require(alg == "RS256") { "خوارزمية التوقيع غير مدعومة" }

        val kid = header.optString("kid")
        val rsaKey = when (kid) {
            KID_ACCOUNT1 -> accountRsaPublicKey()
            KID_LOCAL1 -> localRsaPublicKey()
            else -> throw IllegalArgumentException("إصدار مفتاح الترخيص غير مدعوم")
        }

        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(rsaKey)
        verifier.update("${parts[0]}.${parts[1]}".toByteArray(Charsets.UTF_8))
        require(verifier.verify(signature)) { "توقيع الترخيص غير صالح" }

        val json = runCatching { JSONObject(String(bodyBytes, Charsets.UTF_8)) }
            .getOrElse { throw IllegalArgumentException("محتوى رمز الترخيص غير صالح") }

        require(json.optString("product") == PRODUCT_SMARTLEDGER) { "الترخيص يخص منتجاً آخر" }
        val tokenType = json.optString("type").uppercase()
        when (kid) {
            KID_LOCAL1 -> require(tokenType == "LOCAL") { "مفتاح التوقيع لا يطابق نوع الترخيص" }
            KID_ACCOUNT1 -> require(tokenType == "ACCOUNT") { "مفتاح التوقيع لا يطابق نوع الترخيص" }
        }
        return json
    }

    private fun decodeBase64Url(segment: String): ByteArray {
        require(BASE64URL_SEGMENT_REGEX.matches(segment)) { "صيغة كود التفعيل غير مكتملة أو غير صالحة" }
        return runCatching {
            val normalized = segment.trimEnd('=')
            Base64.decode(normalized, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        }.getOrElse {
            throw IllegalArgumentException("صيغة كود التفعيل غير مكتملة أو غير صالحة")
        }
    }

    private fun toDerEcdsaSignature(sig: ByteArray): ByteArray {
        if (sig.size == 64) {
            val r = sig.copyOfRange(0, 32)
            val s = sig.copyOfRange(32, 64)
            require(r.any { it.toInt() != 0 } && s.any { it.toInt() != 0 }) { "توقيع الترخيص غير صالح" }
            return encodeDerIntegers(r, s)
        }
        if (sig.size >= 8 && sig[0] == 0x30.toByte()) {
            return sig
        }
        throw IllegalArgumentException("توقيع الترخيص غير صالح")
    }

    private fun encodeDerIntegers(r: ByteArray, s: ByteArray): ByteArray {
        val rDer = unsignedBigIntToDerBytes(r)
        val sDer = unsignedBigIntToDerBytes(s)
        val bodyLen = 2 + rDer.size + 2 + sDer.size
        val out = ByteArrayOutputStream(bodyLen + 4)
        out.write(0x30)
        if (bodyLen >= 128) {
            out.write(0x81)
        }
        out.write(bodyLen)
        out.write(0x02)
        out.write(rDer.size)
        out.write(rDer)
        out.write(0x02)
        out.write(sDer.size)
        out.write(sDer)
        return out.toByteArray()
    }

    private fun unsignedBigIntToDerBytes(bytes: ByteArray): ByteArray {
        var start = 0
        while (start < bytes.size - 1 && bytes[start].toInt() == 0) {
            start++
        }
        val slice = bytes.copyOfRange(start, bytes.size)
        return if ((slice[0].toInt() and 0x80) != 0) {
            byteArrayOf(0x00) + slice
        } else {
            slice
        }
    }

    private fun resolveLocalEcPublicKey(kid: String): PublicKey {
        localEcKeyringOverride?.let { overrideMap ->
            return overrideMap[kid] ?: throw IllegalArgumentException("إصدار مفتاح الترخيص غير مدعوم")
        }
        localEcPublicKeyOverride?.let { return it }

        // 1. Dedicated per-kid key file under assets/license_local_keys/<kid>.pem
        val perKidPem = runCatching {
            context.assets.open("$LOCAL_EC_KEY_DIR/$kid.pem").bufferedReader().use { it.readText() }
        }.getOrNull()
        if (!perKidPem.isNullOrBlank()) {
            return parseEcPublicKeyPem(perKidPem)
        }

        // 2. Primary keyring / single PEM in assets/license_local_ec_public.pem
        val primaryPemText = runCatching {
            context.assets.open(LOCAL_EC_KEY_ASSET).bufferedReader().use { it.readText() }
        }.getOrElse { FALLBACK_LOCAL_EC_PUBLIC_KEY }

        val parsedKeyring = parseEcKeyring(primaryPemText)
        parsedKeyring.namedKeys[kid]?.let { return it }
        if (parsedKeyring.namedKeys.isEmpty() && parsedKeyring.unnamedDefaultKey != null) {
            return parsedKeyring.unnamedDefaultKey
        }
        throw IllegalArgumentException("إصدار مفتاح الترخيص غير مدعوم")
    }

    private data class ParsedEcKeyring(
        val namedKeys: Map<String, PublicKey>,
        val unnamedDefaultKey: PublicKey?
    )

    private fun parseEcKeyring(content: String): ParsedEcKeyring {
        val named = linkedMapOf<String, PublicKey>()
        var unnamedDefault: PublicKey? = null
        var pendingKid: String? = null
        var inPem = false
        val currentPem = StringBuilder()

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            if (!inPem && line.startsWith("#")) {
                val kidMatch = KID_COMMENT_REGEX.find(line)
                if (kidMatch != null) {
                    pendingKid = kidMatch.groupValues[1].trim().uppercase()
                }
                return@forEach
            }

            if (line.startsWith("-----BEGIN PUBLIC KEY-----")) {
                inPem = true
                currentPem.clear()
                currentPem.append(line).append('\n')
                return@forEach
            }

            if (inPem) {
                currentPem.append(line).append('\n')
                if (line.startsWith("-----END PUBLIC KEY-----")) {
                    val key = parseEcPublicKeyPem(currentPem.toString())
                    val targetKid = pendingKid
                    if (!targetKid.isNullOrBlank() && LOCAL_EC_KID_REGEX.matches(targetKid)) {
                        named[targetKid] = key
                    } else if (unnamedDefault == null) {
                        unnamedDefault = key
                    }
                    pendingKid = null
                    inPem = false
                    currentPem.clear()
                }
            }
        }

        return ParsedEcKeyring(namedKeys = named, unnamedDefaultKey = unnamedDefault)
    }

    private fun parseEcPublicKeyPem(pem: String): PublicKey {
        val key = parsePemPublicKey(pem, "EC")
        require(key is ECPublicKey && key.params.curve.field.fieldSize == 256) {
            "المفتاح العام المحلي يجب أن يكون من نوع ECDSA P-256"
        }
        return key
    }

    private fun localRsaPublicKey(): PublicKey {
        localRsaPublicKeyOverride?.let { return it }
        val pem = runCatching {
            context.assets.open(LOCAL_KEY_ASSET).bufferedReader().use { it.readText() }
        }.getOrElse { FALLBACK_LOCAL_PUBLIC_KEY }
        return parsePemPublicKey(pem, "RSA")
    }

    private fun accountRsaPublicKey(): PublicKey {
        accountRsaPublicKeyOverride?.let { return it }
        val pem = context.assets.open(ACCOUNT_KEY_ASSET).bufferedReader().use { it.readText() }
        return parsePemPublicKey(pem, "RSA")
    }

    private fun parsePemPublicKey(pem: String, algorithm: String): PublicKey {
        val clean = pem.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("---") && !it.startsWith("#") }
            .joinToString("")
        return KeyFactory.getInstance(algorithm).generatePublic(
            X509EncodedKeySpec(Base64.decode(clean, Base64.DEFAULT))
        )
    }

    companion object {
        const val SLK1_PREFIX = "SLK1"
        const val KID_LOCAL2 = "LOCAL2"
        const val KID_LOCAL1 = "LOCAL1"
        const val KID_ACCOUNT1 = "ACCOUNT1"
        const val PRODUCT_SMARTLEDGER = "SMARTLEDGER"

        private const val ACCOUNT_KEY_ASSET = "license_account_public.pem"
        private const val LOCAL_KEY_ASSET = "license_local_public.pem"
        private const val LOCAL_EC_KEY_ASSET = "license_local_ec_public.pem"
        private const val LOCAL_EC_KEY_DIR = "license_local_keys"

        private val INVISIBLE_CHARS_REGEX = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2060\\u2066-\\u2069\\uFEFF]")
        private val BASE64URL_SEGMENT_REGEX = Regex("^[A-Za-z0-9_-]+={0,2}$")
        private val SLK1_EMBEDDED_REGEX = Regex("SLK1\\.[A-Za-z0-9_-]+={0,2}\\.[A-Za-z0-9_-]+={0,2}")
        private val LOCAL_EC_KID_REGEX = Regex("^LOCAL[A-Z0-9_-]{1,24}$")
        private val KID_COMMENT_REGEX = Regex("^#\\s*kid\\s*[:=]\\s*(LOCAL[A-Z0-9_-]{1,24})\\s*$", RegexOption.IGNORE_CASE)

        private const val FALLBACK_LOCAL_EC_PUBLIC_KEY = """
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE5508ADz4Kv/5UyjfV5VDO3hgfiRA
IhvR+kxY7Yu5si9U6yk+62nEEwva3sN0KW6zDbVlZCLTzGpE0ZevF6llQQ==
"""

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
