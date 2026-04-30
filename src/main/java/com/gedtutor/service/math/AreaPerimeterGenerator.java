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
 * Find the area or perimeter (or circumference) of a 2D shape:
 * rectangle, triangle, circle, or trapezoid.
 *
 * <p>One call picks a random shape AND a random metric (area vs perimeter/
 * circumference). Triangle perimeter respects the triangle inequality;
 * trapezoid uses an isoceles-trapezoid layout so the legs are computable
 * from the base difference and the height.
 *
 * <p>JSON shape:
 * <pre>{@code {
 *   "lengthMin":2,"lengthMax":15,
 *   "shapes":["RECTANGLE","TRIANGLE","CIRCLE","TRAPEZOID"],
 *   "metrics":["AREA","PERIMETER"]
 * }}</pre>
 *
 * <p>Answer shape: SCALAR. π-based answers are decimals computed with
 * {@code Math.PI} and matched under the configured tolerance — students
 * can use 3.14, 3.14159, or full π and still pass with reasonable tolerance.
 *
 * <p>Recommended template tolerance for π-based shapes: 1.0%.
 */
@Service
public class AreaPerimeterGenerator implements MathProblemGenerator {

    public enum Shape { RECTANGLE, TRIANGLE, CIRCLE, TRAPEZOID }
    public enum Metric { AREA, PERIMETER }

    public static final class Config {
        public int lengthMin = 2;
        public int lengthMax = 15;
        public String[] shapes  = { "RECTANGLE", "TRIANGLE", "CIRCLE", "TRAPEZOID" };
        public String[] metrics = { "AREA", "PERIMETER" };
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.AREA_PERIMETER; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Shape shape  = pickEnum(cfg.shapes,  Shape.class,  Shape.RECTANGLE);
        Metric metric = pickEnum(cfg.metrics, Metric.class, Metric.AREA);

        return switch (shape) {
            case RECTANGLE -> rectangle(template, cfg, metric, tol);
            case TRIANGLE  -> triangle(template, cfg, metric, tol);
            case CIRCLE    -> circle(template, cfg, metric, tol);
            case TRAPEZOID -> trapezoid(template, cfg, metric, tol);
        };
    }

    // --- shape implementations ---

    private GeneratedMathProblem rectangle(MathProblemTemplate t, Config cfg, Metric m, double tol) {
        int l = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int w = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        if (m == Metric.AREA) {
            String text = "Find the area of a rectangle with length " + l + " and width " + w + ".";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.of((long) l * w, 1), tol, t.getVideoUrl());
        }
        String text = "Find the perimeter of a rectangle with length " + l + " and width " + w + ".";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.of(2L * (l + w), 1), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem triangle(MathProblemTemplate t, Config cfg, Metric m, double tol) {
        if (m == Metric.AREA) {
            // base * height / 2 — pick even base so result is exact integer when possible.
            int b = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
            int h = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
            String text = "Find the area of a triangle with base " + b + " and height " + h + ".";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.of((long) b * h, 2), tol, t.getVideoUrl());
        }
        // Perimeter: pick three sides satisfying triangle inequality (a + b > c).
        int a, sb, sc;
        for (int tries = 0; tries < 32; tries++) {
            a  = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
            sb = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
            sc = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
            int max = Math.max(a, Math.max(sb, sc));
            if ((long) a + sb + sc - max > max) {
                String text = "A triangle has sides of length " + a + ", " + sb + ", and " + sc
                        + ". What is its perimeter?";
                return GeneratedMathProblem.scalar(t.getId(), text,
                        Rational.of((long) a + sb + sc, 1), tol, t.getVideoUrl());
            }
        }
        // Fallback: 3-4-5
        return GeneratedMathProblem.scalar(t.getId(),
                "A triangle has sides of length 3, 4, and 5. What is its perimeter?",
                Rational.of(12, 1), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem circle(MathProblemTemplate t, Config cfg, Metric m, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        if (m == Metric.AREA) {
            double area = Math.PI * r * r;
            String text = "Find the area of a circle with radius " + r
                    + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.ofDouble(area), tol, t.getVideoUrl());
        }
        double circ = 2 * Math.PI * r;
        String text = "Find the circumference of a circle with radius " + r
                + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(circ), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem trapezoid(MathProblemTemplate t, Config cfg, Metric m, double tol) {
        // Isoceles trapezoid: parallel bases b1 < b2, height h, equal legs.
        int b1 = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
        int b2 = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);
        if (b1 == b2) b2 = b1 + 1; // avoid degenerate parallelogram phrasing
        if (b1 > b2) { int tmp = b1; b1 = b2; b2 = tmp; }
        int h  = randomInRange(Math.max(2, cfg.lengthMin), cfg.lengthMax);

        if (m == Metric.AREA) {
            String text = "Find the area of a trapezoid with parallel sides " + b1
                    + " and " + b2 + ", and height " + h + ".";
            // (b1 + b2) * h / 2
            return GeneratedMathProblem.scalar(t.getId(), text,
                    Rational.of((long) (b1 + b2) * h, 2), tol, t.getVideoUrl());
        }
        // Perimeter: leg length = sqrt(h^2 + ((b2 - b1)/2)^2).
        double half = (b2 - b1) / 2.0;
        double leg = Math.sqrt(h * (double) h + half * half);
        double perim = b1 + b2 + 2 * leg;
        String text = "Find the perimeter of an isosceles trapezoid with parallel sides "
                + b1 + " and " + b2 + " and height " + h
                + ". (Round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(perim), tol, t.getVideoUrl());
    }

    // --- helpers ---

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E pickEnum(String[] options, Class<E> clazz, E fallback) {
        if (options == null || options.length == 0) return fallback;
        String pick = options[ThreadLocalRandom.current().nextInt(options.length)];
        try {
            return Enum.valueOf(clazz, pick);
        } catch (Exception ex) {
            return fallback;
        }
    }
}
