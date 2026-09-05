/**
 * =====================================================================
 * ملف: المحرك الشبكي السحابي الموحد (CloudNetworkEngine.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف البوابة المركزية المشتركة (Unified Network Gateway) لكافة
 * الاتصالات الشبكية وطلبات الـ HTTP في التطبيق، ومجردة بالكامل من أي منطق أعمال.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. عميل OkHttpClient أحادي التكوين (Singleton) لإعادة استخدام قنوات الاتصال وتقليل استهلاك الذاكرة والمعالج.
 * 2. ضبط أوقات المهلة المعيارية (Timeouts): مهلة الاتصال (15s)، ومهلة القراءة والكتابة (30s).
 * 3. آلية إعادة المحاولة الذكية (Exponential Backoff + Jitter Retry Strategy):
 *    - إعادة المحاولة للأخطاء العابرة فقط (5xx، مهلات الاتصال، انقطاع Socket).
 *    - عدم إعادة المحاولة لأخطاء المصادقة والصلاحيات (401، 403) أو الأخطاء الإلغائية (CancellationException).
 *    - مراعاة حدود مهلة إعادة المحاولة وتطبيق التشويش العشوائي (Jitter).
 * 4. نموذج النتائج الشبكية الموحد [NetworkCallResult] لتصنيف حالات النجاح، انتهاء الجلسة، المهلة، وانقطاع الإنترنت.
 * 5. حماية الخصوصية ومنع تسريب الرموز: حظر كامل لتسجيل أي رموز وصول (Tokens) أو ترويسات حساسة في سجلات التطبيق مع دالة تعتيم آمنة (Redaction).
 */
package com.smartledger.aldaftar.data.cloud

// ---------------------------------------------------------------------
// استيراد حزم الاتصال عبر OkHttp وتزامن الكوروتين والمهلات الشبكية
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * [فئة المحرك الشبكي السحابي - CloudNetworkEngine]:
 * مصممة بنمط الـ Singleton لتوفير منفذ اتصال موحد وآمن لجميع العمليات السحابية.
 */
