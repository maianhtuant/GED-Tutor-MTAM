-- Fix: math_problem_templates missing NOT NULL columns
--
-- MathProblemTemplate.java added two new @Column(nullable = false) fields:
--   roundAnswer  -> round_answer  BOOLEAN NOT NULL (Java default: true)
--   decimalPlaces -> decimal_places INTEGER NOT NULL (Java default: 2)
--
-- Hibernate's ddl-auto=update tried to run:
--   ALTER TABLE math_problem_templates ADD COLUMN round_answer boolean not null
--   ALTER TABLE math_problem_templates ADD COLUMN decimal_places integer not null
-- Both failed because the table already has rows, and a NOT NULL column
-- added without a DEFAULT has no value to put in those existing rows
-- (see MIGRATION.sql's "Math-quiz mode on Homework" section for the same
-- issue with homework.math_quiz — this is the same fix pattern).
--
-- Run this once, BEFORE starting Spring Boot again:
--   psql -U postgres gedtutor -f fix_math_problem_templates_columns.sql
--
-- After this runs, Hibernate will see both columns already exist and
-- ddl-auto=update will just leave them alone.

ALTER TABLE math_problem_templates
    ADD COLUMN IF NOT EXISTS round_answer BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE math_problem_templates
    ADD COLUMN IF NOT EXISTS decimal_places INTEGER NOT NULL DEFAULT 2;

SELECT 'Fix applied: round_answer and decimal_places added to math_problem_templates' AS status;

-- Verify
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'math_problem_templates'
  AND column_name IN ('round_answer', 'decimal_places');
