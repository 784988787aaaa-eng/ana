const ACCOUNT_PATTERN = /^SL-[A-Z0-9]{4}-[A-Z0-9]{4}$/;
const FINGERPRINT_PATTERN = /^[A-F0-9]{64}$/;
const BACKUP_PATTERN = /^SMN_\d{4}-\d{2}-\d{2}(?:_\d{4})?\.slb$/i;
const DRIVE_SCOPE = 'https://www.googleapis.com/auth/drive.file';
const DRIVE_ROOT = 'الدفتر الذكي';
const DRIVE_MIME = 'application/vnd.smartledger.backup';
const RATE_WINDOW_MS = 15 * 60 * 1000;
const RATE_LIMIT = 5;
const OAUTH_STATE_TTL_MS = 10 * 60 * 1000;
const CHALLENGE_TTL_MS = 5 * 60 * 1000;
const MAX_UPLOAD_BYTES = 50 * 1024 * 1024;
const SESSION_IDLE_MS = 180 * 24 * 60 * 60 * 1000;

const json = (status, body) => new Response(JSON.stringify(body), {
  status,
  headers: { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' }
});

const html = (status, body) => new Response(body, {
  status,
  headers: { 'Content-Type': 'text/html; charset=utf-8', 'Cache-Control': 'no-store' }
});

function b64Url(bytes) {
  let binary = '';
  const input = bytes instanceof ArrayBuffer ? new Uint8Array(bytes) : bytes;
  for (let i = 0; i < input.length; i += 0x8000) binary += String.fromCharCode(...input.subarray(i, i + 0x8000));
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function fromB64(value) {
  const normalized = String(value).replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(String(value).length / 4) * 4, '=');
  const binary = atob(normalized);
  return Uint8Array.from(binary, char => char.charCodeAt(0));
}

async function sha256(value) {
  const data = typeof value === 'string' ? new TextEncoder().encode(value) : value;
  return new Uint8Array(await crypto.subtle.digest('SHA-256', data));
}

async function hexHash(value) {
  return Array.from(await sha256(value), byte => byte.toString(16).padStart(2, '0')).join('').toUpperCase();
}

function pemToBytes(pem) {
  const clean = String(pem).replace(/-----BEGIN [^-]+-----/g, '').replace(/-----END [^-]+-----/g, '').replace(/\s+/g, '');
  return fromB64(clean);
}

async function importPrivateKey(pem) {
  return crypto.subtle.importKey(
    'pkcs8', pemToBytes(pem),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false, ['sign']
  );
}

async function importPublicKey(base64) {
  return crypto.subtle.importKey(
    'spki', fromB64(base64),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false, ['verify']
  );
}

async function signToken(header, body, env) {
  const key = await importPrivateKey(env.SMARTLEDGER_ACCOUNT_LICENSE_PRIVATE_KEY);
  const input = `${header}.${body}`;
  const signature = await crypto.subtle.sign('RSASSA-PKCS1-v1_5', key, new TextEncoder().encode(input));
  return `${header}.${body}.${b64Url(signature)}`;
}

async function issueAccountToken(accountCode, license, fingerprint, env) {
  const issuedAt = Date.now();
  const payload = {
    product: 'SMARTLEDGER',
    type: 'ACCOUNT',
    plan: 'LIFETIME',
    licenseId: String(license.licenseId || accountCode),
    accountCode,
    issuedAt,
    offlineUntil: issuedAt + 30 * 24 * 60 * 60 * 1000,
    deviceFingerprint: fingerprint,
    keyVersion: 1
  };
  const header = b64Url(new TextEncoder().encode(JSON.stringify({ alg: 'RS256', typ: 'SLT1', kid: 'ACCOUNT1' })));
  const body = b64Url(new TextEncoder().encode(JSON.stringify(payload)));
  return signToken(header, body, env);
}

async function encryptText(value, secret) {
  const keyBytes = await sha256(`${secret}:SMARTLEDGER_DRIVE_TOKEN_V1`);
  const key = await crypto.subtle.importKey('raw', keyBytes, 'AES-GCM', false, ['encrypt', 'decrypt']);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const ciphertext = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, new TextEncoder().encode(value)));
  return `${b64Url(iv)}.${b64Url(ciphertext)}`;
}

