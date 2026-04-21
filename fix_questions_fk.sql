-- Fix: Drop the old homework_id FK and column from questions table.
-- The questions table previously had homework_id as a direct FK.
-- Now questions are linked to homework via the homework_questions join table.
-- This old column/constraint blocks homework deletion.

-- Drop FK constraint if it exists (name may vary, so try all common names)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_questions_homework') THEN
        ALTER TABLE questions DROP CONSTRAINT fk_questions_homework;
    END IF;
END $$;

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'questions_homework_id_fkey') THEN
        ALTER TABLE questions DROP CONSTRAINT questions_homework_id_fkey;
    END IF;
END $$;

-- Drop any other FK on questions.homework_id by scanning pg_constraint
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'questions'::regclass
          AND contype = 'f'
          AND conname LIKE '%homework%'
    LOOP
        EXECUTE format('ALTER TABLE questions DROP CONSTRAINT %I', r.conname);
    END LOOP;
END $$;

-- Now drop the column itself
ALTER TABLE questions DROP COLUMN IF EXISTS homework_id;

SELECT 'Fix applied: homework_id removed from questions table' AS status;

-- Verify no FK constraints remain on questions pointing to homework
SELECT conname, contype
FROM pg_constraint
WHERE conrelid = 'questions'::regclass AND contype = 'f';
