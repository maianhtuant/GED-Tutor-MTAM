-- =====================================================================
-- Question-bank refactor migration
-- Run this AFTER restarting Spring Boot so Hibernate has auto-created
-- the new `subject_id` column on `questions` and the new
-- `homework_questions` join table.
--
-- What it does:
--   1. Drops NOT NULL on questions.homework_id so future bank questions
--      can exist without being tied to a specific homework.
--   2. Backfills questions.subject_id from each question's old homework.
--   3. Copies the old (homework -> question) relation into the new
--      homework_questions join table so existing homeworks keep their
--      questions after the refactor.
-- =====================================================================

-- 1. Allow null homework_id (old column kept around for back-compat)
ALTER TABLE questions ALTER COLUMN homework_id DROP NOT NULL;

-- 2. Backfill subject_id from each question's former homework
UPDATE questions q
SET subject_id = h.subject_id
FROM homework h
WHERE h.id = q.homework_id
  AND q.subject_id IS NULL;

-- 3. Copy old homework<->question mapping into the new join table
INSERT INTO homework_questions (homework_id, question_id)
SELECT q.homework_id, q.id
FROM questions q
WHERE q.homework_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Optional: once you've confirmed everything works, you can drop the
-- legacy homework_id column entirely:
-- ALTER TABLE questions DROP COLUMN homework_id;

-- =====================================================================
-- Math problem template refactor (Phase 1)
-- Run AFTER restarting Spring Boot so Hibernate has auto-created the
-- new parameters_json column.
--
-- The original schema had quadratic-specific coefficient columns
-- (a_min, a_max, b_min, b_max, c_min, c_max). We now store generator
-- parameters as JSON in parameters_json so the same table supports
-- LINEAR_EQUATION, SYSTEM_OF_EQUATIONS, SLOPE, FUNCTION_EVALUATION,
-- and any future kind without schema changes.
--
-- If your database has any rows in math_problem_templates yet, this
-- migrates them; if it's empty (the common case at this point) the
-- ALTER COLUMN drops are still safe.
-- =====================================================================

-- Backfill: existing rows are all QUADRATIC, so encode their old columns
-- into the new JSON shape before dropping them.
UPDATE math_problem_templates
SET parameters_json = COALESCE(
        parameters_json,
        '{"aMin":' || a_min ||
        ',"aMax":' || a_max ||
        ',"bMin":' || b_min ||
        ',"bMax":' || b_max ||
        ',"cMin":' || c_min ||
        ',"cMax":' || c_max || '}'
    )
WHERE a_min IS NOT NULL;

ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS a_min;
ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS a_max;
ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS b_min;
ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS b_max;
ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS c_min;
ALTER TABLE math_problem_templates DROP COLUMN IF EXISTS c_max;

-- The CHECK constraint on `kind` was created when only QUADRATIC existed
-- in the enum. Hibernate's update mode does not refresh constraints when
-- new enum values are added, so drop it. Validation at the Java layer
-- (@Enumerated(EnumType.STRING)) remains.
ALTER TABLE math_problem_templates DROP CONSTRAINT IF EXISTS math_problem_templates_kind_check;

-- =====================================================================
-- Math-quiz mode on Homework (Phase 2 of math integration)
-- Hibernate's `update` mode won't add a NOT NULL boolean column to a
-- table that already has rows, so add them explicitly with defaults.
-- Existing rows get math_quiz=false, preserving normal quiz behavior.
-- =====================================================================
ALTER TABLE homework ADD COLUMN IF NOT EXISTS math_quiz BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS math_question_count INTEGER;

-- =====================================================================
-- Subject hierarchy (parent/child subjects)
-- Adds a self-referential parent_id FK so subjects can be grouped.
-- Run AFTER restarting Spring Boot so Hibernate has auto-created the
-- parent_id column (ddl-auto=update will add it automatically).
--
-- After restart, run this to set the college-math subjects as children
-- of the existing "Math" subject. Adjust IDs if they differ in your DB.
-- =====================================================================
-- Find Math subject id first:
--   SELECT id FROM subjects WHERE name = 'Math';
-- Then set children (replace 1 with actual Math subject id if different):
-- UPDATE subjects SET parent_id = (SELECT id FROM subjects WHERE name = 'Math')
--   WHERE name IN ('Calculus 1','Calculus 2','Calculus 3','Linear Algebra','Differential Equations');
--
-- The DataSeeder handles this automatically on startup for fresh installs.
-- For existing databases, run the UPDATE above once after deploying this version.
