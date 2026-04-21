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
