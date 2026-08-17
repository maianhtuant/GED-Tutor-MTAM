package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 3 — Partial derivative of f(x,y) = a·x^m·y^n evaluated at (x₀, y₀).
 *
 * <p>Partial with respect to x: ∂f/∂x = a·m·x^(m-1)·y^n  →  value = a·m·x₀^(m-1)·y₀^n
 * <br>Partial with respect to y: ∂f/∂y = a·n·x^m·y^(n-1)  →  value = a·n·x₀^m·y₀^(n-1)
 *
 * <p>The answer is always an integer for small integer exponents and evaluation points.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":5,"mMin":1,"mMax":3,"nMin":1,"nMax":3,
 *              "x0Min":1,"x0Max":3,"y0Min":1,"y0Max":3,"withRespectTo":"x"}}</pre>
 * {@code withRespectTo} can be {@code "x"}, {@code "y"}, or {@code "random"}.
 */
@Service
public class PartialDerivativeAtPointGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1,  aMax = 5;
        public int mMin = 1,  mMax = 3;
        public int nMin = 1,  nMax = 3;
        public int x0Min = 1, x0Max = 3;
        public int y0Min = 1, y0Max = 3;
        public String withRespectTo = "random"; // "x", "y", or "random"
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.PARTIAL_DERIVATIVE_AT_POINT; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a  = randomInRange(cfg.aMin, cfg.aMax);
        int m  = randomInRange(Math.max(cfg.mMin, 1), cfg.mMax);
        int n  = randomInRange(Math.max(cfg.nMin, 1), cfg.nMax);
        int x0 = randomInRange(cfg.x0Min, cfg.x0Max);
        int y0 = randomInRange(cfg.y0Min, cfg.y0Max);

        boolean wrtX = "y".equalsIgnoreCase(cfg.withRespectTo)
                ? false
                : "x".equalsIgnoreCase(cfg.withRespectTo) || randomInRange(0, 1) == 0;

        long answer;
        String wrtLabel;
        if (wrtX) {
            // ∂f/∂x = a·m·x^(m-1)·y^n  at (x0, y0)
            answer = (long) a * m * pow(x0, m - 1) * pow(y0, n);
            wrtLabel = "x";
        } else {
            // ∂f/∂y = a·n·x^m·y^(n-1)  at (x0, y0)
            answer = (long) a * n * pow(x0, m) * pow(y0, n - 1);
            wrtLabel = "y";
        }

        String fText = renderFunction(a, m, n);
        String question = MathLatexSupport.text("Let ") + "f(x,y) = " + fText
                + MathLatexSupport.text(". Find ")
                + MathLatexSupport.partialDeriv("f", wrtLabel)
                + MathLatexSupport.text(" at ") + "(" + x0 + ", " + y0 + ")\\text{.}";

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }

    /** Render a·x^m·y^n with suppressed 1 coefficients/exponents. */
    static String renderFunction(int a, int m, int n) {
        StringBuilder sb = new StringBuilder();
        if (a != 1) sb.append(a);
        sb.append("x");
        if (m > 1) sb.append("^").append(m);
        sb.append("y");
        if (n > 1) sb.append("^").append(n);
        return sb.toString();
    }

    static long pow(long base, int exp) {
        if (exp <= 0) return 1L;
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
