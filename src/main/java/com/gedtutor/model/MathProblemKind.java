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
    SURFACE_AREA,

    // --- Phase 4: custom / free-form ---
    /**
     * A teacher-authored template using {placeholder} tokens, e.g. "{a}x^2 + {b}x + {c} = 0".
     * Each unique placeholder is replaced by a random integer drawn from [minValue, maxValue].
     * The rendered expression is shown to the student (display-only; no auto-grading).
     * parametersJson shape: {"template":"{a}x^2 + {b}x + {c} = 0","minValue":1,"maxValue":20}
     */
    FREE_FORM,

    // =========================================================================
    // --- Calculus 1 ---
    // =========================================================================

    /**
     * Evaluate lim_{x→c} (ax² + bx + d) by direct substitution.
     * parametersJson shape: {"aMin":-5,"aMax":5,"bMin":-5,"bMax":5,"dMin":-10,"dMax":10,"cMin":-5,"cMax":5}
     */
    LIMIT_POLYNOMIAL,

    /**
     * Evaluate lim_{x→c} (ax + b) / (dx + e) — two modes controlled by {@code mode}:
     * <ul>
     *   <li>{@code "SUBSTITUTION"} — denominator ≠ 0 at c; answer is the fraction (ac+b)/(dc+e).</li>
     *   <li>{@code "FACTOR_CANCEL"} — 0/0 indeterminate; numerator factors as (x−c)·(px+q),
     *       denominator is (x−c); after cancellation the limit equals pc+q (integer).</li>
     *   <li>{@code "RANDOM"} — pick mode randomly each call.</li>
     * </ul>
     * parametersJson shape: {"aMin":1,"aMax":6,"bMin":-6,"bMax":6,"dMin":1,"dMax":5,"eMin":-8,"eMax":8,
     *                        "cMin":-4,"cMax":4,"pMin":1,"pMax":5,"qMin":-5,"qMax":5,"mode":"RANDOM"}
     */
    LIMIT_RATIONAL_FUNCTION,

    /**
     * Given f(x) = a·x^n, find f'(c) using the power rule: f'(x) = a·n·x^(n-1).
     * parametersJson shape: {"aMin":1,"aMax":8,"nMin":2,"nMax":5,"cMin":-3,"cMax":3}
     */
    DERIVATIVE_POLY_AT_POINT,

    /**
     * Evaluate ∫_a^b (px + q) dx using the Fundamental Theorem of Calculus.
     * parametersJson shape: {"pMin":1,"pMax":6,"qMin":-6,"qMax":6,"aMin":-4,"aMax":0,"bMin":1,"bMax":4}
     */
    DEFINITE_INTEGRAL_POLY,

    // =========================================================================
    // --- Calculus 2 ---
    // =========================================================================

    /**
     * Sum of a finite geometric series: S = a·(1 - r^n) / (1 - r).
     * parametersJson shape: {"aMin":1,"aMax":10,"rNumeratorMin":1,"rNumeratorMax":3,
     *                        "rDenominatorMin":2,"rDenominatorMax":4,"nMin":3,"nMax":6}
     */
    GEOMETRIC_SERIES_SUM,

    /**
     * Evaluate ∫_0^1 (ax + b)^n dx using u-substitution.
     * parametersJson shape: {"aMin":1,"aMax":4,"bMin":1,"bMax":4,"nMin":2,"nMax":4}
     */
    INTEGRAL_BY_SUBSTITUTION,

    /**
     * Find the nth term of an arithmetic or geometric sequence.
     * parametersJson shape: {"sequenceType":"ARITHMETIC","firstMin":1,"firstMax":10,
     *                        "diffMin":1,"diffMax":8,"nMin":5,"nMax":10}
     */
    SEQUENCE_NTH_TERM,

    // =========================================================================
    // --- Calculus 3 ---
    // =========================================================================

    /**
     * Find ∂f/∂x or ∂f/∂y of f(x,y) = a·x^m·y^n evaluated at (x₀, y₀).
     * parametersJson shape: {"aMin":1,"aMax":5,"mMin":1,"mMax":3,"nMin":1,"nMax":3,
     *                        "x0Min":1,"x0Max":3,"y0Min":1,"y0Max":3,"withRespectTo":"x"}
     */
    PARTIAL_DERIVATIVE_AT_POINT,

    /**
     * Find ∂²f/∂x² of f(x) = a·x^n evaluated at x = x₀ (second derivative, Cal3 review).
     * parametersJson shape: {"aMin":1,"aMax":6,"nMin":3,"nMax":5,"x0Min":1,"x0Max":4}
     */
    SECOND_PARTIAL_DERIVATIVE,

    /**
     * Evaluate ∬_R (ax + by) dA over rectangle R = [0,p] × [0,q].
     * parametersJson shape: {"aMin":1,"aMax":4,"bMin":1,"bMax":4,"pMin":1,"pMax":4,"qMin":1,"qMax":4}
     */
    DOUBLE_INTEGRAL_RECTANGLE,

    // =========================================================================
    // --- Linear Algebra ---
    // =========================================================================

    /**
     * Compute det([[a,b],[c,d]]) = ad − bc for random integer entries.
     * parametersJson shape: {"min":-5,"max":5}
     */
    DETERMINANT_2X2,

    /**
     * Compute the dot product u · v for two 2D or 3D integer vectors.
     * parametersJson shape: {"min":-5,"max":5,"dimension":3}
     */
    DOT_PRODUCT,

    /**
     * Compute u × v for two 3D integer vectors; student enters all three components.
     * parametersJson shape: {"min":-4,"max":4}
     */
    CROSS_PRODUCT,

    /**
     * Compute a single entry (i, j) of A · B where A and B are random 2×2 integer matrices.
     * parametersJson shape: {"min":-3,"max":3,"rowIndex":0,"colIndex":0}
     */
    MATRIX_MULTIPLY_ENTRY,

    /**
     * Find the eigenvalues of a 2×2 matrix constructed to have two integer eigenvalues.
     * parametersJson shape: {"eigenMin":-5,"eigenMax":5}
     */
    EIGENVALUE_2X2,

    // =========================================================================
    // --- Differential Equations ---
    // =========================================================================

    /**
     * Solve dy/dx = a·x^n with y(0) = C; evaluate y at x = x₀.
     * parametersJson shape: {"aMin":1,"aMax":4,"nMin":1,"nMax":3,"cMin":0,"cMax":5,"x0Min":1,"x0Max":3}
     */
    ODE_SEPARABLE_POLY,

    /**
     * Find the characteristic equation roots for a·y'' + b·y' + c·y = 0.
     * Only generates cases with two distinct real roots for clean grading.
     * parametersJson shape: {"aMin":1,"aMax":3,"rMin":-5,"rMax":5}
     */
    ODE_CHARACTERISTIC_EQUATION,

    /**
     * Solve y' = k·y, y(0) = y₀; find y(1) = y₀·e^k (2% tolerance for irrational answer).
     * parametersJson shape: {"kMin":1,"kMax":3,"y0Min":1,"y0Max":5}
     */
    ODE_EXPONENTIAL_IC
}
