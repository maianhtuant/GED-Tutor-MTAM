-- ══════════════════════════════════════════════════════════
--  GED TUTOR — 40 Questions (20 Math + 20 Language Arts)
--  Paste this directly into any SQL client / console
-- ══════════════════════════════════════════════════════════

-- ── MATH QUESTIONS ─────────────────────────────────────────

-- Q1 Multiple Choice: Percentage
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'What is 15% of 200?', '30', '15% of 200 = 0.15 × 200 = 30.', 1);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '20', 0),
((SELECT MAX(id) FROM questions), '25', 1),
((SELECT MAX(id) FROM questions), '30', 2),
((SELECT MAX(id) FROM questions), '35', 3);

-- Q2 Multiple Choice: Algebra
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'Solve for x: 3x + 6 = 21', '5', '3x = 15, so x = 5.', 2);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '3', 0),
((SELECT MAX(id) FROM questions), '4', 1),
((SELECT MAX(id) FROM questions), '5', 2),
((SELECT MAX(id) FROM questions), '7', 3);

-- Q3 Multiple Choice: Area
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'A rectangle has a length of 12 cm and a width of 5 cm. What is its area?', '60 cm²', 'Area = length × width = 12 × 5 = 60 cm².', 3);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '34 cm²', 0),
((SELECT MAX(id) FROM questions), '60 cm²', 1),
((SELECT MAX(id) FROM questions), '72 cm²', 2),
((SELECT MAX(id) FROM questions), '17 cm²', 3);

-- Q4 Multiple Choice: Exponents
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'What is the value of 2³ × 4?', '32', '2³ = 8, and 8 × 4 = 32.', 4);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '24', 0),
((SELECT MAX(id) FROM questions), '32', 1),
((SELECT MAX(id) FROM questions), '48', 2),
((SELECT MAX(id) FROM questions), '16', 3);

-- Q5 Multiple Choice: Discount
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'A jacket costs $80 after a 20% discount. What was the original price?', '$100', '$80 is 80% of original. Original = 80 ÷ 0.80 = $100.', 5);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '$96', 0),
((SELECT MAX(id) FROM questions), '$100', 1),
((SELECT MAX(id) FROM questions), '$104', 2),
((SELECT MAX(id) FROM questions), '$110', 3);

-- Q6 Multiple Choice: Linear equation
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'If y = 2x − 3, what is y when x = 5?', '7', 'y = 2(5) − 3 = 10 − 3 = 7.', 6);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '5', 0),
((SELECT MAX(id) FROM questions), '7', 1),
((SELECT MAX(id) FROM questions), '9', 2),
((SELECT MAX(id) FROM questions), '13', 3);

-- Q7 Multiple Choice: Speed
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'A car travels 240 miles in 4 hours. What is its average speed?', '60 mph', 'Speed = Distance ÷ Time = 240 ÷ 4 = 60 mph.', 7);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '40 mph', 0),
((SELECT MAX(id) FROM questions), '50 mph', 1),
((SELECT MAX(id) FROM questions), '60 mph', 2),
((SELECT MAX(id) FROM questions), '80 mph', 3);

-- Q8 Multiple Choice: GCF
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'What is the greatest common factor (GCF) of 24 and 36?', '12', 'Factors of 24 and 36 share 12 as the largest common factor.', 8);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '6', 0),
((SELECT MAX(id) FROM questions), '8', 1),
((SELECT MAX(id) FROM questions), '12', 2),
((SELECT MAX(id) FROM questions), '18', 3);

-- Q9 Multiple Choice: Fraction to decimal
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'Which of the following is equivalent to 3/4?', '0.75', '3 ÷ 4 = 0.75.', 9);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '0.25', 0),
((SELECT MAX(id) FROM questions), '0.50', 1),
((SELECT MAX(id) FROM questions), '0.75', 2),
((SELECT MAX(id) FROM questions), '0.34', 3);

-- Q10 Multiple Choice: Circumference
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'A circle has a radius of 7. What is its circumference? (Use π ≈ 3.14)', '43.96', 'C = 2πr = 2 × 3.14 × 7 = 43.96.', 10);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '21.98', 0),
((SELECT MAX(id) FROM questions), '43.96', 1),
((SELECT MAX(id) FROM questions), '153.86', 2),
((SELECT MAX(id) FROM questions), '49', 3);

-- Q11 Multiple Choice: Volume
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'MULTIPLE_CHOICE', 'What is the volume of a box that is 5 cm × 4 cm × 3 cm?', '60 cm³', 'Volume = 5 × 4 × 3 = 60 cm³.', 11);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), '47 cm³', 0),
((SELECT MAX(id) FROM questions), '60 cm³', 1),
((SELECT MAX(id) FROM questions), '80 cm³', 2),
((SELECT MAX(id) FROM questions), '94 cm³', 3);

-- Q12 True/False: Square root
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'TRUE_FALSE', 'The square root of 144 is 12.', 'true', '12 × 12 = 144, so √144 = 12.', 12);

