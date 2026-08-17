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
 * Linear Algebra — Compute u × v for two 3D integer vectors.
 *
 * <p>The cross product formula:
 * <pre>
 *   u × v = ( u₂v₃ − u₃v₂ ,  u₃v₁ − u₁v₃ ,  u₁v₂ − u₂v₁ )
 * </pre>
 * The student must enter all three components separated by commas
 * (ORDERED answer shape).
 *
 * <p>JSON config shape:
 * <pre>{@code {"min":-4,"max":4}}</pre>
 */
@Service
public class CrossProductGenerator implements MathProblemGenerator {

    public static final class Config {
        public int min = -4, max = 4;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.CROSS_PRODUCT; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int u1, u2, u3, v1, v2, v3;
        // Re-roll until not parallel (cross product != zero vector)
        for (int attempt = 0; attempt < 20; attempt++) {
            u1 = randomInRange(cfg.min, cfg.max);
            u2 = randomInRange(cfg.min, cfg.max);
            u3 = randomInRange(cfg.min, cfg.max);
            v1 = randomInRange(cfg.min, cfg.max);
            v2 = randomInRange(cfg.min, cfg.max);
            v3 = randomInRange(cfg.min, cfg.max);

            long i = (long) u2 * v3 - (long) u3 * v2;
            long j = (long) u3 * v1 - (long) u1 * v3;
            long k = (long) u1 * v2 - (long) u2 * v1;
            if (i != 0 || j != 0 || k != 0) {
                List<Rational> answers = List.of(
                        Rational.of(i, 1), Rational.of(j, 1), Rational.of(k, 1));
                String question = crossQuestion(u1, u2, u3, v1, v2, v3);
                return GeneratedMathProblem.ordered(
                        template.getId(), question, answers, tol, template.getVideoUrl());
            }
        }
        // Fallback: simple non-parallel vectors
        List<Rational> answers = List.of(Rational.of(1, 1), Rational.of(0, 1), Rational.of(0, 1));
        String question = crossQuestion(1, 0, 0, 0, 1, 0);
        return GeneratedMathProblem.ordered(template.getId(), question, answers, tol, template.getVideoUrl());
    }

    static String crossQuestion(int u1, int u2, int u3, int v1, int v2, int v3) {
        return MathLatexSupport.text("Compute ")
                + "\\mathbf{u} \\times \\mathbf{v}"
                + MathLatexSupport.text(" where ")
                + "\\mathbf{u} = \\langle " + u1 + ", " + u2 + ", " + u3 + "\\rangle"
                + MathLatexSupport.text(", ")
                + "\\mathbf{v} = \\langle " + v1 + ", " + v2 + ", " + v3 + "\\rangle"
                + MathLatexSupport.text(". Enter i, j, k components, comma-separated.");
    }
}
