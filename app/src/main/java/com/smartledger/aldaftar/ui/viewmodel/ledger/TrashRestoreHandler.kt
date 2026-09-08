package com.smartledger.aldaftar.ui.viewmodel.ledger

import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.repository.PreferenceManager
import org.json.JSONObject

object TrashRestoreHandler {
    private const val TAG = "TrashRestoreHandler"
    private const val TABLE_HABAYEB_BUNDLE = "habayeb_bundle"

    fun restorePrefsForDeletedItem(context: Context, item: DeletedItemEntity) {
        try {
            if (item.originalTableName != TABLE_HABAYEB_BUNDLE) return

            val root = JSONObject(item.jsonData)
            val customer = root.getJSONObject("customer")
            val customerId = customer.getString("id")
            val sharedPrefs = context.getSharedPreferences(
                PreferenceManager.PREFS_MIZAN_SEC,
                Context.MODE_PRIVATE
            )

            if (customer.has("categoryLink")) {
                sharedPrefs.edit()
                    .putString(
                        "${PreferenceManager.PREF_CAT_LINK_PREFIX}$customerId",
                        customer.getString("categoryLink")
                    )
                    .apply()
            }

            if (customer.has("pinnedCategories")) {
                val pinnedCategories = customer.getJSONArray("pinnedCategories")
                for (index in 0 until pinnedCategories.length()) {
                    val categoryKey = pinnedCategories.getString(index)
                    val key = "${PreferenceManager.PREF_KEY_PINNED_PREFIX}$categoryKey"
                    val existingSet = sharedPrefs.getStringSet(key, emptySet()) ?: emptySet()
                    val updatedSet = existingSet.toMutableSet().apply { add(customerId) }
                    sharedPrefs.edit().putStringSet(key, updatedSet).apply()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring preferences for trash item ${item.id}", e)
        }
    }
}
