package com.gedtutor.dto;

/**
 * Shape of a problem's expected answer. Drives how
 * {@code MathAnswerChecker} parses and matches student input.
 */
public enum AnswerShape {
    /** A single numeric answer (linear equation, slope, function evaluation). */
    SCALAR,

    /** Multiple numeric answers, order does not matter (quadratic roots). */
    UNORDERED,

    /** Multiple numeric answers, order matters (system of equations: x then y). */
    ORDERED
}
