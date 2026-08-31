/**
 * =====================================================================
 * ملف: متصفح ومستكشف مجلدات جوجل درايف (GoogleDriveFolderNavigator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يختص هذا الملف بالاستعلام والتنقل والبحث داخل مجلد التطبيق المخصص السري
 * في Google Drive (والمعروف برمجياً بـ `appDataFolder`).
 * 
 * [المسؤوليات المعمارية والوظيفية]:
 * 1. البحث عن أحدث نسخة احتياطية مشفرة بصيغة `.mzd` تبدأ بـ `Mzd_`.
 * 2. سرد واستعراض جميع ملفات النسخ الاحتياطي المتوفرة في السحابة مع أحجامها وتواريخ إنشائها.
 * 3. التخزين المؤقت (In-Memory Caching) لمعرف الملف الأخير لمدة دقيقة لتجنب تكرار استهلاك واجهة برمجة التطبيقات (API Quota).
 * 4. التمييز الدقيق بين أخطاء المصادقة (401/403 Auth Errors) وأخطاء الشبكة العامة لتوجيه التطبيق لتجديد الرمز.
 */
package com.example.data

// ---------------------------------------------------------------------
// استيراد حزم الاتصال عبر OkHttp ومعالجة بيانات JSON وتشفير روابط URL
// ---------------------------------------------------------------------
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * [فئة متصفح مجلدات Drive - GoogleDriveFolderNavigator]:
 * تستقبل عميل OkHttpClient وتنفذ طلبات البحث والاستعلام في مسار IO غير المتزامن.
 */
