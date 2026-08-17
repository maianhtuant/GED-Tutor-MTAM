-- Idempotent migration for the quiz timer feature.
-- Hibernate's ddl-auto=update cannot add a NOT NULL column to an already
-- populated table, so timer_enabled / timer_minutes on `homework` need to
-- be added manually. Safe to run more than once.

ALTER TABLE homework ADD COLUMN IF NOT EXISTS timer_enabled BOOLEAN;
UPDATE homework SET timer_enabled = FALSE WHERE timer_enabled IS NULL;
ALTER TABLE homework ALTER COLUMN timer_enabled SET NOT NULL;
ALTER TABLE homework ALTER COLUMN timer_enabled SET DEFAULT FALSE;

ALTER TABLE homework ADD COLUMN IF NOT EXISTS timer_minutes INTEGER;
UPDATE homework SET timer_minutes = 10 WHERE timer_minutes IS NULL;
ALTER TABLE homework ALTER COLUMN timer_minutes SET DEFAULT 10;
