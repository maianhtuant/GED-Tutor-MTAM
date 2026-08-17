package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Linear Algebra — Compute the dot product u · v for two integer vectors.
 *
 * <p>Supports 2D and 3D vectors (set {@code dimension} in config).
 * The answer is always a single integer scalar.
 *
 * <p>JSON config shape:
 * <pre>{@code {"min":-5,"max":5,"dimension":3}}</pre>
 */
@Service
public class DotProductGenerator implements MathProblemGenerator {

    public static final class Config {
        public int min = -5, max = 5;
        public int dimension = 3; // 2 or 3
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.DOT_PRODUCT; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        int dim = (cfg.dimension == 2) ? 2 : 3;
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int[] u = new int[dim];
        int[] v = new int[dim];
        long dot = 0;
        for (int i = 0; i < dim; i++) {
            u[i] = randomInRange(cfg.min, cfg.max);
            v[i] = randomInRange(cfg.min, cfg.max);
            dot += (long) u[i] * v[i];
        }

        String question = MathLatexSupport.text("Find ") + "\\mathbf{u} \\cdot \\mathbf{v}"
                + MathLatexSupport.text(" where ")
                + "\\mathbf{u} = " + vecStr(u)
                + MathLatexSupport.text(", ")
                + "\\mathbf{v} = " + vecStr(v)
                + MathLatexSupport.text(".");

        return GeneratedMathProblem.scalar(
                template.getId(), question, Rational.of(dot, 1), tol, template.getVideoUrl());
    }

    static String vecStr(int[] v) {
        StringBuilder sb = new StringBuilder("\\langle ");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(v[i]);
        }
        sb.append("\\rangle");
        return sb.toString();
    }
}
