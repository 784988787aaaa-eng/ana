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
 * 3. آلية إعادة المحاولة الذكية (Exponential Backoff Retry Strategy) للتعامل مع التذبذبات المؤقتة في الشبكة.
 * 4. نموذج النتائج الشبكية الموحد [NetworkCallResult] لتصنيف حالات النجاح، انتهاء الجلسة، المهلة، وانقطاع الإنترنت.
 * 5. حماية الخصوصية: حظر كامل لتسجيل أي رموز وصول (Tokens) أو ترويسات حساسة في سجلات التطبيق.
 */
package com.example.data.cloud

// ---------------------------------------------------------------------
// استيراد حزم الاتصال عبر OkHttp وتزامن الكوروتين والمهلات الشبكية
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
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
    }

    /**
     * [عميل OkHttpClient المهيأ - client]:
     * عميل شبكي مشترك مع ضبط المهلات وسياسة إعادة المحاولة التلقائية عند فشل المسار.
     */
    // [توثيق المتغير/الخاصية: client]: عميل HTTP المشترك لتنفيذ الاتصالات الشبكية.
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
     * - Error: أخطاء الخادم أو الطلبات غير الصالحة.
     */
    sealed class NetworkCallResult<out T> {
        data class Success<out T>(val data: T, val statusCode: Int) : NetworkCallResult<T>()
        data class Unauthorized(val statusCode: Int, val message: String) : NetworkCallResult<Nothing>()
        data class Timeout(val message: String) : NetworkCallResult<Nothing>()
        data class NoConnection(val message: String) : NetworkCallResult<Nothing>()
        data class Error(val statusCode: Int, val message: String) : NetworkCallResult<Nothing>()
    }

    /**
     * [دالة التنفيذ مع إعادة المحاولة التدريجية - executeWithRetry]:
     * تعيد محاولة تنفيذ العمليات الشبكية المعرضة للانقطاع المؤقت مع مضاعفة وقت الانتظار تصاعدياً.
     */
    suspend fun <T> executeWithRetry(
        operationName: String = "NetworkOperation",
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        // [توثيق المتغير/الخاصية: currentDelay]: قيمة التأخير الحالية المستخدمة في آلية إعادة المحاولة التدريجية.
        var currentDelay = initialDelayMs
        // [توثيق المتغير/الخاصية: lastException]: آخر استثناء مسجل أثناء محاولات التنفيذ الفاشلة.
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                return@withContext block()
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "[$operationName] فشل مؤقت في محاولة الاتصال ($attempt من $maxRetries): ${e.javaClass.simpleName}")
                if (attempt < maxRetries) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong()
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
                    // [توثيق المتغير/الخاصية: code]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val code = response.code
                    when {
                        response.isSuccessful -> {
                            // [توثيق المتغير/الخاصية: parsedData]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                            val parsedData = responseParser(response)
                            NetworkCallResult.Success(parsedData, code)
                        }
                        code == 401 || code == 403 -> {
                            Log.w(TAG, "[$operationName] خطأ في المصادقة أو الصلاحيات (رمز الحالة: $code)")
                            NetworkCallResult.Unauthorized(code, "Authentication failed with status $code")
                        }
                        code in 400..499 -> {
                            Log.w(TAG, "[$operationName] خطأ في الطلب (رمز الحالة: $code)")
                            NetworkCallResult.Error(code, "Client request error: $code")
                        }
                        else -> {
                            Log.w(TAG, "[$operationName] استجابة الخادم غير ناجحة (رمز الحالة: $code)")
                            throw IOException("Server responded with error code $code")
                        }
                    }
                }
            }
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

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// - Singleton الشبكي يقلل إنشاء العملاء المتكرر؛ يجب مراقبة lifecycle وعدم الاحتفاظ بسياق Activity.
// - سياسة retry الحالية مناسبة للأخطاء الشبكية المؤقتة، ويجب عدم إعادة محاولة أخطاء HTTP غير القابلة للإصلاح.
// - يجب استمرار حظر Authorization headers والرموز من Log.
// - هذه الملاحظات توصيات مستقبلية فقط ولا تغيّر التنفيذ الحالي أو عقده البرمجي.
