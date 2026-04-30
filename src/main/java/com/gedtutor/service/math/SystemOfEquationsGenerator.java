package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Solve a 2x2 linear system:
 * <pre>
 *   a1 x + b1 y = c1
 *   a2 x + b2 y = c2
 * </pre>
 *
 * <p>Uses Cramer's rule. The determinant of the coefficient matrix
 * (a1*b2 - a2*b1) is checked: zero means the system is either
 * inconsistent (no solution) or dependent (infinite solutions); we
 * re-roll those because GED problems virtually always have a unique
 * solution. If we somehow can't avoid degeneracy in 16 tries we surface
 * the degenerate case as a "no solution" / "infinite solutions" answer.
 *
 * <p>JSON shape:
 * <pre>{@code {"coefMin":-10,"coefMax":10,"constMin":-10,"constMax":10}}</pre>
 *
 * <p>Answer shape: ORDERED pair (x, y).
 */
@Service
public class SystemOfEquationsGenerator implements MathProblemGenerator {

    static final Set<String> NO_SOLUTION_ALIASES = Set.of(
            "no solution", "no solutions", "none", "inconsistent");
    static final Set<String> INFINITE_ALIASES = Set.of(
            "infinite solutions", "infinitely many solutions", "all real numbers", "dependent");

    public static final class Config {
        public int coefMin = -10, coefMax = 10;
        public int constMin = -10, constMax = 10;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.SYSTEM_OF_EQUATIONS; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        for (int tries = 0; tries < 16; tries++) {
            int a1 = randomInRange(cfg.coefMin, cfg.coefMax);
            int b1 = randomInRange(cfg.coefMin, cfg.coefMax);
            int c1 = randomInRange(cfg.constMin, cfg.constMax);
            int a2 = randomInRange(cfg.coefMin, cfg.coefMax);
            int b2 = randomInRange(cfg.coefMin, cfg.coefMax);
            int c2 = randomInRange(cfg.constMin, cfg.constMax);

            // Avoid trivially-zero rows (both coefficients zero on one row).
            if ((a1 == 0 && b1 == 0) || (a2 == 0 && b2 == 0)) continue;

            long det = (long) a1 * b2 - (long) a2 * b1;
            String text = "Solve the system:\n" +
                    "  " + renderEquation(a1, b1, c1) + "\n" +
                    "  " + renderEquation(a2, b2, c2);

            if (det == 0) {
                // Re-roll on degenerate case for now.
                continue;
            }

            // Cramer's rule.
            long detX = (long) c1 * b2 - (long) c2 * b1;
            long detY = (long) a1 * c2 - (long) a2 * c1;
            Rational x = Rational.of(detX, det);
            Rational y = Rational.of(detY, det);
            return GeneratedMathProblem.ordered(template.getId(), text, List.of(x, y), tol, template.getVideoUrl());
        }
        // Extremely unlucky — emit a known-good system.
        Rational x = Rational.of(2, 1), y = Rational.of(3, 1);
        String text = "Solve the system:\n  x + y = 5\n  2x - y = 1";
        return GeneratedMathProblem.ordered(template.getId(), text, List.of(x, y), tol, template.getVideoUrl());
    }

    /** Render "a x + b y = c" with sane signs / units. Handles a or b == 0. */
    static String renderEquation(int a, int b, int c) {
        StringBuilder sb = new StringBuilder();
        boolean wroteSomething = false;

        if (a != 0) {
            if (a == 1)       sb.append("x");
            else if (a == -1) sb.append("-x");
            else              sb.append(a).append("x");
            wroteSomething = true;
        }

        if (b != 0) {
            if (!wroteSomething) {
                if (b == 1)       sb.append("y");
                else if (b == -1) sb.append("-y");
                else              sb.append(b).append("y");
            } else {
                sb.append(MathConfigSupport.signedTerm(b, "y"));
            }
            wroteSomething = true;
        }

        if (!wroteSomething) sb.append("0");
        sb.append(" = ").append(c);
        return sb.toString();
    }
}
