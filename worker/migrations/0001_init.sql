PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS licenses (
  uid TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  is_activated INTEGER NOT NULL DEFAULT 0 CHECK (is_activated IN (0, 1)),
  devices_max INTEGER NOT NULL DEFAULT 1 CHECK (devices_max BETWEEN 1 AND 20),
  device_order_json TEXT NOT NULL DEFAULT '[]',
  license_version INTEGER NOT NULL DEFAULT 1,
  updated_at_ms INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_licenses_email ON licenses(email);

CREATE TABLE IF NOT EXISTS devices (
  uid TEXT NOT NULL,
  device_id TEXT NOT NULL,
  session_id TEXT NOT NULL,
  status TEXT NOT NULL,
  registered_at_ms INTEGER NOT NULL,
  last_seen_at_ms INTEGER NOT NULL,
  app_version TEXT NOT NULL,
  revoked_at_ms INTEGER,
  revoked_reason TEXT,
  PRIMARY KEY (uid, device_id),
  FOREIGN KEY (uid) REFERENCES licenses(uid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(uid, status);
CREATE INDEX IF NOT EXISTS idx_devices_session ON devices(session_id);

CREATE TABLE IF NOT EXISTS license_sessions (
  session_id TEXT PRIMARY KEY,
  uid TEXT NOT NULL,
  device_id TEXT NOT NULL,
  status TEXT NOT NULL,
  issued_at_ms INTEGER NOT NULL,
  updated_at_ms INTEGER NOT NULL,
  offline_valid_until_ms INTEGER NOT NULL,
  revoked_reason TEXT,
  FOREIGN KEY (uid) REFERENCES licenses(uid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_license_sessions_uid ON license_sessions(uid);
CREATE INDEX IF NOT EXISTS idx_license_sessions_status ON license_sessions(uid, status);

CREATE TABLE IF NOT EXISTS license_audit (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  uid TEXT NOT NULL,
  email TEXT,
  device_id_hash TEXT,
  session_id TEXT,
  action TEXT NOT NULL,
  evicted_device_id_hash TEXT,
  created_at_ms INTEGER NOT NULL,
  app_version TEXT
);

CREATE INDEX IF NOT EXISTS idx_license_audit_uid ON license_audit(uid, created_at_ms DESC);


-- Prevent concurrent activations from exceeding devices_max.
-- The activation batch revokes the selected oldest device before inserting the new active device.
CREATE TRIGGER IF NOT EXISTS enforce_device_limit
BEFORE INSERT ON devices
WHEN NEW.status = 'active'
  AND (SELECT COUNT(*) FROM devices d WHERE d.uid = NEW.uid AND d.status = 'active' AND d.device_id <> NEW.device_id)
      >= (SELECT devices_max FROM licenses WHERE uid = NEW.uid)
BEGIN
  SELECT RAISE(ABORT, 'DEVICE_LIMIT_REACHED');
END;
