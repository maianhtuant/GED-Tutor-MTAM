package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Statistics problems over a small random list of integers. Each call
 * randomly picks one of four flavors:
 * <ul>
 *   <li>{@code MEAN}: arithmetic mean of the list</li>
 *   <li>{@code MEDIAN}: middle value (or average of the two middles)</li>
 *   <li>{@code MODE}: most-frequent value (we generate a list with a unique mode)</li>
 *   <li>{@code FIND_MISSING_FOR_MEAN}: "The mean of N values is M. N-1 of them are X. Find the last."</li>
 * </ul>
 *
 * <p>JSON shape:
 * <pre>{@code {"sizeMin":4,"sizeMax":7,"valueMin":1,"valueMax":20,"flavors":["MEAN","MEDIAN","MODE","FIND_MISSING_FOR_MEAN"]}}</pre>
 *
 * <p>Answer shape: SCALAR (rational).
 */
@Service
public class MeanMedianModeGenerator implements MathProblemGenerator {

    public enum Flavor { MEAN, MEDIAN, MODE, FIND_MISSING_FOR_MEAN }

    public static final class Config {
        public int sizeMin = 4, sizeMax = 7;
        public int valueMin = 1, valueMax = 20;
        public String[] flavors = { "MEAN", "MEDIAN", "MODE", "FIND_MISSING_FOR_MEAN" };
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.MEAN_MEDIAN_MODE; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Flavor f = pickFlavor(cfg);
        return switch (f) {
            case MEAN                  -> mean(template, cfg, tol);
            case MEDIAN                -> median(template, cfg, tol);
            case MODE                  -> mode(template, cfg, tol);
            case FIND_MISSING_FOR_MEAN -> findMissing(template, cfg, tol);
        };
    }

    private GeneratedMathProblem mean(MathProblemTemplate t, Config cfg, double tol) {
        int[] values = randomList(cfg);
        long sum = 0;
        for (int v : values) sum += v;
        Rational answer = Rational.of(sum, values.length);
        String text = "Find the mean of: " + listText(values);
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    private GeneratedMathProblem median(MathProblemTemplate t, Config cfg, double tol) {
        int[] values = randomList(cfg);
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        int n = sorted.length;
        Rational answer;
        if (n % 2 == 1) {
            answer = Rational.of(sorted[n / 2], 1);
        } else {
            answer = Rational.of((long) sorted[n / 2 - 1] + sorted[n / 2], 2);
        }
        String text = "Find the median of: " + listText(values);
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    /** Build a list with a strictly-unique mode so the answer is unambiguous. */
    private GeneratedMathProblem mode(MathProblemTemplate t, Config cfg, double tol) {
        int n = randomInRange(Math.max(4, cfg.sizeMin), Math.max(4, cfg.sizeMax));
        // Pick the mode value, then build a list with that value 3 times and other distinct values once each.
        int modeValue = randomInRange(cfg.valueMin, cfg.valueMax);
        int[] arr = new int[n];
        arr[0] = modeValue;
        arr[1] = modeValue;
        arr[2] = modeValue;
        // Fill the rest with values that aren't equal to mode and don't repeat each other.
        java.util.Set<Integer> used = new java.util.HashSet<>();
        used.add(modeValue);
        int filled = 3;
        int safety = 0;
        while (filled < n && safety++ < 200) {
            int v = randomInRange(cfg.valueMin, cfg.valueMax);
            if (used.add(v)) {
                arr[filled++] = v;
            }
        }
        // If we ran out of distinct values (very narrow range), fall back to filling with mode-1 etc.
        while (filled < n) arr[filled++] = cfg.valueMin - 1; // unused unique slot

        // Shuffle so the question doesn't always start with the mode.
        for (int i = arr.length - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }

        Rational answer = Rational.of(modeValue, 1);
        String text = "Find the mode of: " + listText(arr);
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    /** "The mean of these N values is M. Four are listed; find the missing one." */
    private GeneratedMathProblem findMissing(MathProblemTemplate t, Config cfg, double tol) {
        int n = randomInRange(Math.max(3, cfg.sizeMin), Math.max(3, cfg.sizeMax));
        int[] given = new int[n - 1];
        long givenSum = 0;
        for (int i = 0; i < given.length; i++) {
            given[i] = randomInRange(cfg.valueMin, cfg.valueMax);
            givenSum += given[i];
        }
        // Pick a target mean that produces a sensible missing value within 1.5x value range.
        int target = randomInRange(cfg.valueMin, cfg.valueMax);
        // missing = target * n - givenSum
        long missing = (long) target * n - givenSum;

        Rational answer = Rational.of(missing, 1);
        String text = "The mean of " + n + " numbers is " + target + ". " +
                (n - 1) + " of them are " + listText(given) +
                ". What is the missing value?";
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    // --- helpers ---

    private int[] randomList(Config cfg) {
        int n = randomInRange(Math.max(2, cfg.sizeMin), Math.max(2, cfg.sizeMax));
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = randomInRange(cfg.valueMin, cfg.valueMax);
        }
        return arr;
    }

    private static String listText(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private Flavor pickFlavor(Config cfg) {
        if (cfg.flavors == null || cfg.flavors.length == 0) return Flavor.MEAN;
        String pick = cfg.flavors[ThreadLocalRandom.current().nextInt(cfg.flavors.length)];
        try { return Flavor.valueOf(pick); }
        catch (Exception ex) { return Flavor.MEAN; }
    }

    /** Used in tests: count of each value in arr. */
    static Map<Integer, Integer> frequencies(int[] arr) {
        Map<Integer, Integer> out = new HashMap<>();
        for (int v : arr) out.merge(v, 1, Integer::sum);
        return out;
    }
}
