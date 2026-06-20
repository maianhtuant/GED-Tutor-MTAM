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
 * Cal 1 — Evaluate ∫_a^b (px + q) dx by the Fundamental Theorem of Calculus.
 *
 * <p>Antiderivative: F(x) = (p/2)·x² + q·x. Answer = F(b) − F(a), which is
 * always rational. The fractions are kept in reduced form via {@link Rational}.
 *
 * <p>JSON config shape:
 * <pre>{@code {"pMin":1,"pMax":6,"qMin":-6,"qMax":6,"aMin":-4,"aMax":0,"bMin":1,"bMax":4}}</pre>
 *
 * <p>By default {@code a} is non-positive and {@code b} is positive so the
 * interval is non-trivial.
 */
@Service
public class DefiniteIntegralPolyGenerator implements MathProblemGenerator {

    public static final class Config {
        public int pMin = 1, pMax = 6;
        public int qMin = -6, qMax = 6;
        public int aMin = -4, aMax = 0;
        public int bMin = 1,  bMax = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.DEFINITE_INTEGRAL_POLY; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int p = randomNonZeroInRange(cfg.pMin, cfg.pMax);
        int q = randomInRange(cfg.qMin, cfg.qMax);
        int a = randomInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        if (a == b) b = a + 1; // ensure non-degenerate interval

        // F(x) = (p/2)x² + qx  — store numerically as Rational
        // F(b) = p·b²/2 + q·b  → numerator = p·b² + 2·q·b, denominator = 2
        // F(a) = p·a²/2 + q·a  → numerator = p·a² + 2·q·a, denominator = 2
        // Answer = F(b) - F(a) = (p·b² + 2·q·b - p·a² - 2·q·a) / 2
        long numerator = (long) p * b * b + 2L * q * b - (long) p * a * a - 2L * q * a;
        Rational answer = Rational.of(numerator, 2);

        String integrand = MathLatexSupport.linear(p, q);
        String question  = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.defInt(a, b)
                + MathLatexSupport.paren(integrand)
                + "\\, dx";

        return GeneratedMathProblem.scalar(
                template.getId(), question, answer, tol, template.getVideoUrl());
    }

    static String renderIntegrand(int p, int q) {
        StringBuilder sb = new StringBuilder();
        if (p == 1)       sb.append("x");
        else if (p == -1) sb.append("-x");
        else              sb.append(p).append("x");
        sb.append(MathConfigSupport.signedTerm(q, ""));
        return sb.toString();
    }
}
