package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;
import static com.gedtutor.service.math.MathConfigSupport.randomNonZeroInRange;

/**
 * Cal 1 — Limits of rational functions, covering two modes:
 *
 * <p><b>SUBSTITUTION mode</b> — the denominator is non-zero at {@code x = c}, so the limit
 * equals direct substitution and the answer is a (possibly non-integer) fraction:
 * <pre>
 *   lim_{x→c} (ax + b) / (dx + e)  =  (ac + b) / (dc + e)
 * </pre>
 *
 * <p><b>FACTOR_CANCEL mode</b> — a 0/0 indeterminate form that resolves after factoring.
 * We construct the numerator as {@code (x − c)(px + q)} so it factors cleanly, then put
 * {@code (x − c)} in the denominator. After cancellation:
 * <pre>
 *   lim_{x→c} [(x − c)(px + q)] / (x − c)  =  lim_{x→c} (px + q)  =  pc + q
 * </pre>
 * The student must recognize the 0/0 form, factor, and cancel before substituting.
 *
 * <p>JSON config shape:
 * <pre>{@code
 * {"aMin":1,"aMax":6,"bMin":-6,"bMax":6,"dMin":1,"dMax":5,"eMin":-8,"eMax":8,
 *  "cMin":-4,"cMax":4,"pMin":1,"pMax":5,"qMin":-5,"qMax":5,"mode":"RANDOM"}
 * }</pre>
 * {@code mode} accepts {@code "SUBSTITUTION"}, {@code "FACTOR_CANCEL"}, or {@code "RANDOM"}.
 */
@Service
public class LimitRationalFunctionGenerator implements MathProblemGenerator {

    public static final class Config {
        // SUBSTITUTION mode coefficients for (ax + b) / (dx + e)
        public int aMin = 1,  aMax = 6;
        public int bMin = -6, bMax = 6;
        public int dMin = 1,  dMax = 5;
        public int eMin = -8, eMax = 8;
        public int cMin = -4, cMax = 4;

        // FACTOR_CANCEL mode: numerator = (x − c)(px + q)
        public int pMin = 1,  pMax = 5;
        public int qMin = -5, qMax = 5;

        public String mode = "RANDOM"; // "SUBSTITUTION", "FACTOR_CANCEL", or "RANDOM"
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.LIMIT_RATIONAL_FUNCTION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 1.0;

        boolean doFactorCancel = switch (cfg.mode.toUpperCase()) {
            case "FACTOR_CANCEL"  -> true;
            case "SUBSTITUTION"   -> false;
            default               -> randomInRange(0, 1) == 1; // RANDOM
        };

        return doFactorCancel
                ? generateFactorCancel(template, cfg, tol)
                : generateSubstitution(template, cfg, tol);
    }

    // -------------------------------------------------------------------------
    // Mode 1: direct substitution → fractional answer
    // -------------------------------------------------------------------------

    /**
     * lim_{x→c} (ax + b) / (dx + e) where dc + e ≠ 0.
     * Answer = Rational.of(ac + b, dc + e).
     */
    private GeneratedMathProblem generateSubstitution(MathProblemTemplate t, Config cfg, double tol) {
        int a, b, d, e, c;
        int attempts = 0;
        do {
            a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
            b = randomInRange(cfg.bMin, cfg.bMax);
            d = randomNonZeroInRange(cfg.dMin, cfg.dMax);
            e = randomInRange(cfg.eMin, cfg.eMax);
            c = randomInRange(cfg.cMin, cfg.cMax);
            attempts++;
        } while ((long) d * c + e == 0 && attempts < 30); // avoid 0 denominator

        if ((long) d * c + e == 0) {
            // Guaranteed-safe fallback
            a = 2; b = 1; d = 1; e = 3; c = 1; // (2+1)/(1+3) = 3/4
        }

        long num = (long) a * c + b;
        long den = (long) d * c + e;
        Rational answer = Rational.of(num, den);

        String question = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.lim(c)
                + MathLatexSupport.frac(renderLinear(a, b), renderLinear(d, e));

        return GeneratedMathProblem.scalar(t.getId(), question, answer, tol, t.getVideoUrl());
    }

    // -------------------------------------------------------------------------
    // Mode 2: 0/0 factor-and-cancel → integer answer
    // -------------------------------------------------------------------------

    /**
     * lim_{x→c} [(x − c)(px + q)] / (x − c)  =  pc + q.
     * Numerator is expanded as a quadratic before presenting to the student.
     */
    private GeneratedMathProblem generateFactorCancel(MathProblemTemplate t, Config cfg, double tol) {
        int c = randomInRange(cfg.cMin, cfg.cMax);
        int p = randomNonZeroInRange(cfg.pMin, cfg.pMax);
        int q = randomInRange(cfg.qMin, cfg.qMax);

        // Numerator: (x − c)(px + q) = px² + (q − cp)x − cq
        int A = p;
        int B = q - c * p;
        int C = -c * q;

        long answer = (long) p * c + q;

        // e.g. "Evaluate: lim_{x→2} (3x² - 4x - 4) / (x - 2)"
        String question = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.lim(c)
                + MathLatexSupport.frac(renderQuadratic(A, B, C), renderLinear(1, -c))
                + "\\quad" + MathLatexSupport.text("[Hint: factor the numerator first]");

        return GeneratedMathProblem.scalar(
                t.getId(), question, Rational.of(answer, 1), tol, t.getVideoUrl());
    }

    // -------------------------------------------------------------------------
    // Rendering helpers
    // -------------------------------------------------------------------------

    /** Render ax + b as a LaTeX string. */
    static String renderLinear(int a, int b) {
        return MathLatexSupport.linear(a, b);
    }

    /** Render Ax² + Bx + C as a LaTeX string. */
    static String renderQuadratic(int A, int B, int C) {
        return MathLatexSupport.quadratic(A, B, C);
    }
}
