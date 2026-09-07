# AI-Daftar license authority — Cloudflare Worker + D1

This Worker replaces the Firebase Cloud Functions license authority without changing the license protocol used by Android.

## Endpoints

- `POST /v1/license/activate`
- `POST /v1/license/refresh`
- `POST /v1/license/unlink`
- `GET /v1/license/session/{sessionId}`
- `POST /v1/admin/licenses` (admin secret only)

Protected license endpoints require:
- `Authorization: Bearer <Firebase ID token>`
- `X-Firebase-AppCheck: <Firebase App Check token>`

The Worker verifies Firebase ID tokens against Google's rotating signing certificates and verifies App Check tokens against Firebase's JWKS. The lease remains RSA-SHA256/RSASSA-PKCS1-v1_5 and uses the same canonical payload as the former Cloud Function.

## Concurrency / device limit

D1 batches are atomic. A database trigger also enforces `devices_max` at insert time, so two simultaneous device activations cannot leave more active devices than the configured limit. If a concurrent activation loses the race, the Worker retries once against the current license order.

## Secrets

Set these with Wrangler; never commit them:

```bash
wrangler secret put LICENSE_PRIVATE_KEY
wrangler secret put ADMIN_TOKEN
```

`LICENSE_PRIVATE_KEY` must be the existing PKCS#8 PEM private key corresponding to the public key embedded in Android. `ADMIN_TOKEN` is a new random high-entropy administrator token.

## Database

Create the D1 database, put its returned ID into `wrangler.toml`, then run:

```bash
wrangler d1 migrations apply al-daftar-license-db --remote
```

## Deployment

```bash
npm install
npm run check
npm run deploy
```

Do not deploy the old Firebase Functions. Firebase Authentication remains the identity provider; Firebase Firestore is no longer required by the licensing authority.
