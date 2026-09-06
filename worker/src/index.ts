import { createRemoteJWKSet, importPKCS8, importX509, jwtVerify, type KeyLike } from "jose";

type LicenseRow = {
  uid: string;
  email: string;
  is_activated: number;
  devices_max: number;
  device_order_json: string;
  license_version: number;
  updated_at_ms: number;
};

type DeviceRow = {
  uid: string;
  device_id: string;
  session_id: string;
  status: string;
  registered_at_ms: number;
  last_seen_at_ms: number;
  app_version: string;
  revoked_at_ms: number | null;
  revoked_reason: string | null;
};

type SessionRow = {
  session_id: string;
  uid: string;
  device_id: string;
  status: string;
  issued_at_ms: number;
  updated_at_ms: number;
  offline_valid_until_ms: number;
  revoked_reason: string | null;
};

type AuthContext = { uid: string; email: string };

type ApiErrorCode =
  | "unauthenticated"
  | "permission-denied"
  | "invalid-argument"
  | "not-found"
  | "failed-precondition"
  | "internal";

class LicenseError extends Error {
  constructor(
    public readonly code: ApiErrorCode,
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "LicenseError";
  }
}

const OFFLINE_DAYS = 30;
const MAX_DEVICE_ID_LENGTH = 128;
const MAX_APP_VERSION_LENGTH = 32;
const FIREBASE_ID_TOKEN_CERTS = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
const FIREBASE_APPCHECK_JWKS = "https://firebaseappcheck.googleapis.com/v1/jwks";

let privateKeyPromise: Promise<KeyLike> | undefined;
let firebaseCertCache: { expiresAt: number; keys: Map<string, KeyLike> } | undefined;
const appCheckJWKS = createRemoteJWKSet(new URL(FIREBASE_APPCHECK_JWKS));

function json(data: unknown, status = 200, headers: HeadersInit = {}): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      ...headers,
    },
  });
}

function ok(data: unknown): Response {
  return json(data);
}

function fail(error: unknown): Response {
  if (error instanceof LicenseError) {
    return json({ error: { code: error.code, message: error.message } }, error.status);
  }
  console.error("license-worker error", error);
  return json({ error: { code: "internal", message: "Internal licensing error." } }, 500);
}

async function readJson(request: Request): Promise<Record<string, unknown>> {
  try {
    const value = await request.json();
    if (!value || typeof value !== "object" || Array.isArray(value)) {
      throw new Error("not object");
    }
    return value as Record<string, unknown>;
  } catch {
    throw new LicenseError("invalid-argument", "Invalid JSON request body.", 400);
  }
}

function normalizeEmail(value: unknown): string {
  return typeof value === "string" ? value.trim().toLowerCase() : "";
}

function normalizeDeviceId(value: unknown): string {
  if (typeof value !== "string") {
    throw new LicenseError("invalid-argument", "Invalid device identifier.", 400);
  }
  const deviceId = value.trim();
  if (!deviceId || deviceId.length > MAX_DEVICE_ID_LENGTH || !/^[A-Za-z0-9._:-]+$/.test(deviceId)) {
    throw new LicenseError("invalid-argument", "Invalid device identifier.", 400);
  }
  return deviceId;
}

function normalizeAppVersion(value: unknown): string {
  if (typeof value !== "string") return "unknown";
  return value.trim().slice(0, MAX_APP_VERSION_LENGTH) || "unknown";
}

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

function parseDeviceOrder(value: string): string[] {
  try {
    const parsed = JSON.parse(value);
    if (!Array.isArray(parsed)) return [];
    return [...new Set(parsed.filter((id): id is string => typeof id === "string"))];
  } catch {
    return [];
  }
}

function hashSha256(value: string): Promise<string> {
  return crypto.subtle.digest("SHA-256", new TextEncoder().encode(value)).then((buffer) =>
    [...new Uint8Array(buffer)].map((b) => b.toString(16).padStart(2, "0")).join(""),
  );
}

function canonicalize(payload: Record<string, unknown>): string {
  return Object.keys(payload).sort().map((key) => `${key}=${String(payload[key])}`).join("&");
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = "";
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, Math.min(i + chunk, bytes.length)));
  }
  return btoa(binary);
}

