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
 * Solve for x in {@code ax + b = c} (one-sided) or {@code ax + b = cx + d}
 * (two-sided). Toggle via the {@code twoSided} config flag.
 *
 * <p>JSON shape:
 * <pre>{@code {"aMin":1,"aMax":10,"bMin":-10,"bMax":10,"cMin":-10,"cMax":10,"dMin":-10,"dMax":10,"twoSided":false}}</pre>
 *
 * <p>Always solvable for finite x except in the degenerate two-sided case
 * {@code a == c}, which we re-roll to avoid.
 */
@Service
public class LinearEquationGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 10;
        public int bMin = -10, bMax = 10;
        public int cMin = -10, cMax = 10;
        public int dMin = -10, dMax = 10;
        public boolean twoSided = false;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.LINEAR_EQUATION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        if (cfg.twoSided) {
            return generateTwoSided(template, cfg, tol);
        }
        return generateOneSided(template, cfg, tol);
    }

    /** ax + b = c  →  x = (c - b) / a. */
    private GeneratedMathProblem generateOneSided(MathProblemTemplate t, Config cfg, double tol) {
        int a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        int c = randomInRange(cfg.cMin, cfg.cMax);
        Rational answer = Rational.of((long) c - b, a);
        return GeneratedMathProblem.scalar(t.getId(),
                "Solve for x: " + renderOneSided(a, b, c),
                answer, tol, t.getVideoUrl());
    }

    /** ax + b = cx + d  →  (a - c)x = d - b  →  x = (d - b) / (a - c). */
    private GeneratedMathProblem generateTwoSided(MathProblemTemplate t, Config cfg, double tol) {
        int a, b, c, d;
        for (int tries = 0; tries < 16; tries++) {
            a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
            b = randomInRange(cfg.bMin, cfg.bMax);
            c = randomInRange(cfg.cMin, cfg.cMax);
            d = randomInRange(cfg.dMin, cfg.dMax);
            if (a == c) continue; // would collapse the equation
            Rational answer = Rational.of((long) d - b, (long) a - c);
            return GeneratedMathProblem.scalar(t.getId(),
                    "Solve for x: " + renderOneSided(a, b, 0).replace(" = 0", "") + " = " + renderOneSided(c, d, 0).replace(" = 0", ""),
                    answer, tol, t.getVideoUrl());
        }
        // Fallback: force a != c.
        a = 2; b = randomInRange(cfg.bMin, cfg.bMax);
        c = 1; d = randomInRange(cfg.dMin, cfg.dMax);
        Rational answer = Rational.of((long) d - b, (long) a - c);
        return GeneratedMathProblem.scalar(t.getId(),
                "Solve for x: " + renderOneSided(a, b, 0).replace(" = 0", "") + " = " + renderOneSided(c, d, 0).replace(" = 0", ""),
                answer, tol, t.getVideoUrl());
    }

    /** Render "ax + b = c" with sane signs and unit coefficients. */
    static String renderOneSided(int a, int b, int c) {
        StringBuilder sb = new StringBuilder();
        if (a == 1)       sb.append("x");
        else if (a == -1) sb.append("-x");
        else              sb.append(a).append("x");
        sb.append(MathConfigSupport.signedTerm(b, ""));
        sb.append(" = ").append(c);
        return sb.toString();
    }
}
