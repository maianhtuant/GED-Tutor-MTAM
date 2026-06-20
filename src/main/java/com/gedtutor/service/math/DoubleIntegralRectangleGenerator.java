package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 3 — Evaluate ∬_R (ax + by) dA over rectangle R = [0, p] × [0, q].
 *
 * <p>Integrating with respect to x first (inner), then y (outer):
 * <pre>
 *   ∫_0^q ∫_0^p (ax + by) dx dy
 *   = ∫_0^q [ (a/2)·x² + bxy ]_0^p dy
 *   = ∫_0^q ( (a/2)·p² + bp·y ) dy
 *   = [ (a/2)·p²·y + (b/2)·p·y² ]_0^q
 *   = (a/2)·p²·q + (b/2)·p·q²
 *   = (p·q/2)·(a·p + b·q)
 * </pre>
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":4,"bMin":1,"bMax":4,"pMin":1,"pMax":4,"qMin":1,"qMax":4}}</pre>
 */
@Service
public class DoubleIntegralRectangleGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 4;
        public int bMin = 1, bMax = 4;
        public int pMin = 1, pMax = 4;
        public int qMin = 1, qMax = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.DOUBLE_INTEGRAL_RECTANGLE; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a = randomInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        int p = randomInRange(cfg.pMin, cfg.pMax);
        int q = randomInRange(cfg.qMin, cfg.qMax);

        // Answer = (p·q / 2)·(a·p + b·q)
        // = p·q·(a·p + b·q) / 2
        long numerator = (long) p * q * ((long) a * p + (long) b * q);
        Rational answer = Rational.of(numerator, 2);

        String integrand = renderIntegrand(a, b);
        String question = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.doubleInt()
                + MathLatexSupport.paren(integrand) + "\\, dA"
                + "\\quad" + MathLatexSupport.text("over ")
                + "R = [0," + p + "] \\times [0," + q + "]";

        return GeneratedMathProblem.scalar(
                template.getId(), question, answer, tol, template.getVideoUrl());
    }

    static String renderIntegrand(int a, int b) {
        return MathLatexSupport.leadTerm(a, "x") + MathLatexSupport.signedTerm(b, "y");
    }
}
