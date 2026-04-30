package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Right-triangle problems via {@code a^2 + b^2 = c^2}. Two flavors:
 * <ul>
 *   <li>FIND_HYPOTENUSE: legs {@code a}, {@code b} given, find {@code c = sqrt(a^2 + b^2)}</li>
 *   <li>FIND_LEG: hypotenuse {@code c} and one leg {@code a} given, find {@code b = sqrt(c^2 - a^2)}</li>
 * </ul>
 *
 * <p>By default we use a small bank of Pythagorean triples
 * (3-4-5, 5-12-13, 8-15-17, 7-24-25, 20-21-29) so the answer is always
 * a nice integer. With {@code allowIrrational=true} the generator draws
 * arbitrary integer legs and the hypotenuse may be irrational — answers
 * are then approximated and matched under tolerance.
 *
 * <p>JSON shape:
 * <pre>{@code {"allowIrrational": false, "legMin": 3, "legMax": 12}}</pre>
 *
 * <p>Answer shape: SCALAR.
 */
@Service
public class PythagoreanGenerator implements MathProblemGenerator {

    public enum Flavor { FIND_HYPOTENUSE, FIND_LEG }

    /** Common Pythagorean triples (a, b, c) with a < b < c. */
    private static final int[][] TRIPLES = {
            {3, 4, 5},
            {5, 12, 13},
            {8, 15, 17},
            {7, 24, 25},
            {20, 21, 29},
            {6, 8, 10},
            {9, 12, 15},
            {9, 40, 41}
    };

    public static final class Config {
        public boolean allowIrrational = false;
        public int legMin = 3;
        public int legMax = 12;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.PYTHAGOREAN; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;
        Flavor f = ThreadLocalRandom.current().nextBoolean()
                ? Flavor.FIND_HYPOTENUSE : Flavor.FIND_LEG;

        if (cfg.allowIrrational) {
            return generateAnyTriangle(template, cfg, f, tol);
        }
        return generateFromTriple(template, f, tol);
    }

    /** Use a clean Pythagorean triple → integer answer. */
    private GeneratedMathProblem generateFromTriple(MathProblemTemplate t, Flavor f, double tol) {
        int[] tri = TRIPLES[ThreadLocalRandom.current().nextInt(TRIPLES.length)];
        int a = tri[0], b = tri[1], c = tri[2];

        if (f == Flavor.FIND_HYPOTENUSE) {
            String text = "A right triangle has legs of length " + a + " and " + b
                    + ". What is the length of the hypotenuse?";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.of(c, 1), tol, t.getVideoUrl());
        } else {
            // Hide either leg; here we hide b.
            String text = "A right triangle has hypotenuse " + c + " and one leg of length " + a
                    + ". What is the length of the other leg?";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.of(b, 1), tol, t.getVideoUrl());
        }
    }

    /** Random integer legs; hypotenuse is usually irrational. */
    private GeneratedMathProblem generateAnyTriangle(MathProblemTemplate t, Config cfg, Flavor f, double tol) {
        int a = randomInRange(Math.max(1, cfg.legMin), Math.max(1, cfg.legMax));
        int b = randomInRange(Math.max(1, cfg.legMin), Math.max(1, cfg.legMax));
        if (f == Flavor.FIND_HYPOTENUSE) {
            double c = Math.sqrt((double) a * a + (double) b * b);
            String text = "A right triangle has legs of length " + a + " and " + b
                    + ". What is the length of the hypotenuse? (Round to 2 decimal places.)";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.ofDouble(c), tol, t.getVideoUrl());
        }
        // FIND_LEG: pick a > b, then b = sqrt(a^2 - smallerLeg^2). Make sure a is hypotenuse.
        int hyp = Math.max(a, b) + randomInRange(1, 5);
        int leg = Math.min(a, b);
        double other = Math.sqrt((double) hyp * hyp - (double) leg * leg);
        String text = "A right triangle has hypotenuse " + hyp + " and one leg of length " + leg
                + ". What is the length of the other leg? (Round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(other), tol, t.getVideoUrl());
    }
}