function bytesToBase64Url(bytes: Uint8Array): string {
  return bytesToBase64(bytes).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function createSessionId(): string {
  return bytesToBase64Url(crypto.getRandomValues(new Uint8Array(24)));
}

async function signLease(env: Env, payload: Record<string, unknown>): Promise<string> {
  privateKeyPromise ??= importPKCS8(env.LICENSE_PRIVATE_KEY, "RS256");
  const key = await privateKeyPromise;
  const data = new TextEncoder().encode(canonicalize(payload));
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key as CryptoKey, data);
  return bytesToBase64(new Uint8Array(signature));
}

function leasePayload(input: {
  uid: string;
  sessionId: string;
  deviceId: string;
  email: string;
  issuedAtMs: number;
  offlineValidUntilMs: number;
  licenseVersion: number;
}, deviceHash: string): Record<string, unknown> {
  return {
    v: 1,
    uid: input.uid,
    sid: input.sessionId,
    did: deviceHash,
    email: input.email,
    iat: input.issuedAtMs,
    exp: input.offlineValidUntilMs,
    lv: input.licenseVersion,
  };
}

async function getFirebaseCerts(): Promise<Map<string, KeyLike>> {
  const now = Date.now();
  if (firebaseCertCache && firebaseCertCache.expiresAt > now) return firebaseCertCache.keys;

  const response = await fetch(FIREBASE_ID_TOKEN_CERTS);
  if (!response.ok) throw new LicenseError("internal", "Unable to load Firebase signing keys.", 500);
  const certificates = await response.json() as Record<string, string>;
  const keys = new Map<string, KeyLike>();
  for (const [kid, certificate] of Object.entries(certificates)) {
    keys.set(kid, await importX509(certificate, "RS256"));
  }
  const cacheControl = response.headers.get("cache-control") ?? "";
  const maxAge = Number(cacheControl.match(/max-age=(\d+)/i)?.[1] ?? 3600);
  firebaseCertCache = { expiresAt: now + Math.max(60, Math.min(maxAge, 21600)) * 1000, keys };
  return keys;
}

async function authenticate(request: Request, env: Env): Promise<AuthContext> {
  const authorization = request.headers.get("authorization") ?? "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) throw new LicenseError("unauthenticated", "Authentication is required.", 401);

  const appCheckToken = request.headers.get("x-firebase-appcheck") ?? "";
  if (!appCheckToken) throw new LicenseError("unauthenticated", "App Check is required.", 401);

  try {
    const { protectedHeader } = decodeJwtHeaderPayload(match[1]);
    if (protectedHeader.alg !== "RS256" || typeof protectedHeader.kid !== "string") throw new Error("invalid Firebase token header");
    const key = (await getFirebaseCerts()).get(protectedHeader.kid);
    if (!key) throw new Error("unknown Firebase signing key");

    const verified = await jwtVerify(match[1], key, {
      algorithms: ["RS256"],
      issuer: `https://securetoken.google.com/${env.FIREBASE_PROJECT_ID}`,
      audience: env.FIREBASE_PROJECT_ID,
      clockTolerance: 30,
    });
    const uid = typeof verified.payload.sub === "string" ? verified.payload.sub : "";
    const email = normalizeEmail(verified.payload.email);
    if (!uid || !email) throw new Error("Firebase account email missing");
    if (verified.payload.email_verified !== true) throw new Error("Firebase account is not verified");

    const appCheck = await jwtVerify(appCheckToken, appCheckJWKS, {
      algorithms: ["RS256"],
      issuer: `https://firebaseappcheck.googleapis.com/${env.FIREBASE_PROJECT_NUMBER}`,
      audience: `projects/${env.FIREBASE_PROJECT_NUMBER}`,
      clockTolerance: 30,
    });
    if (appCheck.protectedHeader.alg !== "RS256" || appCheck.protectedHeader.typ !== "JWT") {
      throw new Error("invalid App Check header");
    }
    if (env.FIREBASE_APP_ID && appCheck.payload.sub !== env.FIREBASE_APP_ID) {
      throw new Error("App Check app mismatch");
    }

    return { uid, email };
  } catch {
    throw new LicenseError("unauthenticated", "Authentication or App Check validation failed.", 401);
  }
}

