import { createRemoteJWKSet, importX509, jwtVerify, type KeyLike } from "jose";

type SupportIdentityRow = {
  uid: string;
  support_id: string;
  created_at_ms: number;
};

type AuthContext = { uid: string; email: string };

type ApiErrorCode =
  | "unauthenticated"
  | "permission-denied"
  | "invalid-argument"
  | "not-found"
  | "internal";

class ApiError extends Error {
  constructor(
    public readonly code: ApiErrorCode,
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const FIREBASE_ID_TOKEN_CERTS =
  "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
const FIREBASE_APPCHECK_JWKS = "https://firebaseappcheck.googleapis.com/v1/jwks";
const SUPPORT_ID_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

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
  if (error instanceof ApiError) {
    return json({ error: { code: error.code, message: error.message } }, error.status);
  }
  console.error("support-worker error", error);
  return json({ error: { code: "internal", message: "Internal service error." } }, 500);
}

function normalizeEmail(value: unknown): string {
  return typeof value === "string" ? value.trim().toLowerCase() : "";
}

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

function generateRandomSupportId(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(10));
  const chars: string[] = [];
  for (let i = 0; i < 10; i++) {
    chars.push(SUPPORT_ID_ALPHABET[bytes[i] % SUPPORT_ID_ALPHABET.length]);
  }
  return `SD-${chars.slice(0, 4).join("")}-${chars.slice(4, 8).join("")}-${chars.slice(8, 10).join("")}`;
}

async function getFirebaseCerts(): Promise<Map<string, KeyLike>> {
  const now = Date.now();
  if (firebaseCertCache && firebaseCertCache.expiresAt > now) return firebaseCertCache.keys;

  const response = await fetch(FIREBASE_ID_TOKEN_CERTS);
  if (!response.ok) throw new ApiError("internal", "Unable to load Firebase signing keys.", 500);

  const certificates = await response.json() as Record<string, string>;
  const keys = new Map<string, KeyLike>();
  for (const [kid, certificate] of Object.entries(certificates)) {
    keys.set(kid, await importX509(certificate, "RS256"));
  }

  const cacheControl = response.headers.get("cache-control") ?? "";
  const maxAge = Number(cacheControl.match(/max-age=(\d+)/i)?.[1] ?? 3600);
  firebaseCertCache = {
    expiresAt: now + Math.max(60, Math.min(maxAge, 21600)) * 1000,
    keys,
  };
  return keys;
}

async function authenticate(request: Request, env: Env): Promise<AuthContext> {
  const authorization = request.headers.get("authorization") ?? "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) throw new ApiError("unauthenticated", "Authentication is required.", 401);

  const appCheckToken = request.headers.get("x-firebase-appcheck") ?? "";
  if (!appCheckToken) throw new ApiError("unauthenticated", "App Check is required.", 401);

  try {
    const token = match[1];
    const parts = token.split(".");
    if (parts.length !== 3) throw new Error("invalid JWT");
    const header = JSON.parse(
      new TextDecoder().decode(
        Uint8Array.from(
          atob(parts[0].replace(/-/g, "+").replace(/_/g, "/") + "==="),
          (char) => char.charCodeAt(0),
        ),
      ),
    ) as Record<string, unknown>;

    if (header.alg !== "RS256" || typeof header.kid !== "string") {
      throw new Error("invalid token header");
    }

    const key = (await getFirebaseCerts()).get(header.kid);
    if (!key) throw new Error("unknown Firebase signing key");

    const verified = await jwtVerify(token, key, {
      algorithms: ["RS256"],
      issuer: `https://securetoken.google.com/${env.FIREBASE_PROJECT_ID}`,
      audience: env.FIREBASE_PROJECT_ID,
      clockTolerance: 30,
    });

    const uid = typeof verified.payload.sub === "string" ? verified.payload.sub : "";
    const email = normalizeEmail(verified.payload.email);
    if (!uid || !email || verified.payload.email_verified !== true) {
      throw new Error("verified account email is required");
    }

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
    throw new ApiError("unauthenticated", "Authentication or App Check validation failed.", 401);
  }
}

async function getOrCreateSupportIdentity(env: Env, auth: AuthContext): Promise<Response> {
  const existing = await env.DB
    .prepare("SELECT * FROM support_identities WHERE uid = ?1")
    .bind(auth.uid)
    .first<SupportIdentityRow>();

  if (existing) return ok({ supportId: existing.support_id });

  const nowMs = Date.now();
  for (let attempt = 0; attempt < 5; attempt++) {
    const candidateId = generateRandomSupportId();
    try {
      await env.DB
        .prepare(
          `INSERT INTO support_identities (uid, support_id, created_at_ms)
           VALUES (?1, ?2, ?3)
           ON CONFLICT(uid) DO NOTHING`,
        )
        .bind(auth.uid, candidateId, nowMs)
        .run();

      const current = await env.DB
        .prepare("SELECT * FROM support_identities WHERE uid = ?1")
        .bind(auth.uid)
        .first<SupportIdentityRow>();

      if (current) return ok({ supportId: current.support_id });
    } catch {
      // Retry after a rare unique-ID collision.
    }
  }

  throw new ApiError("internal", "Unable to generate a unique support identity.", 500);
}

async function adminAuthorize(request: Request, env: Env): Promise<void> {
  const authorization = request.headers.get("authorization") ?? "";
  const token = authorization.match(/^Bearer\s+(.+)$/i)?.[1] ?? "";
  if (!token || !env.ADMIN_TOKEN || !timingSafeEqual(token, env.ADMIN_TOKEN)) {
    throw new ApiError("permission-denied", "Administrator authorization is required.", 403);
  }
}

async function adminLookupSupportIdentity(
  env: Env,
  request: Request,
  supportId: string,
): Promise<Response> {
  await adminAuthorize(request, env);

  const cleanSupportId = supportId.trim().toUpperCase();
  if (!cleanSupportId) {
    throw new ApiError("invalid-argument", "Missing support ID parameter.", 400);
  }

  const identity = await env.DB
    .prepare("SELECT * FROM support_identities WHERE support_id = ?1")
    .bind(cleanSupportId)
    .first<SupportIdentityRow>();

  if (!identity) {
    throw new ApiError("not-found", "Support identity not found.", 404);
  }

  return ok({
    ok: true,
    supportId: identity.support_id,
    uid: identity.uid,
    createdAtMs: identity.created_at_ms,
  });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "access-control-allow-origin": "*",
          "access-control-allow-headers": "authorization,content-type,x-firebase-appcheck",
          "access-control-allow-methods": "GET,OPTIONS",
        },
      });
    }

    try {
      if (url.pathname === "/health") {
        return ok({ ok: true, service: "al-daftar-support-api" });
      }

      if (url.pathname.startsWith("/v1/admin/support-identities/") && request.method === "GET") {
        const supportId = decodeURIComponent(
          url.pathname.slice("/v1/admin/support-identities/".length),
        ).trim();
        return await adminLookupSupportIdentity(env, request, supportId);
      }

      const auth = await authenticate(request, env);

      if (url.pathname === "/v1/support/identity" && request.method === "GET") {
        return await getOrCreateSupportIdentity(env, auth);
      }

      throw new ApiError("not-found", "Endpoint not found.", 404);
    } catch (error) {
      return fail(error);
    }
  },
};
