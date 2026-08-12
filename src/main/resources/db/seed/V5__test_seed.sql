-- Development-only seed data. This location is wired into the dev Flyway
-- locations only (see application-dev.yml) so it never runs against the
-- production Postgres database.

INSERT INTO operators (name, phone, licence_no, licence_expiry, category, is_active, created_at, updated_at)
VALUES
('Ramesh Yadav', '9111111111', 'DL-1234', '2027-12-31', 'operator', true, now(), now()),
('Suresh Kumar', '9111111112', 'DL-5678', '2027-06-30', 'driver', true, now(), now()),
('Manoj Singh', '9111111113', 'DL-9999', '2027-09-30', 'driver', true, now(), now());

INSERT INTO parties (name, contact_person, phone, email, is_active, created_at, updated_at)
VALUES
('M/S Tata Steel', 'Rajesh Kumar', '9222222222', 'rajesh@tata.com', true, now(), now()),
('Steelco Industries', 'Mohan Lal', '9222222223', 'mohan@steelco.com', true, now(), now());

INSERT INTO work_orders (wo_number, party_id, description, site_location, billing_basis, rate, unit, start_date, end_date, is_active, created_at, updated_at)
VALUES
('WO/2231', 1, 'Excavation work at Yard 3', 'Yard 3', 'hour', 950.00, 'per hour', '2026-07-01', '2026-12-31', true, now(), now()),
('WO/2232', 1, 'Material transport Yard 3 to Gate 1', 'Yard 3', 'trip', 640.00, 'per trip', '2026-07-01', '2026-12-31', true, now(), now()),
('WO/2233', 2, 'Slag removal at Plant 2', 'Plant 2', 'hour', 1200.00, 'per hour', '2026-07-01', '2026-12-31', true, now(), now());

-- status values must match VehicleStatus enum constant names (uppercase):
-- Hibernate's @Enumerated(STRING) uses name()/valueOf(), not the JSON @JsonValue mapping.
INSERT INTO vehicles (vehicle_no, display_name, type, billing_basis, assigned_operator_id, status, is_active, created_at, updated_at)
VALUES
('JCB-01', 'JCB Machine 1', 'jcb', 'hour', 1, 'IDLE', true, now(), now()),
('POC-02', 'Poclain Excavator 1', 'poclain', 'hour', 2, 'IDLE', true, now(), now()),
('TIP-04', 'Tipper Truck 1', 'tipper', 'trip', 3, 'IDLE', true, now(), now());

-- entered_by is left NULL: users are seeded by DevDataSeeder (a
-- CommandLineRunner), which only runs after the full Spring context is up —
-- i.e. strictly after Flyway migrations complete. No user row exists yet
-- at V5 migration time, so a hard-coded entered_by would violate the FK.
INSERT INTO diesel_receipts (date, vehicle_id, litres, received_from, invoice_no, entered_by, created_at)
VALUES
(CURRENT_DATE - 2, 1, 200, 'HP Petrol Pump', 'INV-001', NULL, now()),
(CURRENT_DATE - 2, 2, 150, 'HP Petrol Pump', 'INV-002', NULL, now()),
(CURRENT_DATE - 2, 3, 100, 'HP Petrol Pump', 'INV-003', NULL, now());

-- Billing test data: WO/2231 (id 1, hour basis) gets JCB entries;
-- WO/2232 (id 2, trip basis) gets Tipper entries. submitted_by is NULL for
-- the same FK-ordering reason as entered_by above.
INSERT INTO jcb_daily_log
  (date, vehicle_id, work_order_id, operator_id,
   opening_hrs, closing_hrs, total_hrs,
   diesel_ltr, avg_ltr_per_hr,
   work_description, status, submitted_by,
   created_at, updated_at)
VALUES
  (CURRENT_DATE - 5, 1, 1, 1, 4800.0, 4808.0, 8.0,
   42.0, 5.25, 'Excavation Yard 3', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 4, 1, 1, 1, 4808.0, 4816.5, 8.5,
   45.0, 5.29, 'Excavation Yard 3', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 3, 1, 1, 1, 4816.5, 4824.0, 7.5,
   40.0, 5.33, 'Loading work', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 2, 2, 1, 2, 3200.0, 3208.0, 8.0,
   48.0, 6.0, 'Slag removal', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 1, 2, 1, 2, 3208.0, 3216.0, 8.0,
   47.0, 5.88, 'Slag removal', 'submitted', NULL, now(), now());

INSERT INTO tipper_daily_log
  (date, vehicle_id, work_order_id, driver_id,
   opening_hrs, closing_hrs, total_hrs,
   opening_km, closing_km, total_km,
   diesel_ltr, avg_km_per_ltr, trip_count,
   work_description, status, submitted_by,
   created_at, updated_at)
VALUES
  (CURRENT_DATE - 3, 3, 2, 3, 1200.0, 1208.0, 8.0,
   45000.0, 45120.0, 120.0, 35.0, 3.43, 8,
   'Material transport', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 2, 3, 2, 3, 1208.0, 1216.0, 8.0,
   45120.0, 45240.0, 120.0, 36.0, 3.33, 8,
   'Material transport', 'submitted', NULL, now(), now()),
  (CURRENT_DATE - 1, 3, 2, 3, 1216.0, 1224.0, 8.0,
   45240.0, 45355.0, 115.0, 34.0, 3.38, 7,
   'Material transport', 'submitted', NULL, now(), now());

-- Vehicle documents: no user FK on this table, so no FK-ordering concern
-- like diesel_receipts/daily logs above.
-- 1 expired (gate pass, overdue 2 days), 2 expiring soon (<=30 days), 3 valid.
INSERT INTO vehicle_documents
  (vehicle_id, document_type, document_no,
   issued_date, expiry_date, created_at, updated_at)
VALUES
  (1, 'insurance', 'INS-JCB-2024',
   '2024-07-01', CURRENT_DATE + 25, now(), now()),
  (1, 'puc', 'PUC-JCB-001',
   '2026-01-01', CURRENT_DATE + 5, now(), now()),
  (2, 'insurance', 'INS-POC-2024',
   '2024-07-01', CURRENT_DATE + 45, now(), now()),
  (2, 'gate_pass', 'GP-POC-2024',
   '2024-01-01', CURRENT_DATE - 2, now(), now()),
  (3, 'insurance', 'INS-TIP-2024',
   '2024-07-01', CURRENT_DATE + 12, now(), now()),
  (3, 'fitness', 'FIT-TIP-2024',
   '2024-01-01', CURRENT_DATE + 60, now(), now());
