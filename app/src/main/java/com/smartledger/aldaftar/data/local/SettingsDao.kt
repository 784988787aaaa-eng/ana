package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartledger.aldaftar.data.local.entities.AppSettings
import kotlinx.coroutines.flow.Flow

/** واجهة الوصول إلى سجل إعدادات التطبيق الوحيد مع قراءة تفاعلية وكتابة معلقة. */
@Dao
interface SettingsDao {

    /** يقرأ سجل الإعدادات الوحيد كتدفق لمزامنة الواجهة مع الحقيقة المخزنة. */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    /** يقرأ لقطة الإعدادات في الخلفية دون حجز خيط الواجهة. */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    /** يحفظ سجل الإعدادات باستبدال السجل ذي المعرف نفسه مع الحفاظ على التوافق. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)
}
