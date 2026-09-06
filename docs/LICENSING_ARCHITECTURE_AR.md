# هندسة الترخيص السحابي — Cloudflare Worker + D1

Google Account
→ Firebase Authentication
→ Firebase App Check / Play Integrity
→ Cloudflare Worker (server authority)
→ D1: `licenses` / `devices` / `license_sessions` / `license_audit`
→ signed 30-day RSA lease
→ Android encrypted storage

## قواعد السلطة

1. Android لا يكتب بيانات الترخيص مباشرة.
2. Firebase UID هو هوية الترخيص الأساسية، والبريد يستخدم للتحقق والمطابقة والهجرة القديمة فقط.
3. `devices_max` يحدده المشرف فقط.
4. التفعيل/التجديد/الإلغاء تنفذها سلطة Worker على الخادم.
5. عند امتلاء الحد، يُستبدل أقدم جهاز كما في المنظومة السابقة.
6. خروج هاتف واحد يلغي جلسة ذلك الهاتف فقط؛ الأجهزة الأخرى تبقى مرخصة.
7. تعطيل الترخيص عبر واجهة الإدارة يلغي جلسات أجهزته.
8. Lease الموقّع هو مصدر الصلاحية المحلية، وليس Boolean محليًا.
9. Offline Lease صالح 30 يومًا من آخر إصدار/تجديد ناجح.
10. النسخ الاحتياطي والاستعادة خارج قرار الترخيص.

## الأمن

- المفتاح الخاص RSA لا يدخل APK ولا GitHub؛ يُخزن كـ Cloudflare Worker Secret.
- المفتاح العام فقط داخل Android للتحقق من Lease.
- التوقيع يستخدم RSA-SHA256 / RSASSA-PKCS1-v1_5، مع نفس canonical payload الموجود في المنظومة السابقة.
- Worker يتحقق من Firebase ID Token باستخدام مفاتيح Google الدورية.
- Worker يتحقق من Firebase App Check باستخدام JWKS الخاصة بـ Firebase.
- Android يستخدم Play Integrity في Release وDebug Provider في Debug.
- الإدارة المنفصلة محمية بـ `ADMIN_TOKEN` ولا تصل إلى APK.
- D1 لا يعرّض بيانات الترخيص للعميل مباشرة.

## التغيير عن Firebase Functions

تم استبدال Cloud Functions + Firestore license authority بـ Cloudflare Worker + D1 لتجنب اعتماد منظومة الترخيص على Firebase Blaze/billing. Firebase Authentication وApp Check يبقيان جزءًا من المنظومة.

بدل listener لحظي من Firestore، Android يجري polling آمنًا لحالة جلسة الترخيص كل 60 ثانية عندما تكون الجلسة المحلية فعالة.

## الحد الواقعي

لا يمكن جعل Offline licensing غير قابل للكسر أمام APK معدل بالكامل. الهدف الأمني الصحيح هو أن يكون التجاوز غير صالح عند الاتصال، مع Lease موقّع ونافذة Offline محددة، دون ادعاء حماية مطلقة.
