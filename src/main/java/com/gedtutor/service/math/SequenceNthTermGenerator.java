package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;

/**
 * Cal 2 — Find the nth term of an arithmetic or geometric sequence.
 *
 * <p>Arithmetic: a_n = a₁ + (n−1)·d
 * <br>Geometric:  a_n = a₁ · r^(n−1)
 *
 * <p>JSON config shape:
 * <pre>{@code {"sequenceType":"ARITHMETIC","firstMin":1,"firstMax":10,
 *              "diffMin":1,"diffMax":8,"nMin":5,"nMax":10}}</pre>
 * Set {@code sequenceType} to {@code "GEOMETRIC"} for geometric sequences.
 * For geometric sequences, {@code diffMin}/{@code diffMax} is used as the common ratio range.
 */
@Service
public class SequenceNthTermGenerator implements MathProblemGenerator {

    public static final class Config {
        public String sequenceType = "ARITHMETIC"; // or "GEOMETRIC" or "RANDOM"
        public int firstMin = 1,  firstMax = 10;
        public int diffMin  = 1,  diffMax  = 8;
        public int nMin     = 5,  nMax     = 10;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.SEQUENCE_NTH_TERM; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        boolean isGeometric = "GEOMETRIC".equalsIgnoreCase(cfg.sequenceType)
                || ("RANDOM".equalsIgnoreCase(cfg.sequenceType) && randomInRange(0, 1) == 1);

        int first = randomInRange(cfg.firstMin, cfg.firstMax);
        int d     = randomInRange(Math.max(cfg.diffMin, isGeometric ? 2 : cfg.diffMin), cfg.diffMax);
        int n     = randomInRange(cfg.nMin, cfg.nMax);

        if (isGeometric) {
            // a_n = first * d^(n-1)
            long answer = first * pow(d, n - 1);
            String question = MathLatexSupport.text("Geometric sequence: ")
                    + "a_1 = " + first + MathLatexSupport.text(", ")
                    + "r = " + d + MathLatexSupport.text(". Find ")
                    + "a_{" + n + "}.";
            return GeneratedMathProblem.scalar(
                    template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
        } else {
            // a_n = first + (n-1)*d
            long answer = first + (long)(n - 1) * d;
            String question = MathLatexSupport.text("Arithmetic sequence: ")
                    + "a_1 = " + first + MathLatexSupport.text(", ")
                    + "d = " + d + MathLatexSupport.text(". Find ")
                    + "a_{" + n + "}.";
            return GeneratedMathProblem.scalar(
                    template.getId(), question, Rational.of(answer, 1), tol, template.getVideoUrl());
        }
    }

    static long pow(long base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) result *= base;
        return result;
    }

    static String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }
}
