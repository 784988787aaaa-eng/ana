package com.smartledger.aldaftar.domain.admin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AdminIssuedLicense::class], version = 1, exportSchema = false)
abstract class AdminLicenseDatabase : RoomDatabase() {
    abstract fun adminLicenseDao(): AdminLicenseDao

    companion object {
        private const val DB_NAME = "admin_license_vault.db"

        @Volatile
        private var instance: AdminLicenseDatabase? = null

        fun getInstance(context: Context): AdminLicenseDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdminLicenseDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
