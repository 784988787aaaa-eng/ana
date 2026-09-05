package com.smartledger.aldaftar.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * مخزن تفضيلات مشفر لا يكتب القيم الحساسة كنص صريح.
 * يستخدم Android Keystore عند نجاحه، وعند تعذره يستخدم PBKDF2-HMAC-SHA256 + AES-GCM
 * مع ملح تثبيت عشوائي محفوظ داخل مساحة التطبيق الخاصة. لا توجد أي عودة إلى SharedPreferences
 * عادية عند فشل التشفير.
 */
class SecurePreferenceStore(
    context: Context,
    private val fileName: String,
    private val keystoreAlias: String
) : SharedPreferences {

    private val appContext = context.applicationContext
    private val file = File(appContext.noBackupFilesDir, "$fileName.secure")
    private val backupFile = File(appContext.noBackupFilesDir, "$fileName.secure.bak")
    private val saltFile = File(appContext.noBackupFilesDir, "$fileName.salt")
    private val lock = Any()
    private val listeners = CopyOnWriteArrayList<SharedPreferences.OnSharedPreferenceChangeListener>()
    private val writer = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "secure-pref-writer").apply { isDaemon = true }
    }
    private val values = LinkedHashMap<String, Any?>()
    @Volatile private var loaded = false
    @Volatile private var cryptoKey: SecretKey? = null

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(lock) {
            if (loaded) return
            cryptoKey = createKeystoreKeyOrFallback()
            loadFromDisk()
            loaded = true
        }
    }

    private fun createKeystoreKeyOrFallback(): SecretKey {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (!keyStore.containsAlias(keystoreAlias)) {
                val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
                generator.init(android.security.keystore.KeyGenParameterSpec.Builder(
                    keystoreAlias,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build())
                generator.generateKey()
            }
            return (keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry).secretKey
        } catch (_: Throwable) {
            // لا نعود أبداً إلى SharedPreferences غير مشفرة؛ نستخدم طبقة PBKDF2 المشفرة فقط.
            return fallbackKey()
        }
    }

    private fun fallbackKey(): SecretKey {
        val salt = if (saltFile.exists()) saltFile.readBytes() else ByteArray(32).also {
            SecureRandom().nextBytes(it)
            saltFile.outputStream().use { output -> output.write(it) }
        }
        val certificateDigest = signingCertificateSha256()
        val passwordChars = (appContext.packageName + "|" + certificateDigest + "|" + salt.toHex()).toCharArray()
        return try {
            val spec = PBEKeySpec(passwordChars, salt, 120_000, 256)
            try {
                SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES")
            } finally {
                spec.clearPassword()
                passwordChars.fill('\u0000')
            }
        } catch (t: Throwable) {
            passwordChars.fill('\u0000')
            throw IllegalStateException("Secure fallback encryption is unavailable", t)
        }
    }

    private fun signingCertificateSha256(): String {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            appContext.packageManager.getPackageInfo(
                appContext.packageName,
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            appContext.packageManager.getPackageInfo(
                appContext.packageName,
                android.content.pm.PackageManager.GET_SIGNATURES
            ).signatures
        }
        val certificate = signatures?.firstOrNull()?.toByteArray()
            ?: throw IllegalStateException("Application signing certificate unavailable")
        return MessageDigest.getInstance("SHA-256").digest(certificate).joinToString("") { "%02x".format(it) }
    }

    private fun loadFromDisk() {
        val source = when {
            file.exists() -> file
            backupFile.exists() -> backupFile
            else -> null
        } ?: return
        try {
            val payload = decrypt(source.readBytes(), cryptoKey ?: error("Missing secure key"))
            val root = JSONObject(String(payload, StandardCharsets.UTF_8))
            val entries = root.optJSONObject("entries") ?: JSONObject()
            values.clear()
            val keys = entries.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val entry = entries.getJSONObject(key)
                values[key] = decodeValue(entry)
            }
        } catch (t: Throwable) {
            throw IllegalStateException("Encrypted preference store is corrupted or inaccessible", t)
        }
    }

    private fun decodeValue(entry: JSONObject): Any? = when (entry.getString("type")) {
        "string" -> entry.optString("value", "")
        "boolean" -> entry.getBoolean("value")
        "int" -> entry.getInt("value")
        "long" -> entry.getLong("value")
        "float" -> entry.getDouble("value").toFloat()
        "stringSet" -> buildSet {
            val array = entry.getJSONArray("value")
            for (i in 0 until array.length()) add(array.getString(i))
        }
        "null" -> null
        else -> throw IllegalStateException("Unknown secure preference type")
    }

    private fun encodeValue(value: Any?): JSONObject = JSONObject().apply {
        when (value) {
            null -> put("type", "null")
            is String -> put("type", "string").put("value", value)
            is Boolean -> put("type", "boolean").put("value", value)
            is Int -> put("type", "int").put("value", value)
            is Long -> put("type", "long").put("value", value)
            is Float -> put("type", "float").put("value", value.toDouble())
            is Set<*> -> {
                put("type", "stringSet")
                put("value", JSONArray(value.map { it.toString() }))
            }
            else -> throw IllegalArgumentException("Unsupported preference value type")
        }
    }

    private fun encryptedPayload(snapshot: Map<String, Any?>): ByteArray {
        val root = JSONObject()
        val entries = JSONObject()
        snapshot.forEach { (key, value) -> entries.put(key, encodeValue(value)) }
        root.put("version", 1).put("entries", entries)
        return encrypt(root.toString().toByteArray(StandardCharsets.UTF_8), cryptoKey ?: error("Missing secure key"))
    }

    private fun persistSnapshot(snapshot: Map<String, Any?>) {
        val encrypted = encryptedPayload(snapshot)
        val temp = File(appContext.noBackupFilesDir, "$fileName.secure.tmp")
        FileOutputStream(temp).use { output ->
            output.write(encrypted)
            output.fd.sync()
        }
        synchronized(lock) {
            if (file.exists()) {
                file.copyTo(backupFile, overwrite = true)
            }
            if (!temp.renameTo(file)) {
                temp.delete()
                throw IllegalStateException("Atomic secure preference replacement failed")
            }
            backupFile.delete()
        }
    }

    private fun encrypt(plain: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plain)
        return ByteArray(1 + iv.size + ciphertext.size).also {
            it[0] = 1
            System.arraycopy(iv, 0, it, 1, iv.size)
            System.arraycopy(ciphertext, 0, it, 1 + iv.size, ciphertext.size)
        }
    }

    private fun decrypt(payload: ByteArray, key: SecretKey): ByteArray {
        require(payload.size > 13 && payload[0].toInt() == 1) { "Unsupported secure preference payload" }
        val iv = payload.copyOfRange(1, 13)
        val ciphertext = payload.copyOfRange(13, payload.size)
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            doFinal(ciphertext)
        }
    }

    override val all: Map<String, *> get() = synchronized(lock) { ensureLoaded(); values.toMap() }
    override fun getString(key: String?, defValue: String?): String? = synchronized(lock) { ensureLoaded(); values[key] as? String ?: defValue }
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = synchronized(lock) {
        ensureLoaded(); (values[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues
    }
    override fun getInt(key: String?, defValue: Int): Int = synchronized(lock) { ensureLoaded(); (values[key] as? Number)?.toInt() ?: defValue }
    override fun getLong(key: String?, defValue: Long): Long = synchronized(lock) { ensureLoaded(); (values[key] as? Number)?.toLong() ?: defValue }
    override fun getFloat(key: String?, defValue: Float): Float = synchronized(lock) { ensureLoaded(); (values[key] as? Number)?.toFloat() ?: defValue }
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = synchronized(lock) { ensureLoaded(); values[key] as? Boolean ?: defValue }
    override fun contains(key: String?): Boolean = synchronized(lock) { ensureLoaded(); values.containsKey(key) }

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.addIfAbsent(listener) }
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.remove(listener) }

    private inner class EditorImpl : SharedPreferences.Editor {
        private val changes = LinkedHashMap<String, Any?>()
        private var clearRequested = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = put(key, value)
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = put(key, values?.toSet())
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = put(key, value)
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = put(key, value)
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = put(key, value)
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = put(key, value)
        override fun remove(key: String?): SharedPreferences.Editor { if (key != null) changes[key] = REMOVED; return this }
        override fun clear(): SharedPreferences.Editor { clearRequested = true; changes.clear(); return this }

        private fun put(key: String?, value: Any?): SharedPreferences.Editor { if (key != null) changes[key] = value; return this }

        override fun commit(): Boolean = applyChanges(wait = true)
        override fun apply() { applyChanges(wait = false) }

        private fun applyChanges(wait: Boolean): Boolean {
            val changedKeys: List<String>
            val snapshot: Map<String, Any?>
            try {
                synchronized(lock) {
                    ensureLoaded()
                    val changed = ArrayList<String>()
                    if (clearRequested) {
                        changed.addAll(values.keys)
                        values.clear()
                    }
                    changes.forEach { (key, value) ->
                        if (value === REMOVED) {
                            if (values.remove(key) != null) changed.add(key)
                        } else {
                            values[key] = value
                            changed.add(key)
                        }
                    }
                    changedKeys = changed.distinct()
                    snapshot = values.toMap()
                }
            } catch (_: Throwable) {
                return false
            }

            // apply() يجعل القراءة التالية ترى القيمة فوراً، ثم تتم كتابة القرص في خيط مستقل.
            val persist = Runnable {
                try {
                    synchronized(lock) { persistSnapshot(snapshot) }
                } catch (_: Throwable) {
                    // لا نكتب بيانات جزئية؛ تبقى الحالة في الذاكرة حتى المحاولة التالية.
                }
            }
            if (wait) {
                persist.run()
            } else {
                writer.execute(persist)
            }
            changedKeys.forEach { key -> listeners.forEach { listener -> listener.onSharedPreferenceChanged(this@SecurePreferenceStore, key) } }
            return true
        }
    }

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (byte in this@toHex) append("%02x".format(byte.toInt() and 0xff))
    }

    companion object { private val REMOVED = Any() }
}
