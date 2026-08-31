/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/LedgerCalculator.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * طبقة حساب عرض الدفتر؛ تجمع القيم اللازمة للواجهة دون أن تكون بديلاً عن قواعد المجال المالية الأصلية.
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
// السطر 13: object LedgerCalculator — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 15: fun computeMonthlyLedger — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 16: val chronicTx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 17: val groupedByMonth — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 18: val sortedMonthKeys — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 20: var runningForwardedBalance — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 21: val ledgerList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 24: val monthTx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 26: val monthName — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 28: val groupedByDay — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 29: val sortedDays — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 31: val dayItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 32: var monthIncomes — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 33: var monthExpenses — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 36: val dayTx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 38: val dayTimestamp — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 39: val dayDateText — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: val dayOfWeek — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 42: var dayIncome — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 43: var dayExpense — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 45: val txType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 52: val netDay — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 68: val currentMonthNet — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 69: val totalForwarded — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 70: val monthFinalBalance — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel

import com.example.data.local.entities.TransactionDb
import com.example.domain.DateUtils
import com.example.domain.model.TransactionType
import java.math.BigDecimal

/**
 * حاسبة دفتر اليومية الرئيسي (LedgerCalculator)
 * تُشكل المصدر الوحيد المعتمد لحساب الأرصدة المنقولة والصافيات الشهرية واليومية في الدفتر.
 * تعتمد الحسابات التراكمية على الدقة الكاملة لـ BigDecimal لتجنب أي تفاوت في الحسابات الدورية.
 */
object LedgerCalculator {

    fun computeMonthlyLedger(txList: List<TransactionDb>): List<MonthLedger> {
        val chronicTx = txList.sortedBy { it.timestamp }
        val groupedByMonth = chronicTx.groupBy { DateUtils.getYearMonthKey(it.timestamp) }
        val sortedMonthKeys = groupedByMonth.keys.sorted()

        var runningForwardedBalance = BigDecimal.ZERO
        val ledgerList = mutableListOf<MonthLedger>()

        for (monthKey in sortedMonthKeys) {
            val monthTx = groupedByMonth[monthKey] ?: emptyList()
            if (monthTx.isEmpty()) continue
            val monthName = DateUtils.getMonthNameArabic(monthTx.first().timestamp)

            val groupedByDay = monthTx.groupBy { DateUtils.getDayOfMonth(it.timestamp) }
            val sortedDays = groupedByDay.keys.sortedDescending()

            val dayItems = mutableListOf<DayLedger>()
            var monthIncomes = BigDecimal.ZERO
            var monthExpenses = BigDecimal.ZERO

            for (day in sortedDays) {
                val dayTx = groupedByDay[day] ?: emptyList()
                if (dayTx.isEmpty()) continue
                val dayTimestamp = dayTx.first().timestamp
                val dayDateText = DateUtils.formatDateFull(dayTimestamp)
                val dayOfWeek = DateUtils.getDayOfWeekArabic(dayTimestamp)

                var dayIncome = BigDecimal.ZERO
                var dayExpense = BigDecimal.ZERO
                for (tx in dayTx) {
                    val txType = TransactionType.fromValue(tx.type)
                    if (txType == TransactionType.INCOME) {
                        dayIncome = dayIncome.add(tx.amount)
                    } else {
                        dayExpense = dayExpense.add(tx.amount)
                    }
                }
                val netDay = dayIncome.subtract(dayExpense)

                dayItems.add(
                    DayLedger(
                        dayNumber = day,
                        dayOfWeek = dayOfWeek,
                        fullDate = dayDateText,
                        netAmount = netDay,
                        transactions = dayTx.sortedByDescending { it.timestamp }
                    )
                )

                monthIncomes = monthIncomes.add(dayIncome)
                monthExpenses = monthExpenses.add(dayExpense)
            }

            val currentMonthNet = monthIncomes.subtract(monthExpenses)
            val totalForwarded = runningForwardedBalance
            val monthFinalBalance = totalForwarded.add(currentMonthNet)

            ledgerList.add(
                MonthLedger(
                    monthKey = monthKey,
                    monthName = monthName,
                    forwardedBalance = totalForwarded,
                    netAmount = currentMonthNet,
                    finalBalance = monthFinalBalance,
                    days = dayItems
                )
            )

            runningForwardedBalance = monthFinalBalance
        }

        return ledgerList.sortedByDescending { it.monthKey }
    }
}



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
