package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.data.local.entities.PinnedCustomer
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity

@Database(
    entities = [
        AppSettings::class,
        FixedCommitment::class,
        TransactionDb::class,
        CustomCategory::class,
        DeletedItemEntity::class,
        HabayebCustomer::class,
        HabayebTransaction::class,
        PinnedCustomer::class,
        BusinessProfile::class,
        RecurringConfigEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(BigDecimalConverter::class, IntListConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun commitmentDao(): CommitmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun trashDao(): TrashDao
    abstract fun habayebDao(): HabayebDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun recurringConfigDao(): RecurringConfigDao

    companion object {
        const val DATABASE_NAME = "aldaftar_v1.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                    .also { instance = it }
            }
    }
}
