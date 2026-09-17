# SmartLedger --- Test Architecture / Zero-Regression Contract

هذه المجموعة هي خط الاختبار التراكمي للنسخة الأخيرة فقط.

## طبقات الاختبار

1.  **Unit / Financial Policy** --- BigDecimal والدقة والتطبيع.
2.  **Currency Direction Contract** --- معنى اتجاه الزوج والضرب/القسمة.
3.  **Exchange Rate Matrix Contract** --- مباشر/عكسي/استقلال الأزواج/منع
    cross-rate.
4.  **Missing Rate Safety** --- السعر المفقود لا يتحول إلى 1:1.
5.  **Precision Boundary** --- حدود المال والسعر.
6.  **Transaction Money Semantics** --- محلي/أجنبي/مصروف/غير مصروف.
7.  **Default Currency Invariance** --- تغيير العملة الافتراضية لا يعيد
    كتابة التاريخ.
8.  **Currency Cycle Stress** --- دورات تبديل متكررة.
9.  **Recurring Snapshot / Execution / Schedule** --- ثبات التكرار وعدم
    التكرار.
10. **Room/Robolectric Integration** --- التخزين الحقيقي والتجميع
    والحساب.
11. **FinancialGoldenScenarioIntegrationTest** --- مسارات YER/SAR/USD
    الحقيقية تحت كل عملة افتراضية، بيانات قديمة وجديدة، وحساب الرصيد بعد
    Room.
12. **RateEntryToConversionRegressionTest** --- حادثة 100 USD عند 550
    YER كـ regression صريح.
13. **Trash / Backup / Restore** --- حفظ الحقيقة المالية عبر حدود
    الحالة.
14. **Report Currency Invariants** --- عدم خلط العملات في التقارير.
15. **Keyboard Lifecycle / Passive Surface** --- عدم بقاء لوحة المفاتيح
    وعدم الفتح التلقائي غير المقصود.
16. **BuildHygieneContractTest** --- منع مسارات Migration القديمة
    و`ALWAYS_VISIBLE` ومؤشرات تعارض المصدر.

## Golden Scenarios الإلزامية

### Default = YER

-   `1 USD = 550 YER` → `100 USD = 55,000 YER`.
-   `1 SAR = 139.5 YER` → `100 SAR = 13,950 YER`.
-   مجموع المعاملتين = `68,950 YER`.

### Default = SAR

-   `1 USD = 3.75 SAR` → `100 USD = 375 SAR`.
-   `1 YER = 0.0072 SAR` → `10,000 YER = 72 SAR`.
-   المجموع = `447 SAR`.
-   لا يجوز اختراع USD→YER من USD→SAR وYER→SAR.

### Default = USD

-   `1 SAR = 0.266666666667 USD` → `100 SAR = 26.666666666700 USD` قبل
    سياسة المال.
-   `1 YER = 0.001818181818 USD` → `55,000 YER` يقارب `100 USD` وفق دقة
    السعر والمال.
-   لا يستخدم Double كمصدر حقيقة.

### Historical / New

-   سجل قديم يحتفظ بـ`baseCurrencyCode` و`exchangeRate`
    و`equivalentAmount`.
-   تغيير العملة الافتراضية لا يعيد التقييم.
-   سجل جديد بعد التغيير يستخدم الـbase الجديد عندما يكون ذلك مقصودًا.
-   السجل غير المصروف يبقى بعملته ولا يدخل الإجمالي الافتراضي.

### UI → DB → Balance → Report → Excel

كل سيناريو مالي حرج يجب أن يختبر المسار الكامل، لا دالة التحويل وحدها.

## قاعدة الفشل

أي فشل في اختبار مالي أو persistence أو build hygiene هو Release Blocker
حتى يُفهم السبب الجذري ويُصلح التنفيذ أو يُصحح العقد عن قصد.

لا يجوز تعطيل اختبار فقط لجعل البناء أخضر.

## معيار القبول

قبل الإصدار:

-   تشغيل `testDebugUnitTest` كاملًا.
-   تشغيل اختبارات Room/Robolectric.
-   تكرار المجموعة للتحقق من عدم وجود Flaky Tests.
-   تشغيل `assembleDebug`.
-   تشغيل `assembleRelease` عندما تتوفر بيئة الإصدار.
-   إذا تعذر التنفيذ بسبب البيئة، يسجل كـ **Unverified** وليس Passed.
