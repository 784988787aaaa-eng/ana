package com.smartledger.aldaftar.data.backup

object BackupConstants {

    const val BACKUP_FILE_EXTENSION = ".mzd"

    const val BACKUP_FILE_PREFIX = "Mizan_"

    const val BACKUP_CLOUD_FILE_PREFIX = "Mzd_"

    const val BACKUP_SILENT_FILE_NAME = "Mizan_Silent_Backup.mzd"

    const val BACKUP_TEMP_PREFIX = "tmp_backup_"

    const val BACKUP_TEMP_SUFFIX = ".tmp"


    const val BACKUP_DATE_FORMAT = "yyyy-MM-dd_HH-mm"

    const val MONTH_DATE_PATTERN = "yyyy-MM"

    const val PREFS_BACKUP = "mizan_backup_prefs"

    const val KEY_LAST_SUCCESSFUL_BACKUP = "last_successful_backup_timestamp"

    const val KEY_PENDING_CLOUD_UPLOAD = "pending_cloud_upload"

    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_ALL_APP = "application/*"
    const val MIME_TYPE_OCTET_STREAM = "application/octet-stream"

    const val FINANCIAL_SCALE = 4
    const val MAX_BACKUP_BYTES = 64L * 1024L * 1024L
    const val CURRENT_BACKUP_VERSION = "1.2.0"

    const val JSON_KEY_MIZAN_AL_DAR_DB = "mizan_al_dar_db"
    const val JSON_KEY_HABAYEB_DEBTS_DB = "habayeb_debts_db"
    const val JSON_KEY_SETTINGS = "settings"
    const val JSON_KEY_TRANSACTIONS = "transactions"
    const val JSON_KEY_COMMITMENTS = "commitments"
    const val JSON_KEY_FIXED_COMMITMENTS = "fixed_commitments"
    const val JSON_KEY_HABAYEB_CUSTOMERS = "habayeb_customers"
    const val JSON_KEY_HABAYEB_TRANSACTIONS = "habayeb_transactions"
    const val JSON_KEY_DEBT_TRANSACTIONS = "debt_transactions"
    const val JSON_KEY_CUSTOMERS = "customers"
    const val JSON_KEY_DELETED_ITEMS = "deleted_items"
    const val JSON_KEY_CUSTOM_CATEGORIES = "custom_categories"
    const val JSON_KEY_PINNED_CUSTOMERS = "pinned_customer_ids_by_category"
    const val JSON_KEY_CATEGORY_ORDER_LIST = "category_order_list"
    const val JSON_KEY_CLOSED_CUSTOM_NAME = "closed_custom_name"
}
