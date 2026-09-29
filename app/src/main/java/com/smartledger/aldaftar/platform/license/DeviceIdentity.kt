package com.smartledger.aldaftar.platform.license

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import android.util.Base64
import java.security.Signature

class DeviceIdentity {
    companion object { private const val ALIAS = "smartledger_license_device_v1" }

    private fun publicKeyBytes(): ByteArray {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getCertificate(ALIAS)?.publicKey?.encoded
        if (existing != null) return existing
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        generator.initialize(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        return generator.generateKeyPair().public.encoded
    }

    fun fingerprint(): String = MessageDigest.getInstance("SHA-256").digest(publicKeyBytes())
        .joinToString("") { "%02X".format(it) }

    fun publicKeyBase64(): String = Base64.encodeToString(publicKeyBytes(), Base64.NO_WRAP)

    fun sign(message: String): String {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateKey = store.getKey(ALIAS, null) as java.security.PrivateKey
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(privateKey)
        signer.update(message.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
    }

    fun deviceCode(): String {
        val raw = MessageDigest.getInstance("SHA-256").digest(publicKeyBytes())
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val value = raw.copyOfRange(0, 10).map { alphabet[(it.toInt() and 0xff) % alphabet.length] }
        return "SLD-${value.take(4).joinToString("")}-${value.drop(4).take(4).joinToString("")}-${value.drop(8).take(4).joinToString("")}"
    }
}
