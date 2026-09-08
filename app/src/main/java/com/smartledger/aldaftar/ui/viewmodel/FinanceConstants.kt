package com.smartledger.aldaftar.ui.viewmodel

object FinanceConstants {
    const val PREFS_NAME = "mizan_sec_prefs"
    const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    const val KEY_CATEGORIES_POPULATED = "categories_populated"
    const val KEY_LINK_HABAYEB_DEBTS = "KEY_LINK_HABAYEB_DEBTS"


    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_ALL_APP = "*/*"


    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val DEST_BACKUP_SETTINGS = "backup_settings"

    const val DEFAULT_FALLBACK_VERSION = "1.2"
    
    const val DEFAULT_CURRENCY_CODE = "DEFAULT"
    const val FALLBACK_CURRENCY_SYMBOL = "ر.ي"

    const val CATEGORY_CLOSED = "CLOSED"
    const val TYPE_OWED_TO_THEM = "OWED_TO_THEM"
    const val TYPE_OWED_BY_THEM = "OWED_BY_THEM"

    const val FREQ_DAILY = "DAILY"
    const val FREQ_WEEKLY = "WEEKLY"
    const val FREQ_MONTHLY = "MONTHLY"
}

enum class HabayebTransactionType {
    OWED_BY_THEM,
    PAYMENT_BY_THEM,
    OWED_TO_THEM,
    PAYMENT_TO_THEM
}

