# SmartLedger Firebase backend

هذه هي طبقة الـ Backend الخاصة بالهوية والترخيص فقط؛ النسخ الاحتياطي السحابي لا يمر عبرها.

## المسؤوليات

- Firebase Authentication هو مصدر هوية الحساب.
- Cloud Functions هي واجهة الترخيص.
- Firestore هو مخزن التراخيص والأجهزة وحالات التحقق.
- Google Drive يستخدم مباشرة من تطبيق Android عبر Google OAuth وDrive API.
- مسار هوية Firebase ومسار Google Drive منفصلان: تسجيل الدخول للترخيص لا يطلب نطاق Drive، وربط Drive لا ينشئ جلسة Firebase.
- لا يوجد Cloudflare أو Worker أو Proxy في مسار الترخيص أو النسخ الاحتياطي.

## الترخيص

المسارات الأساسية:

- `/license/activate`
- `/license/verify`
- `/license/auto-activate`
- `/license/check-status`
- `/license/status`
- `/admin/*`

كل طلب ترخيص يجب أن يحمل Firebase ID token في:

`Authorization: Bearer <Firebase ID token>`

تتحقق Cloud Functions من الـ ID token باستخدام Firebase Admin SDK، ثم تستخدم UID الموثوق لربط `users/{uid}` بالترخيص.

## أسرار Firebase

لا تضع أي سر في Git أو APK.

الأسرار المطلوبة في Firebase Secret Manager:

- `SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY`
- `SMARTLEDGER_RATE_LIMIT_SALT`

صلاحيات الإدارة تعتمد على Firebase custom claim باسم `admin=true`، وليس على مفتاح وسيط خارجي أو KV.

## Firestore

النموذج الأساسي:

- `users/{uid}`
- `licenses/{accountCode}`
- `licenses/{accountCode}/devices/{fingerprint}`
- `rateLimits/{hash}`

Android لا يكتب بيانات الترخيص مباشرة إلى Firestore؛ الكتابة تتم عبر Cloud Functions.

## Google Drive

Drive مستقل عن الترخيص:

1. Android يربط Google Drive من شاشة النسخ الاحتياطي فقط.
2. عميل Google Drive يطلب نطاق `drive.file` فقط.
3. مسار Firebase Authentication يستخدم عميل Google منفصلاً ولا يطلب أي نطاق Drive.
4. Android يحصل على Access Token من Google Play Services محلياً.
5. Android يتصل مباشرة بـ Google Drive API (`www.googleapis.com/drive/v3`) للرفع والاستعادة والحذف والقائمة.

لا يتم إرسال Access Token أو Refresh Token إلى Firebase أو وسيط خارجي، ولا تعتمد عمليات Drive على حالة الترخيص.

## النشر

من مجلد `firebase/`:

```bash
firebase deploy --only functions:smartledgerApi,firestore,hosting
```

قبل نشر Functions، أنشئ أسرار Secret Manager المطلوبة ثم أعد النشر بعد أي تغيير في قيمة سر.

## مبدأ الفصل

نجاح Google Drive لا يفعّل الترخيص، وفشل Drive لا يعطل التحقق من الترخيص.

والترخيص لا يمنح التطبيق صلاحية إضافية على Google Drive.
