package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.PinnedCustomer
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity

@Database(
    entities = [
        AppSettings::class,
        CustomCategory::class,
        DeletedItemEntity::class,
        HabayebCustomer::class,
        HabayebTransaction::class,
        com.smartledger.aldaftar.data.local.entities.CustomerBalance::class,
        PinnedCustomer::class,
        BusinessProfile::class,
        RecurringConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class, IntListConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun trashDao(): TrashDao
    abstract fun habayebDao(): HabayebDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun recurringConfigDao(): RecurringConfigDao
    abstract fun customerBalanceDao(): CustomerBalanceDao

    companion object {
        const val DATABASE_NAME = "aldaftar_v1.db"

        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `business_profiles` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `logoPath` TEXT NOT NULL, `phones` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_configs` (`id` TEXT NOT NULL, `originalTxId` TEXT NOT NULL, `customerId` TEXT NOT NULL, `customerName` TEXT NOT NULL, `amount` TEXT NOT NULL, `type` TEXT NOT NULL, `description` TEXT NOT NULL, `frequency` TEXT NOT NULL, `daysOfWeek` TEXT NOT NULL, `daysOfMonth` TEXT NOT NULL, `timeHour` INTEGER NOT NULL, `timeMinute` INTEGER NOT NULL, `startDateMillis` INTEGER NOT NULL, `endDateMillis` INTEGER NOT NULL, `lastExecutedTimestamp` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `isForeign` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `foreignAmount` TEXT NOT NULL, `exchangeRate` TEXT NOT NULL, `isRateCalculated` INTEGER NOT NULL, `equivalentAmount` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `deleted_items_new` (`id` TEXT NOT NULL, `sourceSystem` TEXT NOT NULL, `originalTableName` TEXT NOT NULL, `jsonData` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL, `searchableText` TEXT NOT NULL, `amount` TEXT NOT NULL, `displayName` TEXT NOT NULL, PRIMARY KEY(`id`))")
                
                db.execSQL("INSERT OR IGNORE INTO `deleted_items_new` (`id`, `sourceSystem`, `originalTableName`, `jsonData`, `deletedAt`, `searchableText`, `amount`, `displayName`) SELECT `id`, `sourceSystem`, `originalTableName`, `jsonData`, `deletedAt`, `searchableText`, CAST(`amount` AS TEXT), `displayName` FROM `deleted_items`")
                
                db.execSQL("DROP TABLE IF EXISTS `deleted_items`")
                db.execSQL("ALTER TABLE `deleted_items_new` RENAME TO `deleted_items`")
            }
        }

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                    .also { instance = it }
            }
    }
}
