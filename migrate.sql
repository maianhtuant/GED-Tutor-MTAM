-- GED Tutor - Database Migration
-- Run this in Terminal: psql -U postgres gedtutor
-- Then paste this entire file, or run: psql -U postgres gedtutor -f migrate.sql

-- Step 1: Create subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(500),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Step 2: Seed default subjects
INSERT INTO subjects (name, display_order) VALUES
    ('Math', 1),
    ('Science', 2),
    ('Social Studies', 3),
    ('Language Arts', 4),
    ('Reasoning', 5)
ON CONFLICT (name) DO NOTHING;

-- Step 3: Add subject_id to homework (nullable first so existing rows are OK)
ALTER TABLE homework ADD COLUMN IF NOT EXISTS subject_id BIGINT;

-- Step 4: Assign all existing homework to the first subject (Math)
UPDATE homework SET subject_id = (SELECT id FROM subjects ORDER BY display_order LIMIT 1)
WHERE subject_id IS NULL;

-- Step 5: Add subject_id to videos (nullable first)
ALTER TABLE videos ADD COLUMN IF NOT EXISTS subject_id BIGINT;

-- Step 6: Assign all existing videos to the first subject (Math)
UPDATE videos SET subject_id = (SELECT id FROM subjects ORDER BY display_order LIMIT 1)
WHERE subject_id IS NULL;

-- Step 7: Add FK constraints (safe - skips if already exists)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_homework_subject') THEN
        ALTER TABLE homework ADD CONSTRAINT fk_homework_subject
            FOREIGN KEY (subject_id) REFERENCES subjects(id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_videos_subject') THEN
        ALTER TABLE videos ADD CONSTRAINT fk_videos_subject
            FOREIGN KEY (subject_id) REFERENCES subjects(id);
    END IF;
END $$;

-- Step 8: Make subject_id NOT NULL on homework
ALTER TABLE homework ALTER COLUMN subject_id SET NOT NULL;

-- Verify
SELECT 'Migration complete!' AS status;
SELECT id, name, display_order FROM subjects ORDER BY display_order;
