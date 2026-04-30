package com.gedtutor.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable session-stored state for a single student walking through a
 * practice set. We generate every question upfront when the run starts,
 * so the run page can render them all at once (mirrors the existing
 * quiz UI) and grade each one independently via AJAX.
 *
 * <p>Stored under a session attribute keyed by practice set id; opening
 * a different set creates a separate state for that one.
 */
public class PracticeRunState implements Serializable {

    public Long practiceSetId;

    /** All generated problems in question order (size == totalQuestions). */
    public List<GeneratedMathProblem> problems = new ArrayList<>();

    /** One token per problem; the AJAX submit must echo this token back to grade. */
    public List<String> tokens = new ArrayList<>();

    /** Index → grading result for problems the student has already answered. */
    public Map<Integer, MathAnswerResult> results = new HashMap<>();

    public int totalQuestions() {
        return problems == null ? 0 : problems.size();
    }

    public int totalAsked() {
        return results == null ? 0 : results.size();
    }

    public int totalCorrect() {
        if (results == null) return 0;
        int n = 0;
        for (MathAnswerResult r : results.values()) if (r.correct()) n++;
        return n;
    }

    public boolean isDone() {
        return totalAsked() >= totalQuestions() && totalQuestions() > 0;
    }
}
