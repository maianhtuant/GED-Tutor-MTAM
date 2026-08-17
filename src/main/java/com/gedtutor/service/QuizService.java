package com.gedtutor.service;

import com.gedtutor.dto.QuestionForm;
import com.gedtutor.model.*;
import com.gedtutor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);
    // Unlimited attempts. Set this to a positive number if you want a per-student cap.
    private static final int MAX_ATTEMPTS = Integer.MAX_VALUE;

    private final QuestionRepository questionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAnswerRepository answerRepo;
    private final HomeworkRepository homeworkRepo;
    private final SubjectRepository subjectRepo;

    public QuizService(QuestionRepository questionRepo,
                       QuizAttemptRepository attemptRepo,
                       QuizAnswerRepository answerRepo,
                       HomeworkRepository homeworkRepo,
                       SubjectRepository subjectRepo) {
        this.questionRepo = questionRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.homeworkRepo = homeworkRepo;
        this.subjectRepo = subjectRepo;
    }

    // ── Questions ──────────────────────────────────────────────

    public List<Question> getQuestions(Homework hw) {
        return questionRepo.findByHomeworkWithChoices(hw);
    }

    /** Every question in the system, with its subject pre-loaded. Used by
     *  the global /admin/questions list page. */
    public List<Question> listAllQuestions() {
        return questionRepo.findAllWithSubject();
    }

    /** All questions in the bank for a subject. Used by HomeworkService to
     *  randomly pick N questions when a homework with poolSize is saved. */
    public List<Question> getQuestionsBySubject(Subject subject) {
        return questionRepo.findBySubject(subject);
    }

    public Question findQuestionById(Long id) {
        return questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    /**
     * Fetch a question with its choices + subject eagerly loaded, so the
     * admin edit form can iterate them after the transaction closes
     * (open-in-view=off).
     */
    public Question findQuestionByIdWithChoices(Long id) {
        return questionRepo.findByIdWithChoices(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    @Transactional
    public void saveQuestion(Long subjectId, QuestionForm form) {
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Question q;
        if (form.getId() != null) {
            q = findQuestionById(form.getId());
            q.getChoices().clear();
        } else {
            q = new Question();
            q.setOrderIndex(form.getOrderIndex() > 0 ? form.getOrderIndex() : 0);
        }

        q.setSubject(subject);
        q.setType(form.getType());
        q.setQuestionText(form.getQuestionText());
        String correctAnswer = form.getCorrectAnswer();
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("Correct answer is required.");
        }
        q.setCorrectAnswer(correctAnswer.trim());
        q.setExplanation(form.getExplanation());

        if (form.getType() == QuestionType.MULTIPLE_CHOICE) {
            List<String> choiceTexts = form.getChoices();
            for (int i = 0; i < choiceTexts.size(); i++) {
                String text = choiceTexts.get(i);
                if (text != null && !text.isBlank()) {
                    QuestionChoice c = new QuestionChoice(q, text.trim(), i);
                    q.getChoices().add(c);
                }
            }
        }

        questionRepo.save(q);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepo.deleteById(id);
    }

    /**
     * Inline-update just the question text and correct answer for a bank
     * question — used by the per-row save on the /admin/questions table.
     * Leaves subject, type, choices, and explanation untouched.
     */
    @Transactional
    public Question updateQuestionInline(Long id, String questionText, String correctAnswer) {
        Question q = findQuestionById(id);
        if (questionText != null && !questionText.isBlank()) {
            q.setQuestionText(questionText.trim());
        }
        if (correctAnswer != null && !correctAnswer.isBlank()) {
            q.setCorrectAnswer(correctAnswer.trim());
        }
        return questionRepo.save(q);
    }

    // ── Quiz Attempts ──────────────────────────────────────────

    public boolean canAttempt(User student, Homework hw) {
        long count = attemptRepo.countByStudentAndHomework(student, hw);
        return count < MAX_ATTEMPTS;
    }

    public int getAttemptCount(User student, Homework hw) {
        return (int) attemptRepo.countByStudentAndHomework(student, hw);
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public List<QuizAttempt> getAttempts(User student, Homework hw) {
        return attemptRepo.findByStudentAndHomeworkOrderByAttemptNumberDesc(student, hw);
    }

    @Transactional
    public QuizAttempt startAttempt(User student, Homework hw) {
        if (!canAttempt(student, hw)) {
            throw new IllegalStateException("Maximum attempts reached");
        }
        long count = attemptRepo.countByStudentAndHomework(student, hw);

        // Randomly sample from the homework's question pool if the admin
        // configured a smaller-than-pool cap; otherwise use all.
        List<Question> pool = questionRepo.findByHomeworkWithChoices(hw);
        Integer limit = hw.getQuestionsPerAttempt();
        List<Question> selected;
        if (limit != null && limit > 0 && limit < pool.size()) {
            List<Question> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled);
            selected = shuffled.subList(0, limit);
        } else {
            selected = pool;
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudent(student);
        attempt.setHomework(hw);
        attempt.setAttemptNumber((int) count + 1);
        attempt.setTotalQuestions(selected.size());
        attempt.setQuestionIds(selected.stream()
                .map(Question::getId)
                .collect(Collectors.toList()));
        return attemptRepo.save(attempt);
    }

    /**
     * Returns the questions for a given attempt, in the order they were
     * selected at startAttempt. Falls back to the homework's full pool if
     * the attempt wasn't given a specific subset (older attempts).
     *
     * Uses a join-fetch query so the Question.choices collection is fully
     * initialized before the transaction closes (open-in-view=false).
     */
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForAttempt(QuizAttempt attempt) {
        List<Long> ids = attempt.getQuestionIds();
        if (ids == null || ids.isEmpty()) {
            return getQuestions(attempt.getHomework());
        }
        // Fetch in one query, then reorder to match the stored sequence.
        List<Question> fetched = questionRepo.findByIdInWithChoices(ids);
        java.util.Map<Long, Question> byId = new java.util.HashMap<>();
        for (Question q : fetched) byId.put(q.getId(), q);
        List<Question> ordered = new ArrayList<>(ids.size());
        for (Long qid : ids) {
            Question q = byId.get(qid);
            if (q != null) ordered.add(q);
        }
        return ordered;
    }

    public QuizAttempt findAttemptById(Long id) {
        return attemptRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + id));
    }

    /**
     * Returns true when the given attempt belongs to a math-quiz homework.
     * Runs inside a transaction so the lazy {@code QuizAttempt → Homework}
     * proxy can be initialized — callers in unmanaged scopes (e.g. the
     * controller layer with {@code spring.jpa.open-in-view=false}) must use
     * this helper instead of touching {@code attempt.getHomework()} directly.
     */
    @Transactional(readOnly = true)
    public boolean isMathAttempt(Long attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        return attempt.getHomework() != null && attempt.getHomework().isMathQuiz();
    }

    @Transactional
    public boolean submitAnswer(Long attemptId, Long questionId, String studentAnswer) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        Question question = findQuestionById(questionId);

        boolean correct = checkAnswer(question, studentAnswer);

        QuizAnswer answer = new QuizAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setStudentAnswer(studentAnswer != null ? studentAnswer.trim() : "");
        answer.setCorrect(correct);
        answerRepo.save(answer);

        return correct;
    }

    @Transactional
    public QuizAttempt completeAttempt(Long attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        List<QuizAnswer> answers = answerRepo.findByAttemptWithQuestion(attempt);
        long correctCount = answers.stream().filter(QuizAnswer::isCorrect).count();
        attempt.setScore((int) correctCount);
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepo.save(attempt);
    }

    private boolean checkAnswer(Question q, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            log.info("[QUIZ] qid={} REJECT: blank student answer", q.getId());
            return false;
        }
        if (q.getCorrectAnswer() == null || q.getCorrectAnswer().isBlank()) {
            log.info("[QUIZ] qid={} REJECT: blank correctAnswer in DB", q.getId());
            return false;
        }
        String correct = q.getCorrectAnswer().trim().toLowerCase();
        String given = studentAnswer.trim().toLowerCase();
        if (correct.isEmpty() || given.isEmpty()) {
            log.info("[QUIZ] qid={} REJECT: empty after trim", q.getId());
            return false;
        }

        boolean result = switch (q.getType()) {
            case MULTIPLE_CHOICE -> correct.equals(given);
            case TRUE_FALSE -> correct.equals(given);
            case FILL_BLANK -> matchesFillBlank(correct, given);
        };

        // Log as single-quoted strings with lengths so hidden whitespace / unicode is visible.
        log.info("[QUIZ] qid={} type={} correct='{}' (len={}) given='{}' (len={}) → {}",
                q.getId(), q.getType(), correct, correct.length(),
                given, given.length(), result ? "MATCH" : "NO MATCH");
        return result;
    }

    /**
     * Fill-in-the-blank matching: exact match always counts. Beyond that,
     * the rule depends on whether the answer is numeric or text, because a
     * single "contains" rule can't safely serve both:
     *   - Numeric answers ("2", "12") are compared as numbers, so "2.0"
     *     still matches "2" — but a student typing "123" is NOT a match for
     *     "2" just because "2" happens to appear as a substring of "123".
     *     (That exact bug is what let "123" get marked correct for both
     *     "x = 2" and "x = 12".)
     *   - Text answers ("Lincoln" / "Abraham Lincoln") still get partial
     *     credit, but only on whole-word boundaries — "art" no longer
     *     matches inside "cart".
     */
    private boolean matchesFillBlank(String correct, String given) {
        if (correct.equals(given)) return true;

        Double correctNum = tryParseNumber(correct);
        Double givenNum = tryParseNumber(given);
        if (correctNum != null && givenNum != null) {
            return Math.abs(correctNum - givenNum) < 1e-9;
        }
        if (correctNum != null || givenNum != null) {
            // One side looks numeric and the other doesn't — never a match.
            return false;
        }

        return containsWholeWord(correct, given) || containsWholeWord(given, correct);
    }

    private static Double tryParseNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean containsWholeWord(String haystack, String needle) {
        if (needle.isEmpty()) return false;
        return Pattern.compile("\\b" + Pattern.quote(needle) + "\\b").matcher(haystack).find();
    }
}
