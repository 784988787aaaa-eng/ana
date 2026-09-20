# SmartLedger Firebase migration

هذا المجلد هو طبقة Firebase البديلة لخادم Cloudflare الحالي.

## ما تم الحفاظ عليه

- نفس HTTP paths للترخيص:
  - /license/activate
  - /license/verify
  - /license/auto-activate
  - /license/check-status
  - /license/status
  - /status
- نفس منطق الحساب والتجربة وعدد الأجهزة واستبدال أقدم جهاز.
- نفس RSA license-token contract.
- نفس Google Drive OAuth وعمليات list/upload/download/delete/disconnect.
- نفس جلسات Drive، لكن تخزينها أصبح Firestore.
- لا توجد بيانات مالية للتطبيق داخل Firestore؛ الأرشيف الفعلي يبقى في Google Drive كما كان.

## بنية Firestore

كل مفاتيح KV القديمة توضع في:

smartledgerKv/<base64url(key)>

والحقل key يحتفظ بالمفتاح الأصلي، بينما value يحتفظ بالقيمة النصية الأصلية.

هذا مقصود للحفاظ على عقد Worker الحالي أثناء مرحلة النقل، بدل إعادة كتابة منطق الترخيص دفعة واحدة.

## أسرار Firebase

لا تضع أي قيمة سرية في GitHub أو APK.

يجب إنشاء هذه الأسرار في Secret Manager:

- SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY
- SMARTLEDGER_RATE_LIMIT_SALT
- SMARTLEDGER_ADMIN_SECRET
- GOOGLE_CLIENT_ID
- GOOGLE_CLIENT_SECRET

الأمر:

firebase functions:secrets:set SECRET_NAME

ثم النشر:

firebase deploy --only functions:smartledgerApi

## App Check

الـ wrapper يتحقق من X-Firebase-AppCheck إذا أرسله العميل، لكنه لا يفرض وجوده بعد.
هذا متعمد حتى لا ينكسر الإصدار الحالي قبل إضافة Firebase App Check/Play Integrity إلى Android.

بعد إضافة App Check إلى Android واختبار الترافيك، تصبح المرحلة التالية هي فرض App Check على الـ API.

## ربط المشروع

من داخل مجلد المشروع:

firebase login
firebase use --add

ثم اختر مشروع Firebase الحقيقي.

لا تنشئ أو تكتب project ID في الكود قبل اختيار مشروعك.

## Firestore

انشر القواعد والفهرس:

firebase deploy --only firestore

القواعد الحالية تمنع Android من الوصول المباشر إلى بيانات الترخيص والجلسات؛ الوصول يتم من Cloud Functions عبر Admin SDK.

## الترحيل من Cloudflare KV

لا نحذف Cloudflare.

أولاً نأخذ نسخة من KV ونحولها إلى Firestore، ثم نقارن:

- license:<accountCode>
- email:<email>
- install:<accountCode>:<fingerprint>
- rate:*
- oauth:*
- oauth-connection:*
- session:*

بعد التحقق فقط نبدل endpoint داخل Android.

## ملاحظة

لا نحتاج نقل ملفات Google Drive نفسها إلى Firebase Storage في هذه المرحلة، لأن Worker الحالي لا يخزن ملفات النسخ في Cloudflare؛ هو وسيط OAuth/API فقط، بينما الملفات موجودة في Google Drive.
