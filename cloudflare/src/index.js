/**
 * ============================================================================
 * SmartLedger Backend Core Worker (Production v2)
 * Service: al-daftar-license-api
 * Components: Account Licensing, Device Eviction Engine, Google Drive Sync
 * ============================================================================
 */

// --- 1. Patterns & System Constants ---
const ACCOUNT_PATTERN = /^SL-[A-Z0-9]{4}-[A-Z0-9]{4}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const FINGERPRINT_PATTERN = /^[A-F0-9]{64}$/;
const BACKUP_PATTERN = /^(?:SNA_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}\.sna|SMN_\d{4}-\d{2}-\d{2}(?:_\d{4})?\.slb)$/i;

const PUBLIC_KEY_MAX = 8192;
const ACTIVATION_CODE_LENGTH = 24;

const DEFAULT_TRIAL_DAYS = 30;
const DEFAULT_MAX_DEVICES = 1;
const MAX_TRIAL_DAYS = 3650;
const MAX_DEVICES = 100;

const RATE_WINDOW_MS = 15 * 60 * 1000;
const RATE_LIMIT_DEFAULT = 15;
const RATE_LIMIT_SENSITIVE = 6;

const CHALLENGE_TTL_MS = 5 * 60 * 1000;
const TOKEN_OFFLINE_DAYS = 30;

// Google Drive Constants (Preserved from original worker)
const DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
const DRIVE_ROOT = "الدفتر الذكي برو";
const DRIVE_MIME = "application/vnd.smartledger.backup";
const OAUTH_STATE_TTL_MS = 10 * 60 * 1000;
const SESSION_IDLE_MS = 180 * 24 * 60 * 60 * 1000;

// --- 2. Response Helpers & Dynamic CORS ---

const json = (status, body, extraHeaders = {}) => new Response(
  JSON.stringify(body),
  {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store",
      ...extraHeaders
    }
  }
);

const html = (status, body) => new Response(body, {
  status,
  headers: {
    "Content-Type": "text/html; charset=utf-8",
    "Cache-Control": "no-store"
  }
});

function corsHeaders(request) {
  const origin = request.headers.get("Origin");
  if (!origin) return {};
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization, X-SMARTLEDGER-ADMIN, X-SmartLedger-Cloud-Token, X-SmartLedger-File-Name",
    "Access-Control-Max-Age": "86400",
    "Vary": "Origin"
  };
}

function withCors(response, request) {
  const headers = new Headers(response.headers);
  const extra = corsHeaders(request);
  for (const [k, v] of Object.entries(extra)) {
    headers.set(k, v);
  }
  return new Response(response.body, { status: response.status, headers });
}

// --- 3. Encoding & Cryptographic Primitives ---

function b64Url(bytes) {
  const input = bytes instanceof ArrayBuffer ? new Uint8Array(bytes) : bytes;
  let binary = "";
  for (let i = 0; i < input.length; i += 32768) {
    binary += String.fromCharCode(...input.subarray(i, i + 32768));
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function fromB64(value) {
  const normalized = String(value || "").replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(String(value || "").length / 4) * 4, "=");
  const binary = atob(normalized);
  return Uint8Array.from(binary, char => char.charCodeAt(0));
}

async function sha256(value) {
  const data = typeof value === "string" ? new TextEncoder().encode(value) : value;
  return new Uint8Array(await crypto.subtle.digest("SHA-256", data));
}

async function hexHash(value) {
  return Array.from(await sha256(value), byte => byte.toString(16).padStart(2, "0")).join("").toUpperCase();
}

function pemToBytes(pem) {
  const clean = String(pem || "").replace(/-----BEGIN [^-]+-----/g, "").replace(/-----END [^-]+-----/g, "").replace(/\s+/g, "");
  return fromB64(clean);
}

async function importPrivateKey(pem) {
  return crypto.subtle.importKey(
    "pkcs8",
    pemToBytes(pem),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
}

async function importPublicKey(base64) {
  return crypto.subtle.importKey(
    "spki",
    fromB64(base64),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );
}

async function verifyDeviceSignature(publicKeyBase64, message, signatureBase64) {
  try {
    const key = await importPublicKey(publicKeyBase64);
    return await crypto.subtle.verify("RSASSA-PKCS1-v1_5", key, fromB64(signatureBase64), new TextEncoder().encode(message));
  } catch (_) {
    return false;
  }
}

async function signToken(header, body, env) {
  const pem = String(env.SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY || "").trim();
  if (!pem) {
    throw new Error("license_signing_key_missing");
  }
  const key = await importPrivateKey(pem);
  const input = `${header}.${body}`;
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(input));
  return `${header}.${body}.${b64Url(signature)}`;
}

// --- 4. Environment, KV Resolution & Backward Compatibility ---

function getKV(env) {
  const kv = env.SMARTLEDGER_KV || env.SMARTLEDGER_LICENSE_KV;
  if (!kv) {
    throw new Error("kv_binding_missing");
  }
  return kv;
}

function getSalt(env) {
  return String(env.SMARTLEDGER_RATE_LIMIT_SALT || env.SMARTLEDGER_ADMIN_SECRET || "SMARTLEDGER_SALT_DEFAULT");
}

function normalizeEmail(value) {
  return String(value || "").trim().toLowerCase();
}

function normalizeAccountCode(value) {
  return String(value || "").trim().toUpperCase();
}

function normalizeLicenseType(value) {
  const type = String(value || "LIFETIME").trim().toUpperCase();
  if (type === "TRIAL") return "TRIAL";
  if (type === "LIFETIME") return "LIFETIME";
  throw new Error("invalid_license_type");
}

function normalizePositiveInt(value, fallback, max) {
  const parsed = Number.parseInt(String(value ?? ""), 10);
  if (!Number.isFinite(parsed) || parsed < 1) return fallback;
  return Math.min(parsed, max);
}

function randomToken() {
  return b64Url(crypto.getRandomValues(new Uint8Array(32)));
}

function randomAccountCode() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  const bytes = crypto.getRandomValues(new Uint8Array(8));
  let val = "";
  for (const b of bytes) val += chars[b % chars.length];
  return `SL-${val.slice(0, 4)}-${val.slice(4, 8)}`;
}

function randomActivationCode() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  const bytes = crypto.getRandomValues(new Uint8Array(ACTIVATION_CODE_LENGTH));
  let val = "";
  for (const b of bytes) val += chars[b % chars.length];
  return val;
}

function clientIp(request) {
  return request.headers.get("CF-Connecting-IP") || request.headers.get("X-Forwarded-For")?.split(",")[0]?.trim() || "unknown";
}

