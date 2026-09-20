const ACCOUNT_PATTERN = /^SL-[A-Z0-9]{4}-[A-Z0-9]{4}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const FINGERPRINT_PATTERN = /^[A-F0-9]{64}$/;
const PUBLIC_KEY_MAX = 8192;
const DEFAULT_TRIAL_DAYS = 30;
const DEFAULT_MAX_DEVICES = 1;
const MAX_TRIAL_DAYS = 3650;
const MAX_DEVICES = 100;
const RATE_WINDOW_MS = 15 * 60 * 1000;
const RATE_LIMIT_DEFAULT = 15;
const RATE_LIMIT_SENSITIVE = 6;
const CHALLENGE_TTL_MS = 5 * 60 * 1000;
const TOKEN_OFFLINE_DAYS = 30;

const json = (status, body) => new Response(JSON.stringify(body), {
  status,
  headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" }
});

const normalizeEmail = value => String(value || "").trim().toLowerCase();
const normalizeAccountCode = value => String(value || "").trim().toUpperCase();

function normalizeLicenseType(value) {
  const type = String(value || "LIFETIME").trim().toUpperCase();
  if (type === "TRIAL" || type === "LIFETIME") return type;
  throw new Error("invalid_license_type");
}

function positiveInt(value, fallback, max) {
  const n = Number.parseInt(String(value ?? ""), 10);
  if (!Number.isFinite(n) || n < 1) return fallback;
  return Math.min(n, max);
}

function randomCode(length) {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  const bytes = crypto.getRandomValues(new Uint8Array(length));
  return Array.from(bytes, b => chars[b % chars.length]).join("");
}

function randomAccountCode() {
  const v = randomCode(8);
  return `SL-${v.slice(0, 4)}-${v.slice(4)}`;
}

function randomActivationCode() {
  return randomCode(24);
}

function b64Url(bytes) {
  const input = bytes instanceof ArrayBuffer ? new Uint8Array(bytes) : bytes;
  let binary = "";
  for (let i = 0; i < input.length; i += 32768) binary += String.fromCharCode(...input.subarray(i, i + 32768));
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function fromB64(value) {
  const normalized = String(value || "").replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(String(value || "").length / 4) * 4, "=");
  return Uint8Array.from(atob(normalized), c => c.charCodeAt(0));
}

async function sha256(value) {
  const data = typeof value === "string" ? new TextEncoder().encode(value) : value;
  return new Uint8Array(await crypto.subtle.digest("SHA-256", data));
}

async function hexHash(value) {
  return Array.from(await sha256(value), b => b.toString(16).padStart(2, "0")).join("").toUpperCase();
}

function pemToBytes(pem) {
  return fromB64(String(pem || "").replace(/-----BEGIN [^-]+-----/g, "").replace(/-----END [^-]+-----/g, "").replace(/\s+/g, ""));
}

async function signToken(header, body, env) {
  const pem = String(env.SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY || "").trim();
  if (!pem) throw new Error("license_signing_key_missing");
  const key = await crypto.subtle.importKey("pkcs8", pemToBytes(pem), { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["sign"]);
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(`${header}.${body}`));
  return `${header}.${body}.${b64Url(sig)}`;
}

