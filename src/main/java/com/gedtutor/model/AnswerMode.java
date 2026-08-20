package com.gedtutor.model;

/**
 * How a student answers a generated math problem.
 *
 * <p>{@link #MULTIPLE_CHOICE} only takes effect for problems that resolve to
 * a single plain numeric answer (shape SCALAR, no special non-numeric
 * answer like "no real solution"). Problems that need 2+ values — quadratic
 * roots, (x, y) pairs, systems of equations — always fall back to
 * {@link #FILL_IN_BLANK} even when the template is set to MULTIPLE_CHOICE,
 * since matching several values to one choice isn't meaningful. See
 * {@code com.gedtutor.service.MathProblemService}.
 */
public enum AnswerMode {
    /** Student types the answer (current/default behavior). */
    FILL_IN_BLANK,

    /** Student picks from auto-generated options, one of which is correct. */
    MULTIPLE_CHOICE
}
