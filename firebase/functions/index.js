import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { getAppCheck } from "firebase-admin/app-check";
import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { licenseFetch } from "./license-firestore.js";

initializeApp();
const db = getFirestore();

const LICENSE_PRIVATE_KEY = defineSecret("SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY");
const RATE_LIMIT_SALT = defineSecret("SMARTLEDGER_RATE_LIMIT_SALT");
const secrets = [LICENSE_PRIVATE_KEY, RATE_LIMIT_SALT];

function buildEnv() {
  return {
    SMARTLEDGER_LICENSE_DB: db,
    SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY: LICENSE_PRIVATE_KEY.value(),
    SMARTLEDGER_RATE_LIMIT_SALT: RATE_LIMIT_SALT.value()
  };
}

async function authenticateRequest(request) {
  const authorization = String(request.get("Authorization") || "");
  if (!authorization.startsWith("Bearer ")) {
    return { response: new Response(JSON.stringify({ error: "auth_required" }), {
      status: 401,
      headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" }
    }) };
  }

  const idToken = authorization.slice("Bearer ".length).trim();
  if (!idToken) {
    return { response: new Response(JSON.stringify({ error: "auth_required" }), {
      status: 401,
      headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" }
    }) };
  }

  try {
    const decoded = await getAuth().verifyIdToken(idToken);
    return { decoded };
  } catch (_) {
    return { response: new Response(JSON.stringify({ error: "auth_invalid" }), {
      status: 401,
      headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" }
    }) };
  }
}

function toWebRequest(req, auth) {
  const protocol = req.get("x-forwarded-proto") || "https";
  const host = req.get("x-forwarded-host") || req.get("host");
  const url = new URL(req.originalUrl || req.url || "/", protocol + "://" + host);
  const headers = new Headers();
  for (const [name, value] of Object.entries(req.headers)) {
    if (Array.isArray(value)) headers.set(name, value.join(", "));
    else if (value != null) headers.set(name, String(value));
  }

  headers.set("X-Firebase-UID", String(auth.uid));
  headers.set("X-Firebase-Email", String(auth.email || ""));
  headers.set("X-Firebase-Admin", auth.admin === true ? "true" : "false");

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
  if (!webResponse.body) {
    res.end();
    return;
  }

  const reader = webResponse.body.getReader();
  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      res.write(Buffer.from(value));
    }
  } finally {
    reader.releaseLock();
  }
  res.end();
}

async function verifyOptionalAppCheck(req) {
  const token = String(req.get("X-Firebase-AppCheck") || "").trim();
  if (!token) return true;
  try {
    await getAppCheck().verifyToken(token);
    return true;
  } catch (_) {
    return false;
  }
}

export const smartledgerApi = onRequest({
  region: "us-central1",
  timeoutSeconds: 120,
  memory: "512MiB",
  invoker: "public",
  secrets
}, async (req, res) => {
  try {
    if (!(await verifyOptionalAppCheck(req))) {
      res.status(401)
        .set("Content-Type", "application/json; charset=utf-8")
        .set("Cache-Control", "no-store")
        .send(JSON.stringify({ error: "app_check_invalid" }));
      return;
    }

    const auth = await authenticateRequest(req);
    if (auth.response) {
      await sendWebResponse(auth.response, res);
      return;
    }

    const request = toWebRequest(req, auth.decoded);
    const response = await licenseFetch(request, buildEnv());
    await sendWebResponse(response, res);
  } catch (error) {
    const body = JSON.stringify({ error: String(error?.message || "server_error") });
    res.status(500).set("Content-Type", "application/json; charset=utf-8").send(body);
  }
});