async function verifyDeviceSignature(publicKeyBase64, message, signatureBase64) {
  try {
    const key = await crypto.subtle.importKey("spki", fromB64(publicKeyBase64), { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["verify"]);
    return await crypto.subtle.verify("RSASSA-PKCS1-v1_5", key, fromB64(signatureBase64), new TextEncoder().encode(message));
  } catch (_) {
    return false;
  }
}

function authContext(request) {
  const uid = String(request.headers.get("X-Firebase-UID") || "").trim();
  const email = normalizeEmail(request.headers.get("X-Firebase-Email"));
  const admin = request.headers.get("X-Firebase-Admin") === "true";
  return { uid, email, admin };
}

function requireAuth(request) {
  const auth = authContext(request);
  if (!auth.uid) return json(401, { error: "auth_required" });
  return auth;
}

function requireAdmin(request) {
  const auth = authContext(request);
  if (auth?.admin === true) return auth;
  return json(401, { error: "unauthorized" });
}

function db(env) {
  if (!env.SMARTLEDGER_LICENSE_DB) throw new Error("firestore_license_db_missing");
  return env.SMARTLEDGER_LICENSE_DB;
}

function userRef(env, uid) {
  return db(env).collection("users").doc(uid);
}

function licenseRef(env, accountCode) {
  return db(env).collection("licenses").doc(accountCode);
}

function devicesRef(env, accountCode) {
  return licenseRef(env, accountCode).collection("devices");
}

async function rateLimit(env, identity, ip, limit = RATE_LIMIT_DEFAULT) {
  const salt = String(env.SMARTLEDGER_RATE_LIMIT_SALT || "SMARTLEDGER");
  const key = await hexHash(`${salt}:${identity}:${ip}`);
  const ref = db(env).collection("rateLimits").doc(key);
  const now = Date.now();
  let allowed = true;
  await db(env).runTransaction(async tx => {
    const snap = await tx.get(ref);
    const old = snap.exists ? snap.data() : null;
    if (!old || now - Number(old.startedAt || 0) >= RATE_WINDOW_MS) {
      tx.set(ref, { startedAt: now, attempts: 1, expiresAt: now + RATE_WINDOW_MS }, { merge: true });
      return;
    }
    const attempts = Number(old.attempts || 0);
    if (attempts >= limit) {
      allowed = false;
      return;
    }
    tx.update(ref, { attempts: attempts + 1, expiresAt: now + RATE_WINDOW_MS });
  });
  return allowed;
}

function clientIp(request) {
  return request.headers.get("X-Forwarded-For")?.split(",")[0]?.trim() || request.headers.get("X-Real-IP") || "unknown";
}

async function readJson(request) {
  try { return await request.json(); } catch (_) { throw new Error("invalid_json"); }
}

function sanitizeLicense(license) {
  if (!license) return null;
  const { activationCodeHash, ...safe } = license;
  return safe;
}

async function findLicenseByEmail(env, email) {
  const normalized = normalizeEmail(email);
  if (!normalized) return null;
  const snap = await db(env).collection("licenses").where("email", "==", normalized).limit(1).get();
  return snap.empty ? null : snap.docs[0];
}

async function bindUser(env, uid, email, accountCode) {
  const license = licenseRef(env, accountCode);
  const licenseSnap = await license.get();
  if (!licenseSnap.exists) throw new Error("license_not_found");
  const currentLicense = licenseSnap.data();
  const ownerUid = String(currentLicense.ownerUid || "").trim();
  if (ownerUid && ownerUid !== uid) throw new Error("account_owner_mismatch");
  if (!ownerUid) {
    await license.set({ ownerUid: uid, updatedAt: Date.now() }, { merge: true });
  }

  const ref = userRef(env, uid);
  const existing = await ref.get();
  const data = {
    email: normalizeEmail(email),
    accountCode,
    updatedAt: Date.now()
  };
  if (!existing.exists) data.createdAt = Date.now();
  await ref.set(data, { merge: true });
}

async function licenseForAuthenticatedUser(env, auth, accountCode = null) {
  let code = normalizeAccountCode(accountCode);
  const u = await userRef(env, auth.uid).get();
  if (u.exists && u.data().accountCode) code = normalizeAccountCode(u.data().accountCode);
  if (!code && auth.email) {
    const found = await findLicenseByEmail(env, auth.email);
    if (found) code = found.id;
  }
  if (!code || !ACCOUNT_PATTERN.test(code)) return null;

  const snap = await licenseRef(env, code).get();
  if (!snap.exists) return null;
  const license = { ...snap.data(), accountCode: code };
  const ownerUid = String(license.ownerUid || "").trim();
  if (ownerUid && ownerUid !== auth.uid) return null;
  if (!ownerUid) await bindUser(env, auth.uid, auth.email || license.email, code);
  return { ref: snap.ref, data: license, accountCode: code };
}

async function issueAccountToken(accountCode, license, fingerprint, env) {
  const now = Date.now();
  const plan = String(license.licenseType || license.plan || "LIFETIME").toUpperCase();
  const trialEndsAt = Number(license.trialEndsAt || 0);
  if (plan === "TRIAL" && (!trialEndsAt || now >= trialEndsAt)) throw new Error("trial_expired");
  const offlineUntil = plan === "TRIAL"
    ? Math.min(trialEndsAt, now + TOKEN_OFFLINE_DAYS * 86400000)
    : now + TOKEN_OFFLINE_DAYS * 86400000;
  const payload = {
    product: "SMARTLEDGER", type: "ACCOUNT", plan, licenseType: plan,
    licenseId: String(license.licenseId || accountCode), accountCode,
    email: String(license.email || ""), issuedAt: now, offlineUntil,
    expiresAt: plan === "TRIAL" ? trialEndsAt : null,
    trialEndsAt: plan === "TRIAL" ? trialEndsAt : null,
    maxDevices: Number(license.maxDevices || DEFAULT_MAX_DEVICES),
    deviceFingerprint: fingerprint, keyVersion: 1
  };
  const header = b64Url(new TextEncoder().encode(JSON.stringify({ alg: "RS256", typ: "SLT1", kid: "ACCOUNT1" })));
  const body = b64Url(new TextEncoder().encode(JSON.stringify(payload)));
  return signToken(header, body, env);
}

async function enrollDevice(env, license, fingerprint, publicKey, email) {
  const accountCode = license.accountCode;
  const devices = devicesRef(env, accountCode);
  const target = devices.doc(fingerprint);
  const now = Date.now();
  let result;
  await db(env).runTransaction(async tx => {
    const licenseSnap = await tx.get(licenseRef(env, accountCode));
    if (!licenseSnap.exists) throw new Error("not_found");
    const current = licenseSnap.data();
    const existingSnap = await tx.get(target);
    const query = devices.where("revoked", "==", false);
    const activeSnap = await tx.get(query);
    const active = activeSnap.docs.filter(d => d.id !== fingerprint);
    const existing = existingSnap.exists ? existingSnap.data() : null;
    if (!(existing && existing.revoked !== true) && active.length >= Number(current.maxDevices || DEFAULT_MAX_DEVICES)) {
      active.sort((a, b) => Number(a.data().lastSeenAt || a.data().createdAt || 0) - Number(b.data().lastSeenAt || b.data().createdAt || 0));
      const oldest = active[0];
      if (oldest) {
        tx.update(oldest.ref, {
          revoked: true, revokedAt: now, revocationReason: "device_replaced",
          noticeMessage: "تم تفعيل حساب SmartLedger على جهاز آخر، وتم إلغاء تفعيل هذا الجهاز."
        });
      }
    }
    const deviceData = {
      fingerprint,
      publicKey: publicKey || existing?.publicKey || "",
      email: normalizeEmail(email || current.email),
      createdAt: existing?.createdAt || now,
      lastSeenAt: now,
      revoked: false, revokedAt: null, revocationReason: null, noticeMessage: null
    };
    tx.set(target, deviceData, { merge: true });
    result = deviceData;
  });
  return result;
}

async function activate(request, env) {
  const auth = await requireAuth(request);
  if (auth instanceof Response) return auth;
  const body = await readJson(request);
  let accountCode = normalizeAccountCode(body.accountCode);
  const emailInput = normalizeEmail(auth.email || body.email);
  const activationCode = String(body.activationCode || "").trim().toUpperCase();
  const publicKey = String(body.devicePublicKey || "").trim();
  if (!publicKey || publicKey.length > PUBLIC_KEY_MAX || activationCode.length < 16) return json(400, { error: "invalid_activation" });
  if (!(await rateLimit(env, auth.uid, clientIp(request), RATE_LIMIT_SENSITIVE))) return json(429, { error: "rate_limited" });
  if (!accountCode && emailInput) {
    const found = await findLicenseByEmail(env, emailInput);
    accountCode = found?.id || "";
  }
  if (!ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_activation" });
  const ref = licenseRef(env, accountCode);
  const snap = await ref.get();
  if (!snap.exists) return json(404, { error: "not_found" });
  const license = { ...snap.data(), accountCode };
  if (license.active !== true || license.revoked === true) return json(403, { error: "account_disabled", message: "تم تعطيل هذا الحساب." });
  if (emailInput && normalizeEmail(license.email) !== emailInput) return json(403, { error: "email_mismatch" });
  const fingerprint = await hexHash(fromB64(publicKey));
  if (!FINGERPRINT_PATTERN.test(fingerprint)) return json(400, { error: "invalid_device_key" });
  if (!license.activationCodeHash || await hexHash(activationCode) !== String(license.activationCodeHash).toUpperCase()) return json(403, { error: "activation_invalid" });
  const now = Date.now();
  const isFirst = !license.activatedAt && !license.activationUsedAt;
  if (isFirst) {
    license.activatedAt = now;
    license.activationUsedAt = now;
    if (license.licenseType === "TRIAL") license.trialEndsAt = now + positiveInt(license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS) * 86400000;
    license.updatedAt = now;
    await ref.set(license, { merge: true });
  }
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= Number(license.trialEndsAt)) return json(403, { error: "trial_expired", message: "انتهت الفترة التجريبية لهذا الترخيص." });
  await bindUser(env, auth.uid, emailInput || license.email, accountCode);
  await enrollDevice(env, license, fingerprint, publicKey, license.email);
  const token = await issueAccountToken(accountCode, license, fingerprint, env);
  return json(200, { licensed: true, token, accountCode, email: license.email, licenseType: license.licenseType, plan: license.licenseType, active: true, maxDevices: license.maxDevices, trialEndsAt: license.trialEndsAt || null });
}

async function verify(request, env) {
  const auth = requireAuth(request);
  if (auth instanceof Response) return auth;
  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  const fingerprint = String(body.deviceFingerprint || "").trim().toUpperCase();
  const challenge = String(body.challenge || "").trim();
  const signature = String(body.signature || "").trim();
  if (!ACCOUNT_PATTERN.test(accountCode) || !FINGERPRINT_PATTERN.test(fingerprint) || challenge.length < 32 || !signature) return json(400, { error: "invalid_verification" });
  if (!(await rateLimit(env, auth.uid, clientIp(request)))) return json(429, { error: "rate_limited" });
  const snap = await licenseRef(env, accountCode).get();
  if (!snap.exists) return json(404, { error: "not_found" });
  const license = { ...snap.data(), accountCode };
  if (license.active !== true || license.revoked === true) return json(200, { revoked: true, active: false, status: "account_disabled", message: "تم إيقاف هذا الحساب من قبل الإدارة." });
  const now = Date.now();
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= Number(license.trialEndsAt)) return json(200, { revoked: true, expired: true, status: "trial_expired", message: "انتهت الفترة التجريبية لهذا الترخيص." });
  const deviceRef = devicesRef(env, accountCode).doc(fingerprint);
  const deviceSnap = await deviceRef.get();
  const device = deviceSnap.exists ? deviceSnap.data() : null;
  if (!device || device.revoked === true) return json(200, { revoked: true, active: false, status: "device_revoked", reason: device?.revocationReason || "device_replaced", message: device?.noticeMessage || "تم إلغاء تفعيل هذا الجهاز." });
  const challengeTime = Number(challenge.split(":", 1)[0]);
  if (!Number.isFinite(challengeTime) || Math.abs(now - challengeTime) > CHALLENGE_TTL_MS) return json(403, { error: "challenge_expired" });
  if (!(await verifyDeviceSignature(String(device.publicKey || ""), challenge, signature))) return json(403, { error: "proof_invalid" });
  await bindUser(env, auth.uid, auth.email || license.email, accountCode);
  await deviceRef.set({ lastSeenAt: now }, { merge: true });
  const token = await issueAccountToken(accountCode, license, fingerprint, env);
  return json(200, { token, accountCode, email: license.email, licenseType: license.licenseType, plan: license.licenseType, active: true, maxDevices: license.maxDevices, trialEndsAt: license.trialEndsAt || null });
}

