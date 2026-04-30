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
 * Three percent flavors picked at random per call:
 * <ul>
 *   <li>{@code FORWARD}: "What is 25% of 80?"          → 0.25 × 80 = 20</li>
 *   <li>{@code FIND_PERCENT}: "30 is what percent of 120?" → 30 / 120 × 100 = 25</li>
 *   <li>{@code FIND_BASE}: "30 is 25% of what number?"     → 30 / 0.25 = 120</li>
 * </ul>
 *
 * <p>JSON shape:
 * <pre>{@code {
 *   "percentMin": 1, "percentMax": 100,
 *   "valueMin": 1,   "valueMax": 200,
 *   "flavors": ["FORWARD","FIND_PERCENT","FIND_BASE"]
 * }}</pre>
 *
 * <p>Numbers are drawn so the canonical answer is exact when possible
 * (e.g. base × percent / 100 is an integer); otherwise the answer is a
 * fraction and the checker matches via tolerance.
 */
@Service
public class PercentageGenerator implements MathProblemGenerator {

    public enum Flavor { FORWARD, FIND_PERCENT, FIND_BASE }

    public static final class Config {
        public int percentMin = 5, percentMax = 100;   // % values used for percent values
        public int valueMin   = 1, valueMax   = 200;   // bases / parts
        public String[] flavors = { "FORWARD", "FIND_PERCENT", "FIND_BASE" };
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.PERCENTAGE; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Flavor f = pickFlavor(cfg);
        return switch (f) {
            case FORWARD       -> forward(template, cfg, tol);
            case FIND_PERCENT  -> findPercent(template, cfg, tol);
            case FIND_BASE     -> findBase(template, cfg, tol);
        };
    }

    /** "What is p% of v?" → answer = p × v / 100. */
    private GeneratedMathProblem forward(MathProblemTemplate t, Config cfg, double tol) {
        int p = randomInRange(cfg.percentMin, cfg.percentMax);
        int v = randomInRange(cfg.valueMin,   cfg.valueMax);
        // answer is rational p*v / 100
        Rational answer = Rational.of((long) p * v, 100);
        String text = "What is " + p + "% of " + v + "?";
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    /** "x is what percent of v?" — generate v and a "nice" x then ask for the percent. */
    private GeneratedMathProblem findPercent(MathProblemTemplate t, Config cfg, double tol) {
        int v = Math.max(1, randomInRange(cfg.valueMin, cfg.valueMax));
        // Pick x near a randomly chosen percent of v so the canonical answer is a clean rational.
        int p = randomInRange(cfg.percentMin, cfg.percentMax);
        // x = round(p * v / 100). Use the rounded x and recompute the exact percent
        // so the displayed numbers stay tidy AND the answer matches.
        long xLong = Math.round((double) p * v / 100.0);
        if (xLong <= 0) xLong = 1;
        // exact percent as rational: x / v × 100 = (x*100) / v
        Rational answer = Rational.of(xLong * 100L, v);
        String text = xLong + " is what percent of " + v + "?";
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    /** "x is p% of what number?" → answer = x × 100 / p. */
    private GeneratedMathProblem findBase(MathProblemTemplate t, Config cfg, double tol) {
        int p = Math.max(1, randomInRange(cfg.percentMin, cfg.percentMax));
        int x = Math.max(1, randomInRange(cfg.valueMin, cfg.valueMax));
        Rational answer = Rational.of((long) x * 100L, p);
        String text = x + " is " + p + "% of what number?";
        return GeneratedMathProblem.scalar(t.getId(), text, answer, tol, t.getVideoUrl());
    }

    private Flavor pickFlavor(Config cfg) {
        if (cfg.flavors == null || cfg.flavors.length == 0) {
            return Flavor.FORWARD;
        }
        String pick = cfg.flavors[ThreadLocalRandom.current().nextInt(cfg.flavors.length)];
        try {
            return Flavor.valueOf(pick);
        } catch (Exception ex) {
            return Flavor.FORWARD;
        }
    }
}
