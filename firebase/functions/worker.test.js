import test from "node:test";
import assert from "node:assert/strict";
import worker from "./worker.js";

const env = {};

test("worker health endpoint remains available", async () => {
  const response = await worker.fetch(new Request("https://example.test/"), env);
  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), {
    ok: true,
    service: "al-daftar-license-api",
    system: "SmartLedger Core",
    version: 2
  });
});

test("worker preserves CORS preflight behavior", async () => {
  const response = await worker.fetch(
    new Request("https://example.test/license/verify", {
      method: "OPTIONS",
      headers: { Origin: "https://example.test" }
    }),
    env
  );
  assert.equal(response.status, 204);
  assert.equal(response.headers.get("Access-Control-Allow-Origin"), "https://example.test");
});

test("worker rejects unsupported methods with its normal error contract", async () => {
  const response = await worker.fetch(
    new Request("https://example.test/license/verify", { method: "PUT" }),
    env
  );
  assert.equal(response.status, 405);
  assert.deepEqual(await response.json(), { error: "method_not_allowed" });
});
