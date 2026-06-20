package com.gedtutor.dto;

import java.io.Serializable;

/**
 * Tiny exact rational number used to represent expected math answers
 * without floating-point noise.
 *
 * <p>We use a hand-rolled rational instead of {@link java.math.BigDecimal}
 * because quadratic roots are nearly always either rational (when the
 * discriminant is a perfect square) or irrational (when it isn't); for
 * irrational roots we need the {@code sqrt} value, which is not rational
 * — those are stored separately by the solver via {@code ofDouble}.
 */
public record Rational(long numerator, long denominator) implements Serializable {

    public Rational {
        if (denominator == 0) {
            throw new IllegalArgumentException("denominator must be non-zero");
        }
        // Normalize sign so denominator is positive.
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        // Reduce by GCD so equality checks behave.
        long g = gcd(Math.abs(numerator), denominator);
        numerator = numerator / g;
        denominator = denominator / g;
    }

    public static Rational of(long n, long d) {
        return new Rational(n, d);
    }

    /**
     * Approximate from a double. Used for irrational quadratic roots — we
     * lose exactness here but only need the value for comparison with
     * student input under a tolerance.
     */
    public static Rational ofDouble(double d) {
        // 6 decimal places is plenty for school-level numerics; comparison
        // against student input is by tolerance anyway.
        long denom = 1_000_000L;
        long num = Math.round(d * denom);
        return new Rational(num, denom);
    }

    public double toDouble() {
        return (double) numerator / denominator;
    }

    public boolean isInteger() {
        return denominator == 1;
    }

    @Override
    public String toString() {
        if (isInteger()) return Long.toString(numerator);
        // Large denominators come from ofDouble() — irrational approximations.
        // Display as a rounded decimal (2 d.p.) instead of an unwieldy fraction.
        if (denominator > 1000) {
            double val = toDouble();
            // Format to 2 decimal places, then strip trailing zeros after the dot.
            String s = String.format("%.2f", val);
            return s;
        }
        return numerator + "/" + denominator;
    }

    /** Return this + other as a reduced Rational. */
    public Rational add(Rational other) {
        return Rational.of(
                this.numerator * other.denominator + other.numerator * this.denominator,
                this.denominator * other.denominator);
    }

    /** Return this * other as a reduced Rational. */
    public Rational multiply(Rational other) {
        return Rational.of(this.numerator * other.numerator,
                           this.denominator * other.denominator);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a == 0 ? 1 : a;
    }
}
