package com.smartledger.aldaftar.data.cloud

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CloudConnectionStore(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "smartledger_cloud_connection",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun token(): String? = preferences.getString(KEY_TOKEN, null)
    fun save(token: String) = preferences.edit().putString(KEY_TOKEN, token).apply()
    fun clear() = preferences.edit().remove(KEY_TOKEN).apply()

    companion object { private const val KEY_TOKEN = "drive_session_token" }
}
