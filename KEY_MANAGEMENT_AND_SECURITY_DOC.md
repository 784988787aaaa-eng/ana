# Key Management & Security Architecture Documentation

## 1. Overview and Scope
This document outlines the security specifications, cryptographic key handling, signing configurations, OAuth credentials lifecycle, and licensing isolation rules for the application.

---

## 2. Release Signing & Credentials Management
- **Debug vs Release Separation:** 
  - The `debug` build type uses standard debug signing configurations.
  - The `release` build type strictly requires signing credentials provided via secure environment properties (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`).
- **No Hardcoded Defaults:**
  - All default fallback passwords (such as `123456`) are removed from Gradle build files.
  - If release properties are not provided, release builds fail explicitly and safely rather than falling back to debug keys.
- **CI/CD Credential Injection:**
  - Keystore paths and passwords must be injected at build time from secure CI/CD Secret vaults.

---

## 3. Keystore Handling & Supply-Chain Protection
- **Keystore Isolation:**
  - No private release keys or sensitive signing passwords must be checked into version control.
  - Keystore rotation decisions and fingerprint validations are performed through standard Android App Signing protocols.

---

## 4. Licensing and Offline Safety Contract
- **Offline Resilience (Mandatory Policy):**
  - `NETWORK OUTAGE != LICENSE REVOCATION`.
  - Temporary network errors, DNS lookup failures, socket timeouts, or Firestore `UNAVAILABLE` states do not invalidate local encrypted activation cache.
  - User financial data and database records are never modified, erased, or blocked due to transient network or authentication failures.
- **Explicit License States (`LicenseState`):**
  - `LicenseState.Valid`: Application is activated and authorized.
  - `LicenseState.Invalid`: Invalid code or unregistered account.
  - `LicenseState.Revoked`: Explicit administrative revocation confirmed by server.
  - `LicenseState.NetworkUnavailable`: Network failure with fallback to local cached activation.
  - `LicenseState.AuthRequired`: Explicit sign-in or account linkage required.
  - `LicenseState.Unknown`: Initializing state.

---

## 5. Network Engine & Retry Policies (`CloudNetworkEngine`)
- **Retryable Errors:**
  - Transient I/O exceptions, socket timeouts, 5xx server responses, and 429 (Rate Limited) with Exponential Backoff and Jitter.
- **Non-Retryable Errors:**
  - 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), and `CancellationException`.
  - Coroutine cancellations stop retry loops immediately.
- **Token Redaction:**
  - All tokens (`accessToken`, `refreshToken`, `Authorization` headers) are redacted (`[REDACTED]`) from logs, crash traces, and exception messages.

---

## 6. PIN Protection & Rate Limiting (`AppSecurityManager`)
- **Rate Limiting & Lockout:**
  - Failed PIN attempts are bounded (e.g. 5 failed attempts trigger exponential lockout cooldown).
  - Successful PIN verification resets the failed attempt counter.
  - PIN and recovery phrase checks are performed using constant-time comparison (`DatabaseSecurityGuard.secureEqual` / `HashUtils.secureEquals`).
- **Memory Cleansing:**
  - Sensitive character and byte arrays are scrubbed (`wipeCharArray`, `wipeByteArray`) immediately after cryptographic operations.
