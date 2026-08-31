/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/state/CustomersUiState.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * نموذج الحالة المرئية لواجهة العملاء؛ يحدد البيانات التي تحتاجها Compose لرسم قائمة العملاء وحالات التحميل/الخطأ والتفاعل.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 18: data class CustomerUiState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 19: val id — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 20: val name — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 21: val phone — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 22: val notes — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 23: val createdAt — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 24: val totalTransactions — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 25: val netDebt — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 26: val displayNetDebt — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 27: val displayCurrencySymbol — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 28: val lastTransactionTimestamp — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 29: val isStable — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 30: val originalCustomer — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 31: val foreignDebts — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 32: val defaultCurrencyTotal — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 33: val normalizedName — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 34: val defaultCurrencyTotalAbs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 36: val isClosed — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: data class CustomersUiState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 41: val customers — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 42: val totalOwedByThem — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 43: val totalOwedToThem — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 44: val isLoading — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 48: data class CustomerBalancesPojo — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 49: val customerId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 50: val customerName — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 51: val customerPhone — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 52: val customerNotes — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 53: val customerCreatedAt — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 54: val customerInitialType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 55: val totalTransactions — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 56: val netDebt — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 57: val lastTxTime — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 58: val hasForeign — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 60: fun toEntity — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 71: data class CustomerCurrencyBalancePojo — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 72: val customerId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 73: val currencyCode — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 74: val netBalance — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.state

import androidx.compose.runtime.Immutable
import com.example.data.local.entities.HabayebCustomer
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * نماذج حالة العرض والتجميع المحاسبي لعملاء الحبايب (Habayeb Presentation & Aggregate State Models)
 *
 * التوثيق المعماري وفصل المسؤوليات:
 * 1. `CustomerUiState`: نموذج عرض غير قابل للتغيير (@Immutable) يغذي بطاقات العملاء في واجهة المستخدم،
 *    ويفصل بين المبالغ المالية الدقيقة (BigDecimal) وبين القيم السريعة لتحديث الرسوم.
 * 2. `CustomerBalancesPojo` و `CustomerCurrencyBalancePojo`: كائنات وسيطة خفيفة الوزن لاستقبال نتائج استعلامات
 *    Room المجمعة (Aggregations) مباشرة من قاعدة البيانات دون تحميل جميع سجلات المعاملات في الذاكرة.
 */
@Immutable
data class CustomerUiState(
    val id: String,
    val name: String,
    val phone: String = "",
    val notes: String = "",
    val createdAt: Long = 0L,
    val totalTransactions: Int = 0,
    val netDebt: BigDecimal = BigDecimal.ZERO,
    val displayNetDebt: BigDecimal = BigDecimal.ZERO,
    val displayCurrencySymbol: String = "",
    val lastTransactionTimestamp: Long = 0L,
    val isStable: Boolean = true,
    val originalCustomer: HabayebCustomer,
    val foreignDebts: Map<String, BigDecimal> = emptyMap(),
    val defaultCurrencyTotal: BigDecimal = BigDecimal.ZERO,
    val normalizedName: String = "",
    val defaultCurrencyTotalAbs: BigDecimal = BigDecimal.ZERO
) {
    val isClosed: Boolean get() = defaultCurrencyTotal.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) == 0 && foreignDebts.isEmpty()
}

@Immutable
data class CustomersUiState(
    val customers: List<CustomerUiState> = emptyList(),
    val totalOwedByThem: BigDecimal = BigDecimal.ZERO,
    val totalOwedToThem: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = false
)

@Immutable
data class CustomerBalancesPojo(
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerNotes: String,
    val customerCreatedAt: Long,
    val customerInitialType: String,
    val totalTransactions: Int,
    val netDebt: BigDecimal = BigDecimal.ZERO,
    val lastTxTime: Long,
    val hasForeign: Int
) {
    fun toEntity() = HabayebCustomer(
        id = customerId,
        name = customerName,
        phone = customerPhone,
        notes = customerNotes,
        createdAt = customerCreatedAt,
        initialType = customerInitialType
    )
}

@Immutable
data class CustomerCurrencyBalancePojo(
    val customerId: String,
    val currencyCode: String,
    val netBalance: BigDecimal
)



/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
