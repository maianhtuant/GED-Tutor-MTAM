package com.gedtutor.service.math;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.MathProblemKind;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.service.MathAnswerChecker;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke-level coverage for the new Phase-1 generators: each one should
 * produce a problem whose canonical answer the {@link MathAnswerChecker}
 * accepts. Catches accidental regressions in either direction.
 */
class GeneratorsSmokeTest {

    private final MathAnswerChecker checker = new MathAnswerChecker();
    private final LinearEquationGenerator linear = new LinearEquationGenerator();
    private final SystemOfEquationsGenerator system = new SystemOfEquationsGenerator();
    private final SlopeGenerator slope = new SlopeGenerator();
    private final FunctionEvaluationGenerator funcEval = new FunctionEvaluationGenerator();
    private final PercentageGenerator percentage = new PercentageGenerator();
    private final ProportionGenerator proportion = new ProportionGenerator();
    private final MeanMedianModeGenerator stats = new MeanMedianModeGenerator();
    private final PythagoreanGenerator pythag = new PythagoreanGenerator();
    private final AreaPerimeterGenerator areaPerim = new AreaPerimeterGenerator();
    private final VolumeGenerator volume = new VolumeGenerator();
    private final SurfaceAreaGenerator surfaceArea = new SurfaceAreaGenerator();

