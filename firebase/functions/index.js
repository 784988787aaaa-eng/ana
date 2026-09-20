import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getAppCheck } from "firebase-admin/app-check";
import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import worker from "./worker.js";
import { FirestoreKV } from "./kv-compat.js";

initializeApp();
const db = getFirestore();

const LICENSE_PRIVATE_KEY = defineSecret("SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY");
const RATE_LIMIT_SALT = defineSecret("SMARTLEDGER_RATE_LIMIT_SALT");
const ADMIN_SECRET = defineSecret("SMARTLEDGER_ADMIN_SECRET");
const GOOGLE_CLIENT_ID = defineSecret("GOOGLE_CLIENT_ID");
const GOOGLE_CLIENT_SECRET = defineSecret("GOOGLE_CLIENT_SECRET");
const secrets = [LICENSE_PRIVATE_KEY, RATE_LIMIT_SALT, ADMIN_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET];

function buildEnv() {
  return {
    SMARTLEDGER_KV: new FirestoreKV(db),
    SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY: LICENSE_PRIVATE_KEY.value(),
    SMARTLEDGER_RATE_LIMIT_SALT: RATE_LIMIT_SALT.value(),
    SMARTLEDGER_ADMIN_SECRET: ADMIN_SECRET.value(),
    GOOGLE_CLIENT_ID: GOOGLE_CLIENT_ID.value(),
    GOOGLE_CLIENT_SECRET: GOOGLE_CLIENT_SECRET.value()
  };
}

function toWebRequest(req) {
  const protocol = req.get("x-forwarded-proto") || "https";
  const host = req.get("x-forwarded-host") || req.get("host");
  const url = new URL(req.originalUrl || req.url || "/", protocol + "://" + host);
  const headers = new Headers();
  for (const [name, value] of Object.entries(req.headers)) {
    if (Array.isArray(value)) headers.set(name, value.join(", "));
    else if (value != null) headers.set(name, String(value));
  }
  const init = { method: req.method, headers };
  if (req.method !== "GET" && req.method !== "HEAD") {
    init.body = req.rawBody ?? Buffer.from("");
    init.duplex = "half";
  }
  return new Request(url, init);
}

async function sendWebResponse(webResponse, res) {
  res.status(webResponse.status);
  webResponse.headers.forEach((value, key) => res.setHeader(key, value));
  if (!webResponse.body) { res.end(); return; }
  const reader = webResponse.body.getReader();
  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      res.write(Buffer.from(value));
    }
  } finally { reader.releaseLock(); }
  res.end();
}

async function verifyOptionalAppCheck(req) {
  const token = String(req.get("X-Firebase-AppCheck") || "").trim();
  if (!token) return;
  await getAppCheck().verifyToken(token);
}

export const smartledgerApi = onRequest({
  region: "us-central1",
  timeoutSeconds: 120,
  memory: "512MiB",
  invoker: "public",
  secrets
}, async (req, res) => {
  // Compatibility phase: validate App Check when present; enforcement will be
  // enabled only after the Android client starts sending the token.
  try {
    await verifyOptionalAppCheck(req);
  } catch (_) {
    res.status(401).set("Content-Type", "application/json; charset=utf-8");
    res.send(JSON.stringify({ error: "app_check_failed" }));
    return;
  }

  try {
    const request = toWebRequest(req);
    const response = await worker.fetch(request, buildEnv());
    await sendWebResponse(response, res);
  } catch (error) {
    const request = toWebRequest(req);
    const response = worker.errorResponse(error, request);
    await sendWebResponse(response, res);
  }
});