async function rateLimit(env, identity, ip, limit = RATE_LIMIT_DEFAULT) {
  const salt = getSalt(env);
  const id = await hexHash(`${salt}:${identity}:${ip}`);
  const key = `rate:${id}`;
  const now = Date.now();
  const kv = getKV(env);

  const current = await kv.get(key, "json");
  if (!current || now - Number(current.startedAt || 0) >= RATE_WINDOW_MS) {
    await kv.put(key, JSON.stringify({ startedAt: now, attempts: 1 }), { expirationTtl: 20 * 60 });
    return true;
  }
  if (Number(current.attempts || 0) >= limit) {
    return false;
  }
  await kv.put(
    key,
    JSON.stringify({ startedAt: current.startedAt, attempts: Number(current.attempts || 0) + 1 }),
    { expirationTtl: 20 * 60 }
  );
  return true;
}

async function readJson(request) {
  try {
    return await request.json();
  } catch (_) {
    throw new Error("invalid_json");
  }
}

/**
 * Ensures legacy records stored in KV conform to all updated system expectations
 * without breaking existing keys, trial dates, or device mappings.
 */
function normalizeLicenseRecord(license) {
  if (!license) return null;
  const now = Date.now();
  const licenseType = normalizeLicenseType(license.licenseType || license.plan || "LIFETIME");
  const active = license.active === undefined ? (license.revoked !== true) : (license.active === true);
  const maxDevices = normalizePositiveInt(license.maxDevices, DEFAULT_MAX_DEVICES, MAX_DEVICES);
  const activated = Boolean(license.activatedAt || license.activationUsedAt);

  let trialEndsAt = null;
  if (licenseType === "TRIAL") {
    if (license.trialEndsAt) {
      trialEndsAt = Number(license.trialEndsAt);
    } else if (activated) {
      const start = Number(license.activatedAt || license.activationUsedAt || license.issuedAt || now);
      const days = normalizePositiveInt(license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS);
      trialEndsAt = start + days * 24 * 60 * 60 * 1000;
    }
  }

  return {
    ...license,
    licenseType,
    plan: licenseType,
    active,
    revoked: !active,
    maxDevices,
    activatedAt: license.activatedAt || license.activationUsedAt || null,
    activationUsedAt: license.activationUsedAt || license.activatedAt || null,
    trialDays: licenseType === "TRIAL" ? normalizePositiveInt(license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS) : 0,
    trialEndsAt
  };
}

function sanitizeLicenseForAdmin(license) {
  if (!license) return null;
  const { activationCodeHash, ...safe } = license;
  return safe;
}

async function licenseRecord(env, accountCode) {
  const raw = await getKV(env).get(`license:${accountCode}`, "json");
  return raw ? normalizeLicenseRecord(raw) : null;
}

async function emailAccount(env, email) {
  const val = await getKV(env).get(`email:${email}`);
  return val ? normalizeAccountCode(val) : null;
}

async function saveLicense(env, license) {
  const kv = getKV(env);
  const record = normalizeLicenseRecord(license);
  await kv.put(`license:${record.accountCode}`, JSON.stringify(record));
  if (record.email) {
    await kv.put(`email:${record.email}`, record.accountCode);
  }
}

async function deleteEmailMappingIfOwned(env, email, accountCode) {
  if (!email) return;
  const kv = getKV(env);
  const current = await emailAccount(env, email);
  if (current === accountCode) {
    await kv.delete(`email:${email}`);
  }
}

// --- 5. Device Registry & Smart Eviction Engine ---

async function listInstallations(env, accountCode) {
  const installations = [];
  const kv = getKV(env);
  let cursor;
  do {
    const page = await kv.list({ prefix: `install:${accountCode}:`, limit: 1000, cursor });
    for (const key of page.keys || []) {
      const val = await kv.get(key.name, "json");
      if (val) {
        installations.push({ key: key.name, ...val });
      }
    }
    cursor = page.list_complete ? undefined : page.cursor;
  } while (cursor);
  return installations;
}

/**
 * Registers or touches a device. If maxDevices limit is exceeded, automatically
 * revokes the oldest active device with a clear transition message.
 */
async function enrollOrUpdateDevice(env, license, fingerprint, publicKey, email) {
  const kv = getKV(env);
  const now = Date.now();
  const accountCode = license.accountCode;
  const maxDevices = Number(license.maxDevices || DEFAULT_MAX_DEVICES);
  const installations = await listInstallations(env, accountCode);

  const existingDevice = installations.find(
    item => String(item.fingerprint || "").toUpperCase() === fingerprint
  );

  const activeInstallations = installations.filter(item => item.revoked !== true);
  const isAlreadyActive = existingDevice && existingDevice.revoked !== true;

  if (!isAlreadyActive && activeInstallations.length >= maxDevices) {
    // Sort active devices ascending by lastSeenAt (fallback to createdAt)
    activeInstallations.sort((a, b) => {
      const timeA = Number(a.lastSeenAt || a.createdAt || 0);
      const timeB = Number(b.lastSeenAt || b.createdAt || 0);
      return timeA - timeB;
    });

    const excessCount = (activeInstallations.length - maxDevices) + 1;
    for (let i = 0; i < excessCount; i++) {
      const oldest = activeInstallations[i];
      if (String(oldest.fingerprint || "").toUpperCase() === fingerprint) continue;
      await kv.put(
        oldest.key,
        JSON.stringify({
          ...oldest,
          revoked: true,
          revokedAt: now,
          revocationReason: "device_replaced",
          noticeMessage: "تم تفعيل حساب SmartLedger على جهاز آخر، وتم إلغاء تفعيل هذا الجهاز."
        })
      );
    }
  }

  const deviceData = {
    fingerprint,
    publicKey: publicKey || existingDevice?.publicKey || "",
    email: email || license.email,
    createdAt: existingDevice?.createdAt || now,
    lastSeenAt: now,
    revoked: false,
    revokedAt: null,
    revocationReason: null,
    noticeMessage: null
  };

  await kv.put(`install:${accountCode}:${fingerprint}`, JSON.stringify(deviceData));
  return deviceData;
}

// --- 6. Token Issuance (Guaranteed Expiry & Offline Bound) ---

