package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Differential Equations — Solve the exponential growth/decay ODE.
 *
 * <p>Given: dy/dt = k·y, y(0) = y₀.
 * <br>Solution: y(t) = y₀·e^(k·t)
 * <br>At t = 1: y(1) = y₀·e^k  (stored via {@code Rational.ofDouble}).
 *
 * <p>The answer is irrational (involves e^k), so we use a 2% tolerance
 * and store it as a double approximation. Students are expected to compute
 * this numerically and round to 2 decimal places.
 *
 * <p>JSON config shape:
 * <pre>{@code {"kMin":1,"kMax":3,"y0Min":1,"y0Max":5}}</pre>
 * Use negative kMin for decay problems.
 */
@Service
public class OdeExponentialIcGenerator implements MathProblemGenerator {

    public static final class Config {
        public int kMin  = 1, kMax  = 3;
        public int y0Min = 1, y0Max = 5;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.ODE_EXPONENTIAL_IC; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        // Use a wider tolerance since e^k is irrational
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 2.0;

        int k  = randomInRange(cfg.kMin, cfg.kMax);
        int y0 = randomInRange(cfg.y0Min, cfg.y0Max);

        double answer = y0 * Math.exp(k);
        Rational approx = Rational.ofDouble(answer);

        String question = MathLatexSupport.text("Solve: ")
                + MathLatexSupport.dyOverDt() + " = " + k + "y"
                + MathLatexSupport.text(", ") + "y(0) = " + y0
                + MathLatexSupport.text(". Find ") + "y(1)"
                + MathLatexSupport.text(". Round to 2 decimal places.");

        return GeneratedMathProblem.scalar(
                template.getId(), question, approx, tol, template.getVideoUrl());
    }
}