function decodeJwtHeaderPayload(token: string): { protectedHeader: Record<string, unknown> } {
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("invalid JWT");
  const decode = (part: string): Record<string, unknown> => {
    const normalized = part.replace(/-/g, "+").replace(/_/g, "/") + "===";
    const binary = atob(normalized.slice(0, normalized.length - (normalized.length % 4)));
    return JSON.parse(new TextDecoder().decode(Uint8Array.from(binary, (char) => char.charCodeAt(0))));
  };
  decode(parts[1]);
  return { protectedHeader: decode(parts[0]) };
}

function assertEnabledLicense(license: LicenseRow | null): asserts license is LicenseRow {
  if (!license) throw new LicenseError("permission-denied", "This Google account is not licensed.", 403);
  if (license.is_activated !== 1) throw new LicenseError("permission-denied", "This license is disabled.", 403);
}

async function getLicense(env: Env, uid: string, email: string): Promise<LicenseRow | null> {
  const byUid = await env.DB.prepare("SELECT * FROM licenses WHERE uid = ?1").bind(uid).first<LicenseRow>();
  if (byUid) return byUid;

  const legacy = await env.DB.prepare("SELECT * FROM licenses WHERE email = ?1 LIMIT 1").bind(email).first<LicenseRow>();
  if (!legacy) return null;

  await env.DB.prepare(
    `INSERT INTO licenses (uid, email, is_activated, devices_max, device_order_json, license_version, updated_at_ms)
     VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)
     ON CONFLICT(uid) DO UPDATE SET email=excluded.email, is_activated=excluded.is_activated,
       devices_max=excluded.devices_max, device_order_json=excluded.device_order_json,
       license_version=excluded.license_version, updated_at_ms=excluded.updated_at_ms`,
  ).bind(uid, legacy.email, legacy.is_activated, legacy.devices_max, legacy.device_order_json, legacy.license_version, Date.now()).run();

  return env.DB.prepare("SELECT * FROM licenses WHERE uid = ?1").bind(uid).first<LicenseRow>();
}

async function activateLicense(env: Env, auth: AuthContext, body: Record<string, unknown>, retry = true): Promise<Response> {
  const deviceId = normalizeDeviceId(body.deviceId);
  const appVersion = normalizeAppVersion(body.appVersion);
  const license = await getLicense(env, auth.uid, auth.email);
  assertEnabledLicense(license);

  const licenseEmail = normalizeEmail(license.email || auth.email);
  if (licenseEmail !== auth.email) {
    throw new LicenseError("permission-denied", "The signed-in Google account does not match the license.", 403);
  }

  const maxDevices = Math.max(1, Math.min(20, Number(license.devices_max || 1)));
  const order = parseDeviceOrder(license.device_order_json);
  const existingIndex = order.indexOf(deviceId);
  if (existingIndex >= 0) order.splice(existingIndex, 1);

  let evictedDeviceId: string | null = null;
  let evictedSessionId: string | null = null;
  if (order.length >= maxDevices) {
    evictedDeviceId = order.shift() ?? null;
    if (evictedDeviceId) {
      const evicted = await env.DB.prepare("SELECT session_id FROM devices WHERE uid = ?1 AND device_id = ?2")
        .bind(auth.uid, evictedDeviceId).first<{ session_id: string }>();
      evictedSessionId = evicted?.session_id ?? null;
    }
  }
  order.push(deviceId);

  const nowMs = Date.now();
  const offlineValidUntilMs = nowMs + OFFLINE_DAYS * 24 * 60 * 60 * 1000;
  const sessionId = createSessionId();
  const deviceHash = await hashSha256(deviceId);
  const evictedHash = evictedDeviceId ? await hashSha256(evictedDeviceId) : null;
  const payload = leasePayload({
    uid: auth.uid,
    sessionId,
    deviceId,
    email: licenseEmail,
    issuedAtMs: nowMs,
    offlineValidUntilMs,
    licenseVersion: Number(license.license_version || 1),
  }, deviceHash);
  const signature = await signLease(env, payload);

  const statements: D1PreparedStatement[] = [];
  if (evictedDeviceId) {
    statements.push(env.DB.prepare(
      `UPDATE devices SET status='revoked', revoked_at_ms=?3, revoked_reason='replaced_by_new_device'
       WHERE uid=?1 AND device_id=?2`,
    ).bind(auth.uid, evictedDeviceId, nowMs));
    if (evictedSessionId) {
      statements.push(env.DB.prepare(
        `UPDATE license_sessions SET status='revoked', updated_at_ms=?2, revoked_reason='replaced_by_new_device'
         WHERE session_id=?1`,
      ).bind(evictedSessionId, nowMs));
    }
  }

  statements.push(env.DB.prepare(
    `INSERT INTO devices (uid, device_id, session_id, status, registered_at_ms, last_seen_at_ms, app_version, revoked_at_ms, revoked_reason)
     VALUES (?1, ?2, ?3, 'active', ?4, ?4, ?5, NULL, NULL)
     ON CONFLICT(uid, device_id) DO UPDATE SET session_id=excluded.session_id, status='active',
       registered_at_ms=excluded.registered_at_ms, last_seen_at_ms=excluded.last_seen_at_ms,
       app_version=excluded.app_version, revoked_at_ms=NULL, revoked_reason=NULL`,
  ).bind(auth.uid, deviceId, sessionId, nowMs, appVersion));
  statements.push(env.DB.prepare(
    `INSERT INTO license_sessions (session_id, uid, device_id, status, issued_at_ms, updated_at_ms, offline_valid_until_ms, revoked_reason)
     VALUES (?1, ?2, ?3, 'active', ?4, ?4, ?5, NULL)`,
  ).bind(sessionId, auth.uid, deviceId, nowMs, offlineValidUntilMs));
  statements.push(env.DB.prepare(
    `UPDATE licenses SET email=?2, is_activated=1, devices_max=?3, device_order_json=?4, license_version=?5, updated_at_ms=?6 WHERE uid=?1`,
  ).bind(auth.uid, licenseEmail, maxDevices, JSON.stringify(order), Number(license.license_version || 1), nowMs));
  statements.push(env.DB.prepare(
    `INSERT INTO license_audit (uid, email, device_id_hash, session_id, action, evicted_device_id_hash, created_at_ms, app_version)
     VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)`,
  ).bind(auth.uid, licenseEmail, deviceHash, sessionId, evictedDeviceId ? "activate_and_replace" : "activate", evictedHash, nowMs, appVersion));

  try {
    await env.DB.batch(statements);
  } catch (error) {
    // D1 batches are atomic. The trigger prevents concurrent activations from
    // exceeding devices_max; retry once against the now-current license order.
    if (retry && error instanceof Error && error.message.includes("DEVICE_LIMIT_REACHED")) {
      return activateLicense(env, auth, body, false);
    }
    throw error;
  }

  return ok({
    ok: true,
    sessionId,
    lease: payload,
    signature,
    replaced: Boolean(evictedDeviceId),
    offlineValidUntilMs,
  });
}