class GoogleDriveFolderNavigator(private val client: OkHttpClient) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على روابط REST API لملفات Google Drive ومعايير البحث ومفاتيح JSON.
     */
    companion object {
        private const val TAG = "GoogleDriveFolderNavigator"

        private const val DRIVE_FILES_API_URL = "https://www.googleapis.com/drive/v3/files"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ENCODING_UTF8 = "UTF-8"

        private const val SPACE_APP_DATA_FOLDER = "appDataFolder"
        private const val QUERY_LATEST_BACKUP = "name contains 'Mzd_' and name contains '.mzd' and trashed = false"
        private const val QUERY_ALL_MZD_BACKUPS = "name contains '.mzd' and trashed = false"
        private const val ORDER_BY_CREATED_TIME_DESC = "createdTime desc"
        private const val FIELDS_BACKUPS_LIST = "files(id,name,size,createdTime)"

        private const val JSON_KEY_FILES = "files"
        private const val JSON_KEY_ID = "id"
        private const val JSON_KEY_NAME = "name"
        private const val JSON_KEY_SIZE = "size"
        private const val JSON_KEY_CREATED_TIME = "createdTime"

        private const val CACHE_EXPIRY_MS = 60_000L // كاش لمدة دقيقة لتفادي استهلاك الحصة اليومية لـ Drive API
    }

    // كاش مؤقت في الذاكرة يحفظ (وقت الاستعلام، ومعرف أحدث ملف تم العثور عليه)
    private var cachedLatestFileId: Pair<Long, String?>? = null

    /**
     * [نتيجة البحث عن ملف - FileSearchResult]:
     * فئة تمثل إما النجاح مع معرف الملف (أو null إذا لم توجد نسخ)، أو الفشل مع توضيح إذا كان الخطأ بسبب الصلاحيات.
     */
    sealed class FileSearchResult {
        data class Success(val fileId: String?) : FileSearchResult()
        data class Error(val isAuthError: Boolean, val code: Int) : FileSearchResult()
    }

    /**
     * [نتيجة استعراض قائمة النسخ - ListBackupsResult]:
     * فئة تمثل إما النجاح مع قائمة ملفات النسخ السحابية، أو الفشل مع كود الاستجابة.
     */
    sealed class ListBackupsResult {
        data class Success(val backups: List<CloudBackupFile>) : ListBackupsResult()
        data class Error(val isAuthError: Boolean, val code: Int) : ListBackupsResult()
    }

    /**
     * [دالة بناء طلب مصرح - buildAuthorizedRequest]:
     * تنشئ كائن Request مع ترويسة التفويض المعتمدة Bearer Access Token.
     */
    private fun buildAuthorizedRequest(url: String, accessToken: String): Request {
        return Request.Builder()
            .url(url)
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$accessToken")
            .get()
            .build()
    }

    /**
     * [دالة فحص أخطاء التفويض]:
     * تفحص ما إذا كان كود الاستجابة هو 401 (غير مصرح) أو 403 (ممنوع).
     */
    private fun isAuthError(code: Int): Boolean = code == 401 || code == 403

    /**
     * [دالة مسح الكاش المؤقت]:
     * تصفر القيمة المحفوظة في الذاكرة لفرض استعلام شبكي جديد عند الحاجة.
     */
    fun clearCache() {
        cachedLatestFileId = null
    }

    /**
     * [دالة البحث عن أحدث نسخة احتياطية - findLatestBackupFileId]:
     * تبحث في مساحة appDataFolder في Google Drive عن أحدث ملف .mzd غير محذوف ومرتب تنازلياً حسب وقت الإنشاء.
     */
    suspend fun findLatestBackupFileId(accessToken: String, forceRefresh: Boolean = false): FileSearchResult = withContext(Dispatchers.IO) {
        // [توثيق المتغير/الخاصية: now]: التوقيت الحالي المستخدم كأساس للحسابات الزمنية.
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            // [توثيق المتغير/الخاصية: cached]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val cached = cachedLatestFileId
            if (cached != null && (now - cached.first) < CACHE_EXPIRY_MS) {
                return@withContext FileSearchResult.Success(cached.second)
            }
        }

        try {
            // [توثيق المتغير/الخاصية: searchUrl]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
            val searchUrl = "$DRIVE_FILES_API_URL?spaces=$SPACE_APP_DATA_FOLDER" +
                    "&orderBy=${URLEncoder.encode(ORDER_BY_CREATED_TIME_DESC, ENCODING_UTF8)}" +
                    "&q=${URLEncoder.encode(QUERY_LATEST_BACKUP, ENCODING_UTF8)}"

            // [توثيق المتغير/الخاصية: searchRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val searchRequest = buildAuthorizedRequest(searchUrl, accessToken)

            client.newCall(searchRequest).execute().use { searchResponse ->
                if (searchResponse.isSuccessful) {
                    // [توثيق المتغير/الخاصية: rawBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val rawBody = searchResponse.body?.string() ?: ""
                    // [توثيق المتغير/الخاصية: searchResult]: نتيجة وسيطة أو نهائية للعملية الحالية.
                    val searchResult = JSONObject(rawBody)
                    // [توثيق المتغير/الخاصية: files]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val files = searchResult.optJSONArray(JSON_KEY_FILES)
                    // [توثيق المتغير/الخاصية: existingFileId]: معرّف مرجعي يميز العنصر أو المهمة المرتبطة به.
                    val existingFileId = if (files != null && files.length() > 0) {
                        files.getJSONObject(0).getString(JSON_KEY_ID)
                    } else {
                        null
                    }
                    cachedLatestFileId = Pair(now, existingFileId)
                    FileSearchResult.Success(existingFileId)
                } else {
                    FileSearchResult.Error(isAuthError(searchResponse.code), searchResponse.code)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء البحث عن أحدث نسخة في Drive: ${e.javaClass.simpleName}")
            FileSearchResult.Error(false, -1)
        }
    }

    /**
     * [دالة سرد كافة النسخ السحابية - listCloudBackups]:
     * تستعرض جميع ملفات .mzd المخزنة في مجلد التطبيق السحابي مع تفاصيل الحجم وتاريخ الإنشاء.
     */
    suspend fun listCloudBackups(accessToken: String): ListBackupsResult = withContext(Dispatchers.IO) {
        try {
            // [توثيق المتغير/الخاصية: url]: عنوان URI/URL المستخدم في مسار المصادقة أو الاتصال الخارجي.
            val url = "$DRIVE_FILES_API_URL?spaces=$SPACE_APP_DATA_FOLDER" +
                    "&fields=${URLEncoder.encode(FIELDS_BACKUPS_LIST, ENCODING_UTF8)}" +
                    "&q=${URLEncoder.encode(QUERY_ALL_MZD_BACKUPS, ENCODING_UTF8)}" +
                    "&orderBy=${URLEncoder.encode(ORDER_BY_CREATED_TIME_DESC, ENCODING_UTF8)}" +
                    "&pageSize=1000"

            // [توثيق المتغير/الخاصية: request]: طلب HTTP أو طلب عمل مبني للتنفيذ اللاحق.
            val request = buildAuthorizedRequest(url, accessToken)

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // [توثيق المتغير/الخاصية: rawBody]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val rawBody = response.body?.string() ?: ""
                    // [توثيق المتغير/الخاصية: json]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val json = JSONObject(rawBody)
                    // [توثيق المتغير/الخاصية: filesArray]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val filesArray = json.optJSONArray(JSON_KEY_FILES) ?: return@use ListBackupsResult.Success(emptyList())
                    // [توثيق المتغير/الخاصية: list]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                    val list = mutableListOf<CloudBackupFile>()
                    for (i in 0 until filesArray.length()) {
                        // [توثيق المتغير/الخاصية: obj]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val obj = filesArray.getJSONObject(i)
                        // [توثيق المتغير/الخاصية: id]: معرّف مرجعي يميز العنصر أو المهمة المرتبطة به.
                        val id = obj.getString(JSON_KEY_ID)
                        // [توثيق المتغير/الخاصية: name]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val name = obj.getString(JSON_KEY_NAME)
                        // [توثيق المتغير/الخاصية: size]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val size = obj.optLong(JSON_KEY_SIZE, 0L)
                        // [توثيق المتغير/الخاصية: createdTime]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
                        val createdTime = obj.optString(JSON_KEY_CREATED_TIME, "")
                        list.add(CloudBackupFile(id, name, size, createdTime))
                    }
                    ListBackupsResult.Success(list.sortedByDescending { it.name })
                } else {
                    ListBackupsResult.Error(isAuthError(response.code), response.code)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء سرد النسخ السحابية في Drive: ${e.javaClass.simpleName}")
            ListBackupsResult.Error(false, -1)
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// - يُستحسن توحيد سياسة Cache invalidation مع دورة الرفع والحذف حتى لا تظهر نسخ قديمة للمستخدم.
// - يفضل مستقبلاً تعريف ترتيب النسخ الاحتياطية بمعيار زمني/اسم ملف موثق وثابت.
// - يجب الحفاظ على عدم اعتبار فشل الشبكة مساوياً لعدم وجود ملفات.
// - هذه الملاحظات توصيات مستقبلية فقط ولا تغيّر التنفيذ الحالي أو عقده البرمجي.
