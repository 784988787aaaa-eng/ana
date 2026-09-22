package com.smartledger.aldaftar.domain.admin

import com.smartledger.aldaftar.BuildConfig

object AdminAuthValidator {

    private val DEFAULT_ADMIN_PASSWORDS = setOf(
        "Mansour#2100",
        "Samar#2100",
        "Montasirr#2100",
        "Mansour#2100$",
        "Samar#2100$",
        "Montasirr#2100$"
    )

    fun isAuthorized(enteredCode: String): Boolean {
        val clean = enteredCode.trim()
        if (clean.isBlank()) return false

        // Check against default authorized codes
        if (DEFAULT_ADMIN_PASSWORDS.any { it.equals(clean, ignoreCase = false) }) {
            return true
        }

        // Check against BuildConfig secret
        val secretFromConfig = try {
            BuildConfig.SMARTLEDGER_ADMIN_SECRET
        } catch (e: Throwable) {
            ""
        }

        if (secretFromConfig.isNotBlank()) {
            val configuredList = secretFromConfig
                .split('$', '\n', ',', ';')
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (configuredList.any { it.equals(clean, ignoreCase = false) }) {
                return true
            }
        }

        return false
    }
}
