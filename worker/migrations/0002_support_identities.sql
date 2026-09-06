-- 1. تفعيل المفاتيح الأجنبية للأمان
PRAGMA foreign_keys = ON;

-- 2. إنشاء جدول هويات الدعم الفني
CREATE TABLE IF NOT EXISTS support_identities (
  uid TEXT PRIMARY KEY,
  support_id TEXT NOT NULL UNIQUE,
  created_at_ms INTEGER NOT NULL
);

-- 3. إنشاء فهرس فريد للبحث السريع والفوري برمز Support ID
CREATE UNIQUE INDEX IF NOT EXISTS idx_support_identities_support_id ON support_identities(support_id);
