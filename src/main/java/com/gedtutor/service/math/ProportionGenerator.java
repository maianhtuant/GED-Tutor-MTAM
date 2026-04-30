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
 * Solve a/b = c/x for x. Cross-multiplying gives x = b·c / a.
 *
 * <p>JSON shape:
 * <pre>{@code {"min":1,"max":12}}</pre>
 *
 * <p>Both numerator and denominator are drawn from {@code [min, max]}, with
 * a forced non-zero on the {@code a} side so cross-multiplication doesn't
 * divide by zero.
 *
 * <p>Answer shape: SCALAR (rational).
 */
@Service
public class ProportionGenerator implements MathProblemGenerator {

    public static final class Config {
        public int min = 1;
        public int max = 12;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.PROPORTION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a = randomNonZeroInRange(cfg.min, cfg.max);
        int b = randomInRange(cfg.min, cfg.max);
        int c = randomInRange(cfg.min, cfg.max);
        Rational x = Rational.of((long) b * c, a);

        String text = "Solve the proportion: " + a + "/" + b + " = " + c + "/x";
        return GeneratedMathProblem.scalar(template.getId(), text, x, tol, template.getVideoUrl());
    }
}