async function decryptText(value, secret) {
  const [ivPart, cipherPart] = String(value).split('.');
  if (!ivPart || !cipherPart) throw new Error('invalid_session');
  const keyBytes = await sha256(`${secret}:SMARTLEDGER_DRIVE_TOKEN_V1`);
  const key = await crypto.subtle.importKey('raw', keyBytes, 'AES-GCM', false, ['decrypt']);
  const plaintext = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: fromB64(ivPart) }, key, fromB64(cipherPart));
  return new TextDecoder().decode(plaintext);
}

async function verifyDeviceSignature(publicKeyBase64, message, signatureBase64) {
  const key = await importPublicKey(publicKeyBase64);
  return crypto.subtle.verify('RSASSA-PKCS1-v1_5', key, fromB64(signatureBase64), new TextEncoder().encode(message));
}

function clientIp(request) {
  return request.headers.get('CF-Connecting-IP') || request.headers.get('X-Forwarded-For')?.split(',')[0]?.trim() || 'unknown';
}

async function rateLimit(env, accountCode, ip) {
  const id = await hexHash(`${env.SMARTLEDGER_RATE_LIMIT_SALT}:${accountCode}:${ip}`);
  const key = `rate:${id}`;
  const now = Date.now();
  const current = await env.SMARTLEDGER_KV.get(key, 'json');
  if (!current || now - Number(current.startedAt || 0) >= RATE_WINDOW_MS) {
    await env.SMARTLEDGER_KV.put(key, JSON.stringify({ startedAt: now, attempts: 1 }), { expirationTtl: 20 * 60 });
    return true;
  }
  if (Number(current.attempts || 0) >= RATE_LIMIT) return false;
  await env.SMARTLEDGER_KV.put(key, JSON.stringify({ startedAt: current.startedAt, attempts: Number(current.attempts || 0) + 1 }), { expirationTtl: 20 * 60 });
  return true;
}

async function readJson(request) {
  try { return await request.json(); } catch (_) { throw new Error('invalid_json'); }
}

async function licenseRecord(env, accountCode) {
  return env.SMARTLEDGER_KV.get(`license:${accountCode}`, 'json');
}

async function activate(request, env) {
  const body = await readJson(request);
  let accountCode = String(body.accountCode || '').trim().toUpperCase();
  const email = String(body.email || '').trim().toLowerCase();
  if (!accountCode && email) {
    const mappedAccount = await env.SMARTLEDGER_KV.get(`email:${email}`);
    if (mappedAccount) accountCode = String(mappedAccount).trim().toUpperCase();
  }
  const activationCode = String(body.activationCode || '').trim();
  const publicKey = String(body.devicePublicKey || '').trim();
  if (!ACCOUNT_PATTERN.test(accountCode) || activationCode.length < 16 || !publicKey) return json(400, { error: 'invalid_activation' });
  if (!(await rateLimit(env, accountCode, clientIp(request)))) return json(429, { error: 'rate_limited' });
  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: 'not_found' });
  if (license.revoked === true) return json(403, { error: 'revoked' });
  if (license.type !== 'ACCOUNT' || license.plan !== 'LIFETIME') return json(409, { error: 'license_not_ready' });
  const expected = String(license.activationCodeHash || '').toUpperCase();
  const supplied = await hexHash(activationCode);
  if (!expected || supplied !== expected) return json(403, { error: 'activation_invalid' });
  const fingerprint = await hexHash(fromB64(publicKey));
  if (!FINGERPRINT_PATTERN.test(fingerprint)) return json(400, { error: 'invalid_activation' });
  await env.SMARTLEDGER_KV.put(`install:${accountCode}:${fingerprint}`, JSON.stringify({ publicKey, createdAt: Date.now(), lastSeenAt: Date.now(), revoked: false }));
  return json(200, { token: await issueAccountToken(accountCode, license, fingerprint, env) });
}