async function refreshLicense(env: Env, auth: AuthContext, body: Record<string, unknown>): Promise<Response> {
  const deviceId = normalizeDeviceId(body.deviceId);
  const sessionId = typeof body.sessionId === "string" ? body.sessionId.trim() : "";
  if (!sessionId) throw new LicenseError("invalid-argument", "Missing license session.", 400);

  const license = await getLicense(env, auth.uid, auth.email);
  assertEnabledLicense(license);
  const device = await env.DB.prepare("SELECT * FROM devices WHERE uid=?1 AND device_id=?2").bind(auth.uid, deviceId).first<DeviceRow>();
  const session = await env.DB.prepare("SELECT * FROM license_sessions WHERE session_id=?1").bind(sessionId).first<SessionRow>();
  if (!device || device.status !== "active" || device.session_id !== sessionId) {
    throw new LicenseError("permission-denied", "This device is no longer licensed.", 403);
  }
  if (!session || session.uid !== auth.uid || session.status !== "active") {
    throw new LicenseError("permission-denied", "This license session is no longer active.", 403);
  }

  const nowMs = Date.now();
  const offlineValidUntilMs = nowMs + OFFLINE_DAYS * 24 * 60 * 60 * 1000;
  const deviceHash = await hashSha256(deviceId);
  const payload = leasePayload({
    uid: auth.uid,
    sessionId,
    deviceId,
    email: normalizeEmail(license.email || auth.email),
    issuedAtMs: nowMs,
    offlineValidUntilMs,
    licenseVersion: Number(license.license_version || 1),
  }, deviceHash);
  const signature = await signLease(env, payload);

  await env.DB.batch([
    env.DB.prepare("UPDATE devices SET last_seen_at_ms=?3 WHERE uid=?1 AND device_id=?2").bind(auth.uid, deviceId, nowMs),
    env.DB.prepare("UPDATE license_sessions SET updated_at_ms=?2, offline_valid_until_ms=?3 WHERE session_id=?1").bind(sessionId, nowMs, offlineValidUntilMs),
  ]);

  return ok({ ok: true, sessionId, lease: payload, signature, offlineValidUntilMs });
}

