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
 * Surface area of a 3D shape: rectangular prism, closed cylinder, or sphere.
 *
 * <p>Formulas:
 * <ul>
 *   <li>rectangular prism: SA = 2(lw + lh + wh)</li>
 *   <li>cylinder (closed): SA = 2π r² + 2π r h</li>
 *   <li>sphere: SA = 4π r²</li>
 * </ul>
 *
 * <p>JSON shape:
 * <pre>{@code {"lengthMin":2,"lengthMax":12,"shapes":["RECTANGULAR_PRISM","CYLINDER","SPHERE"]}}</pre>
 *
 * <p>π-based shapes return decimal answers — set template tolerance ≥ 1%.
 */
@Service
public class SurfaceAreaGenerator implements MathProblemGenerator {

    public enum Shape { RECTANGULAR_PRISM, CYLINDER, SPHERE }

    public static final class Config {
        public int lengthMin = 2;
        public int lengthMax = 12;
        public String[] shapes = { "RECTANGULAR_PRISM", "CYLINDER", "SPHERE" };
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.SURFACE_AREA; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Shape shape = pickShape(cfg);
        return switch (shape) {
            case RECTANGULAR_PRISM -> rectPrism(template, cfg, tol);
            case CYLINDER          -> cylinder(template, cfg, tol);
            case SPHERE            -> sphere(template, cfg, tol);
        };
    }

    private GeneratedMathProblem rectPrism(MathProblemTemplate t, Config cfg, double tol) {
        int l = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int w = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int h = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        long sa = 2L * ((long) l * w + (long) l * h + (long) w * h);
        String text = "Find the surface area of a rectangular prism with length " + l
                + ", width " + w + ", and height " + h + ".";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.of(sa, 1), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem cylinder(MathProblemTemplate t, Config cfg, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int h = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        double sa = 2 * Math.PI * r * r + 2 * Math.PI * r * h;
        String text = "Find the surface area of a closed cylinder with radius " + r
                + " and height " + h + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(sa), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem sphere(MathProblemTemplate t, Config cfg, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        double sa = 4 * Math.PI * r * r;
        String text = "Find the surface area of a sphere with radius " + r
                + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(sa), tol, t.getVideoUrl());
    }

    private Shape pickShape(Config cfg) {
        if (cfg.shapes == null || cfg.shapes.length == 0) return Shape.RECTANGULAR_PRISM;
        String pick = cfg.shapes[ThreadLocalRandom.current().nextInt(cfg.shapes.length)];
        try { return Shape.valueOf(pick); }
        catch (Exception ex) { return Shape.RECTANGULAR_PRISM; }
    }
}
