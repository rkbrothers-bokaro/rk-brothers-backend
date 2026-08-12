-- Reworks the V1 master tables to the refined masters API contract:
-- phone-based login, soft delete everywhere, and revised field sets.
-- Vehicle/operator assignment for day-to-day work now lives on the fleet
-- daily-log tables (V2), not on work_orders itself.

ALTER TABLE users ADD COLUMN phone VARCHAR(15) UNIQUE;

ALTER TABLE vehicles RENAME COLUMN registration_number TO vehicle_no;
ALTER TABLE vehicles RENAME COLUMN vehicle_type TO type;
ALTER TABLE vehicles ADD COLUMN display_name VARCHAR(150);
ALTER TABLE vehicles ADD COLUMN billing_basis VARCHAR(20);
ALTER TABLE vehicles ADD COLUMN assigned_operator_id BIGINT REFERENCES operators(id);
ALTER TABLE vehicles ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE vehicles DROP COLUMN make;
ALTER TABLE vehicles DROP COLUMN model;
ALTER TABLE vehicles DROP COLUMN capacity;
ALTER TABLE vehicles DROP COLUMN ownership_type;
ALTER TABLE vehicles DROP COLUMN purchase_date;

ALTER TABLE operators RENAME COLUMN license_number TO licence_no;
ALTER TABLE operators ADD COLUMN licence_expiry DATE;
ALTER TABLE operators ADD COLUMN category VARCHAR(50);
ALTER TABLE operators ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE operators DROP COLUMN address;
ALTER TABLE operators DROP COLUMN joining_date;
ALTER TABLE operators DROP COLUMN status;

ALTER TABLE parties ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE parties DROP COLUMN party_type;
ALTER TABLE parties DROP COLUMN address;
ALTER TABLE parties DROP COLUMN gst_number;

DROP INDEX IF EXISTS idx_work_orders_vehicle_id;
DROP INDEX IF EXISTS idx_work_orders_operator_id;
ALTER TABLE work_orders RENAME COLUMN work_order_number TO wo_number;
ALTER TABLE work_orders ADD COLUMN site_location VARCHAR(255);
ALTER TABLE work_orders ADD COLUMN billing_basis VARCHAR(20);
ALTER TABLE work_orders ADD COLUMN rate NUMERIC(10,2);
ALTER TABLE work_orders ADD COLUMN unit VARCHAR(20);
ALTER TABLE work_orders ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE work_orders DROP COLUMN vehicle_id;
ALTER TABLE work_orders DROP COLUMN operator_id;
ALTER TABLE work_orders DROP COLUMN status;
