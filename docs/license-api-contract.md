# SmartLedger License API Contract (Current)

This document records the current license contract before any Android endpoint migration.

## Scope

The license subsystem is independent from Google Drive OAuth.

License responsibilities:
- account activation
- automatic activation for an already activated account
- device verification
- account status
- admin license operations
- device revocation
- signed account license tokens

Google Drive responsibilities are intentionally out of scope:
- Google OAuth
- Drive sessions
- Drive list/upload/download/delete/disconnect

No Android Drive code is changed by this contract.

## Public license endpoints

Base URL is the configured license service base. The Android client currently resolves paths from `app/src/main/assets/license_endpoint.txt`.

### POST /license/activate

Purpose: first activation and device enrollment.

Request JSON:
- `accountCode`: optional if `email` resolves it
- `email`: optional
- `activationCode`: required
- `devicePublicKey`: required

Success 200:
- `licensed: true`
- `token`
- `accountCode`
- `email`
- `licenseType`
- `plan`
- `active`
- `maxDevices`
- `trialEndsAt`

Common errors:
- 400 `invalid_activation`
- 400 `invalid_device_key`
- 403 `account_disabled`
- 403 `email_mismatch`
- 403 `activation_invalid`
- 403 `trial_expired`
- 404 `not_found`
- 429 `rate_limited`

### POST /license/verify

Purpose: prove possession of the enrolled device key and issue a fresh signed token.

Request JSON:
- `accountCode`
- `deviceFingerprint`
- `challenge`
- `signature`

Success 200:
- `token`
- `accountCode`
- `email`
- `licenseType`
- `plan`
- `active`
- `maxDevices`
- `trialEndsAt`

Revocation/expiry responses use HTTP 200 with:
- `revoked: true`
- `active: false`
- `status`
- optional `reason`
- `message`

Common errors:
- 400 `invalid_verification`
- 403 `challenge_expired`
- 403 `proof_invalid`
- 404 `not_found`
- 429 `rate_limited`

### POST /license/auto-activate

Purpose: seamless enrollment of another device after the account has already been activated once.

Request JSON:
- `email`
- `devicePublicKey`

Possible 200 responses:
- `licensed: true` + signed `token`
- `registered: false`
- `registered: true, activationRequired: true`
- `registered: true, revoked: true`
- `registered: true, expired: true`

### POST /license/check-status

Purpose: non-device-bound account status lookup.

Request JSON:
- `accountCode` optional
- `email` optional; resolves account code when supplied

Success 200:
- `licensed`
- `registered`
- `active`
- `expired`
- `accountCode`
- `email`
- `licenseType`
- `plan`
- `maxDevices`
- `trialEndsAt`
- `activatedAt`
- `remainingDays`

## Admin endpoints

All admin endpoints require the `X-SMARTLEDGER-ADMIN` header.

### POST /admin/ping
Checks administrative connectivity.

### POST /admin/licenses/issue

Request:
- `email` required
- `accountCode` optional
- `licenseType` / `plan` optional
- `trialDays` optional
- `maxDevices` optional
- `active` optional
- `activationCode` optional
- `notes` optional
- `reissue` optional

Returns the generated activation code only to the admin caller. The stored record contains only its SHA-256 hash.

### POST /admin/licenses/update

Request supports:
- `accountCode`
- `replaceEmail`
- `regenerateActivationCode`
- `active`
- `maxDevices`
- `notes`
- `licenseType` / `plan`
- `extendTrialDays`

### POST /admin/licenses/get

Request:
- `accountCode`

Returns:
- sanitized license record
- device list with fingerprint, timestamps, revoked state and reason

### POST /admin/devices/revoke

Request:
- `accountCode`
- `deviceFingerprint`

Returns:
- `ok: true`
- `revoked: true`
- `accountCode`
- `deviceFingerprint`

## License data model

Current logical records use these KV key families:

- `license:<accountCode>` — license record
- `email:<email>` — email to account mapping
- `install:<accountCode>:<fingerprint>` — enrolled device
- `rate:<hash>` — rate-limit state

Drive uses separate key families:

- `oauth:<stateHash>`
- `oauth-connection:<connectionHash>`
- `session:<tokenHash>`

The key families are therefore logically separable even though the current Cloudflare Worker resolves them through the same KV binding.

## Signed token contract

Account tokens are signed server-side with RSASSA-PKCS1-v1_5 / SHA-256 and contain:
- product
- type = ACCOUNT
- plan / licenseType
- licenseId
- accountCode
- email
- issuedAt
- offlineUntil
- expiresAt when applicable
- trialEndsAt when applicable
- maxDevices
- deviceFingerprint
- keyVersion

The Android client verifies the signed token locally and uses the server verification endpoint for ongoing authorization.

## Migration invariant

The replacement license service must preserve the public request/response contract above unless a deliberate Android change is made later.

Google Drive OAuth must remain a separate service contract and must not become a prerequisite for license activation or verification.
