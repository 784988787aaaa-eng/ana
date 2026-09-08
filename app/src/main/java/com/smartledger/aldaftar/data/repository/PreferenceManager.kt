package com.smartledger.aldaftar.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Access to the existing shared preference stores used by repository data operations.
 *
 * The two preference files are retained for compatibility with existing stored data.
 */
class PreferenceManager(private val context: Context) {

    companion object {
        const val PREFS_MIZAN_SEC = "mizan_sec_prefs"
        const val PREFS_MIZAN_FINANCE = "mizan_finance_prefs"
        const val PREF_CAT_LINK_PREFIX = "CAT_LINK_"
        const val PREF_KEY_PINNED_PREFIX = "KEY_PINNED_IN_"
    }

    fun getSecurityPreferences(): SharedPreferences =
        context.getSharedPreferences(PREFS_MIZAN_SEC, Context.MODE_PRIVATE)

    fun writeDualPreference(
        action: (SharedPreferences.Editor, SharedPreferences.Editor) -> Unit
    ) {
        val securityPrefs = getSecurityPreferences()
        val financePrefs = context.getSharedPreferences(PREFS_MIZAN_FINANCE, Context.MODE_PRIVATE)
        val securityEditor = securityPrefs.edit()
        val financeEditor = financePrefs.edit()

        action(securityEditor, financeEditor)

        securityEditor.apply()
        financeEditor.apply()
    }
}
