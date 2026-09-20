# SmartLedger Firebase backend

هذه هي طبقة الـ Backend الوحيدة للتطبيق.

## المسؤوليات

- Firebase Authentication هو مصدر هوية الحساب.
- Cloud Functions هي واجهة الترخيص.
- Firestore هو مخزن التراخيص والأجهزة وحالات التحقق.
- Google Drive يستخدم مباشرة من تطبيق Android عبر Google Sign-In وDrive API.
- لا يوجد Cloudflare Worker أو Workers KV أو وسيط Cloudflare في المسار.

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

صلاحيات الإدارة تعتمد على Firebase custom claim باسم `admin=true`، وليس على مفتاح Cloudflare أو KV.

## Firestore

النموذج الأساسي:

- `users/{uid}`
- `licenses/{accountCode}`
- `licenses/{accountCode}/devices/{fingerprint}`
- `rateLimits/{hash}`

Android لا يكتب بيانات الترخيص مباشرة إلى Firestore؛ الكتابة تتم عبر Cloud Functions.

## Google Drive

Drive مستقل عن الترخيص:

1. Android يسجل الدخول بحساب Google.
2. Firebase Authentication يستخدم نفس حساب Google لهوية التطبيق.
3. Android يطلب `drive.file` و`drive.appdata` عند الحاجة.
4. Android يحصل على Access Token من Google Play Services.
5. Android يتصل مباشرة بـ `www.googleapis.com/drive/v3`.

لا يتم إرسال Access Token أو Refresh Token إلى Firebase أو Cloudflare.

## النشر

من مجلد `firebase/`:

```bash
firebase deploy --only functions,firestore,hosting
```

قبل نشر Functions، أنشئ أسرار Secret Manager المطلوبة ثم أعد النشر بعد أي تغيير في قيمة سر.

## مبدأ الفصل

نجاح Google Drive لا يفعّل الترخيص، وفشل Drive لا يعطل التحقق من الترخيص.

والترخيص لا يمنح التطبيق صلاحية إضافية على Google Drive.
