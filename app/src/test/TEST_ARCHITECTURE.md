# SmartLedger — Test Architecture / Zero-Regression Contract

هذه المجموعة تحل محل مجموعة الاختبارات السابقة بالكامل.

## مبدأ الاختبار

الاختبارات مقسمة إلى طبقات مستقلة:

1. **FinancialPolicyContractTest** — دقة BigDecimal والتطبيع.
2. **CurrencyDirectionContractTest** — معنى اتجاه زوج الصرف والضرب/القسمة.
3. **ExchangeRateMatrixContractTest** — الزوج المباشر، العكسي، استقلال الأزواج، ومنع cross-rate.
4. **CurrencyMissingRateSafetyTest** — السعر المفقود لا يتحول إلى 1:1.
5. **CurrencyPrecisionBoundaryTest** — الحدود والدقة والقيم الكبيرة.
6. **TransactionMoneySemanticsTest** — محلي/أجنبي/مصروف/غير مصروف.
7. **DefaultCurrencyInvarianceTest** — تبديل العملة الافتراضية لا يغيّر الحقيقة التاريخية.
8. **CurrencyCycleStressTest** — دورات تبديل متكررة.
9. **RecurringSnapshotContractTest** — Snapshot ثابت للتكرار.
10. **RecurringExecutionContractTest** — نسخ المعاملة دون استخدام سعر السوق الحالي.
11. **RecurringScheduleExhaustiveContractTest** — الجدولة والحدود وعدم التكرار.
12. **RecurringExecutionIntegrationTest** — Room + تنفيذ فعلي + منع التنفيذ المكرر.
13. **TrashFinancialSnapshotContractTest** — حفظ كل حقول الحقيقة المالية.
14. **TrashPersistenceExhaustiveContractTest** — الحذف/الاستعادة الفعليان.
15. **BackupContractTest** — غلاف النسخة ومخططها.
16. **BackupRoundTripContractTest** — البيانات التي يجب أن تبقى في Snapshot.
17. **BackupTamperAndAtomicityContractTest** — سلامة البيانات وعدم commit قبل التحقق.
18. **BackupRestoreIntegrationTest** — إنشاء/تغيير/استعادة فعلية.
19. **ReportCurrencyInvariantTest** — التقارير لا تخلط العملات.
20. **BackupTrashRecurringIntegrationContractTest** — ثبات الحقيقة عبر الحدود الثلاثة.

## قاعدة الفشل المقصودة

أي اختبار يفشل هنا ليس "اختبارًا سيئًا"؛ بل يعني أن التنفيذ الحالي لم يحقق العقد المعماري بعد.

خصوصًا:
- لا يوجد سعر = لا تحويل.
- لا يوجد cross-rate تلقائي.
- السعر اتجاهي.
- المعاملة المصروفة تحفظ مكافئها التاريخي.
- التكرار الثابت لا يقرأ سعر السوق الحالي.
- الحذف لا يعيد التقييم.
- الاستعادة لا تعيد التقييم.
- النسخ الاحتياطي Snapshot وليس إعادة حساب.
- تغيير العملة الافتراضية لا يعيد كتابة العملة الأصلية.

## معيار القبول

لا يعتمد الإصدار النهائي قبل:
- نجاح جميع اختبارات الوحدة.
- نجاح اختبارات Room/Robolectric.
- نجاح اختبار الضغط/الدورات.
- عدم وجود Flaky Tests.
- تشغيل المجموعة نفسها عدة مرات والحصول على النتائج نفسها.
- مراجعة أي فشل قبل تعطيل الاختبار؛ لا يجوز إلغاء اختبار لحل فشل تنفيذي.

> ملاحظة: الاختبارات ترفع مستوى الضمان وتكشف الانتهاكات، لكنها لا تعني منطقيًا ضمانًا رياضيًا مطلقًا "100%" لكل حالات العالم. الضمان العملي هنا هو منع الانتهاكات المحددة بالعقود واختبارها بصورة قابلة للتكرار.
