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
 * Volume of a 3D shape: rectangular prism, cylinder, cone, or sphere.
 * Picks a random shape per call.
 *
 * <p>Formulas:
 * <ul>
 *   <li>rectangular prism: V = l × w × h</li>
 *   <li>cylinder: V = π r² h</li>
 *   <li>cone: V = (1/3) π r² h</li>
 *   <li>sphere: V = (4/3) π r³</li>
 * </ul>
 *
 * <p>JSON shape:
 * <pre>{@code {"lengthMin":2,"lengthMax":12,"shapes":["RECTANGULAR_PRISM","CYLINDER","CONE","SPHERE"]}}</pre>
 *
 * <p>π-based shapes return decimal answers — use template tolerance ≥ 1%
 * so students can submit answers rounded to 2 decimal places.
 */
@Service
public class VolumeGenerator implements MathProblemGenerator {

    public enum Shape { RECTANGULAR_PRISM, CYLINDER, CONE, SPHERE }

    public static final class Config {
        public int lengthMin = 2;
        public int lengthMax = 12;
        public String[] shapes = { "RECTANGULAR_PRISM", "CYLINDER", "CONE", "SPHERE" };
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.VOLUME; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Shape shape = pickShape(cfg);
        return switch (shape) {
            case RECTANGULAR_PRISM -> rectPrism(template, cfg, tol);
            case CYLINDER          -> cylinder(template, cfg, tol);
            case CONE              -> cone(template, cfg, tol);
            case SPHERE            -> sphere(template, cfg, tol);
        };
    }

    private GeneratedMathProblem rectPrism(MathProblemTemplate t, Config cfg, double tol) {
        int l = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int w = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int h = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        String text = "Find the volume of a rectangular prism with length " + l
                + ", width " + w + ", and height " + h + ".";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.of((long) l * w * h, 1), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem cylinder(MathProblemTemplate t, Config cfg, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int h = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        double v = Math.PI * r * r * h;
        String text = "Find the volume of a cylinder with radius " + r + " and height " + h
                + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(v), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem cone(MathProblemTemplate t, Config cfg, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        int h = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        double v = Math.PI * r * r * h / 3.0;
        String text = "Find the volume of a cone with radius " + r + " and height " + h
                + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(v), tol, t.getVideoUrl());
    }

    private GeneratedMathProblem sphere(MathProblemTemplate t, Config cfg, double tol) {
        int r = randomInRange(Math.max(1, cfg.lengthMin), cfg.lengthMax);
        double v = (4.0 / 3.0) * Math.PI * r * r * r;
        String text = "Find the volume of a sphere with radius " + r
                + ". (Use π ≈ 3.14159, round to 2 decimal places.)";
        return GeneratedMathProblem.scalar(t.getId(), text,
                Rational.ofDouble(v), tol, t.getVideoUrl());
    }

    private Shape pickShape(Config cfg) {
        if (cfg.shapes == null || cfg.shapes.length == 0) return Shape.RECTANGULAR_PRISM;
        String pick = cfg.shapes[ThreadLocalRandom.current().nextInt(cfg.shapes.length)];
        try { return Shape.valueOf(pick); }
        catch (Exception ex) { return Shape.RECTANGULAR_PRISM; }
    }
}
