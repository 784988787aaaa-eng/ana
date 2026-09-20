package com.smartledger.aldaftar.data.cloud

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Local-only Google Drive metadata cache.
 * No Cloudflare session or backend token is stored here.
 */
class CloudConnectionStore(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        context, "smartledger_cloud_connection",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun email(): String? = preferences.getString(KEY_EMAIL, null)
    fun folderId(): String? = preferences.getString(KEY_FOLDER_ID, null)

    fun saveEmail(email: String?) =
        preferences.edit().putString(KEY_EMAIL, email?.trim()?.lowercase()).apply()

    fun saveFolderId(folderId: String?) =
        preferences.edit().putString(KEY_FOLDER_ID, folderId).apply()

    fun clear() =
        preferences.edit().remove(KEY_EMAIL).remove(KEY_FOLDER_ID).apply()

    companion object {
        private const val KEY_EMAIL = "drive_account_email"
        private const val KEY_FOLDER_ID = "drive_backup_folder_id"
    }
}