async function verify(request, env) {
  const body = await readJson(request);
  const accountCode = String(body.accountCode || '').trim().toUpperCase();
  const fingerprint = String(body.deviceFingerprint || '').trim().toUpperCase();
  const challenge = String(body.challenge || '').trim();
  const signature = String(body.signature || '').trim();
  if (!ACCOUNT_PATTERN.test(accountCode) || !FINGERPRINT_PATTERN.test(fingerprint) || challenge.length < 32 || !signature) return json(400, { error: 'invalid_verification' });
  if (!(await rateLimit(env, accountCode, clientIp(request)))) return json(429, { error: 'rate_limited' });
  const license = await licenseRecord(env, accountCode);
  if (!license) return json(404, { error: 'not_found' });
  if (license.revoked === true) return json(200, { revoked: true });
  const installation = await env.SMARTLEDGER_KV.get(`install:${accountCode}:${fingerprint}`, 'json');
  if (!installation || installation.revoked === true) return json(403, { error: 'installation_not_authorized' });
  const challengeTime = Number(challenge.split(':', 1)[0]);
  if (!Number.isFinite(challengeTime) || Math.abs(Date.now() - challengeTime) > CHALLENGE_TTL_MS) return json(403, { error: 'challenge_expired' });
  if (!(await verifyDeviceSignature(String(installation.publicKey || ''), challenge, signature))) return json(403, { error: 'proof_invalid' });
  await env.SMARTLEDGER_KV.put(`install:${accountCode}:${fingerprint}`, JSON.stringify({ ...installation, lastSeenAt: Date.now() }));
  return json(200, { token: await issueAccountToken(accountCode, license, fingerprint, env) });
}

function redirectUri(request) {
  const url = new URL(request.url);
  return `${url.origin}/driveOAuthCallback`;
}

function randomToken() {
  return b64Url(crypto.getRandomValues(new Uint8Array(32)));
}

async function exchangeCode(code, request, env) {
  const body = new URLSearchParams({
    code,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    redirect_uri: redirectUri(request),
    grant_type: 'authorization_code'
  });
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body
  });
  let data = {};
  try {
    data = await response.json();
  } catch (err) {
    console.error('Google token exchange failed to parse JSON response', { status: response.status });
  }
  if (!response.ok || !data.refresh_token) {
    console.error('Google OAuth token exchange failed', {
      status: response.status,
      error: data.error || null,
      description: data.error_description || null,
      hasRefreshToken: Boolean(data.refresh_token)
    });
    const failureCode = data.error === 'invalid_grant' ? 'oauth_invalid_grant'
      : data.error === 'redirect_uri_mismatch' ? 'oauth_redirect_uri_mismatch'
      : data.error === 'invalid_client' ? 'oauth_invalid_client'
      : data.error === 'access_denied' ? 'oauth_access_denied'
      : !data.refresh_token && response.ok ? 'oauth_no_refresh_token'
      : 'oauth_token_exchange_failed';
    const err = new Error('authorization_failed');
    err.failureCode = failureCode;
    throw err;
  }
  return data.refresh_token;
}

async function accessToken(refreshToken, env) {
  const body = new URLSearchParams({
    refresh_token: refreshToken,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    grant_type: 'refresh_token'
  });
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body
  });
  const data = await response.json();
  if (!response.ok || !data.access_token) throw new Error('invalid_session');
  return data.access_token;
}

async function sessionRecord(token, env) {
  if (!token) throw new Error('invalid_session');
  const key = `session:${await hexHash(token)}`;
  const session = await env.SMARTLEDGER_KV.get(key, 'json');
  if (!session) throw new Error('invalid_session');
  if (Date.now() - Number(session.lastSeenAt || session.createdAt || 0) > SESSION_IDLE_MS) {
    await env.SMARTLEDGER_KV.delete(key);
    throw new Error('invalid_session');
  }
  const updated = { ...session, lastSeenAt: Date.now() };
  await env.SMARTLEDGER_KV.put(key, JSON.stringify(updated));
  return { key, ...updated };
}

