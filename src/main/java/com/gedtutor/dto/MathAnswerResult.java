package com.gedtutor.dto;

import java.io.Serializable;

/**
 * Result of grading a student's submission against a {@link GeneratedMathProblem}.
 */
public record MathAnswerResult(
        boolean correct,
        String message,
        String expected,
        String submitted
) implements Serializable {
    public static MathAnswerResult correct(String expected, String submitted) {
        return new MathAnswerResult(true, "Correct!", expected, submitted);
    }

    public static MathAnswerResult incorrect(String message, String expected, String submitted) {
        return new MathAnswerResult(false, message, expected, submitted);
    }
}