async function issueAccountToken(accountCode, license, fingerprint, env) {
  const now = Date.now();
  const trialEndsAt = Number(license.trialEndsAt || 0);
  const plan = String(license.licenseType || "LIFETIME").toUpperCase();

  let offlineUntil;
  let expiresAt = null;

  if (plan === "TRIAL") {
    if (!trialEndsAt || now >= trialEndsAt) {
      throw new Error("trial_expired");
    }
    expiresAt = trialEndsAt;
    offlineUntil = Math.min(trialEndsAt, now + TOKEN_OFFLINE_DAYS * 24 * 60 * 60 * 1000);
  } else {
    offlineUntil = now + TOKEN_OFFLINE_DAYS * 24 * 60 * 60 * 1000;
  }

  const payload = {
    product: "SMARTLEDGER",
    type: "ACCOUNT",
    plan,
    licenseType: plan,
    licenseId: String(license.licenseId || accountCode),
    accountCode,
    email: String(license.email || ""),
    issuedAt: now,
    offlineUntil,
    expiresAt,
    trialEndsAt: plan === "TRIAL" ? trialEndsAt : null,
    maxDevices: Number(license.maxDevices || DEFAULT_MAX_DEVICES),
    deviceFingerprint: fingerprint,
    keyVersion: 1
  };

  const header = b64Url(new TextEncoder().encode(JSON.stringify({ alg: "RS256", typ: "SLT1", kid: "ACCOUNT1" })));
  const body = b64Url(new TextEncoder().encode(JSON.stringify(payload)));
  return signToken(header, body, env);
}

// --- 7. Core Public License Endpoints ---

/**
 * First-time account activation using the initial activation code.
 * Fails if code has already been consumed and the account is activated.
 */
async function activate(request, env) {
  const body = await readJson(request);
  let accountCode = normalizeAccountCode(body.accountCode);
  const emailInput = normalizeEmail(body.email);
  const activationCode = String(body.activationCode || "").trim().toUpperCase();
  const publicKey = String(body.devicePublicKey || "").trim();

  if (!publicKey || publicKey.length > PUBLIC_KEY_MAX || activationCode.length < 16) {
    return json(400, { error: "invalid_activation" });
  }

  if (!accountCode && emailInput) {
    accountCode = await emailAccount(env, emailInput);
  }

  if (!ACCOUNT_PATTERN.test(accountCode)) {
    return json(400, { error: "invalid_activation" });
  }

  if (!(await rateLimit(env, accountCode, clientIp(request), RATE_LIMIT_SENSITIVE))) {
    return json(429, { error: "rate_limited" });
  }

  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: "not_found" });

  if (license.active !== true) {
    return json(403, { error: "account_disabled", message: "تم تعطيل هذا الحساب." });
  }

  if (emailInput && normalizeEmail(license.email) !== emailInput) {
    return json(403, { error: "email_mismatch" });
  }

  const fingerprint = await hexHash(fromB64(publicKey));
  if (!FINGERPRINT_PATTERN.test(fingerprint)) {
    return json(400, { error: "invalid_device_key" });
  }

  // Verify Activation Code
  const expectedHash = String(license.activationCodeHash || "").toUpperCase();
  const suppliedHash = await hexHash(activationCode);
  if (!expectedHash || suppliedHash !== expectedHash) {
    return json(403, { error: "activation_invalid" });
  }

  const now = Date.now();
  const isFirstActivation = !license.activatedAt && !license.activationUsedAt;

  // Initialize Trial on first activation only
  if (isFirstActivation) {
    license.activatedAt = now;
    license.activationUsedAt = now;
    if (license.licenseType === "TRIAL") {
      const days = normalizePositiveInt(license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS);
      license.trialEndsAt = now + days * 24 * 60 * 60 * 1000;
    }
    license.updatedAt = now;
    await saveLicense(env, license);
  }

  // Check trial expiration
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= license.trialEndsAt) {
    return json(403, { error: "trial_expired", message: "انتهت الفترة التجريبية لهذا الترخيص." });
  }

  await enrollOrUpdateDevice(env, license, fingerprint, publicKey, license.email);

  const token = await issueAccountToken(accountCode, license, fingerprint, env);

  return json(200, {
    licensed: true,
    token,
    accountCode,
    email: license.email,
    licenseType: license.licenseType,
    plan: license.licenseType,
    active: true,
    maxDevices: license.maxDevices,
    trialEndsAt: license.trialEndsAt || null
  });
}

/**
 * Verifies ongoing device installation authorization and issues fresh session tokens.
 */
async function verify(request, env) {
  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  const fingerprint = String(body.deviceFingerprint || "").trim().toUpperCase();
  const challenge = String(body.challenge || "").trim();
  const signature = String(body.signature || "").trim();

  if (!ACCOUNT_PATTERN.test(accountCode) || !FINGERPRINT_PATTERN.test(fingerprint) || challenge.length < 32 || !signature) {
    return json(400, { error: "invalid_verification" });
  }

  if (!(await rateLimit(env, accountCode, clientIp(request)))) {
    return json(429, { error: "rate_limited" });
  }

  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: "not_found" });

  if (license.active !== true) {
    return json(200, {
      revoked: true,
      active: false,
      status: "account_disabled",
      message: "تم إيقاف هذا الحساب من قبل الإدارة."
    });
  }

  // Check Trial Expiry
  const now = Date.now();
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= license.trialEndsAt) {
    return json(200, {
      revoked: true,
      expired: true,
      status: "trial_expired",
      message: "انتهت الفترة التجريبية لهذا الترخيص."
    });
  }

  const kv = getKV(env);
  const installation = await kv.get(`install:${accountCode}:${fingerprint}`, "json");

  // If device is missing or has been revoked by eviction engine
  if (!installation || installation.revoked === true) {
    return json(200, {
      revoked: true,
      active: false,
      status: "device_revoked",
      reason: installation?.revocationReason || "device_replaced",
      message: installation?.noticeMessage || "تم تفعيل حساب SmartLedger على جهاز آخر، وتم إلغاء تفعيل هذا الجهاز."
    });
  }

  // Challenge TTL verification
  const challengeTime = Number(challenge.split(":", 1)[0]);
  if (!Number.isFinite(challengeTime) || Math.abs(now - challengeTime) > CHALLENGE_TTL_MS) {
    return json(403, { error: "challenge_expired" });
  }

  // Device Signature verification
  const validProof = await verifyDeviceSignature(String(installation.publicKey || ""), challenge, signature);
  if (!validProof) {
    return json(403, { error: "proof_invalid" });
  }

  // Touch device last seen
  await kv.put(
    `install:${accountCode}:${fingerprint}`,
    JSON.stringify({ ...installation, lastSeenAt: now })
  );

  const token = await issueAccountToken(accountCode, license, fingerprint, env);

  return json(200, {
    token,
    accountCode,
    email: license.email,
    licenseType: license.licenseType,
    plan: license.licenseType,
    active: true,
    maxDevices: license.maxDevices,
    trialEndsAt: license.trialEndsAt || null
  });
}

/**
 * Seamless automatic enrollment for subsequent devices upon validated login.
 * Does not require activation code if account is already activated.
 */
