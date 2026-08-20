-- V5 originally used ON CONFLICT (phone) DO NOTHING. That was safe as a
-- fresh seed, but a row for phone 9999999999 had already been inserted
-- manually (outside Flyway, before V5 ever ran) with a bcrypt hash that
-- does not actually decode to 'admin123' -- confirmed via
-- BCryptPasswordEncoder#matches() and via live login attempts returning
-- 401 with "User found" in the logs. V5 therefore left that bad password
-- in place.
--
-- This is a NEW migration rather than an edit to V5: V5 already ran
-- against prod and Flyway tracks it by checksum, so changing its content
-- after the fact breaks validation ("Migration checksum mismatch for
-- migration version 5") and prevents the app from starting at all.
-- Never edit an already-applied migration -- add a new one instead.
--
-- Idempotent: safe to run again (e.g. after another manual insert) since
-- it just re-asserts the correct password for these two phones.
UPDATE users
SET password = '$2a$10$jDrtp/6KZZOmJLVEBmTAvObFs7hPDHUSapGpDU4Wtxnl3tendAhhK', updated_at = now()
WHERE phone = '9999999999';

UPDATE users
SET password = '$2a$10$kMjmzwPhEiOVObZH.zNAReh0iU8I1hDMRgPAYJ.HKwcuQfPOvwjXS', updated_at = now()
WHERE phone = '8888888888';

INSERT INTO users (username, phone, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'admin', '9999999999', '$2a$10$jDrtp/6KZZOmJLVEBmTAvObFs7hPDHUSapGpDU4Wtxnl3tendAhhK', 'admin@rkbrothers.local', 'Default Admin', 'ADMIN', true, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE phone = '9999999999');

INSERT INTO users (username, phone, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'staff', '8888888888', '$2a$10$kMjmzwPhEiOVObZH.zNAReh0iU8I1hDMRgPAYJ.HKwcuQfPOvwjXS', 'staff@rkbrothers.local', 'Default Staff', 'STAFF', true, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE phone = '8888888888');
