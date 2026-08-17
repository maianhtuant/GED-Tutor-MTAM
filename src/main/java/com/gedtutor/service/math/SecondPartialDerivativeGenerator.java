package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 3 — Second derivative of f(x) = a·x^n evaluated at x = x₀.
 *
 * <p>f'(x)  = a·n·x^(n-1)
 * <br>f''(x) = a·n·(n-1)·x^(n-2)   →   f''(x₀) = a·n·(n-1)·x₀^(n-2)
 *
 * <p>Framed in a Cal 3 context as a second-order partial derivative
 * ∂²f/∂x² for functions of one variable, which is the first step before
 * students encounter mixed partials.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":6,"nMin":3,"nMax":5,"x0Min":1,"x0Max":4}}</pre>
 * We require n ≥ 3 so the second derivative is still a non-trivial function.
 */
@Service
public class SecondPartialDerivativeGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin  = 1, aMax  = 6;
        public int nMin  = 3, nMax  = 5;
        public int x0Min = 1, x0Max = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.SECOND_PARTIAL_DERIVATIVE; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a  = randomInRange(cfg.aMin, cfg.aMax);
        int n  = randomInRange(Math.max(cfg.nMin, 3), cfg.nMax); // n >= 3 required
        int x0 = randomInRange(cfg.x0Min, cfg.x0Max);

        // f''(x0) = a · n · (n-1) · x0^(n-2)
        long answer = (long) a * n * (n - 1) * pow(x0, n - 2);

        String fText = MathLatexSupport.polyLeadTerm(a, n);
        String question = MathLatexSupport.text("Let ") + "f(x) = " + fText
                + MathLatexSupport.text(". Find ") + "f''(" + x0 + ")"
                + MathLatexSupport.text(".");

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }

    static long pow(long base, int exp) {
        if (exp <= 0) return 1L;
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
