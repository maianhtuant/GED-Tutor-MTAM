-- Adds the answer_mode column (fill-in-the-blank vs multiple-choice) to the
-- existing (populated) math_problem_templates table. Hibernate's
-- ddl-auto=update cannot add a NOT NULL column to a table that already has
-- rows, so this has to be run manually — same reason as
-- fix_homework_timer_columns.sql / fix_math_problem_templates_columns.sql.
--
-- Written to be safe to run even if the app already tried and failed to
-- start once: if Hibernate got as far as adding "answer_mode" as a nullable
-- column before erroring out, the ADD COLUMN below is skipped (IF NOT
-- EXISTS) but the UPDATE + SET NOT NULL below still repair it.
--
-- Run with: psql -U postgres gedtutor -f fix_math_template_answer_mode.sql

ALTER TABLE math_problem_templates ADD COLUMN IF NOT EXISTS answer_mode VARCHAR(20);
UPDATE math_problem_templates SET answer_mode = 'FILL_IN_BLANK' WHERE answer_mode IS NULL;
ALTER TABLE math_problem_templates ALTER COLUMN answer_mode SET NOT NULL;
ALTER TABLE math_problem_templates ALTER COLUMN answer_mode SET DEFAULT 'FILL_IN_BLANK';
