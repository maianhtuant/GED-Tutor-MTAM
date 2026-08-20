package com.gedtutor.service.math;

import com.gedtutor.dto.Rational;
import com.gedtutor.service.MathAnswerChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds plausible wrong-answer options for multiple-choice math problems.
 *
 * <p>Distractors are generated from the correct {@link Rational} answer
 * using a handful of "common mistake" patterns a student might actually
 * make — sign flip, off-by-a-little arithmetic slip, doubling/halving, and
 * numerator/denominator swap — instead of pure random numbers, so the wrong
 * choices don't stick out as obviously wrong. If those patterns don't yield
 * enough distinct options (e.g. the correct answer is 0, or several
 * collide after rounding), random offsets fill in the rest.
 */
public final class MultipleChoiceSupport {

    private MultipleChoiceSupport() {}

    /**
     * Build {@code totalChoices} answer-option strings — already formatted
     * the same way {@link MathAnswerChecker} displays the expected answer —
     * including the correct one, shuffled into random order.
     */
    public static List<String> buildChoices(Rational correct, int decimalPlaces,
                                            boolean roundAnswer, int totalChoices) {
        String correctText = MathAnswerChecker.displayRational(correct, decimalPlaces, roundAnswer);

        Set<String> usedText = new LinkedHashSet<>();
        usedText.add(correctText);
        List<Rational> distractors = new ArrayList<>();

        for (Rational candidate : candidateDistractors(correct)) {
            if (distractors.size() >= totalChoices - 1) break;
            String text = safeDisplay(candidate, decimalPlaces, roundAnswer);
            if (text == null || !usedText.add(text)) continue;
            distractors.add(candidate);
        }

        // Fall back to random offsets if the pattern-based candidates weren't
        // enough — guarded so a pathological case (e.g. correct == 0) can't
        // loop forever.
        int guard = 0;
        while (distractors.size() < totalChoices - 1 && guard++ < 200) {
            int magnitude = 1 + (guard % 15);
            int sign = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
            Rational candidate = safeAdd(correct, Rational.of((long) sign * magnitude, 1));
            if (candidate == null) continue;
            String text = safeDisplay(candidate, decimalPlaces, roundAnswer);
            if (text == null || !usedText.add(text)) continue;
            distractors.add(candidate);
        }

        List<String> options = new ArrayList<>();
        options.add(correctText);
        for (Rational d : distractors) {
            options.add(safeDisplay(d, decimalPlaces, roundAnswer));
        }
        Collections.shuffle(options);
        return options;
    }

    private static List<Rational> candidateDistractors(Rational c) {
        List<Rational> out = new ArrayList<>();
        addSafe(out, safeNegate(c));
        addSafe(out, safeAdd(c, Rational.of(1, 1)));
        addSafe(out, safeAdd(c, Rational.of(-1, 1)));
        addSafe(out, safeAdd(c, Rational.of(2, 1)));
        addSafe(out, safeAdd(c, Rational.of(-2, 1)));
        addSafe(out, safeAdd(c, Rational.of(3, 1)));
        addSafe(out, safeAdd(c, Rational.of(-3, 1)));
        addSafe(out, safeAdd(c, Rational.of(1, 2)));
        addSafe(out, safeAdd(c, Rational.of(-1, 2)));
        addSafe(out, safeMultiply(c, Rational.of(2, 1)));
        addSafe(out, safeMultiply(c, Rational.of(1, 2)));
        if (c.numerator() != 0) {
            addSafe(out, safeReciprocal(c));
        }
        Collections.shuffle(out);
        return out;
    }

    private static void addSafe(List<Rational> list, Rational r) {
        if (r != null) list.add(r);
    }

    private static Rational safeNegate(Rational r) {
        try { return Rational.of(-r.numerator(), r.denominator()); }
        catch (RuntimeException e) { return null; }
    }

    private static Rational safeAdd(Rational a, Rational b) {
        try { return a.add(b); }
        catch (RuntimeException e) { return null; }
    }

    private static Rational safeMultiply(Rational a, Rational b) {
        try { return a.multiply(b); }
        catch (RuntimeException e) { return null; }
    }

    private static Rational safeReciprocal(Rational r) {
        try { return Rational.of(r.denominator(), r.numerator()); }
        catch (RuntimeException e) { return null; }
    }

    private static String safeDisplay(Rational r, int decimalPlaces, boolean roundAnswer) {
        try { return MathAnswerChecker.displayRational(r, decimalPlaces, roundAnswer); }
        catch (RuntimeException e) { return null; }
    }
}
