package com.gedtutor.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session-stored state for a single math-quiz attempt. Mirrors the
 * shape of {@code PracticeRunState} but is tied to a persisted
 * {@code QuizAttempt} (scoring is saved when the attempt completes).
 */
public class MathQuizState implements Serializable {

    public Long attemptId;
    public Long homeworkId;

    /** All generated problems in question order. */
    public List<GeneratedMathProblem> problems = new ArrayList<>();

    /** Token per problem; AJAX submit must echo it back to grade. */
    public List<String> tokens = new ArrayList<>();

    /** Index → grading result for problems already answered. */
    public Map<Integer, MathAnswerResult> results = new HashMap<>();

    public int totalQuestions() { return problems == null ? 0 : problems.size(); }

    public int totalAsked() { return results == null ? 0 : results.size(); }

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