async function autoActivate(request, env) {
  const auth = requireAuth(request);
  if (auth instanceof Response) return auth;
  const body = await readJson(request);
  const publicKey = String(body.devicePublicKey || "").trim();
  if (!auth.email || !EMAIL_PATTERN.test(auth.email) || !publicKey || publicKey.length > PUBLIC_KEY_MAX) return json(400, { error: "invalid_request" });
  if (!(await rateLimit(env, auth.uid, clientIp(request), RATE_LIMIT_SENSITIVE))) return json(429, { error: "rate_limited" });
  const found = await findLicenseByEmail(env, auth.email);
  if (!found) return json(200, { licensed: false, registered: false, error: "no_license_for_email" });
  const license = { ...found.data(), accountCode: found.id };
  if (license.active !== true || license.revoked === true) return json(200, { licensed: false, registered: true, revoked: true, accountCode: found.id, message: "الحساب غير نشط." });
  const activated = Boolean(license.activatedAt || license.activationUsedAt);
  if (!activated) return json(200, { licensed: false, registered: true, activationRequired: true, accountCode: found.id, error: "initial_activation_required", message: "الحساب يحتاج إلى إدخال كود التفعيل لأول مرة." });
  const now = Date.now();
  if (license.licenseType === "TRIAL" && license.trialEndsAt && now >= Number(license.trialEndsAt)) return json(200, { licensed: false, registered: true, expired: true, accountCode: found.id, message: "انتهت الفترة التجريبية." });
  const fingerprint = await hexHash(fromB64(publicKey));
  if (!FINGERPRINT_PATTERN.test(fingerprint)) return json(400, { error: "invalid_device_key" });
  await bindUser(env, auth.uid, auth.email, found.id);
  await enrollDevice(env, license, fingerprint, publicKey, auth.email);
  const token = await issueAccountToken(found.id, license, fingerprint, env);
  return json(200, { licensed: true, registered: true, accountCode: found.id, email: license.email, licenseType: license.licenseType, plan: license.licenseType, token, maxDevices: license.maxDevices, trialEndsAt: license.trialEndsAt || null });
}