async function driveRequest(token, method, path, env, options = {}) {
  const session = await sessionRecord(token, env);
  const refreshToken = await decryptText(session.refreshToken, env.SMARTLEDGER_RATE_LIMIT_SALT);
  const bearer = await accessToken(refreshToken, env);
  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${bearer}`);
  return fetch(`https://www.googleapis.com/drive/v3/${path}`, { method, headers, body: options.body });
}

async function driveJson(token, method, path, env, options = {}) {
  const response = await driveRequest(token, method, path, env, options);
  const text = await response.text();
  let data = {};
  try { data = JSON.parse(text); } catch (_) {}
  if (!response.ok) {
    console.error('Google Drive API error', {
      status: response.status,
      reason: data.error?.errors?.[0]?.reason || null,
      message: data.error?.message || null
    });
    if (response.status === 401) throw new Error('invalid_session');
    if (response.status === 403) throw new Error('drive_forbidden');
    if (response.status === 404) throw new Error('drive_not_found');
    if (response.status === 429) throw new Error('drive_rate_limited');
    if (response.status >= 500) throw new Error('drive_unavailable');
    throw new Error('drive_error');
  }
  return data;
}

async function listFiles(token, search, env) {
  const lower = search.toLowerCase();
  const params = new URLSearchParams({
    q: "trashed = false and mimeType = 'application/vnd.smartledger.backup' and name contains 'SMN_'",
    pageSize: '1000',
    fields: 'nextPageToken,files(id,name,mimeType,size,modifiedTime,parents)',
    orderBy: 'name'
  });
  if (lower && /^[a-z0-9_.-]+$/i.test(lower)) params.set('q', `${params.get('q')} and name contains '${lower.replace(/'/g, "\\'")}'`);
  const items = [];
  let pageToken = '';
  do {
    if (pageToken) params.set('pageToken', pageToken); else params.delete('pageToken');
    const data = await driveJson(token, 'GET', `files?${params.toString()}`, env);
    for (const item of data.files || []) {
      if (!BACKUP_PATTERN.test(item.name || '')) continue;
      const month = String(item.name).slice(4, 11);
      if (lower && !String(item.name).toLowerCase().includes(lower) && !month.toLowerCase().includes(lower)) continue;
      items.push({ id: item.id, name: item.name, size: Number(item.size || 0), modifiedTime: Date.parse(item.modifiedTime || '') || 0, month });
    }
    pageToken = data.nextPageToken || '';
  } while (pageToken);
  items.sort((a, b) => b.modifiedTime - a.modifiedTime);
  return items;
}

async function findFolder(token, parentId, name, env) {
  const q = `'${parentId}' in parents and trashed = false and mimeType = 'application/vnd.google-apps.folder' and name = '${name.replace(/'/g, "\\'")}'`;
  const data = await driveJson(token, 'GET', `files?pageSize=10&fields=files(id,name,mimeType)&q=${encodeURIComponent(q)}`, env);
  return data.files?.[0] || null;
}

async function ensureFolder(token, parentId, name, env) {
  const existing = await findFolder(token, parentId, name, env);
  if (existing) return existing;
  return driveJson(token, 'POST', 'files', env, {
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, mimeType: 'application/vnd.google-apps.folder', parents: [parentId] })
  });
}

async function monthFolder(token, month, env) {
  const root = await ensureFolder(token, 'root', DRIVE_ROOT, env);
  return ensureFolder(token, root.id, month, env);
}