async function autoActivateByEmail(request, env) {
  const body = await readJson(request);
  const email = normalizeEmail(body.email);
  const publicKey = String(body.devicePublicKey || "").trim();

  if (!EMAIL_PATTERN.test(email) || !publicKey || publicKey.length > PUBLIC_KEY_MAX) {
    return json(400, { error: "invalid_request" });
  }

  if (!(await rateLimit(env, email, clientIp(request), RATE_LIMIT_SENSITIVE))) {
    return json(429, { error: "rate_limited" });
  }

  const accountCode = await emailAccount(env, email);
  if (!accountCode) {
    return json(200, { licensed: false, registered: false, error: "no_license_for_email" });
  }

  const license = await licenseRecord(env, accountCode);
  if (!license || license.active !== true) {
    return json(200, { licensed: false, registered: true, revoked: true, accountCode, message: "الحساب غير نشط." });
  }

  // An unactivated account must pass first-time activation with its activation code
  const isActivated = Boolean(license.activatedAt || license.activationUsedAt);
  if (!isActivated) {
    return json(200, {
      licensed: false,
      registered: true,
      activationRequired: true,
      accountCode,
      error: "initial_activation_required",
      message: "الحساب يحتاج إلى إدخال كود التفعيل لأول مرة."
    });
  }

  const now = Date.now();
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= license.trialEndsAt) {
    return json(200, {
      licensed: false,
      registered: true,
      expired: true,
      accountCode,
      message: "انتهت الفترة التجريبية."
    });
  }

  const fingerprint = await hexHash(fromB64(publicKey));
  if (!FINGERPRINT_PATTERN.test(fingerprint)) {
    return json(400, { error: "invalid_device_key" });
  }

  await enrollOrUpdateDevice(env, license, fingerprint, publicKey, license.email);

  const token = await issueAccountToken(accountCode, license, fingerprint, env);

  return json(200, {
    licensed: true,
    registered: true,
    accountCode,
    email: license.email,
    licenseType: license.licenseType,
    plan: license.licenseType,
    token,
    maxDevices: license.maxDevices,
    trialEndsAt: license.trialEndsAt || null
  });
}

/**
 * Queries the operational status of an account (trial remaining days, devices, status).
 */
async function checkLicenseStatus(request, env) {
  const body = await readJson(request);
  let accountCode = normalizeAccountCode(body.accountCode);
  const email = normalizeEmail(body.email);

  if (!accountCode && email) {
    accountCode = await emailAccount(env, email);
  }

  if (!accountCode) {
    return json(200, { licensed: false, registered: false });
  }

  const license = await licenseRecord(env, accountCode);
  if (!license) {
    return json(200, { licensed: false, registered: false });
  }

  const now = Date.now();
  const active = license.active === true;
  const isTrial = license.licenseType === "TRIAL";
  const expired = isTrial && Boolean(license.trialEndsAt && now >= license.trialEndsAt);

  let remainingDays = 0;
  if (isTrial && license.trialEndsAt && license.trialEndsAt > now) {
    remainingDays = Math.max(0, Math.ceil((license.trialEndsAt - now) / (24 * 60 * 60 * 1000)));
  }

  return json(200, {
    licensed: active && !expired,
    registered: true,
    active,
    expired,
    accountCode,
    email: license.email,
    licenseType: license.licenseType,
    plan: license.licenseType,
    maxDevices: license.maxDevices,
    trialEndsAt: license.trialEndsAt || null,
    activatedAt: license.activatedAt || null,
    remainingDays: isTrial ? remainingDays : null
  });
}

// --- 8. Admin Security & Operations ---

function adminAuthorized(request, env) {
  const expected = String(env.SMARTLEDGER_ADMIN_SECRET || "").trim();
  const supplied = String(request.headers.get("X-SMARTLEDGER-ADMIN") || "").trim();
  return expected.length > 0 && supplied.length > 0 && supplied === expected;
}

async function requireAdmin(request, env) {
  if (!String(env.SMARTLEDGER_ADMIN_SECRET || "").trim()) {
    return json(500, { error: "admin_secret_missing" });
  }
  if (!adminAuthorized(request, env)) {
    return json(401, { error: "unauthorized" });
  }
  return null;
}

async function adminPing(request, env) {
  const authErr = await requireAdmin(request, env);
  if (authErr) return authErr;
  return json(200, {
    ok: true,
    status: "connected",
    service: "al-daftar-license-api",
    version: 2
  });
}

async function adminIssueLicense(request, env) {
  const authErr = await requireAdmin(request, env);
  if (authErr) return authErr;

  const body = await readJson(request);
  const email = normalizeEmail(body.email);
  if (!EMAIL_PATTERN.test(email)) {
    return json(400, { error: "email_required" });
  }

  // Prevent email collision
  const existingAccount = await emailAccount(env, email);
  if (existingAccount && !body.reissue) {
    const currentLic = await licenseRecord(env, existingAccount);
    if (currentLic) {
      return json(409, {
        error: "email_already_registered",
        accountCode: existingAccount,
        licenseId: currentLic.licenseId
      });
    }
  }

  let accountCode = normalizeAccountCode(body.accountCode);
  if (accountCode && !ACCOUNT_PATTERN.test(accountCode)) {
    return json(400, { error: "invalid_account_code" });
  }

  if (!accountCode) {
    for (let i = 0; i < 10; i++) {
      const cand = randomAccountCode();
      if (!(await licenseRecord(env, cand))) {
        accountCode = cand;
        break;
      }
    }
  }

  if (!accountCode) return json(500, { error: "account_code_generation_failed" });

  const licenseType = normalizeLicenseType(body.licenseType || body.plan || "LIFETIME");
  const trialDays = normalizePositiveInt(body.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS);
  const maxDevices = normalizePositiveInt(body.maxDevices, DEFAULT_MAX_DEVICES, MAX_DEVICES);
  const active = body.active === undefined ? true : (body.active === true || String(body.active).trim().toUpperCase() === "TRUE");

  const activationCode = String(body.activationCode || "").trim().toUpperCase() || randomActivationCode();
  if (activationCode.length < 16 || activationCode.length > 64) {
    return json(400, { error: "invalid_activation_code" });
  }

  const now = Date.now();
  const license = {
    product: "SMARTLEDGER",
    type: "ACCOUNT",
    licenseType,
    plan: licenseType,
    licenseId: `SL-${crypto.randomUUID().replace(/-/g, "").toUpperCase()}`,
    accountCode,
    email,
    active,
    revoked: !active,
    trialDays: licenseType === "TRIAL" ? trialDays : 0,
    trialEndsAt: null, // Initialized only on first activation
    activatedAt: null,
    activationUsedAt: null,
    maxDevices,
    activationCodeHash: await hexHash(activationCode),
    issuedAt: now,
    updatedAt: now,
    notes: String(body.notes || "").trim()
  };

  await saveLicense(env, license);

  return json(200, {
    ok: true,
    created: true,
    accountCode,
    email,
    licenseId: license.licenseId,
    active: license.active,
    licenseType,
    plan: licenseType,
    trialDays: license.trialDays,
    maxDevices,
    activationCode,
    license: sanitizeLicenseForAdmin(license)
  });
}

