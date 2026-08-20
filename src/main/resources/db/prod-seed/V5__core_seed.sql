-- Seeds the default production admin/staff logins. Local dev seeds the same
-- credentials at runtime via DevDataSeeder (@Profile("dev")), which only
-- runs against H2 and never touches Postgres -- prod had no equivalent, so
-- no user row ever existed there and every login attempt returned 401.
--
-- This location (classpath:db/prod-seed) is wired into application-prod.yml
-- only. It is deliberately NOT nested under db/seed -- Flyway's classpath
-- scanning is recursive, so a db/seed/prod subfolder would still collide
-- with db/seed/V5__test_seed.sql (dev-only fixture data) under dev's
-- combined classpath:db/migration,classpath:db/seed locations.
--
-- Hashes are bcrypt (cost 10) for 'admin123' / 'staff123', generated with
-- the app's own BCryptPasswordEncoder and verified with matches() before
-- being committed here. Change these passwords after first login.
--
-- ON CONFLICT ... DO UPDATE (not DO NOTHING): a row for phone 9999999999
-- was found live in prod before this migration ever ran, so it must have
-- been inserted manually with one of the bcrypt hashes floated earlier in
-- the debugging session -- none of which actually decoded to 'admin123'
-- (verified with matches()). DO UPDATE overwrites any such bad password
-- with the verified-correct hash instead of silently skipping it.
INSERT INTO users (username, phone, password, email, full_name, role, enabled, created_at, updated_at)
VALUES
  ('admin', '9999999999', '$2a$10$jDrtp/6KZZOmJLVEBmTAvObFs7hPDHUSapGpDU4Wtxnl3tendAhhK', 'admin@rkbrothers.local', 'Default Admin', 'ADMIN', true, now(), now()),
  ('staff', '8888888888', '$2a$10$kMjmzwPhEiOVObZH.zNAReh0iU8I1hDMRgPAYJ.HKwcuQfPOvwjXS', 'staff@rkbrothers.local', 'Default Staff', 'STAFF', true, now(), now())
ON CONFLICT (phone) DO UPDATE SET password = EXCLUDED.password, updated_at = now();
