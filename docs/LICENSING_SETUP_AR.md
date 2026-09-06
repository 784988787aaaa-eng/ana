# إعداد الترخيص السحابي — الدفتر الذكي

> هذا الدليل يخص الترخيص فقط. لا يغيّر منطق النسخ الاحتياطي أو الاستعادة.

## 1) Firebase Authentication

1. افتح مشروع Firebase: `al-daftar-android`.
2. Authentication → Sign-in method.
3. فعّل Google.
4. تأكد أن تسجيل الدخول في التطبيق يعيد مستخدم Firebase صالحًا وله بريد موثّق.

Firebase هنا هو **مزود الهوية** فقط، وليس قاعدة بيانات سلطة الترخيص.

## 2) Firebase App Check

التطبيق يرسل App Check token مع كل طلب ترخيص إلى Worker. Firebase توثّق استخدام App Check لحماية موارد Backend مخصصة، وإرسال الرمز في ترويسة `X-Firebase-AppCheck`.

### Debug

1. شغّل Debug build.
2. ابحث في Logcat عن Debug App Check token.
3. Firebase Console → App Check → التطبيق → Manage debug tokens.
4. سجّل الرمز.

### Release

استخدم Play Integrity، وسجّل SHA-256 لشهادة الإصدار الفعلية في App Check. لا تستخدم Debug Provider في نسخة المستخدمين.

## 3) مفتاح RSA

المشروع الحالي يحتوي على المفتاح العام فقط. المفتاح الخاص الموجود خارج المشروع هو الذي يجب رفعه كـ Cloudflare Secret.

إذا احتجت إنشاء زوج جديد مستقبلًا، استخدم:

```bash
cd worker
node scripts/generate-license-keypair.mjs
```

ثم حدّث المفتاح العام داخل `LicenseLeaseVerifier.kt`. **لا تغيّر الزوج الحالي لمجرد التجربة**؛ المفتاح العام الحالي مرتبط بالمفتاح الخاص المحفوظ لدينا.

## 4) إنشاء Cloudflare Worker

من داخل `worker`:

```bash
npm install
npx wrangler login
npx wrangler d1 create al-daftar-license-db
```

سيعيد Cloudflare `database_id`. ضع القيمة مكان:

```toml
database_id = "76b6b20b-6780-4706-bc3c-e1a2c68a35bd"
```

داخل `worker/wrangler.toml`.

## 5) إنشاء جداول D1

بعد وضع `database_id`:

```bash
npx wrangler d1 migrations apply al-daftar-license-db --remote
```

الجداول هي:

- `licenses`
- `devices`
- `license_sessions`
- `license_audit`

## 6) الأسرار

### المفتاح الخاص

نفّذ:

```bash
npx wrangler secret put LICENSE_PRIVATE_KEY
```

ثم الصق **محتوى ملف المفتاح الخاص PKCS#8** فقط. لا تضعه في GitHub ولا داخل Android.

### مفتاح الإدارة

أنشئ قيمة عشوائية قوية:

```bash
node scripts/generate-admin-token.mjs
```

ثم:

```bash
npx wrangler secret put ADMIN_TOKEN
```

احتفظ به خارج المشروع.

## 7) فحص Worker ونشره

```bash
npm run check
npm run deploy
```

بعد النشر سيكون لديك عنوان مثل:

`https://al-daftar-license-api.mansour-ghawy.workers.dev`

اختبر:

```text
GET /health
```

يجب أن يعيد:

```json
{"ok":true,"service":"al-daftar-license-api"}
```

## 8) ربط Android بالـ Worker

ضع عنوان Worker في متغير Gradle:

```bash
LICENSE_BACKEND_URL=https://al-daftar-license-api.mansour-ghawy.workers.dev
```

أو مرره كـ Gradle property:

```bash
./gradlew assembleDebug -PLICENSE_BACKEND_URL=https://al-daftar-license-api.mansour-ghawy.workers.dev
```

القيمة ليست سرًا؛ هي عنوان خدمة فقط.

## 9) إنشاء أول ترخيص

بعد تسجيل دخول المستخدم إلى Google والحصول على Firebase UID، استخدم endpoint الإدارة من جهازك/Termux فقط. مثال:

```bash
curl -X POST "https://al-daftar-license-api.mansour-ghawy.workers.dev/v1/admin/licenses" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"uid":"FIREBASE_UID","email":"user@example.com","is_activated":true,"devices_max":2,"license_version":1}'
```

لا تضع `ADMIN_TOKEN` داخل التطبيق.

## 10) منطق التشغيل

- الحساب المرخص دائم ما دام `is_activated=true`.
- عدد الأجهزة تحدده `devices_max` من 1 إلى 20.
- عند تجاوز الحد، أقدم جهاز في `device_order_json` يُلغى ويُنشأ للجهاز الجديد Session جديدة.
- تسجيل الخروج اليدوي يلغي الهاتف الحالي فقط.
- الجهاز الآخر لا يتأثر.
- Lease الموقع صالح 30 يومًا.
- Android يتحقق من التوقيع بالمفتاح العام قبل حفظ Lease.
- عند الاتصال يجدد التطبيق Lease عبر `/v1/license/refresh`.
- مراقبة الجلسة أصبحت polling كل 60 ثانية بدل Firestore listener.

## 11) الاختبارات الإلزامية

1. مستخدم بلا ترخيص → رفض.
2. `is_activated=false` → رفض.
3. جهاز واحد مع `devices_max=1` → تفعيل.
4. جهاز ثانٍ مع الحد 1 → الأول يُلغى والثاني يعمل.
5. تسجيل خروج الهاتف الحالي → هذا الهاتف فقط يُلغى.
6. الهاتف الآخر يبقى فعالًا.
7. Refresh بنفس Session → Lease جديد بنفس session.
8. Session ملغاة → Refresh مرفوض.
9. انقطاع الإنترنت بعد Lease صالح → يستمر حتى انتهاء 30 يومًا وفق منطق Android المحلي.
10. تعطيل الترخيص من endpoint الإدارة → الجلسات النشطة تُلغى.
11. App Check غير صالح → Worker يرفض الطلب.
12. Firebase ID Token غير صالح → Worker يرفض الطلب.

## 12) لا تفعل

- لا ترفع المفتاح الخاص إلى GitHub.
- لا تضع `ADMIN_TOKEN` في APK.
- لا تجعل Android يكتب D1 مباشرة.
- لا تعتمد على البريد وحده بدل UID.
- لا تعيد إضافة Firebase Cloud Functions لهذا الجزء دون سبب معماري واضح.
- لا تدّع أن Offline licensing يمنع كل تعديل ممكن على APK.