/**
 * Differential and Idempotent Admin Update.
 * Modifies only requested attributes without resetting activation codes or license state.
 */
async function adminUpdateLicense(request, env) {
  const authErr = await requireAdmin(request, env);
  if (authErr) return authErr;

  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  if (!ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_account_code" });

  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: "not_found" });

  let changed = false;

  // 1. Dedicated Email Replacement Logic
  let newActivationCode = null;
  if (body.replaceEmail !== undefined && body.replaceEmail !== "") {
    const targetEmail = normalizeEmail(body.replaceEmail);
    if (!EMAIL_PATTERN.test(targetEmail)) {
      return json(400, { error: "invalid_new_email" });
    }

    // Check collision
    const existingOwner = await emailAccount(env, targetEmail);
    if (existingOwner && existingOwner !== accountCode) {
      return json(409, { error: "email_collision", message: "البريد الإلكتروني الجديد مرتبط بحساب آخر." });
    }

    const oldEmail = normalizeEmail(license.email);
    if (oldEmail && oldEmail !== targetEmail) {
      await deleteEmailMappingIfOwned(env, oldEmail, accountCode);
    }

    // Revoke previous devices on email swap
    const oldInstallations = await listInstallations(env, accountCode);
    const kv = getKV(env);
    for (const inst of oldInstallations) {
      await kv.put(
        inst.key,
        JSON.stringify({
          ...inst,
          revoked: true,
          revokedAt: Date.now(),
          revocationReason: "email_replaced",
          noticeMessage: "تم تغيير البريد الإلكتروني للحساب، يرجى إعادة التفعيل."
        })
      );
    }

    newActivationCode = randomActivationCode();
    license.email = targetEmail;
    license.activationCodeHash = await hexHash(newActivationCode);
    license.activatedAt = null;
    license.activationUsedAt = null;
    if (license.licenseType === "TRIAL") {
      license.trialEndsAt = null;
    }
    changed = true;
  }

  // 2. Explicit Activation Code Regeneration
  if (body.regenerateActivationCode === true && !newActivationCode) {
    newActivationCode = randomActivationCode();
    license.activationCodeHash = await hexHash(newActivationCode);
    license.activationUsedAt = null;
    license.activatedAt = null;
    changed = true;
  }

  // 3. Status Toggle (Active / Disabled)
  if (body.active !== undefined) {
    const newActive = body.active === true || String(body.active).trim().toUpperCase() === "TRUE";
    if (license.active !== newActive) {
      license.active = newActive;
      license.revoked = !newActive;
      changed = true;
    }
  }

  // 4. Max Simultaneous Devices
  if (body.maxDevices !== undefined) {
    const newMax = normalizePositiveInt(body.maxDevices, DEFAULT_MAX_DEVICES, MAX_DEVICES);
    if (license.maxDevices !== newMax) {
      license.maxDevices = newMax;
      changed = true;
    }
  }

  // 5. Notes
  if (body.notes !== undefined) {
    const newNotes = String(body.notes || "").trim();
    if (license.notes !== newNotes) {
      license.notes = newNotes;
      changed = true;
    }
  }

  // 6. License Type (TRIAL <-> LIFETIME)
  if (body.licenseType !== undefined || body.plan !== undefined) {
    const targetType = normalizeLicenseType(body.licenseType || body.plan);
    if (license.licenseType !== targetType) {
      license.licenseType = targetType;
      license.plan = targetType;
      if (targetType === "LIFETIME") {
        license.trialEndsAt = null;
      } else if (targetType === "TRIAL" && license.activatedAt) {
        const days = normalizePositiveInt(body.trialDays || license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS);
        license.trialEndsAt = Number(license.activatedAt) + days * 24 * 60 * 60 * 1000;
      }
      changed = true;
    }
  }

  // 7. Explicit Trial Extension
  if (body.extendTrialDays !== undefined && license.licenseType === "TRIAL") {
    const additionalDays = normalizePositiveInt(body.extendTrialDays, 0, MAX_TRIAL_DAYS);
    if (additionalDays > 0) {
      const baseTime = (license.trialEndsAt && license.trialEndsAt > Date.now()) ? license.trialEndsAt : Date.now();
      license.trialEndsAt = baseTime + additionalDays * 24 * 60 * 60 * 1000;
      changed = true;
    }
  }

  if (changed) {
    license.updatedAt = Date.now();
    await saveLicense(env, license);
  }

  return json(200, {
    ok: true,
    changed,
    accountCode,
    activationCode: newActivationCode,
    license: sanitizeLicenseForAdmin(license)
  });
}

async function adminGetLicense(request, env) {
  const authErr = await requireAdmin(request, env);
  if (authErr) return authErr;

  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  if (!ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_account_code" });

  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: "not_found" });

  const installations = await listInstallations(env, accountCode);

  return json(200, {
    ok: true,
    license: sanitizeLicenseForAdmin(license),
    devices: installations.map(d => ({
      fingerprint: d.fingerprint,
      email: d.email,
      createdAt: d.createdAt,
      lastSeenAt: d.lastSeenAt,
      revoked: d.revoked === true,
      revocationReason: d.revocationReason || null
    }))
  });
}

async function adminRevokeDevice(request, env) {
  const authErr = await requireAdmin(request, env);
  if (authErr) return authErr;

  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  const fingerprint = String(body.deviceFingerprint || "").trim().toUpperCase();

  if (!ACCOUNT_PATTERN.test(accountCode) || !FINGERPRINT_PATTERN.test(fingerprint)) {
    return json(400, { error: "invalid_request" });
  }

  const kv = getKV(env);
  const key = `install:${accountCode}:${fingerprint}`;
  const installation = await kv.get(key, "json");
  if (!installation) return json(404, { error: "device_not_found" });

  await kv.put(
    key,
    JSON.stringify({
      ...installation,
      revoked: true,
      revokedAt: Date.now(),
      revocationReason: "admin_revoked",
      noticeMessage: "تم إلغاء تفعيل هذا الجهاز من قبل الإدارة."
    })
  );

  return json(200, { ok: true, revoked: true, accountCode, deviceFingerprint: fingerprint });
}

// --- 9. Google Drive System (Preserved Complete Architecture) ---

async function encryptText(value, secret) {
  const keyBytes = await sha256(`${secret}:SMARTLEDGER_DRIVE_TOKEN_V1`);
  const key = await crypto.subtle.importKey("raw", keyBytes, "AES-GCM", false, ["encrypt", "decrypt"]);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const ciphertext = new Uint8Array(await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, new TextEncoder().encode(value)));
  return `${b64Url(iv)}.${b64Url(ciphertext)}`;
}

