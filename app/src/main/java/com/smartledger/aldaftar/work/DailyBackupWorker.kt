package com.smartledger.aldaftar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Result as WorkResult
import com.smartledger.aldaftar.data.backup.AutomaticBackupCoordinator
import com.smartledger.aldaftar.data.cloud.CloudOperationException
import com.smartledger.aldaftar.platform.notifications.BackupNotificationManager
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * النسخة اليومية الفعلية: تُحفظ محلياً أولاً، ثم تُرفع إلى Google Drive عند توفر الربط.
 * عند فشل الشبكة المؤقت، يعيد WorkManager المحاولة بدلاً من اعتبار النسخ السحابي ناجحاً.
 */
class DailyBackupWorker(
    appContext: Context,
    params: WorkerParameters,
    private val coordinator: AutomaticBackupCoordinator
) : CoroutineWorker(appContext, params) {

    private val notifications = BackupNotificationManager(appContext)

    override suspend fun doWork(): WorkResult {
        return try {
            when (val result = coordinator.runDaily()) {
                is AutomaticBackupCoordinator.Result.LocalAndCloud -> {
                    notifications.show(
                        "اكتملت النسخة الاحتياطية اليومية",
                        "تم حفظ نسخة آمنة محلياً ومزامنتها مع Google Drive."
                    )
                    WorkResult.success()
                }
                is AutomaticBackupCoordinator.Result.LocalOnly -> {
                    val error = result.cloudError
                    if (error != null && shouldRetry(error) && runAttemptCount < 3) {
                        notifications.show(
                            "تم حفظ النسخة محلياً",
                            "تعذر رفع النسخة إلى السحابة مؤقتاً؛ ستتم إعادة المحاولة تلقائياً."
                        )
                        WorkResult.retry()
                    } else {
                        notifications.show(
                            "تم حفظ النسخة الاحتياطية",
                            if (error == null) {
                                "تم حفظ نسخة اليوم في التخزين المحلي. اربط Google Drive للمزامنة السحابية."
                            } else {
                                "تم حفظ نسخة اليوم محلياً، وتعذر إكمال المزامنة السحابية."
                            }
                        )
                        WorkResult.success()
                    }
                }
                AutomaticBackupCoordinator.Result.AlreadyRunning -> WorkResult.success()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            notifications.show(
                "تعذر إكمال النسخ الاحتياطي",
                "حدث خطأ أثناء إنشاء نسخة اليوم؛ سيحاول التطبيق مرة أخرى تلقائياً."
            )
            if (runAttemptCount < 3) WorkResult.retry() else WorkResult.failure()
        }
    }

    private fun shouldRetry(error: Throwable): Boolean {
        val cause = error.cause
        return when (error) {
            is UnknownHostException, is SocketTimeoutException, is IOException -> true
            is CloudOperationException -> error.statusCode == 0 || error.statusCode == 408 || error.statusCode == 429 || error.statusCode in 500..599
            else -> cause?.let(::shouldRetry) == true
        }
    }
}
