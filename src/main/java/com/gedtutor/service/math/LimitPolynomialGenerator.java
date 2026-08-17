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
 * Cal 1 — Evaluate lim_{x→c} (ax² + bx + d) by direct substitution.
 *
 * <p>Since the function is a polynomial (continuous everywhere), the limit
 * equals f(c) = a·c² + b·c + d. This problem reinforces the substitution
 * method before students encounter indeterminate forms.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":-5,"aMax":5,"bMin":-5,"bMax":5,"dMin":-10,"dMax":10,"cMin":-5,"cMax":5}}</pre>
 */
@Service
public class LimitPolynomialGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = -5, aMax = 5;
        public int bMin = -5, bMax = 5;
        public int dMin = -10, dMax = 10;
        public int cMin = -5, cMax = 5;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.LIMIT_POLYNOMIAL; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        int d = randomInRange(cfg.dMin, cfg.dMax);
        int c = randomInRange(cfg.cMin, cfg.cMax);

        // f(c) = a·c² + b·c + d  (exact integer)
        long answer = (long) a * c * c + (long) b * c + d;

        String question = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.lim(c)
                + MathLatexSupport.paren(renderPoly(a, b, d));

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }

    /** Render ax² + bx + d as a LaTeX string. */
    static String renderPoly(int a, int b, int d) {
        return MathLatexSupport.quadratic(a, b, d);
    }
}
