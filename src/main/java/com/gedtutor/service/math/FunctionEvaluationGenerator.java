package com.gedtutor.service.math;

import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.dto.Rational;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import org.springframework.stereotype.Service;

import static com.gedtutor.service.math.MathConfigSupport.parseOrDefault;
import static com.gedtutor.service.math.MathConfigSupport.randomInRange;
import static com.gedtutor.service.math.MathConfigSupport.randomNonZeroInRange;

/**
 * Given a function and an input, ask the student to evaluate it.
 *
 * <p>Two function families:
 * <ul>
 *   <li>LINEAR: f(x) = ax + b</li>
 *   <li>QUADRATIC: f(x) = ax^2 + bx + c</li>
 * </ul>
 *
 * <p>JSON shape:
 * <pre>{@code {"familyName":"LINEAR","coefMin":-5,"coefMax":5,"inputMin":-5,"inputMax":5}}</pre>
 *
 * <p>Always SCALAR. Answer is always an integer with the integer ranges
 * we use, but we still return a Rational so future fractional configs
 * "just work".
 */
@Service
public class FunctionEvaluationGenerator implements MathProblemGenerator {

    public enum Family { LINEAR, QUADRATIC }

    public static final class Config {
        public String familyName = Family.LINEAR.name();
        public int coefMin = -5;
        public int coefMax = 5;
        public int inputMin = -5;
        public int inputMax = 5;

        Family family() {
            try { return Family.valueOf(familyName); }
            catch (Exception ex) { return Family.LINEAR; }
        }
    }

    @Override
    public MathProblemKind kind() { return MathProblemKind.FUNCTION_EVALUATION; }

    @Override
    public GeneratedMathProblem generate(MathProblemTemplate template) {
        Config cfg = parseOrDefault(template.getParametersJson(), Config.class);
        double tol = template.getTolerancePercent() != null ? template.getTolerancePercent() : 0.5;

        Family fam = cfg.family();
        int input = randomInRange(cfg.inputMin, cfg.inputMax);

        if (fam == Family.LINEAR) {
            int a = randomNonZeroInRange(cfg.coefMin, cfg.coefMax);
            int b = randomInRange(cfg.coefMin, cfg.coefMax);
            long answer = (long) a * input + b;
            String text = "Given f(x) = " + LinearEquationGenerator.renderOneSided(a, b, 0).replace(" = 0", "")
                    + ", what is f(" + input + ")?";
            return GeneratedMathProblem.scalar(template.getId(), text,
                    Rational.of(answer, 1), tol, template.getVideoUrl());
        }

        // QUADRATIC
        int a = randomNonZeroInRange(cfg.coefMin, cfg.coefMax);
        int b = randomInRange(cfg.coefMin, cfg.coefMax);
        int c = randomInRange(cfg.coefMin, cfg.coefMax);
        long answer = (long) a * input * input + (long) b * input + c;
        String fStr;
        if (a == 1)       fStr = "x^2";
        else if (a == -1) fStr = "-x^2";
        else              fStr = a + "x^2";
        fStr = fStr + MathConfigSupport.signedTerm(b, "x") + MathConfigSupport.signedTerm(c, "");
        String text = "Given f(x) = " + fStr + ", what is f(" + input + ")?";
        return GeneratedMathProblem.scalar(template.getId(), text,
                Rational.of(answer, 1), tol, template.getVideoUrl());
    }
}
