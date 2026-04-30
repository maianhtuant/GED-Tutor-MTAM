package com.gedtutor.service;

import com.gedtutor.dto.AnswerShape;
import com.gedtutor.dto.GeneratedMathProblem;
import com.gedtutor.model.MathProblemTemplate;
import com.gedtutor.service.math.QuadraticSolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pin coefficients via the package-private {@code solve(...)} hook so
 * each test asserts on the canonical answer without invoking the random
 * generator.
 */
class QuadraticSolverTest {

    private final QuadraticSolver solver = new QuadraticSolver();

    @Test
    void twoDistinctIntegerRoots() {
        // x^2 - 5x + 6 = 0  →  x = 2, x = 3
        GeneratedMathProblem p = solver.solve(template(), 1, -5, 6);

        assertFalse(p.hasSpecialAnswer());
        assertEquals(AnswerShape.UNORDERED, p.shape());
        assertEquals(2, p.expectedAnswers().size());
        assertEquals(2.0, p.expectedAnswers().get(0).toDouble(), 1e-9);
        assertEquals(3.0, p.expectedAnswers().get(1).toDouble(), 1e-9);
    }

    @Test
    void repeatedRoot() {
        // x^2 - 4x + 4 = 0  →  x = 2 (double)
        GeneratedMathProblem p = solver.solve(template(), 1, -4, 4);

        assertFalse(p.hasSpecialAnswer());
        assertEquals(AnswerShape.SCALAR, p.shape());
        assertEquals(1, p.expectedAnswers().size());
        assertEquals(2.0, p.expectedAnswers().get(0).toDouble(), 1e-9);
    }

    @Test
    void noRealSolutionWhenDiscriminantNegative() {
        // x^2 + x + 1 = 0  →  discriminant -3
        GeneratedMathProblem p = solver.solve(template(), 1, 1, 1);

        assertTrue(p.hasSpecialAnswer());
        assertEquals("no real solution", p.specialAnswer());
        assertTrue(p.expectedAnswers().isEmpty());
    }

    @Test
    void irrationalRootsAreApproximated() {
        // x^2 - 2 = 0  →  x = ±√2
        GeneratedMathProblem p = solver.solve(template(), 1, 0, -2);

        assertEquals(2, p.expectedAnswers().size());
        assertEquals(-Math.sqrt(2), p.expectedAnswers().get(0).toDouble(), 1e-3);
        assertEquals( Math.sqrt(2), p.expectedAnswers().get(1).toDouble(), 1e-3);
    }

    @Test
    void questionTextOmitsZeroAndUnitCoefficients() {
        assertEquals("Solve for x: x^2 - 5x + 6 = 0",
                QuadraticSolver.renderQuestion(1, -5, 6));
        assertEquals("Solve for x: x^2 - 4 = 0",
                QuadraticSolver.renderQuestion(1, 0, -4));
        assertEquals("Solve for x: 2x^2 + x = 0",
                QuadraticSolver.renderQuestion(2, 1, 0));
        assertEquals("Solve for x: -x^2 + 3x - 2 = 0",
                QuadraticSolver.renderQuestion(-1, 3, -2));
    }

    @Test
    void generateAlwaysProducesNonZeroLeadingCoefficient() {
        MathProblemTemplate t = template();
        // Force a small range that includes 0; solver must still pick a != 0.
        t.setParametersJson("{\"aMin\":-1,\"aMax\":1,\"bMin\":-1,\"bMax\":1,\"cMin\":-1,\"cMax\":1}");
        for (int i = 0; i < 200; i++) {
            GeneratedMathProblem p = solver.generate(t);
            assertFalse(p.questionText().contains("0x^2"),
                    "questionText should not contain '0x^2': " + p.questionText());
        }
    }

    private static MathProblemTemplate template() {
        MathProblemTemplate t = new MathProblemTemplate();
        t.setTolerancePercent(0.5);
        return t;
    }
}
