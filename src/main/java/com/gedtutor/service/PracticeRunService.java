package com.gedtutor.service;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.PracticeRunState;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.model.PracticeSet;
import com.gedtutor.model.PracticeSetItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Coordinates a single student's walk through a practice set. State
 * lives entirely in the user's HTTP session under a per-set key. The
 * service is stateless itself.
 *
 * <p>Lifecycle: {@code start} (generates every question once) → {@code grade}
 * per question (AJAX) → {@code summary} when {@link PracticeRunState#isDone()}.
 */
@Service
public class PracticeRunService {

    private static final String SESSION_KEY_PREFIX = "practiceRun:";

    private final PracticeSetService setService;
    private final MathProblemService problemService;
    private final MathAnswerChecker checker;

    public PracticeRunService(PracticeSetService setService,
                              MathProblemService problemService,
                              MathAnswerChecker checker) {
        this.setService = setService;
        this.problemService = problemService;
        this.checker = checker;
    }

    /**
     * Begin a fresh run, replacing any previous state for this set.
     * Generates every question for the entire set up front so the UI
     * can render them all on one page.
     */
    public PracticeRunState start(HttpSession session, Long setId) {
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
     * session expired between load and submit).
     */
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
        store(session, state);
        return result;
    }

    private void store(HttpSession session, PracticeRunState state) {
        session.setAttribute(SESSION_KEY_PREFIX + state.practiceSetId, state);
    }
}