async function decryptText(value, secret) {
  const [ivPart, cipherPart] = String(value || "").split(".");
  if (!ivPart || !cipherPart) throw new Error("invalid_session");
  const keyBytes = await sha256(`${secret}:SMARTLEDGER_DRIVE_TOKEN_V1`);
  const key = await crypto.subtle.importKey("raw", keyBytes, "AES-GCM", false, ["decrypt"]);
  const plaintext = await crypto.subtle.decrypt({ name: "AES-GCM", iv: fromB64(ivPart) }, key, fromB64(cipherPart));
  return new TextDecoder().decode(plaintext);
}

function redirectUri(request) {
  const url = new URL(request.url);
  return `${url.origin}/driveOAuthCallback`;
}

async function exchangeCode(code, request, env) {
  const body = new URLSearchParams({
    code,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    redirect_uri: redirectUri(request),
    grant_type: "authorization_code"
  });
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
  let data = {};
  try {
    data = await response.json();
  } catch (_) {}
  if (!response.ok || !data.refresh_token) {
    const err = new Error("authorization_failed");
    err.failureCode = data.error || "oauth_token_exchange_failed";
    throw err;
  }
  return data.refresh_token;
}

async function accessToken(refreshToken, env) {
  const body = new URLSearchParams({
    refresh_token: refreshToken,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    grant_type: "refresh_token"
  });
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
  const data = await response.json();
  if (!response.ok || !data.access_token) throw new Error("invalid_session");
  return data.access_token;
}

async function sessionRecord(token, env) {
  if (!token) throw new Error("invalid_session");
  const kv = getKV(env);
  const key = `session:${await hexHash(token)}`;
  const session = await kv.get(key, "json");
  if (!session) throw new Error("invalid_session");
  if (Date.now() - Number(session.lastSeenAt || session.createdAt || 0) > SESSION_IDLE_MS) {
    await kv.delete(key);
    throw new Error("invalid_session");
  }
  const updated = { ...session, lastSeenAt: Date.now() };
  // Keep the backend session alive while it is actively used. The Google
  // refresh token remains encrypted in KV; only this opaque session token
  // is exposed to the Android client.
  await kv.put(key, JSON.stringify(updated), { expirationTtl: 60 * 60 * 24 * 180 });
  return { key, ...updated };
}

async function driveRequest(token, method, path, env, options = {}) {
  const session = await sessionRecord(token, env);
  const refreshToken = await decryptText(session.refreshToken, getSalt(env));
  const bearer = await accessToken(refreshToken, env);
  const headers = new Headers(options.headers || {});
  headers.set("Authorization", `Bearer ${bearer}`);
  return fetch(`https://www.googleapis.com/drive/v3/${path}`, { method, headers, body: options.body });
}

async function driveJson(token, method, path, env, options = {}) {
  const response = await driveRequest(token, method, path, env, options);
  const text = await response.text();
  let data = {};
  try {
    data = JSON.parse(text);
  } catch (_) {}
  if (!response.ok) {
    if (response.status === 401) throw new Error("invalid_session");
    if (response.status === 403) throw new Error("drive_forbidden");
    if (response.status === 404) throw new Error("drive_not_found");
    if (response.status === 429) throw new Error("drive_rate_limited");
    if (response.status >= 500) throw new Error("drive_unavailable");
    throw new Error("drive_error");
  }
  return data;
}

async function listFiles(token, search, env) {
  const lower = search.toLowerCase();
  const root = await ensureFolder(token, "root", DRIVE_ROOT, env);
  const folderQuery = `'${root.id}' in parents and trashed = false and mimeType = 'application/vnd.google-apps.folder'`;
  const folderData = await driveJson(token, "GET", `files?pageSize=100&fields=files(id,name,mimeType)&q=${encodeURIComponent(folderQuery)}`, env);
  const items = [];

  for (const folder of folderData.files || []) {
    const qParts = [
      `'${folder.id}' in parents`,
      "trashed = false",
      "mimeType = 'application/vnd.smartledger.backup'",
      "name contains 'SNA_'"
    ];
    if (lower && /^[a-z0-9_.-]+$/i.test(lower)) {
      qParts.push(`name contains '${lower.replace(/'/g, "\\'")}'`);
    }
    const data = await driveJson(token, "GET", `files?pageSize=100&fields=files(id,name,mimeType,size,modifiedTime)&orderBy=modifiedTime desc&q=${encodeURIComponent(qParts.join(" and "))}`, env);
    for (const item of data.files || []) {
      if (!BACKUP_PATTERN.test(item.name || "")) continue;
      if (lower && !String(item.name).toLowerCase().includes(lower) && !String(folder.name).toLowerCase().includes(lower)) continue;
      items.push({
        id: item.id,
        name: item.name,
        size: Number(item.size || 0),
        modifiedTime: Date.parse(item.modifiedTime || "") || 0,
        month: folder.name
      });
    }
  }
  items.sort((a, b) => b.modifiedTime - a.modifiedTime);
  return items;
}

async function findFolder(token, parentId, name, env) {
  const q = `'${parentId}' in parents and trashed = false and mimeType = 'application/vnd.google-apps.folder' and name = '${name.replace(/'/g, "\\'")}'`;
  const data = await driveJson(token, "GET", `files?pageSize=10&fields=files(id,name,mimeType)&q=${encodeURIComponent(q)}`, env);
  return data.files?.[0] || null;
}

async function ensureFolder(token, parentId, name, env) {
  const existing = await findFolder(token, parentId, name, env);
  if (existing) return existing;
  return driveJson(token, "POST", "files", env, {
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, mimeType: "application/vnd.google-apps.folder", parents: [parentId] })
  });
}

function monthFolderName(month) {
  const match = /^(\d{4})-(\d{2})$/.exec(String(month || ""));
  return match ? `شهر ${match[2]}` : "شهر غير محدد";
}

async function monthFolder(token, month, env) {
  const root = await ensureFolder(token, "root", DRIVE_ROOT, env);
  return ensureFolder(token, root.id, monthFolderName(month), env);
}

async function driveConnectWithServerAuthCode(request, env) {
  const body = await readJson(request);
  const code = String(body.serverAuthCode || "").trim();
  if (!code) return json(400, { error: "invalid_auth_code" });
  const exchangeBody = new URLSearchParams({
    code,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    grant_type: "authorization_code"
  });
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: exchangeBody
  });
  let data = {};
  try {
    data = await response.json();
  } catch (_) {}
  if (!response.ok || !data.refresh_token) {
    const failureCode = data.error === "invalid_grant" ? "oauth_invalid_grant" : data.error === "invalid_client" ? "oauth_invalid_client" : data.error === "access_denied" ? "oauth_access_denied" : !data.refresh_token && response.ok ? "oauth_no_refresh_token" : "oauth_token_exchange_failed";
    return json(400, { error: failureCode });
  }
  const cloudToken = randomToken();
  await getKV(env).put(
    `session:${await hexHash(cloudToken)}`,
    JSON.stringify({ refreshToken: await encryptText(data.refresh_token, getSalt(env)), createdAt: Date.now(), lastSeenAt: Date.now() }),
    { expirationTtl: 60 * 60 * 24 * 180 }
  );
  return json(200, { status: "connected", cloudToken });
}

