package com.smartledger.aldaftar.domain

/**
 * Current release policy: all application features are available.
 * Kept as a single policy boundary so a future entitlement system can be
 * introduced without scattering checks through business logic.
 */
object UsagePolicy {
    fun isRestricted(): Boolean = false
}
