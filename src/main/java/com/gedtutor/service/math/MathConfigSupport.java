package com.gedtutor.service.math;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tiny helpers shared by every {@link MathProblemGenerator}: JSON parsing
 * (with sensible defaults), random number drawing inside inclusive
 * ranges, and a non-zero variant for leading coefficients.
 */
public final class MathConfigSupport {
    private MathConfigSupport() {}

    static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Deserialize {@code json} into {@code clazz}. If the JSON is null,
     * empty, or unparseable, returns a fresh instance of {@code clazz}
     * via its no-arg constructor (so each config class can supply
     * defaults via field initializers).
     */
    public static <T> T parseOrDefault(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return newInstance(clazz);
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception ex) {
            return newInstance(clazz);
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Config class needs a no-arg constructor: " + clazz, e);
        }
    }

    public static int randomInRange(int min, int max) {
        if (min > max) { int t = min; min = max; max = t; }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int randomNonZeroInRange(int min, int max) {
        if (min == 0 && max == 0) return 1;
        for (int i = 0; i < 16; i++) {
            int v = randomInRange(min, max);
            if (v != 0) return v;
        }
        return min != 0 ? min : max;
    }

    /** Render an integer as a signed term suffix: "+3", "-3" with the leading space. */
    public static String signedTerm(int v, String varSuffix) {
        if (v == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(v > 0 ? " + " : " - ");
        int abs = Math.abs(v);
        if (varSuffix.isEmpty()) {
            sb.append(abs);
        } else if (abs == 1) {
            sb.append(varSuffix);
        } else {
            sb.append(abs).append(varSuffix);
        }
        return sb.toString();
    }
}
