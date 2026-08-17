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
 * Cal 1 — Power rule derivative evaluated at a point.
 *
 * <p>Given f(x) = a·x^n, apply the power rule to get f'(x) = a·n·x^(n-1),
 * then evaluate at x = c. The answer is always an integer, keeping
 * auto-grading simple.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":8,"nMin":2,"nMax":5,"cMin":-3,"cMax":3}}</pre>
 */
@Service
public class DerivativePolyAtPointGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 8;
        public int nMin = 2, nMax = 5;
        public int cMin = -3, cMax = 3;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.DERIVATIVE_POLY_AT_POINT; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
        int n = randomInRange(Math.max(cfg.nMin, 2), cfg.nMax); // n >= 2 so derivative is interesting
        int c = randomInRange(cfg.cMin, cfg.cMax);

        // f'(x) = a·n·x^(n-1)  →  f'(c) = a·n·c^(n-1)
        long cpow = pow(c, n - 1);
        long answer = (long) a * n * cpow;

        String fText = MathLatexSupport.polyLeadTerm(a, n);
        String question = MathLatexSupport.text("Let ") + "f(x) = " + fText
                + MathLatexSupport.text(". Find ") + "f'(" + c + ")"
                + MathLatexSupport.text(" using the power rule.");

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }

    /** Render a·x^n with suppressed coefficients/exponents for 1 and 2. */
    static String renderTerm(int a, int n, String var) {
        StringBuilder sb = new StringBuilder();
        if (a == 1)       sb.append(var);
        else if (a == -1) sb.append("-").append(var);
        else              sb.append(a).append(var);

        if (n == 1)       { /* no exponent */ }
        else if (n == 2)  sb.append("^2");
        else              sb.append("^").append(n);
        return sb.toString();
    }

    static long pow(long base, int exp) {
        if (exp == 0) return 1L;
        long result = 1L;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
