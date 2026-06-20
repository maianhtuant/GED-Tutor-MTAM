package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Differential Equations — Find the characteristic roots of a·y'' + b·y' + c·y = 0.
 *
 * <p>We construct the problem by choosing two distinct real roots r₁ and r₂ first,
 * then back-calculating the coefficients. The characteristic equation is:
 * <pre>
 *   a·r² − a(r₁+r₂)·r + a·r₁·r₂ = 0
 * </pre>
 * With a = 1, b = −(r₁+r₂), c = r₁·r₂.
 *
 * <p>This guarantees two clean integer roots and a tractable auto-graded answer.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":3,"rMin":-5,"rMax":5}}</pre>
 * {@code aMin}/{@code aMax} scale the coefficients uniformly; roots are unchanged.
 */
@Service
public class OdeCharacteristicEquationGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 1; // default: keep coefficients small
        public int rMin = -4, rMax = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.ODE_CHARACTERISTIC_EQUATION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        // Pick two distinct integer roots (initialize to guarantee definite assignment)
        int r1 = cfg.rMin;
        int r2 = cfg.rMin + 1;
        for (int attempt = 0; attempt <= 20; attempt++) {
            r1 = randomInRange(cfg.rMin, cfg.rMax);
            r2 = randomInRange(cfg.rMin, cfg.rMax);
            if (r1 != r2) break;
            if (attempt == 20) r2 = r1 + 1; // ensure distinct on last try
        }
        int a = randomInRange(Math.max(cfg.aMin, 1), Math.max(cfg.aMax, 1));

        // Coefficients: a·y'' − a(r1+r2)·y' + a·r1·r2·y = 0
        int bCoeff = -a * (r1 + r2);
        int cCoeff =  a * r1 * r2;

        List<Rational> answers = r1 <= r2
                ? List.of(Rational.of(r1, 1), Rational.of(r2, 1))
                : List.of(Rational.of(r2, 1), Rational.of(r1, 1));

        String odeText = renderODE(a, bCoeff, cCoeff);
        String question = MathLatexSupport.text("Find characteristic roots for ")
                + odeText + " = 0"
                + MathLatexSupport.text(". Enter both roots, comma-separated.");

        return GeneratedMathProblem.unordered(
                template.getId(), question, answers, tol, template.getVideoUrl());
    }

    static String renderODE(int a, int b, int c) {
        return MathLatexSupport.leadTerm(a, "y''")
                + MathLatexSupport.signedTerm(b, "y'")
                + MathLatexSupport.signedTerm(c, "y");
    }
}