async function unlinkCurrentDevice(env: Env, auth: AuthContext, body: Record<string, unknown>): Promise<Response> {
  const deviceId = normalizeDeviceId(body.deviceId);
  const device = await env.DB.prepare("SELECT * FROM devices WHERE uid=?1 AND device_id=?2").bind(auth.uid, deviceId).first<DeviceRow>();
  if (!device) return ok({ ok: true });

  const license = await env.DB.prepare("SELECT * FROM licenses WHERE uid=?1").bind(auth.uid).first<LicenseRow>();
  const order = license ? parseDeviceOrder(license.device_order_json).filter((id) => id !== deviceId) : [];
  const nowMs = Date.now();
  const deviceHash = await hashSha256(deviceId);

  await env.DB.batch([
    env.DB.prepare("UPDATE licenses SET device_order_json=?2, updated_at_ms=?3 WHERE uid=?1").bind(auth.uid, JSON.stringify(order), nowMs),
    env.DB.prepare("UPDATE devices SET status='revoked', revoked_at_ms=?3, revoked_reason='manual_logout' WHERE uid=?1 AND device_id=?2").bind(auth.uid, deviceId, nowMs),
    env.DB.prepare("UPDATE license_sessions SET status='revoked', updated_at_ms=?2, revoked_reason='manual_logout' WHERE session_id=?1").bind(device.session_id, nowMs),
    env.DB.prepare("INSERT INTO license_audit (uid, email, device_id_hash, session_id, action, created_at_ms, app_version) VALUES (?1, ?2, ?3, ?4, 'manual_logout', ?5, NULL)")
      .bind(auth.uid, auth.email, deviceHash, device.session_id, nowMs),
  ]);

  return ok({ ok: true });
}

async function getSessionStatus(env: Env, auth: AuthContext, sessionId: string): Promise<Response> {
  const session = await env.DB.prepare("SELECT * FROM license_sessions WHERE session_id=?1 AND uid=?2")
    .bind(sessionId, auth.uid).first<SessionRow>();
  if (!session) throw new LicenseError("not-found", "License session not found.", 404);
  return ok({
    ok: true,
    sessionId: session.session_id,
    status: session.status,
    offlineValidUntilMs: session.offline_valid_until_ms,
  });
}

async function adminAuthorize(request: Request, env: Env): Promise<void> {
  const authorization = request.headers.get("authorization") ?? "";
  const token = authorization.match(/^Bearer\s+(.+)$/i)?.[1] ?? "";
  if (!token || !env.ADMIN_TOKEN || !timingSafeEqual(token, env.ADMIN_TOKEN)) {
    throw new LicenseError("permission-denied", "Administrator authorization is required.", 403);
  }
}

