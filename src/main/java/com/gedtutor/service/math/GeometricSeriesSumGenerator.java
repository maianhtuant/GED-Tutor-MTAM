package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 2 — Finite geometric series sum.
 *
 * <p>S_n = a · (1 − r^n) / (1 − r), where r = rNumerator / rDenominator.
 * The answer is exact rational arithmetic so no floating-point rounding
 * occurs before comparison with the student's answer.
 *
 * <p>JSON config shape:
 * <pre>{@code {"aMin":1,"aMax":6,"rNumeratorMin":1,"rNumeratorMax":3,
 *              "rDenominatorMin":2,"rDenominatorMax":4,"nMin":3,"nMax":5}}</pre>
 */
@Service
public class GeometricSeriesSumGenerator implements MathProblemGenerator {

    public static final class Config {
        public int aMin = 1, aMax = 6;
        public int rNumeratorMin = 1, rNumeratorMax = 3;
        public int rDenominatorMin = 2, rDenominatorMax = 4;
        public int nMin = 3, nMax = 5;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.GEOMETRIC_SERIES_SUM; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 1.0;

        int a     = randomInRange(cfg.aMin, cfg.aMax);
        int rNum  = randomInRange(cfg.rNumeratorMin, cfg.rNumeratorMax);
        int rDen  = randomInRange(cfg.rDenominatorMin, cfg.rDenominatorMax);
        // Ensure |r| < 1 and r != 0
        if (rNum >= rDen) rNum = 1;
        if (rDen == 0)    rDen = 2;
        int n = randomInRange(cfg.nMin, cfg.nMax);

        // Compute r^n as an exact rational
        long rNumPow = pow(rNum, n);
        long rDenPow = pow(rDen, n);

        // S = a * (1 - r^n) / (1 - r)
        //   = a * (rDen^n - rNum^n) / rDen^n  ÷  (rDen - rNum) / rDen
        //   = a * (rDen^n - rNum^n) * rDen / (rDen^n * (rDen - rNum))
        long sNum = (long) a * (rDenPow - rNumPow) * rDen;
        long sDen = rDenPow * (rDen - rNum);
        Rational answer = Rational.of(sNum, sDen);

        String rLatex = MathLatexSupport.frac(rNum, rDen);
        String formula = "S = " + MathLatexSupport.frac(
                "a\\left(1 - r^{" + n + "}\\right)", "1 - r");
        String question = MathLatexSupport.text("Find the sum of the geometric series with ")
                + "a = " + a + MathLatexSupport.text(", ")
                + "r = " + rLatex + MathLatexSupport.text(", ")
                + "n = " + n + MathLatexSupport.text(" terms.")
                + "\\quad " + formula;

        return GeneratedMathProblem.scalar(
                template.getId(), question, answer, tol, template.getVideoUrl());
    }

    static long pow(long base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }
}
