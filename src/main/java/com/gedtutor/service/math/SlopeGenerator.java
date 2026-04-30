package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Find the slope of the line through two random points (x1, y1) and
 * (x2, y2). Slope is m = (y2 - y1) / (x2 - x1).
 *
 * <p>Vertical line case ({@code x1 == x2}) is presented as the special
 * answer "undefined". Same-point degenerate case is re-rolled.
 *
 * <p>JSON shape:
 * <pre>{@code {"coordMin":-10,"coordMax":10,"allowVertical":true}}</pre>
 *
 * <p>Answer shape: SCALAR (or special "undefined").
 */
@Service
public class SlopeGenerator implements MathProblemGenerator {

    static final Set<String> UNDEFINED_ALIASES = Set.of(
            "undefined", "no slope", "vertical");

    public static final class Config {
        public int coordMin = -10;
        public int coordMax = 10;
        /** When false, retries until x1 != x2 so the slope is always finite. */
        public boolean allowVertical = true;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.SLOPE; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        int x1, y1, x2, y2;
        for (int tries = 0; tries < 32; tries++) {
            x1 = randomInRange(cfg.coordMin, cfg.coordMax);
            y1 = randomInRange(cfg.coordMin, cfg.coordMax);
            x2 = randomInRange(cfg.coordMin, cfg.coordMax);
            y2 = randomInRange(cfg.coordMin, cfg.coordMax);

            if (x1 == x2 && y1 == y2) continue;       // same point — no line
            if (x1 == x2 && !cfg.allowVertical) continue; // skip vertical when disallowed

            String text = "Find the slope of the line passing through the points "
                    + "(" + x1 + ", " + y1 + ") and (" + x2 + ", " + y2 + ").";

            if (x1 == x2) {
                return GeneratedMathProblem.special(template.getId(), text,
                        "undefined", UNDEFINED_ALIASES, tol, template.getVideoUrl());
            }
            Rational m = Rational.of((long) y2 - y1, (long) x2 - x1);
            return GeneratedMathProblem.scalar(template.getId(), text, m, tol, template.getVideoUrl());
        }
        // Fallback known-good problem.
        return GeneratedMathProblem.scalar(template.getId(),
                "Find the slope of the line passing through the points (1, 2) and (3, 6).",
                Rational.of(2, 1), tol, template.getVideoUrl());
    }
}