async function driveConnectWithServerAuthCode(request, env) {
  const body = await readJson(request);
  const code = String(body.serverAuthCode || '').trim();
  if (!code) return json(400, { error: 'invalid_auth_code' });
  const exchangeBody = new URLSearchParams({
    code,
    client_id: env.GOOGLE_CLIENT_ID,
    client_secret: env.GOOGLE_CLIENT_SECRET,
    grant_type: 'authorization_code'
  });
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: exchangeBody
  });
  let data = {};
  try { data = await response.json(); } catch (_) {}
  if (!response.ok || !data.refresh_token) {
    const failureCode = data.error === 'invalid_grant' ? 'oauth_invalid_grant'
      : data.error === 'invalid_client' ? 'oauth_invalid_client'
      : data.error === 'access_denied' ? 'oauth_access_denied'
      : !data.refresh_token && response.ok ? 'oauth_no_refresh_token'
      : 'oauth_token_exchange_failed';
    return json(400, { error: failureCode });
  }
  const cloudToken = randomToken();
  await env.SMARTLEDGER_KV.put(
    `session:${await hexHash(cloudToken)}`,
    JSON.stringify({ refreshToken: await encryptText(data.refresh_token, env.SMARTLEDGER_RATE_LIMIT_SALT), createdAt: Date.now(), lastSeenAt: Date.now() }),
    { expirationTtl: 60 * 60 * 24 * 180 }
  );
  return json(200, { status: 'connected', cloudToken });
}

async function driveStart(request, env) {
  const body = await readJson(request);
  const connectionId = String(body.connectionId || '').trim();
  if (!/^[A-Za-z0-9_-]{20,80}$/.test(connectionId)) return json(400, { error: 'invalid_connection' });
  const state = randomToken();
  const stateHash = await hexHash(state);
  const connectionHash = await hexHash(connectionId);
  await env.SMARTLEDGER_KV.put(`oauth:${stateHash}`, JSON.stringify({ connectionId, createdAt: Date.now(), status: 'pending' }), { expirationTtl: 15 * 60 });
  await env.SMARTLEDGER_KV.put(`oauth-connection:${connectionHash}`, stateHash, { expirationTtl: 15 * 60 });
  const params = new URLSearchParams({
    client_id: env.GOOGLE_CLIENT_ID,
    redirect_uri: redirectUri(request),
    response_type: 'code',
    access_type: 'offline',
    prompt: 'consent',
    scope: DRIVE_SCOPE,
    state
  });
  return json(200, { authorizationUrl: `https://accounts.google.com/o/oauth2/v2/auth?${params}` });
}