    @RepeatedTest(50)
    void linearGeneratorAlwaysProducesValidProblem() {
        GeneratedMathProblem p = linear.generate(template(MathProblemKind.LINEAR_EQUATION));
        assertEquals(AnswerShape.SCALAR, p.shape());
        assertEquals(1, p.expectedAnswers().size());
        // Submit the generator's own answer back; it must accept it.
        String back = String.valueOf(p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "Linear: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(50)
    void systemGeneratorAlwaysProducesValidProblem() {
        GeneratedMathProblem p = system.generate(template(MathProblemKind.SYSTEM_OF_EQUATIONS));
        assertEquals(AnswerShape.ORDERED, p.shape());
        assertEquals(2, p.expectedAnswers().size());
        String back = p.expectedAnswers().get(0).toDouble()
                + ", " + p.expectedAnswers().get(1).toDouble();
        assertTrue(checker.check(p, back).correct(),
                "System: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(100)
    void slopeGeneratorAlwaysProducesValidProblem() {
        GeneratedMathProblem p = slope.generate(template(MathProblemKind.SLOPE));
        if (p.hasSpecialAnswer()) {
            assertTrue(checker.check(p, p.specialAnswer()).correct());
        } else {
            assertEquals(AnswerShape.SCALAR, p.shape());
            String back = String.valueOf(p.expectedAnswers().get(0).toDouble());
            assertTrue(checker.check(p, back).correct(),
                    "Slope: generator answer not accepted: " + p.questionText() + " ans=" + back);
        }
    }

    @RepeatedTest(50)
    void functionEvalLinear() {
        MathProblemTemplate t = template(MathProblemKind.FUNCTION_EVALUATION);
        t.setParametersJson("{\"familyName\":\"LINEAR\"}");
        runFuncEval(t);
    }

    @RepeatedTest(50)
    void functionEvalQuadratic() {
        MathProblemTemplate t = template(MathProblemKind.FUNCTION_EVALUATION);
        t.setParametersJson("{\"familyName\":\"QUADRATIC\"}");
        runFuncEval(t);
    }

    @Test
    void linearRendersCleanly() {
        // Spot-check rendering helper.
        assertEquals("x + 3 = 5", LinearEquationGenerator.renderOneSided(1, 3, 5));
        assertEquals("-x + 3 = 5", LinearEquationGenerator.renderOneSided(-1, 3, 5));
        assertEquals("2x - 4 = 7", LinearEquationGenerator.renderOneSided(2, -4, 7));
    }

    // --- Phase 2 generators ---

    @RepeatedTest(50)
    void percentageGenerator() {
        GeneratedMathProblem p = percentage.generate(template(MathProblemKind.PERCENTAGE));
        assertEquals(AnswerShape.SCALAR, p.shape());
        assertEquals(1, p.expectedAnswers().size());
        // Submit the canonical fraction back as a fraction so tolerance can't bite us.
        String back = p.expectedAnswers().get(0).numerator()
                + "/" + p.expectedAnswers().get(0).denominator();
        assertTrue(checker.check(p, back).correct(),
                "Percentage: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(50)
    void proportionGenerator() {
        GeneratedMathProblem p = proportion.generate(template(MathProblemKind.PROPORTION));
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = p.expectedAnswers().get(0).numerator()
                + "/" + p.expectedAnswers().get(0).denominator();
        assertTrue(checker.check(p, back).correct(),
                "Proportion: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(80)
    void statsGenerator() {
        GeneratedMathProblem p = stats.generate(template(MathProblemKind.MEAN_MEDIAN_MODE));
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = p.expectedAnswers().get(0).numerator()
                + "/" + p.expectedAnswers().get(0).denominator();
        assertTrue(checker.check(p, back).correct(),
                "Stats: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(50)
    void pythagoreanGenerator_cleanTriples() {
        GeneratedMathProblem p = pythag.generate(template(MathProblemKind.PYTHAGOREAN));
        assertEquals(AnswerShape.SCALAR, p.shape());
        // Default config uses Pythagorean triples → integer answer.
        long n = (long) p.expectedAnswers().get(0).toDouble();
        assertTrue(checker.check(p, String.valueOf(n)).correct(),
                "Pythagorean: integer answer not accepted: " + p.questionText() + " ans=" + n);
    }

    @RepeatedTest(30)
    void pythagoreanGenerator_irrationalAllowed() {
        MathProblemTemplate t = template(MathProblemKind.PYTHAGOREAN);
        t.setParametersJson("{\"allowIrrational\":true,\"legMin\":3,\"legMax\":12}");
        // Use a generous tolerance so rounded answers always match.
        t.setTolerancePercent(1.0);
        GeneratedMathProblem p = pythag.generate(t);
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = String.format("%.2f", p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "Pythagorean (irrational): rounded answer not accepted: "
                        + p.questionText() + " ans=" + back);
    }

    // --- Phase 3 generators (geometry) ---
    // Geometry generators with π in them produce decimal answers; bump
    // tolerance so a 2-dp rounded student answer always passes.

    @RepeatedTest(80)
    void areaPerimeterGenerator() {
        MathProblemTemplate t = template(MathProblemKind.AREA_PERIMETER);
        t.setTolerancePercent(1.0);
        GeneratedMathProblem p = areaPerim.generate(t);
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = String.format("%.2f", p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "AreaPerimeter: rounded answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(80)
    void volumeGenerator() {
        MathProblemTemplate t = template(MathProblemKind.VOLUME);
        t.setTolerancePercent(1.0);
        GeneratedMathProblem p = volume.generate(t);
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = String.format("%.2f", p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "Volume: rounded answer not accepted: " + p.questionText() + " ans=" + back);
    }

    @RepeatedTest(60)
    void surfaceAreaGenerator() {
        MathProblemTemplate t = template(MathProblemKind.SURFACE_AREA);
        t.setTolerancePercent(1.0);
        GeneratedMathProblem p = surfaceArea.generate(t);
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = String.format("%.2f", p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "SurfaceArea: rounded answer not accepted: " + p.questionText() + " ans=" + back);
    }

    private void runFuncEval(MathProblemTemplate t) {
        GeneratedMathProblem p = funcEval.generate(t);
        assertEquals(AnswerShape.SCALAR, p.shape());
        String back = String.valueOf((long) p.expectedAnswers().get(0).toDouble());
        assertTrue(checker.check(p, back).correct(),
                "FunctionEval: generator answer not accepted: " + p.questionText() + " ans=" + back);
    }

    private static MathProblemTemplate template(MathProblemKind kind) {
        MathProblemTemplate t = new MathProblemTemplate();
        t.setKind(kind);
        t.setTolerancePercent(0.5);
        return t;
    }
}
