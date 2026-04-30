package com.gedtutor.model;

/**
 * Kinds of randomly-generated math problems supported by the math problem
 * generator. Each kind has a corresponding implementation of
 * {@code MathProblemGenerator} (see {@code com.gedtutor.service.math}).
 *
 * <p>To add a new kind: append it here, write a generator service, give it
 * a stable JSON parameter shape, and Spring will pick it up via the
 * generator registry in {@code MathProblemService}.
 */
public enum MathProblemKind {
    // --- Phase 1: algebra ---
    /** Solve for x in {@code ax^2 + bx + c = 0}. */
    QUADRATIC,

    /** Solve for x in {@code ax + b = c} (or two-sided variant). */
    LINEAR_EQUATION,

    /** 2x2 linear system; find the (x, y) intersection. */
    SYSTEM_OF_EQUATIONS,

    /** Slope between two points, or slope of a line. */
    SLOPE,

    /** Evaluate a given f(x) at a given input. */
    FUNCTION_EVALUATION,

    // --- Phase 2: quantitative ---
    /** Percent calculations (forward, reverse-percent, reverse-base). */
    PERCENTAGE,

    /** Solve a proportion {@code a/b = c/x} for x. */
    PROPORTION,

    /** Mean / median / mode of a random list, or find a missing value given a target mean. */
    MEAN_MEDIAN_MODE,

    /** Pythagorean theorem: find a missing side of a right triangle. */
    PYTHAGOREAN,

    // --- Phase 3: geometry ---
    /** Area or perimeter of a 2D shape (rectangle, triangle, circle, trapezoid). */
    AREA_PERIMETER,

    /** Volume of a 3D shape (rectangular prism, cylinder, cone, sphere). */
    VOLUME,

    /** Surface area of a 3D shape (rectangular prism, cylinder, sphere). */
    SURFACE_AREA
}
