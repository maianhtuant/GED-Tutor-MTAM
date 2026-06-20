package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Linear Algebra — Compute det([[a, b],[c, d]]) = ad − bc.
 *
 * <p>The matrix entries are random integers; the answer is always an integer
 * (no fractions or square roots).
 *
 * <p>JSON config shape:
 * <pre>{@code {"min":-5,"max":5}}</pre>
 */
@Service
public class Determinant2x2Generator implements MathProblemGenerator {

    public static final class Config {
        public int min = -5, max = 5;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.DETERMINANT_2X2; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int a = randomInRange(cfg.min, cfg.max);
        int b = randomInRange(cfg.min, cfg.max);
        int c = randomInRange(cfg.min, cfg.max);
        int d = randomInRange(cfg.min, cfg.max);
        long answer = (long) a * d - (long) b * c;

        String matrix = "\\begin{vmatrix}" + a + " & " + b + " \\\\ " + c + " & " + d + "\\end{vmatrix}";
        String question = MathLatexSupport.text("Find ") + "\\det(A)"
                + MathLatexSupport.text(" for ")
                + "A = \\begin{pmatrix}" + a + " & " + b + " \\\\ " + c + " & " + d + "\\end{pmatrix}";

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
    }
}
