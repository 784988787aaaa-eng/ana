# خدمة SMARTLEDGER السحابية

هذه الخدمة تستبدل Firebase Functions بالكامل. لا تحتوي على بيانات التطبيق المالية، وتتعامل فقط مع الترخيص ووسيط Google Drive.

## المكونات

- Cloudflare Worker: واجهة HTTPS العامة.
- Workers KV: تخزين التراخيص، جلسات الجهاز، حالات OAuth، جلسات Drive، وحدود المحاولات.
- Google Drive API: التخزين الفعلي لملفات `.slb`.

## الأسرار

تُضاف الأسرار إلى Cloudflare Secrets فقط:

```text
SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY
SMARTLEDGER_RATE_LIMIT_SALT
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

لا تُضاف هذه القيم إلى المستودع أو APK.

## إنشاء KV

من مجلد `cloudflare`:

```bash
npx wrangler login
npx wrangler kv namespace create SMARTLEDGER_KV
```

انسخ `id` الناتج إلى `wrangler.toml` بدل:

```text
REPLACE_AFTER_CREATION
```

## إضافة الأسرار

```bash
npx wrangler secret put SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY
npx wrangler secret put SMARTLEDGER_RATE_LIMIT_SALT
npx wrangler secret put GOOGLE_CLIENT_ID
npx wrangler secret put GOOGLE_CLIENT_SECRET
```

لا تعرض القيم أثناء المحادثة ولا تضعها في الملفات.

## نشر Worker

```bash
npx wrangler deploy
```

سيظهر عنوان شبيه بـ:

```text
https://smartledger-cloud.<SUBDOMAIN>.workers.dev
```

عنوان الترخيص الذي يدخل إلى التطبيق هو:

```text
https://smartledger-cloud.<SUBDOMAIN>.workers.dev/license
```

وعنوان OAuth هو:

```text
https://smartledger-cloud.<SUBDOMAIN>.workers.dev/driveOAuthCallback
```

يجب تسجيل عنوان OAuth نفسه حرفيًا في Google Cloud OAuth Client.

## إعداد ترخيص حساب

كل ترخيص حساب هو قيمة JSON في KV بالمفتاح:

```text
license:SL-7K4M-92PX
```

القيمة:

```json
{
  "type": "ACCOUNT",
  "plan": "LIFETIME",
  "licenseId": "معرف داخلي ثابت",
  "activationCodeHash": "SHA-256 بصيغة HEX كبيرة",
  "revoked": false
}
```

أمر الإدخال:

```bash
npx wrangler kv key put --binding=SMARTLEDGER_KV "license:SL-7K4M-92PX" '{"type":"ACCOUNT","plan":"LIFETIME","licenseId":"LICENSE-ID","activationCodeHash":"HASH","revoked":false}'
```

لا تحفظ رمز التفعيل الخام في KV.

## إنشاء SHA-256 لرمز التفعيل

```bash
python3 - <<'PY'
import hashlib, secrets
code = secrets.token_urlsafe(24)
print('Activation Code:', code)
print('SHA-256:', hashlib.sha256(code.encode()).hexdigest().upper())
PY
```

أرسل رمز التفعيل الخام للمستخدم فقط، وخزّن قيمة SHA-256.

## Google Drive

النطاق المستخدم هو:

```text
https://www.googleapis.com/auth/drive.file
```

الWorker يحتفظ بـ Refresh Token مشفرًا في KV. لا يصل Refresh Token إلى Android.

البنية داخل Drive:

```text
الدفتر الذكي/
└── YYYY-MM/
    └── SMN_YYYY-MM-DD.slb
```

الاستعراض يعتمد على `files.list` لملفات النسخ التي أنشأها التطبيق، لذلك لا يحتاج إلى اجتياز شجرة المجلدات في كل طلب.

## الجلسات

جلسة Drive عبارة عن رمز عشوائي طويل محفوظ محليًا داخل التخزين المشفر للتطبيق. الخادم يحفظ نسخة مشتقة منه فقط مع Refresh Token المشفر.

الجلسة تستمر حتى قطع الاتصال، مع مهلة خمول طويلة مقدارها 180 يومًا. عند قطع الاتصال يحذف الخادم الجلسة ويحاول إلغاء Refresh Token لدى Google.

## حذف Firebase

بعد نقل الإعدادات والنشر بنجاح:

1. احذف مجلد `firebase/` من المشروع.
2. احذف إعدادات Firebase من وثائق المشروع.
3. لا تستخدم Firestore لتخزين الترخيص أو Drive.
4. لا تضع Google Client Secret أو مفتاح توقيع الحساب في Android.