async function driveCallback(request, env) {
  const url = new URL(request.url);
  const state = String(url.searchParams.get('state') || '');
  const code = String(url.searchParams.get('code') || '');
  const googleError = String(url.searchParams.get('error') || '');
  if (googleError) {
    console.error('Google OAuth returned error query parameter', { error: googleError });
  }
  if (!state || (!code && !googleError)) return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تعذر إكمال ربط Google Drive.</h2></body></html>');
  const key = `oauth:${await hexHash(state)}`;
  const pending = await env.SMARTLEDGER_KV.get(key, 'json');
  if (!pending || Date.now() - Number(pending.createdAt || 0) > OAUTH_STATE_TTL_MS) return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>انتهت جلسة الربط. أعد المحاولة من التطبيق.</h2></body></html>');
  if (googleError) {
    const safeFailureCode = googleError === 'access_denied' ? 'oauth_access_denied' : 'oauth_google_error';
    await env.SMARTLEDGER_KV.put(key, JSON.stringify({ ...pending, status: 'failed', failureCode: safeFailureCode, updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(400, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تم إلغاء أو رفض تفويض Google Drive.</h2><p>يمكنك العودة للتطبيق والمحاولة مرة أخرى.</p></body></html>');
  }
  try {
    const refreshToken = await exchangeCode(code, request, env);
    await env.SMARTLEDGER_KV.put(key, JSON.stringify({ ...pending, status: 'connected', refreshToken: await encryptText(refreshToken, env.SMARTLEDGER_RATE_LIMIT_SALT), updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(200, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تم ربط Google Drive بنجاح.</h2><p>يمكنك الآن العودة إلى تطبيق الدفتر الذكي.</p></body></html>');
  } catch (err) {
    const safeFailureCode = err?.failureCode || 'oauth_token_exchange_failed';
    await env.SMARTLEDGER_KV.put(key, JSON.stringify({ ...pending, status: 'failed', failureCode: safeFailureCode, updatedAt: Date.now() }), { expirationTtl: 15 * 60 });
    return html(500, '<html dir="rtl"><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:32px"><h2>تعذر إكمال ربط Google Drive.</h2><p>ارجع إلى التطبيق وحاول مرة أخرى.</p></body></html>');
  }
}

async function driveStatus(request, env) {
  const body = await readJson(request);
  const connectionId = String(body.connectionId || '').trim();
  if (!connectionId) return json(400, { error: 'invalid_connection' });
  const stateHash = await env.SMARTLEDGER_KV.get(`oauth-connection:${await hexHash(connectionId)}`);
  if (!stateHash) return json(404, { error: 'not_found' });
  const key = `oauth:${stateHash}`;
  const pending = await env.SMARTLEDGER_KV.get(key, 'json');
  if (!pending) return json(404, { error: 'not_found' });
  if (Date.now() - Number(pending.createdAt || 0) > OAUTH_STATE_TTL_MS) {
    await env.SMARTLEDGER_KV.delete(key);
    await env.SMARTLEDGER_KV.delete(`oauth-connection:${await hexHash(connectionId)}`);
    return json(410, { error: 'expired' });
  }
  if (pending.status === 'connected') {
    const cloudToken = randomToken();
    await env.SMARTLEDGER_KV.put(`session:${await hexHash(cloudToken)}`, JSON.stringify({ refreshToken: pending.refreshToken, createdAt: Date.now(), lastSeenAt: Date.now() }));
    await env.SMARTLEDGER_KV.delete(key);
    await env.SMARTLEDGER_KV.delete(`oauth-connection:${await hexHash(connectionId)}`);
    return json(200, { status: 'connected', cloudToken });
  }
  if (pending.status === 'failed') {
    return json(200, { status: 'failed', failureCode: pending.failureCode || 'oauth_failed' });
  }
  return json(200, { status: pending.status || 'pending' });
}

async function driveUpload(request, env) {
  const token = String(request.headers.get('X-SmartLedger-Cloud-Token') || '');
  const encodedName = String(request.headers.get('X-SmartLedger-File-Name') || '');
  const name = decodeURIComponent(encodedName);
  if (!BACKUP_PATTERN.test(name)) return json(400, { error: 'invalid_name' });
  const contentLength = Number(request.headers.get('Content-Length') || 0);
  if (contentLength && contentLength > MAX_UPLOAD_BYTES) return json(413, { error: 'payload_too_large' });
  if (!request.body) return json(400, { error: 'invalid_payload' });
  const month = name.slice(4, 11);
  const folder = await monthFolder(token, month, env);
  const existingData = await driveJson(token, 'GET', `files?q=${encodeURIComponent(`'${folder.id}' in parents and trashed = false and name = '${name.replace(/'/g, "\\'")}'`)}&pageSize=10&fields=files(id,name,size,modifiedTime)`, env);
  const existing = existingData.files?.find(item => item.name === name);
  let item;
  if (existing) {
    const response = await driveRequest(token, 'PATCH', `upload/drive/v3/files/${encodeURIComponent(existing.id)}?uploadType=media`, env, { headers: { 'Content-Type': DRIVE_MIME }, body: request.body });
    if (!response.ok) throw new Error('drive_error');
    item = await driveJson(token, 'GET', `files/${encodeURIComponent(existing.id)}?fields=id,name,size,modifiedTime`, env);
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
    item = await driveJson(token, 'POST', 'upload/drive/v3/files?uploadType=multipart&fields=id,name,size,modifiedTime', env, {
      headers: { 'Content-Type': `multipart/related; boundary=${boundary}` },
      body: stream
    });
  }
  return json(200, { item: { id: item.id, name: item.name, size: Number(item.size || contentLength || 0), modifiedTime: Date.parse(item.modifiedTime || '') || Date.now(), month } });
}

async function driveList(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || '');
  const search = String(body.search || '').trim();
  return json(200, { items: await listFiles(token, search, env) });
}

async function driveDownload(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || '');
  const fileId = String(body.fileId || '');
  if (!fileId) return json(400, { error: 'invalid_file' });
  const response = await driveRequest(token, 'GET', `files/${encodeURIComponent(fileId)}?alt=media`, env);
  if (!response.ok) throw new Error(response.status === 401 ? 'invalid_session' : 'drive_error');
  const headers = new Headers({ 'Cache-Control': 'no-store', 'Content-Type': 'application/octet-stream' });
  if (response.headers.has('Content-Length')) headers.set('Content-Length', response.headers.get('Content-Length'));
  return new Response(response.body, { status: 200, headers });
}

async function driveDelete(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || '');
  const ids = Array.isArray(body.fileIds) ? body.fileIds.map(String).slice(0, 100) : [];
  let deleted = 0;
  for (const id of ids) {
    const response = await driveRequest(token, 'DELETE', `files/${encodeURIComponent(id)}`, env);
    if (response.ok || response.status === 404) deleted++;
  }
  return json(200, { deleted });
}

async function driveDisconnect(request, env) {
  const body = await readJson(request);
  const token = String(body.cloudToken || '');
  if (!token) return json(200, { ok: true });
  const key = `session:${await hexHash(token)}`;
  const session = await env.SMARTLEDGER_KV.get(key, 'json');
  await env.SMARTLEDGER_KV.delete(key);
  if (session?.refreshToken) {
    try {
      const refreshToken = await decryptText(session.refreshToken, env.SMARTLEDGER_RATE_LIMIT_SALT);
      await fetch(`https://oauth2.googleapis.com/revoke?token=${encodeURIComponent(refreshToken)}`, { method: 'POST' });
    } catch (_) {}
  }
  return json(200, { ok: true });
}

function errorResponse(error) {
  const message = error?.message || 'server_error';
  if (message === 'invalid_session') return json(401, { error: 'invalid_session' });
  if (message === 'authorization_failed') return json(502, { error: 'authorization_failed' });
  if (message === 'rate_limited' || message === 'drive_rate_limited') return json(429, { error: 'rate_limited' });
  if (message === 'drive_forbidden') return json(403, { error: 'drive_forbidden' });
  if (message === 'drive_not_found') return json(404, { error: 'drive_not_found' });
  if (message === 'drive_unavailable') return json(503, { error: 'drive_unavailable' });
  if (message === 'drive_error') return json(502, { error: 'drive_error' });
  if (message === 'invalid_json') return json(400, { error: 'invalid_json' });
  return json(500, { error: 'server_error' });
}

export default {
  async fetch(request, env) {
    try {
      const url = new URL(request.url);
      if (request.method === 'GET' && url.pathname === '/driveOAuthCallback') return await driveCallback(request, env);
      if (request.method !== 'POST') return json(405, { error: 'method_not_allowed' });
      if (url.pathname === '/license/activate') return await activate(request, env);
      if (url.pathname === '/license/verify') return await verify(request, env);
      if (url.pathname === '/driveApi/config') return json(200, { googleClientId: String(env.GOOGLE_CLIENT_ID || '') });
      if (url.pathname === '/driveApi/connect/google-signin') return await driveConnectWithServerAuthCode(request, env);
      if (url.pathname === '/driveApi/connect/start') return await driveStart(request, env);
      if (url.pathname === '/driveApi/connect/status') return await driveStatus(request, env);
      if (url.pathname === '/driveApi/list') return await driveList(request, env);
      if (url.pathname === '/driveApi/upload') return await driveUpload(request, env);
      if (url.pathname === '/driveApi/download') return await driveDownload(request, env);
      if (url.pathname === '/driveApi/delete') return await driveDelete(request, env);
      if (url.pathname === '/driveApi/disconnect') return await driveDisconnect(request, env);
      return json(404, { error: 'not_found' });
    } catch (error) {
      return errorResponse(error);
    }
  }
};
