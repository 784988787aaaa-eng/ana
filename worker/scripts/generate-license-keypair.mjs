import { generateKeyPairSync } from "node:crypto";
import { mkdirSync, writeFileSync } from "node:fs";

mkdirSync("keys", { recursive: true });
const { publicKey, privateKey } = generateKeyPairSync("rsa", {
  modulusLength: 3072,
  publicKeyEncoding: { type: "spki", format: "pem" },
  privateKeyEncoding: { type: "pkcs8", format: "pem" },
});
writeFileSync("keys/license-public-key.pem", publicKey, { mode: 0o644 });
writeFileSync("keys/license-private-key.pem", privateKey, { mode: 0o600 });
console.log("Generated RSA-3072 license key pair in worker/keys/. Keep the private key out of Git and upload it only as a Cloudflare Worker secret.");
