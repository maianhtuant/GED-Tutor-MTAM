package com.gedtutor.service;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.PracticeRunState;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.PracticeAnswer;
import com.gedtutor.model.PracticeAttempt;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.PracticeSetItem;
import com.gedtutor.model.User;
import com.gedtutor.repository.PracticeAnswerRepository;
import com.gedtutor.repository.PracticeAttemptRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Coordinates a single student's walk through a practice set. Question
 * state (problems, tokens, per-question results) lives entirely in the
 * user's HTTP session under a per-set key — the service itself is
 * stateless there. What IS persisted is a {@link PracticeAttempt} row per
 * run: created when the run starts, finalized with a score once every
 * question has been answered, so admins can see practice history
 * alongside quiz history.
 *
 * <p>Lifecycle: {@code start} (generates every question once, creates the
 * PracticeAttempt row) → {@code grade} per question (AJAX; finalizes the
 * attempt once {@link PracticeRunState#isDone()} becomes true) → {@code
 * summary}.
 */
@Service
public class PracticeRunService {

    private static final String SESSION_KEY_PREFIX = "practiceRun:";

    private final PracticeSetService setService;
    private final MathProblemService problemService;
    private final MathAnswerChecker checker;
    private final PracticeAttemptRepository attemptRepo;
    private final PracticeAnswerRepository answerRepo;

    public PracticeRunService(PracticeSetService setService,
                              MathProblemService problemService,
                              MathAnswerChecker checker,
                              PracticeAttemptRepository attemptRepo,
                              PracticeAnswerRepository answerRepo) {
        this.setService = setService;
        this.problemService = problemService;
        this.checker = checker;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
    }

    /**
     * Begin a fresh run, replacing any previous state for this set.
     * Generates every question for the entire set up front so the UI
     * can render them all on one page, and creates the PracticeAttempt
     * row that will hold the score once the run is complete.
     */
    @Transactional
    public PracticeRunState start(HttpSession session, Long setId, User student) {
        PracticeSet set = setService.findById(setId);
        PracticeRunState state = new PracticeRunState();
        state.practiceSetId = setId;
        for (PracticeSetItem item : set.getItems()) {
            MathProblemTemplate template = item.getTemplate();
            int n = Math.max(0, item.getQuestionCount());
            for (int i = 0; i < n; i++) {
                GeneratedMathProblem problem = problemService.generate(template);
                state.problems.add(problem);
                state.tokens.add(UUID.randomUUID().toString());
            }
        }

        if (!state.problems.isEmpty()) {
            PracticeAttempt attempt = new PracticeAttempt();
            attempt.setStudent(student);
            attempt.setPracticeSet(set);
            attempt.setTotalQuestions(state.problems.size());
            PracticeAttempt saved = attemptRepo.save(attempt);
            state.attemptId = saved.getId();
        }

        store(session, state);
        return state;
    }

    public PracticeRunState load(HttpSession session, Long setId) {
        Object stashed = session.getAttribute(SESSION_KEY_PREFIX + setId);
        return (stashed instanceof PracticeRunState s) ? s : null;
    }

    public void clear(HttpSession session, Long setId) {
        session.removeAttribute(SESSION_KEY_PREFIX + setId);
    }

    /**
     * Grade one question identified by its zero-based index. Returns
     * {@code null} if the index/token don't match anything (e.g. the
     * session expired between load and submit). Once the run is fully
     * answered, saves the final score onto the backing PracticeAttempt.
     */
    @Transactional
    public MathAnswerResult gradeQuestion(HttpSession session, Long setId,
                                          int questionIndex, String token, String answer) {
        PracticeRunState state = load(session, setId);
        if (state == null) return null;
        if (questionIndex < 0 || questionIndex >= state.problems.size()) return null;
        if (!token.equals(state.tokens.get(questionIndex))) return null;
        // Don't re-grade an already-answered question.
        if (state.results.containsKey(questionIndex)) {
            return state.results.get(questionIndex);
        }
        GeneratedMathProblem problem = state.problems.get(questionIndex);
        MathAnswerResult result = checker.check(problem, answer);
        state.results.put(questionIndex, result);

        if (state.attemptId != null) {
            attemptRepo.findById(state.attemptId).ifPresent(attempt -> {
                PracticeAnswer record = new PracticeAnswer();
                record.setAttempt(attempt);
                record.setQuestionIndex(questionIndex);
                record.setQuestionText(problem.questionText());
                record.setExpectedAnswer(result.expected());
                record.setStudentAnswer(result.submitted());
                record.setCorrect(result.correct());
                answerRepo.save(record);

                if (state.isDone() && !state.attemptSaved) {
                    attempt.setScore(state.totalCorrect());
                    attempt.setCompletedAt(LocalDateTime.now());
                    attemptRepo.save(attempt);
                    state.attemptSaved = true;
                }
            });
        }

        store(session, state);
        return result;
    }

    private void store(HttpSession session, PracticeRunState state) {
        session.setAttribute(SESSION_KEY_PREFIX + state.practiceSetId, state);
    }
}
