package com.gedtutor.service.math;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;
import static com.gedtutor.service.math.MathConfigSupport.randomNonZeroInRange;

/**
 * Generates and solves quadratic equations of the form
 * {@code ax^2 + bx + c = 0}. Three outcomes are possible: two real roots
 * (sometimes irrational), one repeated root, or no real solution. The
 * grader accepts the literal answer "no real solution" in the last case.
 *
 * <p>Configuration JSON shape:
 * <pre>{@code {"aMin":1,"aMax":10,"bMin":-10,"bMax":10,"cMin":-10,"cMax":10}}</pre>
 */
@Service
public class QuadraticSolver implements MathProblemGenerator {

    /** Aliases the checker accepts for the "no real solution" case. */
    static final Set<String> NO_REAL_SOLUTION_ALIASES = Set.of(
            "no real solution",
            "no real solutions",
            "no real roots",
            "no solution",
            "no solutions",
            "none"
    );

    public static final class Config {
        public int aMin = 1, aMax = 10;
        public int bMin = -10, bMax = 10;
        public int cMin = -10, cMax = 10;
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.QUADRATIC; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        int a = randomNonZeroInRange(cfg.aMin, cfg.aMax);
        int b = randomInRange(cfg.bMin, cfg.bMax);
        int c = randomInRange(cfg.cMin, cfg.cMax);
        return solve(template, a, b, c);
    }

    /** Test hook to pin coefficients (no random draw). */
    public GeneratedMathProblem solve(MathProblemTemplate template, int a, int b, int c) {
        long discriminant = (long) b * b - 4L * a * c;
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;
        int decimalPlaces = template.getDecimalPlaces();
        boolean roundAnswer = template.isRoundAnswer();
        String text = renderQuestion(a, b, c);
        String videoUrl = template.getVideoUrl();
        Long id = template.getId();

        if (discriminant < 0) {
            return new GeneratedMathProblem(
                    id, text, AnswerShape.SCALAR, List.of(),
                    "no real solution", NO_REAL_SOLUTION_ALIASES, tol, videoUrl,
                    decimalPlaces, roundAnswer);
        }
        if (discriminant == 0) {
            // Repeated root — but it's still a single answer.
            return GeneratedMathProblem.scalar(id, text,
                    Rational.of(-b, 2L * a), tol, videoUrl, decimalPlaces, roundAnswer);
        }
        long perfectSqrt = integerSqrt(discriminant);
        List<Rational> roots;
        if (perfectSqrt * perfectSqrt == discriminant) {
            Rational r1 = Rational.of(-b - perfectSqrt, 2L * a);
            Rational r2 = Rational.of(-b + perfectSqrt, 2L * a);
            roots = sortedPair(r1, r2);
        } else {
            double sqrt = Math.sqrt(discriminant);
            double r1 = (-b - sqrt) / (2.0 * a);
            double r2 = (-b + sqrt) / (2.0 * a);
            roots = sortedPair(Rational.ofDouble(r1), Rational.ofDouble(r2));
        }
        return GeneratedMathProblem.unordered(id, text, roots, tol, videoUrl, decimalPlaces, roundAnswer);
    }

    /** Render "Solve for x: ax^2 + bx + c = 0" with sane signs / unit coefficients. */
    public static String renderQuestion(int a, int b, int c) {
        StringBuilder sb = new StringBuilder("Solve for x: ");
        if (a == 1)       sb.append("x^2");
        else if (a == -1) sb.append("-x^2");
        else              sb.append(a).append("x^2");
        sb.append(MathConfigSupport.signedTerm(b, "x"));
        sb.append(MathConfigSupport.signedTerm(c, ""));
        sb.append(" = 0");
        return sb.toString();
    }

    private static List<Rational> sortedPair(Rational r1, Rational r2) {
        return r1.toDouble() <= r2.toDouble() ? List.of(r1, r2) : List.of(r2, r1);
    }

    private static long integerSqrt(long n) {
        if (n < 0) return -1;
        long s = (long) Math.sqrt((double) n);
        while ((s + 1) * (s + 1) <= n) s++;
        while (s * s > n) s--;
        return s;
    }
}
