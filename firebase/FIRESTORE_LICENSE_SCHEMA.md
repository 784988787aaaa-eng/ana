# SmartLedger Firestore schema — licensing

This is the canonical licensing schema. It does not create collections.

## Identity
- Firebase Authentication UID is the canonical account identity.
- users/{uid} stores the application account mapping.
- Email is an attribute and lookup aid, never the primary key.
- users/{uid}.accountCode must match licenses/{accountCode}.ownerUid after successful binding.

## users/{uid}
- email: string, required, server controlled
- accountCode: string, nullable until bound, server controlled
- createdAt: timestamp/number, required, server controlled
- updatedAt: timestamp/number, required, server controlled

## licenses/{accountCode}
- product: string, SMARTLEDGER
- type: string, ACCOUNT
- licenseType: string, LIFETIME or TRIAL
- plan: string, same as licenseType
- licenseId: string
- accountCode: string
- ownerUid: string, nullable until bound
- email: string
- active: boolean
- revoked: boolean
- trialDays: integer (0 for lifetime, 1..3650 for trial)
- trialEndsAt: timestamp/number/null
- activatedAt: timestamp/number/null
- activationUsedAt: timestamp/number/null
- maxDevices: integer 1..100
- activationCodeHash: string
- issuedAt: timestamp/number
- updatedAt: timestamp/number
- notes: string
All license fields are server controlled. Google Sheets/admin tooling must never write these documents directly from an untrusted client; it must call the secured admin workflow/function using a Firebase Admin identity.

## licenses/{accountCode}/devices/{fingerprint}
- fingerprint: string, 64-char uppercase SHA-256
- publicKey: string, device SPKI public key
- email: string
- createdAt: timestamp/number
- lastSeenAt: timestamp/number
- revoked: boolean
- revokedAt: timestamp/number/null
- revocationReason: string/null (device_replaced, admin_revoked, email_replaced)
- noticeMessage: string/null
All device fields are server controlled.

## rateLimits/{hash}
Internal anti-abuse state only: startedAt, attempts, expiresAt. It is not license data.


## Security invariants
1. Android never writes Firestore licensing documents directly.
2. activationCodeHash is never returned to Android.
3. Raw activation codes are returned only at issuance/regeneration through an admin channel.
4. The RSA license-signing private key remains in Firebase Secret Manager.
5. Device private keys remain in Android Keystore; only public keys are stored.
6. maxDevices is enforced server-side in a Firestore transaction.
7. Firebase Auth ID tokens are verified server-side; body email is not trusted as identity.
8. Google Drive is handled directly by Android and Google; no Drive OAuth/session tokens are stored in Firestore.


## Google Sheets administrative contract

The existing administrative sheet can remain the operator-facing table:

1. Account Code → `accountCode`
2. Email → `email`
3. License Type → `licenseType`
4. Active → `active`
5. Max Devices → `maxDevices`
6. Trial Days → `trialDays`
7. Activation Code → generated/admin-only value; only `activationCodeHash` is stored in Firestore
8. Activated At → `activatedAt`
9. Trial Ends At → `trialEndsAt`
10. Remaining Days → computed value, not authoritative storage
11. Active Devices → computed from `devices` where `revoked == false`
12. Replace Email → admin update operation
13. Regenerate Code → admin update operation
14. Notes → `notes`
15. License ID → `licenseId`
16. Sync Status → sheet/workflow state
17. Last Updated → `updatedAt`

Recommended flow:

`Google Sheets → secured admin workflow → Firebase Function → Firestore`

Do not give the sheet direct Firestore credentials or client-side write access.