async function adminUpsertLicense(env: Env, request: Request): Promise<Response> {
  await adminAuthorize(request, env);
  const body = await readJson(request);
  const uid = typeof body.uid === "string" ? body.uid.trim() : "";
  const email = normalizeEmail(body.email);
  if (!uid || !email) throw new LicenseError("invalid-argument", "uid and email are required.", 400);

  const isActivated = body.is_activated === false ? 0 : 1;
  const devicesMax = Math.max(1, Math.min(20, Number(body.devices_max ?? 1)));
  const licenseVersion = Math.max(1, Number(body.license_version ?? 1));
  const existing = await env.DB.prepare("SELECT * FROM licenses WHERE uid=?1").bind(uid).first<LicenseRow>();
  const currentOrder = parseDeviceOrder(existing?.device_order_json ?? "[]");
  const nowMs = Date.now();

  const statements: D1PreparedStatement[] = [
    env.DB.prepare(
      `INSERT INTO licenses (uid,email,is_activated,devices_max,device_order_json,license_version,updated_at_ms)
       VALUES (?1,?2,?3,?4,?5,?6,?7)
       ON CONFLICT(uid) DO UPDATE SET email=excluded.email,is_activated=excluded.is_activated,
         devices_max=excluded.devices_max,license_version=excluded.license_version,updated_at_ms=excluded.updated_at_ms`,
    ).bind(uid, email, isActivated, devicesMax, existing?.device_order_json ?? "[]", licenseVersion, nowMs),
  ];

  if (isActivated === 0) {
    const activeDevices = await env.DB.prepare("SELECT * FROM devices WHERE uid=?1 AND status='active'").bind(uid).all<DeviceRow>();
    statements[0] = env.DB.prepare(
      `INSERT INTO licenses (uid,email,is_activated,devices_max,device_order_json,license_version,updated_at_ms)
       VALUES (?1,?2,0,?3,'[]',?4,?5)
       ON CONFLICT(uid) DO UPDATE SET email=excluded.email,is_activated=0,
         devices_max=excluded.devices_max,device_order_json='[]',license_version=excluded.license_version,updated_at_ms=excluded.updated_at_ms`,
    ).bind(uid, email, devicesMax, licenseVersion, nowMs);
    for (const device of activeDevices.results) {
      statements.push(env.DB.prepare("UPDATE devices SET status='revoked', revoked_at_ms=?3, revoked_reason='license_disabled' WHERE uid=?1 AND device_id=?2").bind(uid, device.device_id, nowMs));
      statements.push(env.DB.prepare("UPDATE license_sessions SET status='revoked', updated_at_ms=?2, revoked_reason='license_disabled' WHERE session_id=?1").bind(device.session_id, nowMs));
    }
  } else if (currentOrder.length > devicesMax) {
    const revokeIds = currentOrder.slice(0, currentOrder.length - devicesMax);
    const keepIds = currentOrder.slice(-devicesMax);
    statements[0] = env.DB.prepare(
      `INSERT INTO licenses (uid,email,is_activated,devices_max,device_order_json,license_version,updated_at_ms)
       VALUES (?1,?2,1,?3,?4,?5,?6)
       ON CONFLICT(uid) DO UPDATE SET email=excluded.email,is_activated=1,
         devices_max=excluded.devices_max,device_order_json=excluded.device_order_json,license_version=excluded.license_version,updated_at_ms=excluded.updated_at_ms`,
    ).bind(uid, email, devicesMax, JSON.stringify(keepIds), licenseVersion, nowMs);
    for (const deviceId of revokeIds) {
      const device = await env.DB.prepare("SELECT * FROM devices WHERE uid=?1 AND device_id=?2").bind(uid, deviceId).first<DeviceRow>();
      if (!device) continue;
      statements.push(env.DB.prepare("UPDATE devices SET status='revoked', revoked_at_ms=?3, revoked_reason='device_limit_reduced' WHERE uid=?1 AND device_id=?2").bind(uid, deviceId, nowMs));
      statements.push(env.DB.prepare("UPDATE license_sessions SET status='revoked', updated_at_ms=?2, revoked_reason='device_limit_reduced' WHERE session_id=?1").bind(device.session_id, nowMs));
    }
  }

  await env.DB.batch(statements);
  return ok({ ok: true, uid, email, is_activated: Boolean(isActivated), devices_max: devicesMax, license_version: licenseVersion });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: { "access-control-allow-origin": "*", "access-control-allow-headers": "authorization,content-type,x-firebase-appcheck", "access-control-allow-methods": "GET,POST,OPTIONS" } });

    try {
      if (url.pathname === "/health") return ok({ ok: true, service: "al-daftar-license-api" });

      if (url.pathname === "/v1/admin/licenses" && request.method === "POST") {
        return await adminUpsertLicense(env, request);
      }

      const auth = await authenticate(request, env);

      if (url.pathname === "/v1/license/activate" && request.method === "POST") {
        return await activateLicense(env, auth, await readJson(request));
      }
      if (url.pathname === "/v1/license/refresh" && request.method === "POST") {
        return await refreshLicense(env, auth, await readJson(request));
      }
      if (url.pathname === "/v1/license/unlink" && request.method === "POST") {
        return await unlinkCurrentDevice(env, auth, await readJson(request));
      }
      if (url.pathname.startsWith("/v1/license/session/") && request.method === "GET") {
        const sessionId = decodeURIComponent(url.pathname.slice("/v1/license/session/".length)).trim();
        if (!sessionId) throw new LicenseError("invalid-argument", "Missing license session.", 400);
        return await getSessionStatus(env, auth, sessionId);
      }

      throw new LicenseError("not-found", "Endpoint not found.", 404);
    } catch (error) {
      return fail(error);
    }
  },
};
