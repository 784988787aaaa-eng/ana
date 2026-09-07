# AI-Daftar license authority — Cloudflare Worker + D1

This Worker replaces the Firebase Cloud Functions license authority without changing the license protocol used by Android.

## Endpoints

- `POST /v1/google/oauth/exchange` (public Google Drive OAuth exchange; not a licensing endpoint)
- `POST /v1/google/oauth/refresh` (public Google Drive OAuth refresh; not a licensing endpoint)
- `GET /v1/support/identity` (Firebase Auth + App Check)
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
wrangler secret put GOOGLE_OAUTH_CLIENT_SECRET
```

`LICENSE_PRIVATE_KEY` and `ADMIN_TOKEN` remain server-only secrets. `GOOGLE_OAUTH_CLIENT_SECRET` is also server-only and is used exclusively by the Worker for Google Drive OAuth token exchange/refresh; it is never shipped in the Android app. The Android app contains no certificate fingerprints or Google client secrets.

## Database

Create the D1 database, put its returned ID into `wrangler.toml`, then run:

```bash
wrangler d1 migrations apply al-daftar-license-db --remote
```

## Deployment

The production GitHub Actions workflow deploys `al-daftar-license-api` with Wrangler. The Google OAuth client secret is intentionally **not** stored in GitHub and is never overwritten by the deployment workflow; it remains a Cloudflare Worker secret.

Required GitHub Actions secrets:
- `CLOUDFLARE_API_TOKEN`
- `CLOUDFLARE_ACCOUNT_ID`
- `GOOGLE_CLIENT_ID` (the Web OAuth Client ID)

Required Cloudflare Worker secret:
- `GOOGLE_OAUTH_CLIENT_SECRET` (the Client Secret belonging to the same Web OAuth Client ID)

The workflow verifies that `GOOGLE_OAUTH_CLIENT_SECRET` already exists in Cloudflare before deployment.

For a manual deployment:

```bash
npm install
npm run check
npm run deploy
```

Do not deploy the old Firebase Functions. Firebase Authentication remains the identity provider; Firebase Firestore is no longer required by the licensing authority.
