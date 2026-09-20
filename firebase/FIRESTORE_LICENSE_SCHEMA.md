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
All license fields are server controlled.

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

## smartledgerKv/{base64url(key)}
Retained only for the independent Google Drive/OAuth compatibility layer. License endpoints must not read or write license:* or email:* KV keys.

## Security invariants
1. Android never writes Firestore licensing documents directly.
2. activationCodeHash is never returned to Android.
3. Raw activation codes are returned only at issuance/regeneration through an admin channel.
4. The RSA license-signing private key remains in Firebase Secret Manager.
5. Device private keys remain in Android Keystore; only public keys are stored.
6. maxDevices is enforced server-side in a Firestore transaction.
7. Firebase Auth ID tokens are verified server-side; body email is not trusted as identity.
8. Google Drive sessions/tokens remain separate from license documents.
