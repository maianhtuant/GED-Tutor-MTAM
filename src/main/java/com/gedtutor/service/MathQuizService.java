package com.gedtutor.service;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.MathQuizState;
import com.gedtutor.model.Homework;
import com.gedtutor.model.HomeworkMathItem;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.MathQuizAnswer;
import com.gedtutor.model.QuizAttempt;
import com.gedtutor.model.User;
import com.gedtutor.repository.MathProblemTemplateRepository;
import com.gedtutor.repository.MathQuizAnswerRepository;
import com.gedtutor.repository.QuizAttemptRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Coordinates a math-quiz attempt: creating a {@link QuizAttempt} row,
 * generating fresh problems across all active math templates, stashing
 * the canonical answers in the user's HTTP session, and tallying the
 * score on completion.
 *
 * <p>Distribution: {@code mathQuestionCount} is spread evenly across
 * every active template (the +1 remainder lands on the first few
 * shuffled templates), so a 40-question quiz with 14 templates becomes
 * 12 templates × 3 questions + 2 templates × 2 questions, randomized.
 */
@Service
public class MathQuizService {

    private static final String SESSION_KEY_PREFIX = "mathQuizAttempt:";

    private final QuizAttemptRepository attemptRepo;
    private final MathProblemTemplateRepository templateRepo;
    private final MathProblemService problemService;
    private final MathAnswerChecker checker;
    private final MathQuizAnswerRepository answerRepo;

    public MathQuizService(QuizAttemptRepository attemptRepo,
                           MathProblemTemplateRepository templateRepo,
                           MathProblemService problemService,
                           MathAnswerChecker checker,
                           MathQuizAnswerRepository answerRepo) {
        this.attemptRepo = attemptRepo;
        this.templateRepo = templateRepo;
        this.problemService = problemService;
        this.checker = checker;
        this.answerRepo = answerRepo;
    }

    /**
     * Start a new math-quiz attempt: create the QuizAttempt row,
     * generate the problems, stash the state in session.
     */
    @Transactional
    public QuizAttempt startAttempt(User student, Homework hw, HttpSession session) {
        if (!hw.isMathQuiz()) {
            throw new IllegalArgumentException("Homework is not in math-quiz mode: " + hw.getId());
        }

        // Admin picked specific templates + counts (like a practice set's
        // items) → use that recipe exactly instead of the even spread below.
        List<HomeworkMathItem> items = hw.getMathItems();
        boolean useItems = items != null && !items.isEmpty();

        int total;
        if (useItems) {
            total = 0;
            for (HomeworkMathItem it : items) total += Math.max(0, it.getQuestionCount());
        } else {
            total = hw.getMathQuestionCount() != null && hw.getMathQuestionCount() > 0
                    ? hw.getMathQuestionCount() : 40;
        }

        long count = attemptRepo.countByStudentAndHomework(student, hw);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudent(student);
        attempt.setHomework(hw);
        attempt.setAttemptNumber((int) count + 1);
        attempt.setTotalQuestions(total);
        QuizAttempt saved = attemptRepo.save(attempt);

        MathQuizState state = new MathQuizState();
        state.attemptId = saved.getId();
        state.homeworkId = hw.getId();

        if (useItems) {
            for (HomeworkMathItem it : items) {
                for (int q = 0; q < it.getQuestionCount(); q++) {
                    state.problems.add(problemService.generate(it.getTemplate()));
                    state.tokens.add(UUID.randomUUID().toString());
                }
            }
            // Interleave kinds so the student doesn't see the same item's
            // questions clustered together.
            shuffleParallel(state.problems, state.tokens);
            session.setAttribute(SESSION_KEY_PREFIX + saved.getId(), state);
            return saved;
        }

        List<MathProblemTemplate> templates =
                templateRepo.findByActiveTrueOrderByIdAsc();
        if (templates.isEmpty()) {
            // No templates → empty state; the page will say "no templates".
            session.setAttribute(SESSION_KEY_PREFIX + saved.getId(), state);
            return saved;
        }

        // Distribute total across templates as evenly as possible, then
        // generate one problem per slot. Shuffle templates for the +1
        // remainder, then shuffle the assembled list of problems so kinds
        // interleave instead of clustering.
        List<MathProblemTemplate> shuffled = new ArrayList<>(templates);
        Collections.shuffle(shuffled);
        int n = shuffled.size();
        int base = total / n;
        int remainder = total % n;
        for (int i = 0; i < n; i++) {
            int qty = base + (i < remainder ? 1 : 0);
            for (int q = 0; q < qty; q++) {
                state.problems.add(problemService.generate(shuffled.get(i)));
                state.tokens.add(UUID.randomUUID().toString());
            }
        }
        // Interleave kinds so the student doesn't see 5 quadratics in a row.
        shuffleParallel(state.problems, state.tokens);

        session.setAttribute(SESSION_KEY_PREFIX + saved.getId(), state);
        return saved;
    }

    public MathQuizState load(HttpSession session, Long attemptId) {
        Object stashed = session.getAttribute(SESSION_KEY_PREFIX + attemptId);
        return (stashed instanceof MathQuizState s) ? s : null;
    }

    /**
     * Grade one question by index. Returns {@code null} if the index/token
     * don't line up with the stored state. Also persists a
     * {@link MathQuizAnswer} row (question text, expected/given answer,
     * correct flag) so admins can review exactly what the student saw and
     * typed — the generated problem itself only ever lived in session state
     * otherwise.
     */
    @Transactional
    public MathAnswerResult gradeQuestion(HttpSession session, Long attemptId,
                                          int questionIndex, String token, String answer) {
        MathQuizState state = load(session, attemptId);
        if (state == null) return null;
        if (questionIndex < 0 || questionIndex >= state.problems.size()) return null;
        if (!token.equals(state.tokens.get(questionIndex))) return null;
        if (state.results.containsKey(questionIndex)) {
            return state.results.get(questionIndex);
        }
        GeneratedMathProblem problem = state.problems.get(questionIndex);
        MathAnswerResult result = checker.check(problem, answer);
        state.results.put(questionIndex, result);
        session.setAttribute(SESSION_KEY_PREFIX + attemptId, state);

        attemptRepo.findById(attemptId).ifPresent(attempt -> {
            MathQuizAnswer record = new MathQuizAnswer();
            record.setAttempt(attempt);
            record.setQuestionIndex(questionIndex);
            record.setQuestionText(problem.questionText());
            record.setExpectedAnswer(result.expected());
            record.setStudentAnswer(result.submitted());
            record.setCorrect(result.correct());
            answerRepo.save(record);
        });

        return result;
    }

    /**
     * Sum up the session's grading results, save score + completedAt to
     * the QuizAttempt, and return it.
     */
    @Transactional
    public QuizAttempt completeAttempt(HttpSession session, Long attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        MathQuizState state = load(session, attemptId);
        int correct = state == null ? 0 : state.totalCorrect();
        attempt.setScore(correct);
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepo.save(attempt);
    }

    private static <A, B> void shuffleParallel(List<A> a, List<B> b) {
        if (a.size() != b.size()) return;
        java.util.Random rnd = new java.util.Random();
        for (int i = a.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            A ta = a.get(i); a.set(i, a.get(j)); a.set(j, ta);
            B tb = b.get(i); b.set(i, b.get(j)); b.set(j, tb);
        }
    }
}
