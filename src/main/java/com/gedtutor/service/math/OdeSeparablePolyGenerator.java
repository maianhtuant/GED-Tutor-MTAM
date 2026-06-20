package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Differential Equations — Solve dy/dx = a·x^n with initial condition y(0) = C,
 * then evaluate y at x = x₀.
 *
 * <p>Solution: y(x) = a·x^(n+1)/(n+1) + C
 * <br>y(x₀) = a·x₀^(n+1)/(n+1) + C
 *
 * <p>The answer is always rational (exact).
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":4,"nMin":1,"nMax":3,"cMin":0,"cMax":5,"x0Min":1,"x0Max":3}}</pre>
 */
@Service
public class OdeSeparablePolyGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin  = 1, aMax  = 4;
        public int nMin  = 1, nMax  = 3;
        public int cMin  = 0, cMax  = 5;
        public int x0Min = 1, x0Max = 3;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.ODE_SEPARABLE_POLY; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a  = randomInRange(cfg.aMin, cfg.aMax);
        int n  = randomInRange(cfg.nMin, cfg.nMax);
        int C  = randomInRange(cfg.cMin, cfg.cMax);
        int x0 = randomInRange(cfg.x0Min, cfg.x0Max);

        // y(x0) = a·x0^(n+1) / (n+1) + C
        long x0pow = pow(x0, n + 1);
        Rational answer = Rational.of((long) a * x0pow, n + 1).add(Rational.of(C, 1));

        String rhsText = MathLatexSupport.polyLeadTerm(a, n);
        String question = MathLatexSupport.text("Solve: ")
                + MathLatexSupport.dyOverDx() + " = " + rhsText
                + MathLatexSupport.text(", ") + "y(0) = " + C
                + MathLatexSupport.text(". Find ") + "y(" + x0 + ")"
                + MathLatexSupport.text(".");

        return GeneratedMathProblem.scalar(
                template.getId(), question, answer, tol, template.getVideoUrl());
    }

    static long pow(long base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