async function driveStart(request, env) {
  const body = await readJson(request);
  const connectionId = String(body.connectionId || "").trim();
  if (!/^[A-Za-z0-9_-]{20,80}$/.test(connectionId)) return json(400, { error: "invalid_connection" });
  const state = randomToken();
  const stateHash = await hexHash(state);
  const connectionHash = await hexHash(connectionId);
  const kv = getKV(env);
  await kv.put(`oauth:${stateHash}`, JSON.stringify({ connectionId, createdAt: Date.now(), status: "pending" }), { expirationTtl: 15 * 60 });
  await kv.put(`oauth-connection:${connectionHash}`, stateHash, { expirationTtl: 15 * 60 });
  const params = new URLSearchParams({
    client_id: env.GOOGLE_CLIENT_ID,
    redirect_uri: redirectUri(request),
    response_type: "code",
    access_type: "offline",
    prompt: "consent",
    scope: DRIVE_SCOPE,
    state
  });
  return json(200, { authorizationUrl: `https://accounts.google.com/o/oauth2/v2/auth?${params}` });
}

async function driveCallback(request, env) {
  const url = new URL(request.url);
  const state = String(url.searchParams.get("state") || "");
  const code = String(url.searchParams.get("code") || "");
  const googleError = String(url.searchParams.get("error") || "");
  const kv = getKV(env);

  if (!state || (!code && !googleError)) {
    return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تعذر إكمال ربط Google Drive.</h2></body></html>');
  }

  const key = `oauth:${await hexHash(state)}`;
  const pending = await kv.get(key, "json");
  if (!pending || Date.now() - Number(pending.createdAt || 0) > OAUTH_STATE_TTL_MS) {
    return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>انتهت جلسة الربط. أعد المحاولة من التطبيق.</h2></body></html>');
  }

  if (googleError) {
    const safeFailureCode = googleError === "access_denied" ? "oauth_access_denied" : "oauth_google_error";
    await kv.put(key, JSON.stringify({ ...pending, status: "failed", failureCode: safeFailureCode, updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تم إلغاء أو رفض تفويض Google Drive.</h2><p>يمكنك العودة للتطبيق والمحاولة مرة أخرى.</p></body></html>');
  }

  try {
    const refreshToken = await exchangeCode(code, request, env);
    await kv.put(key, JSON.stringify({ ...pending, status: "connected", refreshToken: await encryptText(refreshToken, getSalt(env)), updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(200, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تم ربط Google Drive بنجاح.</h2><p>يمكنك الآن العودة إلى تطبيق الدفتر الذكي.</p></body></html>');
  } catch (err) {
    const safeFailureCode = err?.failureCode || "oauth_token_exchange_failed";
    await kv.put(key, JSON.stringify({ ...pending, status: "failed", failureCode: safeFailureCode, updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(500, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تعذر إكمال ربط Google Drive.</h2><p>ارجع إلى التطبيق وحاول مرة أخرى.</p></body></html>');
  }
}

async function driveStatus(request, env) {
  const body = await readJson(request);
  const connectionId = String(body.connectionId || "").trim();
  if (!connectionId) return json(400, { error: "invalid_connection" });
  const kv = getKV(env);
  const stateHash = await kv.get(`oauth-connection:${await hexHash(connectionId)}`);
  if (!stateHash) return json(404, { error: "not_found" });
  const key = `oauth:${stateHash}`;
  const pending = await kv.get(key, "json");
  if (!pending) return json(404, { error: "not_found" });
  if (Date.now() - Number(pending.createdAt || 0) > OAUTH_STATE_TTL_MS) {
    await kv.delete(key);
    await kv.delete(`oauth-connection:${await hexHash(connectionId)}`);
    return json(410, { error: "expired" });
  }
  if (pending.status === "connected") {
    const cloudToken = randomToken();
    await kv.put(`session:${await hexHash(cloudToken)}`, JSON.stringify({ refreshToken: pending.refreshToken, createdAt: Date.now(), lastSeenAt: Date.now() }));
    await kv.delete(key);
    await kv.delete(`oauth-connection:${await hexHash(connectionId)}`);
    return json(200, { status: "connected", cloudToken });
  }
  if (pending.status === "failed") {
    return json(200, { status: "failed", failureCode: pending.failureCode || "oauth_failed" });
  }
  return json(200, { status: pending.status || "pending" });
}

/**
 * Uploads a backup file. Notice: All artificial size limits (like 50MB) are removed.
 */
async function driveUpload(request, env) {
  const token = String(request.headers.get("X-SmartLedger-Cloud-Token") || "");
  const encodedName = String(request.headers.get("X-SmartLedger-File-Name") || "");
  const name = decodeURIComponent(encodedName);
  if (!BACKUP_PATTERN.test(name)) return json(400, { error: "invalid_name" });
  if (!request.body) return json(400, { error: "invalid_payload" });

  const month = name.slice(4, 11);
  const folder = await monthFolder(token, month, env);
  const existingData = await driveJson(token, "GET", `files?q=${encodeURIComponent(`'${folder.id}' in parents and trashed = false and name = '${name.replace(/'/g, "\\'")}'`)}&pageSize=10&fields=files(id,name,size,modifiedTime)`, env);
  const existing = existingData.files?.find((item) => item.name === name);

  let item;
  if (existing) {
    const response = await driveRequest(token, "PATCH", `upload/drive/v3/files/${encodeURIComponent(existing.id)}?uploadType=media`, env, { headers: { "Content-Type": DRIVE_MIME }, body: request.body });
    if (!response.ok) throw new Error("drive_error");
    item = await driveJson(token, "GET", `files/${encodeURIComponent(existing.id)}?fields=id,name,size,modifiedTime`, env);
  } else {
    const boundary = `smartledger_${randomToken().slice(0, 24)}`;
    const metadata = JSON.stringify({ name, mimeType: DRIVE_MIME, parents: [folder.id] });
    const encoder = new TextEncoder();
    const head = encoder.encode(`--${boundary}\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${metadata}\r\n--${boundary}\r\nContent-Type: ${DRIVE_MIME}\r\n\r\n`);
    const tail = encoder.encode(`\r\n--${boundary}--`);
    const source = request.body;
    const stream = new ReadableStream({
      async start(controller) {
        controller.enqueue(head);
        const reader = source.getReader();
        try {
          while (true) {
            const part = await reader.read();
            if (part.done) break;
            controller.enqueue(part.value);
          }
          controller.enqueue(tail);
          controller.close();
        } catch (error) {
          controller.error(error);
        } finally {
          reader.releaseLock();
        }
      }
    });
    item = await driveJson(token, "POST", "upload/drive/v3/files?uploadType=multipart&fields=id,name,size,modifiedTime", env, {
      headers: { "Content-Type": `multipart/related; boundary=${boundary}` },
      body: stream
    });
  }
  return json(200, {
    item: {
      id: item.id,
      name: item.name,
      size: Number(item.size || 0),
      modifiedTime: Date.parse(item.modifiedTime || "") || Date.now(),
      month
    }
  });
}

async function driveList(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || "");
  const search = String(body.search || "").trim();
  return json(200, { items: await listFiles(token, search, env) });
}

async function driveDownload(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || "");
  const fileId = String(body.fileId || "");
  if (!fileId) return json(400, { error: "invalid_file" });
  const response = await driveRequest(token, "GET", `files/${encodeURIComponent(fileId)}?alt=media`, env);
  if (!response.ok) throw new Error(response.status === 401 ? "invalid_session" : "drive_error");
  const headers = new Headers({ "Cache-Control": "no-store", "Content-Type": "application/octet-stream" });
  if (response.headers.has("Content-Length")) headers.set("Content-Length", response.headers.get("Content-Length"));
  return new Response(response.body, { status: 200, headers });
}

async function driveDelete(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || "");
  const ids = Array.isArray(body.fileIds) ? body.fileIds.map(String).slice(0, 100) : [];
  let deleted = 0;
  for (const id of ids) {
    const response = await driveRequest(token, "DELETE", `files/${encodeURIComponent(id)}`, env);
    if (response.ok || response.status === 404) deleted++;
  }
  return json(200, { deleted });
}

async function driveDisconnect(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || "");
  if (!token) return json(200, { ok: true });
  const kv = getKV(env);
  const key = `session:${await hexHash(token)}`;
  const session = await kv.get(key, "json");
  await kv.delete(key);
  if (session?.refreshToken) {
    try {
      const refreshToken = await decryptText(session.refreshToken, getSalt(env));
      await fetch(`https://oauth2.googleapis.com/revoke?token=${encodeURIComponent(refreshToken)}`, { method: "POST" });
    } catch (_) {}
  }
  return json(200, { ok: true });
}

// --- 10. Unified Dispatcher & Error Normalization ---

function routeNotFound() {
  return json(404, { error: "not_found" });
}

function errorResponse(error, request) {
  const code = String(error?.message || "server_error");

  if (code === "invalid_session") return withCors(json(401, { error: "invalid_session" }), request);
  if (code === "authorization_failed") return withCors(json(502, { error: "authorization_failed" }), request);
  if (code === "rate_limited" || code === "drive_rate_limited") return withCors(json(429, { error: "rate_limited" }), request);
  if (code === "drive_forbidden") return withCors(json(403, { error: "drive_forbidden" }), request);
  if (code === "drive_not_found") return withCors(json(404, { error: "drive_not_found" }), request);
  if (code === "drive_unavailable") return withCors(json(503, { error: "drive_unavailable" }), request);
  if (code === "drive_error") return withCors(json(502, { error: "drive_error" }), request);
  if (code === "invalid_json") return withCors(json(400, { error: "invalid_json" }), request);
  if (code === "license_signing_key_missing") return withCors(json(503, { error: "license_signing_key_missing" }), request);
  if (code === "invalid_license_type") return withCors(json(400, { error: "invalid_license_type" }), request);
  if (code === "trial_expired") return withCors(json(403, { error: "trial_expired" }), request);
  if (code === "kv_binding_missing") return withCors(json(500, { error: "kv_binding_missing" }), request);

  return withCors(json(500, { error: "server_error" }), request);
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return withCors(new Response(null, { status: 204 }), request);
    }

    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, "") || "/";

    try {
      let response;

      // Public GET endpoints & OAuth callback
      if (request.method === "GET") {
        if (path === "/") {
          response = json(200, {
            ok: true,
            service: "al-daftar-license-api",
            system: "SmartLedger Core",
            version: 2
          });
        } else if (path === "/driveOAuthCallback") {
          return await driveCallback(request, env);
        } else {
          response = routeNotFound();
        }
        return withCors(response, request);
      }

      if (request.method !== "POST") {
        return withCors(json(405, { error: "method_not_allowed" }), request);
      }

      // --- License Endpoints (Compatible with old & new clients) ---
      if (path === "/license/activate" || path === "/activate") {
        response = await activate(request, env);
      } else if (path === "/license/verify" || path === "/verify") {
        response = await verify(request, env);
      } else if (path === "/license/auto-activate" || path === "/auto-activate") {
        response = await autoActivateByEmail(request, env);
      } else if (path === "/license/check-status" || path === "/license/status" || path === "/status") {
        response = await checkLicenseStatus(request, env);
      }

      // --- Google Drive Endpoints (100% Preserved) ---
      else if (path === "/driveApi/config") {
        response = json(200, { googleClientId: String(env.GOOGLE_CLIENT_ID || "") });
      } else if (path === "/driveApi/connect/google-signin") {
        response = await driveConnectWithServerAuthCode(request, env);
      } else if (path === "/driveApi/connect/start") {
        response = await driveStart(request, env);
      } else if (path === "/driveApi/connect/status") {
        response = await driveStatus(request, env);
      } else if (path === "/driveApi/list") {
        response = await driveList(request, env);
      } else if (path === "/driveApi/upload") {
        response = await driveUpload(request, env);
      } else if (path === "/driveApi/download") {
        return await driveDownload(request, env);
      } else if (path === "/driveApi/delete") {
        response = await driveDelete(request, env);
      } else if (path === "/driveApi/disconnect") {
        response = await driveDisconnect(request, env);
      }

      // --- Admin Endpoints (Secured with X-SMARTLEDGER-ADMIN) ---
      else if (path === "/admin/ping") {
        response = await adminPing(request, env);
      } else if (path === "/admin/licenses/issue") {
        response = await adminIssueLicense(request, env);
      } else if (path === "/admin/licenses/update") {
        response = await adminUpdateLicense(request, env);
      } else if (path === "/admin/licenses/get") {
        response = await adminGetLicense(request, env);
      } else if (path === "/admin/devices/revoke") {
        response = await adminRevokeDevice(request, env);
      } else {
        response = routeNotFound();
      }

      return withCors(response, request);
    } catch (error) {
      return errorResponse(error, request);
    }
  }
};