async function status(request, env) {
  const auth = requireAuth(request);
  if (auth instanceof Response) return auth;
  const license = await licenseForAuthenticatedUser(env, auth);
  if (!license) return json(200, { licensed: false, registered: false });
  const l = license.data;
  const now = Date.now();
  const isTrial = l.licenseType === "TRIAL";
  const expired = isTrial && Boolean(l.trialEndsAt && now >= Number(l.trialEndsAt));
  const remainingDays = isTrial && l.trialEndsAt && l.trialEndsAt > now ? Math.max(0, Math.ceil((Number(l.trialEndsAt) - now) / 86400000)) : 0;
  return json(200, { licensed: l.active === true && !expired && l.revoked !== true, registered: true, active: l.active === true, expired, accountCode: license.accountCode, email: l.email, licenseType: l.licenseType, plan: l.licenseType, maxDevices: l.maxDevices, trialEndsAt: l.trialEndsAt || null, activatedAt: l.activatedAt || null, remainingDays: isTrial ? remainingDays : null });
}

async function adminCreate(request, env) {
  const admin = await requireAdmin(request, env);
  if (admin instanceof Response) return admin;
  const body = await readJson(request);
  const email = normalizeEmail(body.email);
  if (!EMAIL_PATTERN.test(email)) return json(400, { error: "email_required" });
  const existing = await findLicenseByEmail(env, email);
  if (existing && !body.reissue) return json(409, { error: "email_already_registered", accountCode: existing.id, licenseId: existing.data().licenseId });
  let accountCode = normalizeAccountCode(body.accountCode);
  if (accountCode && !ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_account_code" });
  if (!accountCode) {
    for (let i = 0; i < 10; i++) {
      const candidate = randomAccountCode();
      if (!(await licenseRef(env, candidate).get()).exists) { accountCode = candidate; break; }
    }
  }
  if (!accountCode) return json(500, { error: "account_code_generation_failed" });
  const licenseType = normalizeLicenseType(body.licenseType || body.plan || "LIFETIME");
  const trialDays = positiveInt(body.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS);
  const maxDevices = positiveInt(body.maxDevices, DEFAULT_MAX_DEVICES, MAX_DEVICES);
  const active = body.active === undefined ? true : body.active === true || String(body.active).trim().toUpperCase() === "TRUE";
  const activationCode = String(body.activationCode || "").trim().toUpperCase() || randomActivationCode();
  if (activationCode.length < 16 || activationCode.length > 64) return json(400, { error: "invalid_activation_code" });
  const now = Date.now();
  const license = {
    product: "SMARTLEDGER", type: "ACCOUNT", licenseType, plan: licenseType,
    licenseId: `SL-${crypto.randomUUID().replace(/-/g, "").toUpperCase()}`,
    accountCode, email, active, revoked: !active, trialDays: licenseType === "TRIAL" ? trialDays : 0,
    trialEndsAt: null, activatedAt: null, activationUsedAt: null, maxDevices,
    activationCodeHash: await hexHash(activationCode), issuedAt: now, updatedAt: now,
    ownerUid: null, notes: String(body.notes || "").trim()
  };
  await licenseRef(env, accountCode).set(license);
  return json(200, { ok: true, created: true, accountCode, email, licenseId: license.licenseId, active, licenseType, plan: licenseType, trialDays: license.trialDays, maxDevices, activationCode, license: sanitizeLicense(license) });
}

async function adminUpdate(request, env) {
  const admin = requireAdmin(request, env);
  if (admin instanceof Response) return admin;
  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  if (!ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_account_code" });
  const ref = licenseRef(env, accountCode);
  const snap = await ref.get();
  if (!snap.exists) return json(404, { error: "not_found" });
  const license = { ...snap.data(), accountCode };
  let changed = false;
  let newActivationCode = null;
  if (body.replaceEmail !== undefined && body.replaceEmail !== "") {
    const target = normalizeEmail(body.replaceEmail);
    if (!EMAIL_PATTERN.test(target)) return json(400, { error: "invalid_new_email" });
    const collision = await findLicenseByEmail(env, target);
    if (collision && collision.id !== accountCode) return json(409, { error: "email_collision", message: "البريد الإلكتروني الجديد مرتبط بحساب آخر." });
    if (normalizeEmail(license.email) !== target) {
      license.email = target;
      license.activationCodeHash = await hexHash(newActivationCode = randomActivationCode());
      license.activatedAt = null; license.activationUsedAt = null;
      if (license.licenseType === "TRIAL") license.trialEndsAt = null;
      const devices = await devicesRef(env, accountCode).where("revoked", "==", false).get();
      const batch = db(env).batch();
      devices.docs.forEach(d => batch.update(d.ref, { revoked: true, revokedAt: Date.now(), revocationReason: "email_replaced", noticeMessage: "تم تغيير البريد الإلكتروني للحساب، يرجى إعادة التفعيل." }));
      await batch.commit();
      changed = true;
    }
  }
  if (body.regenerateActivationCode === true && !newActivationCode) {
    newActivationCode = randomActivationCode();
    license.activationCodeHash = await hexHash(newActivationCode);
    license.activationUsedAt = null; license.activatedAt = null; changed = true;
  }
  if (body.active !== undefined) {
    const active = body.active === true || String(body.active).trim().toUpperCase() === "TRUE";
    if (license.active !== active) { license.active = active; license.revoked = !active; changed = true; }
  }
  if (body.maxDevices !== undefined) {
    const max = positiveInt(body.maxDevices, DEFAULT_MAX_DEVICES, MAX_DEVICES);
    if (license.maxDevices !== max) { license.maxDevices = max; changed = true; }
  }
  if (body.notes !== undefined) {
    const notes = String(body.notes || "").trim();
    if (license.notes !== notes) { license.notes = notes; changed = true; }
  }
  if (body.licenseType !== undefined || body.plan !== undefined) {
    const type = normalizeLicenseType(body.licenseType || body.plan);
    if (license.licenseType !== type) {
      license.licenseType = type; license.plan = type;
      license.trialEndsAt = type === "LIFETIME" ? null : (license.activatedAt ? Number(license.activatedAt) + positiveInt(body.trialDays || license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS) * 86400000 : null);
      license.trialDays = type === "TRIAL" ? positiveInt(body.trialDays || license.trialDays, DEFAULT_TRIAL_DAYS, MAX_TRIAL_DAYS) : 0;
      changed = true;
    }
  }
  if (body.extendTrialDays !== undefined && license.licenseType === "TRIAL") {
    const extra = Math.min(Math.max(Number.parseInt(String(body.extendTrialDays), 10) || 0, 0), MAX_TRIAL_DAYS);
    if (extra > 0) { const base = license.trialEndsAt && license.trialEndsAt > Date.now() ? license.trialEndsAt : Date.now(); license.trialEndsAt = base + extra * 86400000; changed = true; }
  }
  if (changed) { license.updatedAt = Date.now(); await ref.set(license, { merge: true }); }
  return json(200, { ok: true, changed, accountCode, activationCode: newActivationCode, license: sanitizeLicense(license) });
}

async function adminGet(request, env) {
  const admin = requireAdmin(request, env);
  if (admin instanceof Response) return admin;
  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  if (!ACCOUNT_PATTERN.test(accountCode)) return json(400, { error: "invalid_account_code" });
  const snap = await licenseRef(env, accountCode).get();
  if (!snap.exists) return json(404, { error: "not_found" });
  const devices = await devicesRef(env, accountCode).get();
  return json(200, { ok: true, license: sanitizeLicense(snap.data()), devices: devices.docs.map(d => ({ fingerprint: d.id, ...d.data() })) });
}

async function adminRevokeDevice(request, env) {
  const admin = requireAdmin(request, env);
  if (admin instanceof Response) return admin;
  const body = await readJson(request);
  const accountCode = normalizeAccountCode(body.accountCode);
  const fingerprint = String(body.deviceFingerprint || "").trim().toUpperCase();
  if (!ACCOUNT_PATTERN.test(accountCode) || !FINGERPRINT_PATTERN.test(fingerprint)) return json(400, { error: "invalid_request" });
  const ref = devicesRef(env, accountCode).doc(fingerprint);
  const snap = await ref.get();
  if (!snap.exists) return json(404, { error: "device_not_found" });
  await ref.set({ revoked: true, revokedAt: Date.now(), revocationReason: "admin_revoked", noticeMessage: "تم إلغاء تفعيل هذا الجهاز من قبل الإدارة." }, { merge: true });
  return json(200, { ok: true, revoked: true, accountCode, deviceFingerprint: fingerprint });
}

export async function licenseFetch(request, env) {
  if (request.method === "OPTIONS") return new Response(null, { status: 204 });
  const url = new URL(request.url);
  const path = url.pathname.replace(/\/+$/, "") || "/";
  try {
    if (request.method === "GET" && path === "/") return json(200, { ok: true, service: "smartledger-firebase-license-api", version: 1 });
    if (request.method !== "POST") return json(405, { error: "method_not_allowed" });
    if (path === "/license/activate" || path === "/activate") return activate(request, env);
    if (path === "/license/verify" || path === "/verify") return verify(request, env);
    if (path === "/license/auto-activate" || path === "/auto-activate") return autoActivate(request, env);
    if (path === "/license/check-status" || path === "/license/status" || path === "/status") return status(request, env);
    if (path === "/admin/ping") {
      const a = await requireAdmin(request, env); if (a instanceof Response) return a;
      return json(200, { ok: true, status: "connected", service: "smartledger-firebase-license-api", version: 1 });
    }
    if (path === "/admin/licenses/issue") return adminCreate(request, env);
    if (path === "/admin/licenses/update") return adminUpdate(request, env);
    if (path === "/admin/licenses/get") return adminGet(request, env);
    if (path === "/admin/devices/revoke") return adminRevokeDevice(request, env);
    return json(404, { error: "not_found" });
  } catch (error) {
    const code = String(error?.message || "server_error");
    if (code === "invalid_json") return json(400, { error: code });
    if (code === "trial_expired") return json(403, { error: code });
    if (code === "license_signing_key_missing") return json(503, { error: code });  if (code === "account_owner_mismatch") return json(403, { error: "account_owner_mismatch" });
    if (code === "invalid_license_type") return json(400, { error: code });
    if (code === "firestore_license_db_missing") return json(500, { error: code });
    return json(500, { error: "server_error" });
  }
}
