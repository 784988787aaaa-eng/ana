package com.smartledger.aldaftar.ui.viewmodel

/** Constants shared by the surviving customer/debt workspace. */
object FinanceConstants {
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
