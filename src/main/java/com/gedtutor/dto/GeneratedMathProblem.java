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
 */
public record GeneratedMathProblem(
        Long templateId,
        String questionText,
        AnswerShape shape,
        List<Rational> expectedAnswers,
        String specialAnswer,
        Set<String> specialAliases,
        double tolerancePercent,
        String videoUrl
) implements Serializable {

    /** Convenience: scalar numeric answer. */
    public static GeneratedMathProblem scalar(Long templateId, String text, Rational answer,
                                              double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.SCALAR,
                List.of(answer), null, Set.of(), tolerancePercent, videoUrl);
    }

    /** Convenience: unordered roots. */
    public static GeneratedMathProblem unordered(Long templateId, String text, List<Rational> answers,
                                                 double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.UNORDERED,
                answers, null, Set.of(), tolerancePercent, videoUrl);
    }

    /** Convenience: ordered tuple (e.g. system of equations). */
    public static GeneratedMathProblem ordered(Long templateId, String text, List<Rational> answers,
                                               double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.ORDERED,
                answers, null, Set.of(), tolerancePercent, videoUrl);
    }

    /** Convenience: special answer (no solution / undefined / infinite). */
    public static GeneratedMathProblem special(Long templateId, String text, String special,
                                               Set<String> aliases, double tolerancePercent, String videoUrl) {
        return new GeneratedMathProblem(templateId, text, AnswerShape.SCALAR,
                List.of(), special, aliases, tolerancePercent, videoUrl);
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
