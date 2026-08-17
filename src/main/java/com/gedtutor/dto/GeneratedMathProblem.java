package com.gedtutor.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * One concrete instance of a math problem produced by a generator.
 *
 * <p>For most problems the student types numbers and we compare against
 * {@link #expectedAnswers} under the configured tolerance. Some problems
 * have a special non-numeric answer ("no real solution", "undefined",
 * etc.) — for those, {@link #specialAnswer} holds the canonical token and
 * the student must type one of the {@link #specialAliases}.
 *
 * @param templateId      the template this came from (may be null for the in-memory default)
 * @param questionText    text shown to the student
 * @param shape           SCALAR / UNORDERED / ORDERED — see {@link AnswerShape}
 * @param expectedAnswers numeric answers (empty when {@code specialAnswer} is set)
 * @param specialAnswer   non-numeric canonical answer, e.g. "no real solution" or "undefined"; null otherwise
 * @param specialAliases  case-insensitive accepted strings for {@code specialAnswer}, used by the checker
 * @param tolerancePercent relative tolerance for numeric matching (0.5 means 0.5%)
 * @param videoUrl        optional lesson-video URL
 * @param decimalPlaces   how many decimals to round displayed/graded answers to (default 2)
 * @param roundAnswer     whether a rounded decimal hint is shown alongside exact fractions
 */
public record GeneratedMathProblem(
        Long templateId,
        String questionText,
        AnswerShape shape,
        List<Rational> expectedAnswers,
        String specialAnswer,
        Set<String> specialAliases,
        double tolerancePercent,
        String videoUrl,
        int decimalPlaces,
        boolean roundAnswer
) implements Serializable {

    /** Default rounding used by generators that don't take an explicit template setting. */
    public static final int DEFAULT_DECIMAL_PLACES = 2;

    /** Convenience: scalar numeric answer (default rounding). */
    public static GeneratedMathProblem scalar(Long templateId, String text, Rational answer,
                                              double tolerancePercent, String videoUrl) {
        return scalar(templateId, text, answer, tolerancePercent, videoUrl, DEFAULT_DECIMAL_PLACES, true);
    }

    /** Convenience: scalar numeric answer with explicit rounding settings. */
    public static GeneratedMathProblem scalar(Long templateId, String text, Rational answer,
                                              double tolerancePercent, String videoUrl,
                                              int decimalPlaces, boolean roundAnswer) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.SCALAR,
                List.of(answer), null, Set.of(), tolerancePercent, videoUrl, decimalPlaces, roundAnswer);
    }

    /** Convenience: unordered roots (default rounding). */
    public static GeneratedMathProblem unordered(Long templateId, String text, List<Rational> answers,
                                                 double tolerancePercent, String videoUrl) {
        return unordered(templateId, text, answers, tolerancePercent, videoUrl, DEFAULT_DECIMAL_PLACES, true);
    }

    /** Convenience: unordered roots with explicit rounding settings. */
    public static GeneratedMathProblem unordered(Long templateId, String text, List<Rational> answers,
                                                 double tolerancePercent, String videoUrl,
                                                 int decimalPlaces, boolean roundAnswer) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.UNORDERED,
                answers, null, Set.of(), tolerancePercent, videoUrl, decimalPlaces, roundAnswer);
    }

    /** Convenience: ordered tuple (e.g. system of equations). */
    public static GeneratedMathProblem ordered(Long templateId, String text, List<Rational> answers,
                                               double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.ORDERED,
                answers, null, Set.of(), tolerancePercent, videoUrl, DEFAULT_DECIMAL_PLACES, true);
    }

    /** Convenience: special answer (no solution / undefined / infinite), default rounding. */
    public static GeneratedMathProblem special(Long templateId, String text, String special,
                                               Set<String> aliases, double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.SCALAR,
                List.of(), special, aliases, tolerancePercent, videoUrl, DEFAULT_DECIMAL_PLACES, true);
    }

    /** True when the canonical answer is a special non-numeric token. */
    public boolean hasSpecialAnswer() {
        return specialAnswer != null;
    }

    /** Number of distinct numeric values the student must provide (0 for special). */
    public int expectedAnswerCount() {
        return hasSpecialAnswer() ? 0 : expectedAnswers.size();
    }
}