class CloudNetworkEngine private constructor(private val context: Context) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يتضمن ثوابت المهلات الزمنية وموفر النسخة الأحادية الآمنة خيطياً (Thread-safe Singleton).
     */
    companion object {
        private const val TAG = "CloudNetworkEngine"

        // ثوابت المهلات الموحدة لعمليات الشبكة
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30L

        @Volatile
        private var instance: CloudNetworkEngine? = null

        /**
         * [دالة الحصول على النسخة الأحادية - getInstance]:
         * تضمن إنشاء نسخة وحيدة للمحرك مع قفل التزامن (Double-checked locking).
         */
        fun getInstance(context: Context): CloudNetworkEngine {
            return instance ?: synchronized(this) {
                instance ?: CloudNetworkEngine(context.applicationContext).also { instance = it }
            }
        }

        /**
         * [تعتيم البيانات الحساسة - redactSensitiveString]:
         * يضمن إخفاء أي رمز وصول أو كلمة مرور أو ترويسة تفويض من السجلات.
         */
        fun redactSensitiveString(input: String?): String {
            if (input.isNullOrBlank()) return ""
            return "[REDACTED]"
        }

        /**
         * [فحص قابلية رمز الحالة لإعادة المحاولة - isRetryableStatusCode]:
         * 5xx و 429 أخطاء عابرة تقبل إعادة المحاولة المحدودة.
         * 400 و 401 و 403 أخطاء غير قابلة لإعادة المحاولة.
         */
        fun isRetryableStatusCode(code: Int): Boolean {
            return code == 429 || code in 500..599
        }
    }

    /**
     * [عميل OkHttpClient المهيأ - client]:
     * عميل شبكي مشترك مع ضبط المهلات وسياسة إعادة المحاولة التلقائية عند فشل المسار.
     */
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * [نموذج نتائج الاتصال الموحد - NetworkCallResult]:
     * يمثل التصنيفات الصريحة لاستجابات الشبكة:
     * - Success: نجاح الطلب وإرجاع البيانات المحللة مع رمز الحالة.
     * - Unauthorized: خطأ صلاحيات أو انتهاء الجلسة (401/403).
     * - Timeout: انتهاء مهلة الاتصال بالخادم.
     * - NoConnection: انقطاع تام للإنترنت أو فشل الوصول لنظام أسماء النطاقات (DNS).
     * - RateLimited: استجابة خنق الطلبات (429) مع مهلة إعادة المحاولة إن وجدت.
     * - Error: أخطاء الخادم أو الطلبات غير الصالحة.
     */
    sealed class NetworkCallResult<out T> {
        data class Success<out T>(val data: T, val statusCode: Int) : NetworkCallResult<T>()
        data class Unauthorized(val statusCode: Int, val message: String) : NetworkCallResult<Nothing>()
        data class Timeout(val message: String) : NetworkCallResult<Nothing>()
        data class NoConnection(val message: String) : NetworkCallResult<Nothing>()
        data class RateLimited(val statusCode: Int, val retryAfterSeconds: Long?) : NetworkCallResult<Nothing>()
        data class Error(val statusCode: Int, val message: String) : NetworkCallResult<Nothing>()
    }

    /**
     * [فحص قابلية الخطأ لإعادة المحاولة - isRetryableException]:
     * يصنف الأخطاء لتحديد ما إذا كانت عابرة وتقبل إعادة المحاولة.
     */
    private fun isRetryableException(e: Throwable): Boolean {
        if (e is CancellationException) return false
        return e is IOException || e is SocketTimeoutException || e is UnknownHostException
    }

    /**
     * [فحص قابلية رمز الحالة لإعادة المحاولة - isRetryableStatusCode]:
     * 5xx و 429 أخطاء عابرة تقبل إعادة المحاولة المحدودة.
     * 400 و 401 و 403 أخطاء غير قابلة لإعادة المحاولة.
     */
    fun isRetryableStatusCode(code: Int): Boolean {
        return code == 429 || code in 500..599
    }

    /**
     * [دالة التنفيذ مع إعادة المحاولة التدريجية والتشويش - executeWithRetry]:
     * تعيد محاولة تنفيذ العمليات الشبكية المعرضة للانقطاع المؤقت مع Exponential Backoff و Jitter.
     * تحترم إلغاء Coroutine تماماً ولا تعيد المحاولة في حال الإلغاء أو الأخطاء غير القابلة للتكرار.
     */
    suspend fun <T> executeWithRetry(
        operationName: String = "NetworkOperation",
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                return@withContext block()
            } catch (e: CancellationException) {
                // الكوروتين أُلغي: توقف فوري دون أي إعادة محاولة
                Log.d(TAG, "[$operationName] العملية أُلغيت بنجاح.")
                throw e
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "[$operationName] فشل مؤقت في محاولة الاتصال ($attempt من $maxRetries): ${e.javaClass.simpleName}")
                if (attempt < maxRetries) {
                    // إضافة تشويش عشوائي (Jitter: ±20%) لمنع تزامن الطلبات المتكررة
                    val jitter = Random.nextDouble(0.8, 1.2)
                    val delayTime = (currentDelay * jitter).toLong().coerceIn(100L, 30_000L)
                    delay(delayTime)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(30_000L)
                }
            } catch (e: Exception) {
                // الأخطاء غير الشبكية لا تتطلب إعادة المحاولة
                Log.e(TAG, "[$operationName] خطأ غير قابل لإعادة المحاولة: ${e.javaClass.simpleName}")
                throw e
            }
        }
        throw lastException ?: IOException("[$operationName] فشلت محاولات الاتصال بعد $maxRetries محاولة")
    }

    /**
     * [دالة تنفيذ الطلب العام - executeRequest]:
     * تنفذ طلب OkHttp وتحلل الاستجابة عبر دالة تحويل ممررة وتعيد النتيجة ضمن [NetworkCallResult].
     */
    suspend fun <T> executeRequest(
        operationName: String,
        request: Request,
        responseParser: (Response) -> T
    ): NetworkCallResult<T> = withContext(Dispatchers.IO) {
        try {
            executeWithRetry(operationName = operationName) {
                client.newCall(request).execute().use { response ->
                    val code = response.code
                    when {
                        response.isSuccessful -> {
                            val parsedData = responseParser(response)
                            NetworkCallResult.Success(parsedData, code)
                        }
                        code == 401 || code == 403 -> {
                            Log.w(TAG, "[$operationName] خطأ في المصادقة أو الصلاحيات (رمز الحالة: $code) - لن تتم إعادة المحاولة")
                            NetworkCallResult.Unauthorized(code, "Authentication failed with status $code")
                        }
                        code == 429 -> {
                            val retryAfter = response.header("Retry-After")?.toLongOrNull()
                            Log.w(TAG, "[$operationName] خنق معدل الطلبات 429 - Retry-After: $retryAfter")
                            NetworkCallResult.RateLimited(code, retryAfter)
                        }
                        code in 400..499 -> {
                            Log.w(TAG, "[$operationName] خطأ في الطلب (رمز الحالة: $code)")
                            NetworkCallResult.Error(code, "Client request error: $code")
                        }
                        code in 500..599 -> {
                            Log.w(TAG, "[$operationName] خطأ في الخادم (رمز الحالة: $code)")
                            throw IOException("Server responded with error code $code")
                        }
                        else -> {
                            Log.w(TAG, "[$operationName] استجابة الخادم غير ناجحة (رمز الحالة: $code)")
                            throw IOException("Server responded with error code $code")
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "[$operationName] انتهاء مهلة الاتصال بالشبكة: ${e.javaClass.simpleName}")
            NetworkCallResult.Timeout("انتهت مهلة الاتصال بالشبكة")
        } catch (e: UnknownHostException) {
            Log.e(TAG, "[$operationName] تعذر الوصول للخادم (انقطاع الاتصال): ${e.javaClass.simpleName}")
            NetworkCallResult.NoConnection("لا يوجد اتصال بالإنترنت")
        } catch (e: IOException) {
            Log.e(TAG, "[$operationName] تعذر الاتصال بالشبكة: ${e.javaClass.simpleName}")
            NetworkCallResult.Error(-1, e.localizedMessage ?: "Network I/O failure")
        } catch (e: Exception) {
            Log.e(TAG, "[$operationName] فشل تنفيذ الطلب: ${e.javaClass.simpleName}")
            NetworkCallResult.Error(-1, e.localizedMessage ?: "Unexpected failure")
        }
    }
}
