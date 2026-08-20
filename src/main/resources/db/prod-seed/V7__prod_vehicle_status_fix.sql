-- GET /api/v1/fleet/dashboard/summary and /api/v1/fleet/diesel/anomalies
-- were returning 500 with "No enum constant VehicleStatus.working" once
-- GlobalExceptionHandler started logging the real cause. Vehicle.status is
-- @Enumerated(EnumType.STRING), matched case-sensitively via
-- Enum.valueOf() against WORKING/IDLE/BREAKDOWN/INACTIVE -- the enum's
-- lowercase @JsonValue mapping only applies to the JSON API, never to the
-- raw DB column. Some vehicle rows were inserted directly (outside the
-- app, outside Flyway) with lowercase status values, e.g. 'working'
-- instead of 'WORKING' -- the same class of problem as the manually
-- inserted admin user in V6, just on a different table/column.
--
-- This is a data-correction migration, not a schema/mapping change: the
-- app's own convention (User.role, this same VehicleStatus column when
-- written correctly) already stores enum names uppercase and works fine.
-- Uppercasing here restores that convention rather than adding a
-- converter to accommodate the bad data.
--
-- UPPER() rather than enumerating specific old values so this also
-- catches any other case variant (Working, WORKING already fine, etc.),
-- not just the one value that happened to surface in logs. Idempotent:
-- the WHERE clause makes re-runs a no-op once corrected.
UPDATE vehicles
SET status = UPPER(status)
WHERE status <> UPPER(status);