-- Q13 True/False: Pythagorean theorem
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'TRUE_FALSE', 'A triangle with sides 3, 4, and 5 is a right triangle.', 'true', '3² + 4² = 9 + 16 = 25 = 5². It satisfies the Pythagorean theorem.', 13);

-- Q14 True/False: Negative numbers
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'TRUE_FALSE', 'The product of two negative numbers is always negative.', 'false', 'Negative × Negative = Positive. Example: (−3) × (−4) = 12.', 14);

-- Q15 True/False: Triangle angles
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'TRUE_FALSE', 'The sum of interior angles of a triangle is always 180°.', 'true', 'This is a fundamental theorem of Euclidean geometry.', 15);

-- Q16 True/False: Comparing negatives
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'TRUE_FALSE', '-5 is greater than -3.', 'false', 'On a number line, -5 is to the left of -3, so -5 < -3.', 16);

-- Q17 Fill Blank: Perimeter
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'FILL_BLANK', 'The perimeter of a square with side length 9 cm is ___ cm.', '36', 'Perimeter = 4 × 9 = 36 cm.', 17);

-- Q18 Fill Blank: Median
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'FILL_BLANK', 'What is the median of: 3, 7, 9, 15, 21?', '9', 'The middle value when sorted is 9.', 18);

-- Q19 Fill Blank: Simplify fraction
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'FILL_BLANK', 'Simplify 18/24 to its lowest terms (write as a fraction, e.g. 3/4).', '3/4', 'GCF of 18 and 24 is 6. 18÷6 / 24÷6 = 3/4.', 19);

-- Q20 Fill Blank: Factorial
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Math'), 'FILL_BLANK', 'What is the value of 5! (5 factorial)?', '120', '5! = 5 × 4 × 3 × 2 × 1 = 120.', 20);


-- ── LANGUAGE ARTS / ESL QUESTIONS ──────────────────────────

-- Q21 Multiple Choice: Grammar
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which sentence is grammatically correct?', 'She doesn''t like coffee.', 'Third-person singular uses "doesn''t," not "don''t."', 1);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'She don''t like coffee.', 0),
((SELECT MAX(id) FROM questions), 'She doesn''t like coffee.', 1),
((SELECT MAX(id) FROM questions), 'She not like coffee.', 2),
((SELECT MAX(id) FROM questions), 'She no like coffee.', 3);

-- Q22 Multiple Choice: Irregular verb
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'What is the correct past tense of "go"?', 'went', '"Go" is irregular. Its past tense is "went," not "goed."', 2);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'goed', 0),
((SELECT MAX(id) FROM questions), 'gone', 1),
((SELECT MAX(id) FROM questions), 'went', 2),
((SELECT MAX(id) FROM questions), 'goes', 3);

-- Q23 Multiple Choice: Article
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Choose the correct article: "She is ___ nurse."', 'a', 'Use "a" before consonant sounds. "Nurse" starts with "n," so we say "a nurse."', 3);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'an', 0),
((SELECT MAX(id) FROM questions), 'a', 1),
((SELECT MAX(id) FROM questions), 'the', 2),
((SELECT MAX(id) FROM questions), '(no article)', 3);

-- Q24 Multiple Choice: Synonym
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which word is a synonym for "happy"?', 'joyful', '"Joyful" means feeling great happiness — a direct synonym for happy.', 4);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'sad', 0),
((SELECT MAX(id) FROM questions), 'angry', 1),
((SELECT MAX(id) FROM questions), 'joyful', 2),
((SELECT MAX(id) FROM questions), 'tired', 3);

-- Q25 Multiple Choice: Punctuation
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which sentence uses correct punctuation?', 'I love reading; it helps me learn.', 'A semicolon joins two independent clauses correctly.', 5);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'I love reading it helps me learn.', 0),
((SELECT MAX(id) FROM questions), 'I love reading; it helps me learn.', 1),
((SELECT MAX(id) FROM questions), 'I love reading, it helps me learn.', 2),
((SELECT MAX(id) FROM questions), 'I love reading: it helps me learn?', 3);

-- Q26 Multiple Choice: Passive voice
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which sentence is in the passive voice?', 'The book was written by Mark Twain.', 'Passive voice: subject receives the action. "The book" receives the action.', 6);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'Mark Twain wrote the book.', 0),
((SELECT MAX(id) FROM questions), 'The book was written by Mark Twain.', 1),
((SELECT MAX(id) FROM questions), 'Mark Twain is writing the book.', 2),
((SELECT MAX(id) FROM questions), 'The book will be long.', 3);

-- Q27 Multiple Choice: Comparative
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Choose the correct form: "This test is ___ than the last one."', 'harder', 'Use "-er" to compare two things with one-syllable adjectives like "hard."', 7);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'more hard', 0),
((SELECT MAX(id) FROM questions), 'hardest', 1),
((SELECT MAX(id) FROM questions), 'harder', 2),
((SELECT MAX(id) FROM questions), 'hard', 3);

