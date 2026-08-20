package com.gedtutor.service;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.MathAnswerResult;
import com.gedtutor.dto.Rational;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Grades free-text student input against a {@link GeneratedMathProblem}.
 *
 * <p>Accepted formats by shape:
 * <ul>
 *   <li>SCALAR: "5", "-3", "2.5", "5/2", "-10/3"</li>
 *   <li>UNORDERED: "2, -3" (any order; comma or "and" separator)</li>
 *   <li>ORDERED: "(2, 3)" or "2, 3" (x first, then y)</li>
 * </ul>
 *
 * <p>Problems with a special non-numeric answer (e.g. "no real solution",
 * "undefined") accept any case-insensitive alias from
 * {@link GeneratedMathProblem#specialAliases()}.
 *
 * <p>Numeric matching uses a relative tolerance from the template (default
 * 0.5%). For values near zero we apply an absolute tolerance floor (1e-4)
 * so "0" matches "0.0" without exact equality.
 */
@Service
public class MathAnswerChecker {

    /**
     * Absolute tolerance floor used when a problem doesn't ask for a rounded
     * decimal answer — still needs a tiny epsilon to absorb floating-point noise.
     */
    private static final double EXACT_FLOOR = 1e-6;

    /**
     * Absolute tolerance floor for a rounded numeric answer, derived from the
     * problem's decimal places so a correctly-rounded answer is always accepted
     * (e.g. 2 decimal places → 0.005, so student typing -0.67 for exact answer
     * -2/3 ≈ -0.6667 still matches).
     */
    private static double absoluteFloor(GeneratedMathProblem problem) {
        if (!problem.roundAnswer()) return EXACT_FLOOR;
        return 0.5 * Math.pow(10, -problem.decimalPlaces());
    }

    public MathAnswerResult check(GeneratedMathProblem problem, String rawInput) {
        String submitted = rawInput == null ? "" : rawInput.trim();
        String expectedPretty = prettyExpected(problem);

        if (submitted.isEmpty()) {
            String msg = problem.isMultipleChoice() ? "Please select an answer." : "Please type an answer.";
            return MathAnswerResult.incorrect(msg, expectedPretty, submitted);
        }

        // --- Multiple-choice problems: compare against the option text first,
        // with a numeric fallback so a caller that (for whatever reason) still
        // renders a free-text box instead of the choices still grades right. ---
        if (problem.isMultipleChoice()) {
            return checkMultipleChoice(problem, submitted, expectedPretty);
        }

        String normalized = stripWrappingParens(submitted).toLowerCase(Locale.ROOT);

        // --- Special-answer problems (no real solution, undefined, etc.) ---
        if (problem.hasSpecialAnswer()) {
            for (String alias : problem.specialAliases()) {
                if (normalized.equals(alias.toLowerCase(Locale.ROOT))) {
                    return MathAnswerResult.correct(expectedPretty, submitted);
                }
            }
            return MathAnswerResult.incorrect(
                    "Try answering with: " + problem.specialAnswer() + ".",
                    expectedPretty, submitted);
        }

        // --- A student typed a special-looking answer for a numeric problem ---
        if (looksLikeSpecialToken(normalized)) {
            return MathAnswerResult.incorrect(
                    "This problem has a numeric answer — try again.",
                    expectedPretty, submitted);
        }

        // --- Parse numbers ---
        List<Double> parsed;
        try {
            parsed = parseNumbers(submitted);
        } catch (IllegalArgumentException ex) {
            return MathAnswerResult.incorrect(
                    "Couldn't read that as a number. Use formats like 5, -3, 2.5, or 5/2.",
                    expectedPretty, submitted);
        }

        int needed = problem.expectedAnswerCount();
        if (needed == 0) {
            // Template has no answer formula — cannot grade automatically.
            return MathAnswerResult.incorrect(
                    "This problem isn't configured for auto-grading yet.",
                    "(no answer key)", submitted);
        }
        if (parsed.size() != needed) {
            return MathAnswerResult.incorrect(
                    countHint(problem.shape(), needed),
                    expectedPretty, submitted);
        }

        // --- Match by shape ---
        List<Double> expected = new ArrayList<>();
        for (Rational r : problem.expectedAnswers()) expected.add(r.toDouble());

        double tol = problem.tolerancePercent() / 100.0;
        double absoluteFloor = absoluteFloor(problem);
        boolean ok = switch (problem.shape()) {
            case SCALAR    -> within(parsed.get(0), expected.get(0), tol, absoluteFloor);
            case ORDERED   -> matchOrdered(parsed, expected, tol, absoluteFloor);
            case UNORDERED -> matchUnordered(parsed, expected, tol, absoluteFloor);
        };

        return ok
                ? MathAnswerResult.correct(expectedPretty, submitted)
                : MathAnswerResult.incorrect("Not quite — check your work.", expectedPretty, submitted);
    }

    // --- helpers ---

    /**
     * Grade a multiple-choice submission. {@code submitted} is expected to be
     * the exact text of the option the student clicked (that's what the
     * radio-button UI posts back). Falls back to numeric comparison so a
     * page that renders this problem with a plain text box instead of the
     * choices — e.g. an older widget that hasn't been updated — still
     * grades a correct numeric answer as correct.
     */
    private MathAnswerResult checkMultipleChoice(GeneratedMathProblem problem, String submitted,
                                                 String expectedPretty) {
        String correctText = displayRational(problem.expectedAnswers().get(0),
                problem.decimalPlaces(), problem.roundAnswer());
        if (submitted.equals(correctText)) {
            return MathAnswerResult.correct(expectedPretty, submitted);
        }
        try {
            List<Double> parsed = parseNumbers(submitted);
            if (parsed.size() == 1) {
                double tol = problem.tolerancePercent() / 100.0;
                double floor = absoluteFloor(problem);
                if (within(parsed.get(0), problem.expectedAnswers().get(0).toDouble(), tol, floor)) {
                    return MathAnswerResult.correct(expectedPretty, submitted);
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Not a parseable number either — just wasn't the right choice.
        }
        return MathAnswerResult.incorrect("Not quite — check your work.", expectedPretty, submitted);
    }

    static String prettyExpected(GeneratedMathProblem p) {
        if (p.hasSpecialAnswer()) return p.specialAnswer();
        if (p.expectedAnswers().isEmpty()) return "(empty)";
        int decimalPlaces = p.decimalPlaces();
        boolean roundAnswer = p.roundAnswer();
        if (p.shape() == AnswerShape.ORDERED && p.expectedAnswers().size() == 2) {
            return "(" + displayRational(p.expectedAnswers().get(0), decimalPlaces, roundAnswer)
                    + ", " + displayRational(p.expectedAnswers().get(1), decimalPlaces, roundAnswer) + ")";
        }
        if (p.expectedAnswers().size() == 1) {
            return displayRational(p.expectedAnswers().get(0), decimalPlaces, roundAnswer);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.expectedAnswers().size(); i++) {
            if (i > 0) sb.append(p.shape() == AnswerShape.UNORDERED ? " or " : ", ");
            sb.append(displayRational(p.expectedAnswers().get(i), decimalPlaces, roundAnswer));
        }
        return sb.toString();
    }

    /**
     * Display a rational answer in a student-friendly way.
     * Integers show as plain numbers. Fractions show as "n/d ≈ 0.67" when
     * {@code roundAnswer} is true (rounded to {@code decimalPlaces}), or as the
     * bare exact fraction when false. Irrational approximations (denominator
     * from {@code Rational.ofDouble}) always show a rounded decimal since
     * there's no clean fraction to fall back to.
     */
    public static String displayRational(Rational r, int decimalPlaces, boolean roundAnswer) {
        if (r.isInteger()) return Long.toString(r.numerator());
        if (r.denominator() > 1000) return formatDecimal(r.toDouble(), decimalPlaces);
        if (!roundAnswer) return r.toString();
        return r + " ≈ " + formatDecimal(r.toDouble(), decimalPlaces);
    }

    private static String formatDecimal(double value, int decimalPlaces) {
        return String.format("%." + Math.max(0, decimalPlaces) + "f", value);
    }

    private static String countHint(AnswerShape shape, int needed) {
        if (needed == 1) return "Enter one value.";
        if (shape == AnswerShape.ORDERED && needed == 2) {
            return "Enter both values in order: x, y (e.g. 2, 3).";
        }
        return "Enter " + needed + " values, separated by commas.";
    }

    private static boolean looksLikeSpecialToken(String s) {
        return switch (s) {
            case "no solution", "no real solution", "no real solutions",
                 "no real roots", "none", "undefined",
                 "infinite solutions", "infinitely many solutions",
                 "all real numbers", "all reals" -> true;
            default -> false;
        };
    }

    static String stripWrappingParens(String s) {
        String t = s.trim();
        if (t.length() >= 2 && t.charAt(0) == '(' && t.charAt(t.length() - 1) == ')') {
            return t.substring(1, t.length() - 1).trim();
        }
        return t;
    }

    static List<Double> parseNumbers(String input) {
        String prepared = stripWrappingParens(input).replaceAll("(?i)\\band\\b", ",");
        String[] parts = prepared.split(",");
        List<Double> out = new ArrayList<>();
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) continue;
            // Strip "x =", "y =" etc. prefixes — common in homework writeups.
            int eq = token.indexOf('=');
            if (eq >= 0) token = token.substring(eq + 1).trim();
            out.add(parseOneNumber(token));
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("no numbers in '" + input + "'");
        }
        return out;
    }

    static double parseOneNumber(String token) {
        if (token.contains("/")) {
            String[] parts = token.split("/", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("bad fraction: " + token);
            }
            double num = Double.parseDouble(parts[0].trim());
            double den = Double.parseDouble(parts[1].trim());
            if (den == 0.0) throw new IllegalArgumentException("zero denominator: " + token);
            return num / den;
        }
        return Double.parseDouble(token);
    }

    private static boolean within(double v, double exp, double relTol, double absoluteFloor) {
        double allowed = Math.max(Math.abs(exp) * relTol, absoluteFloor);
        return Math.abs(v - exp) <= allowed;
    }

    private static boolean matchOrdered(List<Double> parsed, List<Double> expected, double relTol,
                                        double absoluteFloor) {
        for (int i = 0; i < parsed.size(); i++) {
            if (!within(parsed.get(i), expected.get(i), relTol, absoluteFloor)) return false;
        }
        return true;
    }

    private static boolean matchUnordered(List<Double> parsed, List<Double> expected, double relTol,
                                          double absoluteFloor) {
        boolean[] used = new boolean[expected.size()];
        for (double v : parsed) {
            int best = -1;
            double bestDelta = Double.POSITIVE_INFINITY;
            for (int i = 0; i < expected.size(); i++) {
                if (used[i]) continue;
                double exp = expected.get(i);
                double delta = Math.abs(v - exp);
                double allowed = Math.max(Math.abs(exp) * relTol, absoluteFloor);
                if (delta <= allowed && delta < bestDelta) {
                    best = i;
                    bestDelta = delta;
                }
            }
            if (best < 0) return false;
            used[best] = true;
        }
        return true;
    }
}
