package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 2 — Evaluate ∫_0^1 (ax + b)^n dx by u-substitution.
 *
 * <p>Let u = ax + b.  When x = 0, u = b; when x = 1, u = a + b.
 * du = a dx  →  dx = du/a.
 * So the integral becomes (1/a) · ∫_b^{a+b} u^n du
 *   = (1/a) · [u^{n+1}/(n+1)]_b^{a+b}
 *   = ((a+b)^{n+1} − b^{n+1}) / (a·(n+1))
 *
 * <p>All values are exact rational, so the answer is exact.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":4,"bMin":1,"bMax":4,"nMin":2,"nMax":4}}</pre>
 */
@Service
public class IntegralBySubstitutionGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 4;
        public int bMin = 1, bMax = 4;
        public int nMin = 2, nMax = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.INTEGRAL_BY_SUBSTITUTION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 1.0;

        int a = randomInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        int n = randomInRange(cfg.nMin, cfg.nMax);

        // ((a+b)^{n+1} - b^{n+1}) / (a*(n+1))
        long top = pow(a + b, n + 1) - pow(b, n + 1);
        long bot = (long) a * (n + 1);
        Rational answer = Rational.of(top, bot);

        String inner = MathLatexSupport.linear(a, b);
        String question = MathLatexSupport.label("Evaluate:")
                + MathLatexSupport.defInt(0, 1)
                + MathLatexSupport.paren(inner) + "^{" + n + "}"
                + "\\, dx"
                + "\\quad" + MathLatexSupport.text("(u-sub: u = ") + inner
                + MathLatexSupport.text(")");

        return GeneratedMathProblem.scalar(
                template.getId(), question, answer, tol, template.getVideoUrl());
    }

    static long pow(long base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
