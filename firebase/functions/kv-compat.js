import { Timestamp } from "firebase-admin/firestore";

const COLLECTION = "smartledgerKv";

function encodeDocId(key) {
  return Buffer.from(String(key), "utf8").toString("base64url");
}

export class FirestoreKV {
  constructor(db) { this.db = db; }

  async get(key, type) {
    const ref = this.db.collection(COLLECTION).doc(encodeDocId(key));
    const snap = await ref.get();
    if (!snap.exists) return null;
    const data = snap.data() || {};
    if (data.expiresAt && data.expiresAt.toMillis() <= Date.now()) {
      await ref.delete().catch(() => {});
      return null;
    }
    const value = data.value ?? null;
    if (type === "json") {
      if (value == null) return null;
      if (typeof value === "string") {
        try { return JSON.parse(value); } catch (_) { return null; }
      }
      return value;
    }
    return value == null ? null : String(value);
  }

  async put(key, value, options = {}) {
    const data = {
      key: String(key),
      value: typeof value === "string" ? value : JSON.stringify(value),
      updatedAt: Timestamp.now()
    };
    const ttl = Number(options.expirationTtl);
    if (Number.isFinite(ttl) && ttl > 0) {
      data.expiresAt = Timestamp.fromMillis(Date.now() + ttl * 1000);
    } else {
      data.expiresAt = null;
    }
    await this.db.collection(COLLECTION).doc(encodeDocId(key)).set(data, { merge: true });
  }

  async delete(key) {
    await this.db.collection(COLLECTION).doc(encodeDocId(key)).delete();
  }

  async list({ prefix = "", limit = 1000 } = {}) {
    const end = String(prefix) + "\uf8ff";
    const snap = await this.db.collection(COLLECTION)
      .where("key", ">=", String(prefix))
      .where("key", "<=", end)
      .limit(Math.min(Number(limit) || 1000, 1000))
      .get();
    const keys = [];
    const expired = [];
    for (const doc of snap.docs) {
      const data = doc.data() || {};
      if (data.expiresAt && data.expiresAt.toMillis() <= Date.now()) {
        expired.push(doc.ref);
        continue;
      }
      const key = String(data.key || "");
      if (key.startsWith(prefix)) keys.push({ name: key });
    }
    if (expired.length) await Promise.all(expired.map(ref => ref.delete().catch(() => {})));
    return { keys, list_complete: true, cursor: undefined };
  }
}