-- Q28 Multiple Choice: Vocabulary
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'What does "benevolent" mean?', 'Kind and generous', '"Benevolent" comes from Latin meaning "well-wishing" — kind and generous.', 8);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'Cruel and harsh', 0),
((SELECT MAX(id) FROM questions), 'Kind and generous', 1),
((SELECT MAX(id) FROM questions), 'Loud and aggressive', 2),
((SELECT MAX(id) FROM questions), 'Shy and quiet', 3);

-- Q29 Multiple Choice: Complete sentence
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which of the following is a complete sentence?', 'The dog ran across the yard.', 'A complete sentence needs a subject and a verb expressing a complete thought.', 9);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'Running fast.', 0),
((SELECT MAX(id) FROM questions), 'Because it was raining.', 1),
((SELECT MAX(id) FROM questions), 'The dog ran across the yard.', 2),
((SELECT MAX(id) FROM questions), 'When she arrived', 3);

-- Q30 Multiple Choice: Transition word
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'Which transition word shows CONTRAST?', 'however', '"However" introduces a contrasting idea. "Therefore" shows result; "furthermore" shows addition.', 10);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'furthermore', 0),
((SELECT MAX(id) FROM questions), 'therefore', 1),
((SELECT MAX(id) FROM questions), 'however', 2),
((SELECT MAX(id) FROM questions), 'additionally', 3);

-- Q31 Multiple Choice: Parts of speech
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'MULTIPLE_CHOICE', 'In "The old, rusty car sat in the driveway," what type of words are "old" and "rusty"?', 'Adjectives', 'Adjectives modify nouns. "Old" and "rusty" both describe the noun "car."', 11);

INSERT INTO question_choices (question_id, choice_text, order_index) VALUES
((SELECT MAX(id) FROM questions), 'Adverbs', 0),
((SELECT MAX(id) FROM questions), 'Adjectives', 1),
((SELECT MAX(id) FROM questions), 'Nouns', 2),
((SELECT MAX(id) FROM questions), 'Verbs', 3);

-- Q32 True/False: Homophones
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'TRUE_FALSE', '"Their," "there," and "they''re" all have the same meaning.', 'false', '"Their" = possessive; "there" = place; "they''re" = they are. Same sound, different meanings.', 12);

-- Q33 True/False: Noun definition
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'TRUE_FALSE', 'A noun is a word that describes an action.', 'false', 'A verb describes an action. A noun names a person, place, thing, or idea.', 13);

-- Q34 True/False: Adverb
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'TRUE_FALSE', 'An adverb can modify a verb, an adjective, or another adverb.', 'true', 'Adverbs describe how, when, where, or to what extent.', 14);

-- Q35 True/False: Compound sentence
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'TRUE_FALSE', 'A compound sentence contains two or more independent clauses joined by a conjunction.', 'true', 'Example: "I wanted to go, but it was raining." Two clauses joined by "but."', 15);

-- Q36 True/False: Adverb vs adjective
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'TRUE_FALSE', 'The word "quickly" is an adjective.', 'false', '"Quickly" is an adverb — it modifies verbs. Adjectives modify nouns.', 16);

-- Q37 Fill Blank: Already/yet
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'FILL_BLANK', 'Complete the sentence: "I have ___ lived here for 10 years." (already / yet)', 'already', '"Already" is used in affirmative sentences to mean sooner than expected.', 17);

-- Q38 Fill Blank: Irregular plural
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'FILL_BLANK', 'Write the plural form of "child".', 'children', '"Children" is the irregular plural of "child."', 18);

-- Q39 Fill Blank: Subjunctive
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'FILL_BLANK', 'Fill in the blank: "If I ___ (be) rich, I would travel the world."', 'were', 'In conditional (subjunctive) sentences, use "were" for all persons: "If I were…"', 19);

-- Q40 Fill Blank: Object pronoun
INSERT INTO questions (subject_id, type, question_text, correct_answer, explanation, order_index)
VALUES ((SELECT id FROM subjects WHERE name='Language Arts'), 'FILL_BLANK', 'Choose the correct word: "The manager spoke to ___ about the project." (I / me)', 'me', '"Me" is the object pronoun. After a verb or preposition, use "me," not "I."', 20);


-- ── Verify ──────────────────────────────────────────────────
SELECT s.name AS subject, COUNT(q.id) AS total_questions
FROM subjects s
LEFT JOIN questions q ON q.subject_id = s.id
GROUP BY s.name ORDER BY s.name;



ALTER TABLE homework ADD COLUMN IF NOT EXISTS math_quiz BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS math_question_count INTEGER DEFAULT 40;

psql -U postgres -d gedtutor -c "SELECT id, title FROM homework;"
psql -U postgres -d gedtutor -c "SELECT COUNT(*) FROM questions;"
psql -U postgres -d gedtutor -c "SELECT COUNT(*) FROM homework_questions;"
